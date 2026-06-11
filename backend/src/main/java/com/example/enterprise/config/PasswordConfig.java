package com.example.enterprise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码配置类
 * <p>配置BCrypt密码编码器，用于用户密码的加密和校验</p>
 */
@Configuration
public class PasswordConfig {
    /**
     * 密码编码器Bean，使用BCrypt算法
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
