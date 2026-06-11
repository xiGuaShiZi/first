package com.example.enterprise.service;

import com.example.enterprise.entity.OperationLog;
import com.example.enterprise.repository.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务
 * <p>记录系统关键操作的日志信息，包括操作人、请求信息、执行结果等</p>
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {
    /** 操作日志数据访问 */
    private final OperationLogRepository operationLogRepository;
    /** 认证服务，用于解析操作人信息 */
    private final AuthService authService;

    /**
     * 记录操作日志，异常不会阻断业务请求
     * @param request HTTP请求对象
     * @param statusCode HTTP响应状态码
     * @param durationMs 请求耗时（毫秒）
     * @param exception 异常对象，无异常时为null
     */
    public void record(HttpServletRequest request, int statusCode, long durationMs, Exception exception) {
        try {
            OperationLog log = new OperationLog();
            AuthService.TokenUser user = authService.currentOrNull(resolveToken(request));
            if (user != null) {
                log.setOperatorId(user.id());
                log.setOperatorName(user.username());
                log.setOperatorType(user.role());
            } else {
                log.setOperatorType("ANONYMOUS");
            }
            log.setAction(resolveAction(request));
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setModule(resolveModule(request.getRequestURI()));
            log.setTargetType(resolveTargetType(request.getRequestURI()));
            log.setTargetId(resolveTargetId(request.getRequestURI()));
            log.setIpAddress(resolveIp(request));
            log.setUserAgent(limit(request.getHeader("User-Agent"), 300));
            log.setStatusCode(statusCode);
            log.setResultStatus(exception == null && statusCode < 400 ? "SUCCESS" : "FAIL");
            log.setErrorMessage(exception == null ? null : limit(exception.getMessage(), 500));
            log.setDurationMs(durationMs);
            log.setCreateTime(LocalDateTime.now());
            operationLogRepository.save(log);
        } catch (RuntimeException ignored) {
            // Logging must not block business requests.
        }
    }

    /** 从请求头解析Token */
    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /** 根据HTTP方法推断操作动作 */
    private String resolveAction(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equals(method)) {
            return "VIEW";
        }
        if ("POST".equals(method)) {
            return "CREATE";
        }
        if ("PUT".equals(method) || "PATCH".equals(method)) {
            return "UPDATE";
        }
        if ("DELETE".equals(method)) {
            return "DELETE";
        }
        return method;
    }

    /** 根据URI推断所属模块 */
    private String resolveModule(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        if (parts.length >= 3) {
            return parts[2];
        }
        return "system";
    }

    /** 根据URI推断操作目标类型 */
    private String resolveTargetType(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }

    /** 根据URI中的数字部分推断操作目标ID */
    private String resolveTargetId(String uri) {
        String[] parts = uri.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        return null;
    }

    /** 解析客户端真实IP，支持代理转发头 */
    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** 字符串截断，超长时截取前maxLength个字符 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
