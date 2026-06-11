package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户登录数据传输对象
 * <p>用于前端客户登录时的请求参数封装，包含验证码校验</p>
 */
@Data
public class CustomerLoginDTO {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码内容 */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /** 验证码标识，用于关联服务端存储的验证码 */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaId;
}
