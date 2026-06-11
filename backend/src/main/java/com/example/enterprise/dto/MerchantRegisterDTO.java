package com.example.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家注册请求DTO
 * <p>包含商家注册所需的所有信息</p>
 */
@Data
public class MerchantRegisterDTO {
    /** 用户名，最长80个字符 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 80, message = "用户名不能超过80个字符")
    private String username;

    /** 密码，长度6-40个字符 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度为6到40个字符")
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    /** 性别，必填字段，值为MALE或FEMALE */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性别只能为MALE或FEMALE")
    private String gender;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号不能超过18个字符")
    private String idCard;

    /** 身份证图片URL */
    @NotBlank(message = "身份证图片不能为空")
    private String idCardImage;

    /** 营业执照图片URL */
    @NotBlank(message = "营业执照图片不能为空")
    private String businessLicense;

    /** 银行账号，必填字段，必须为数字 */
    @NotBlank(message = "银行账号不能为空")
    @Pattern(regexp = "^\\d{10,30}$", message = "银行账号必须为10-30位数字")
    private String bankAccount;
}
