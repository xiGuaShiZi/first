package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商家登录请求DTO
 * <p>包含商家登录所需的用户名、密码和验证码信息</p>
 */
@Data
public class MerchantLoginDTO {
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
