package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录数据传输对象
 * <p>用于管理员和客户登录时的请求参数封装</p>
 */
@Data
public class LoginDTO {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码内容 */
    private String captcha;

    /** 验证码标识 */
    private String captchaId;
}
