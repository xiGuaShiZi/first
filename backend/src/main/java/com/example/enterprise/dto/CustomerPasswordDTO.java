package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码数据传输对象
 * <p>用于管理员或客户修改密码时的请求参数封装</p>
 */
@Data
public class CustomerPasswordDTO {
    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码，长度6-40个字符 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 40, message = "新密码长度为6到40个字符")
    private String newPassword;
}
