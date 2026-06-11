package com.example.enterprise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
/**
 * Web配置类
 * <p>配置静态资源映射（上传文件目录）和操作日志拦截器</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /** 上传文件存储目录 */
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** 操作日志拦截器 */
    @Autowired
    private OperationLogInterceptor operationLogInterceptor;

    /**
     * 配置静态资源映射，将/uploads/**映射到上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    /**
     * 注册操作日志拦截器，拦截认证、管理和用户相关API，排除验证码接口
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/auth/**", "/api/admin/**", "/api/user/**", "/api/public/messages")
                .excludePathPatterns("/api/auth/captcha");
    }
}
