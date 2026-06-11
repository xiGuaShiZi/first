package com.example.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户注册数据传输对象
 * <p>用于前端客户注册时的请求参数封装</p>
 */
@Data
public class CustomerRegisterDTO {
    /** 用户名，最长80个字符 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 80, message = "用户名不能超过80个字符")
    private String username;

    /** 密码，长度6-40个字符 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度为6到40个字符")
    private String password;

    /** 手机号，最长50个字符 */
    @Size(max = 50, message = "电话不能超过50个字符")
    private String phone;

    /** 邮箱，需符合邮箱格式，最长120个字符 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 120, message = "邮箱不能超过120个字符")
    private String email;

    /** 城市，必填字段 */
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市不能超过50个字符")
    private String city;

    /** 性别，必填字段，值为MALE或FEMALE */
    @NotBlank(message = "性别不能为空")
    private String gender;

    /** 银行账号，必填字段，必须为16位数字 */
    @NotBlank(message = "银行账号不能为空")
    @Pattern(regexp = "^\\d{16}$", message = "银行账号必须为16位数字")
    private String bankAccount;
}
