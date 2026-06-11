package com.example.enterprise.config;

import com.example.enterprise.common.Result;
import com.example.enterprise.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 安全配置类
 * <p>配置Spring Security安全过滤链，包括CSRF禁用、CORS、无状态会话、
 * URL权限规则和自定义Token认证过滤器</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    /** 认证服务 */
    private final AuthService authService;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /** 允许的跨域来源地址 */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    /**
     * 安全过滤链配置：禁用CSRF、配置CORS、无状态会话、URL权限规则、Token过滤器
     * @param http HttpSecurity构建器
     * @return 安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/merchant/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new TokenFilter(authService, objectMapper), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CORS跨域配置，允许指定的来源、方法和请求头
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        configuration.setAllowedOriginPatterns(origins.isEmpty() ? List.of("http://localhost:5173") : origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Token认证过滤器，拦截/api/admin/和/api/user/开头的请求，
     * 校验Token有效性、角色区域匹配和管理员权限
     */
    static class TokenFilter extends OncePerRequestFilter {
        /** 认证服务 */
        private final AuthService authService;
        /** JSON序列化工具 */
        private final ObjectMapper objectMapper;
        /** 日志 */
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenFilter.class);

        TokenFilter(AuthService authService, ObjectMapper objectMapper) {
            this.authService = authService;
            this.objectMapper = objectMapper;
        }

        /**
         * 执行Token认证：解析Token、校验区域和权限
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String uri = request.getRequestURI();
            // Intercept admin, user and merchant areas for token authentication
            if (!uri.startsWith("/api/admin/") && !uri.startsWith("/api/user/") && !uri.startsWith("/api/merchant/")) {
                chain.doFilter(request, response);
                return;
            }

            String token = resolveToken(request);
            AuthService.TokenUser user = authService.currentOrNull(token);
            if (user == null) {
                // token 无效或已过期
                log.debug("TokenFilter: no session for token=[{}] uri={}", token, uri);
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
                return;
            }
            if (!matchesArea(uri, user)) {
                // token 对应账户角色与请求区域不匹配
                log.debug("TokenFilter: role mismatch user={} role={} uri={}", user.username(), user.role(), uri);
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权访问该区域：请使用具有对应角色的账号登录");
                return;
            }
            String requiredPermission = requiredAdminPermission(uri, request.getMethod());
            if (requiredPermission != null && !authService.hasPermission(token, requiredPermission)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权访问该后台功能");
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication(user));
            log.debug("TokenFilter: authenticated user={} role={} uri={}", user.username(), user.role(), uri);
            chain.doFilter(request, response);
        }

        /** 从请求头中解析Token */
        private String resolveToken(HttpServletRequest request) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return token.substring(7);
            }
            return token;
        }

        /** 校验Token用户角色是否匹配请求区域（ADMIN→/api/admin/，USER→/api/user/，MERCHANT→/api/merchant/） */
        private boolean matchesArea(String uri, AuthService.TokenUser user) {
            if (uri.startsWith("/api/admin/")) {
                return "ADMIN".equals(user.role());
            }
            if (uri.startsWith("/api/user/")) {
                return "USER".equals(user.role());
            }
            if (uri.startsWith("/api/merchant/")) {
                return "MERCHANT".equals(user.role());
            }
            return false;
        }

        /** 构建Spring Security认证对象，包含角色和权限 */
        private UsernamePasswordAuthenticationToken authentication(AuthService.TokenUser user) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.roles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            user.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            return new UsernamePasswordAuthenticationToken(user.username(), null, authorities);
        }

        /** 根据URI和方法推断所需的管理员权限编码 */
        private String requiredAdminPermission(String uri, String method) {
            if (!uri.startsWith("/api/admin/")) {
                return null;
            }
            if (uri.equals("/api/admin/password") || uri.equals("/api/admin/upload")) {
                return null;
            }
            if (uri.startsWith("/api/admin/dashboard")) {
                return "dashboard:view";
            }
            if (uri.startsWith("/api/admin/company")) {
                return "company:update";
            }
            if (uri.startsWith("/api/admin/banners")) {
                return "banner:update";
            }
            if (uri.startsWith("/api/admin/news")) {
                return "news:update";
            }
            if (uri.startsWith("/api/admin/products")) {
                return "product:update";
            }
            if (uri.startsWith("/api/admin/messages")) {
                return "message:handle";
            }
            if (uri.startsWith("/api/admin/returns")) {
                return "return:handle";
            }
            if (uri.startsWith("/api/admin/orders") || uri.startsWith("/api/admin/order-statistics")) {
                return "order:view";
            }
            return "GET".equals(method) ? "dashboard:view" : "rbac:update";
        }

        /** 向响应中写入错误JSON */
        private void writeError(HttpServletResponse response, int status, String message) throws IOException {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail(status, message)));
        }
    }
}
