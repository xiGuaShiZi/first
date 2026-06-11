package com.example.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园二手交易平台后端应用启动类
 * <p>Spring Boot应用入口，启动后提供服务端API接口</p>
 */
@SpringBootApplication
@EnableScheduling
public class EnterpriseWebsiteApplication {
    /**
     * 应用主入口方法
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseWebsiteApplication.class, args);
    }
}
