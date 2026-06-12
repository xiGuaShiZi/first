package com.example.enterprise.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * Web配置类
 * <p>确保上传目录存在，并注册操作日志拦截器</p>
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
     * 确保上传目录在应用启动时存在，避免静态资源请求报 NoResourceFoundException
     */
    @PostConstruct
    public void ensureUploadDir() throws IOException {
        Files.createDirectories(Path.of(uploadDir).toAbsolutePath().normalize());
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
