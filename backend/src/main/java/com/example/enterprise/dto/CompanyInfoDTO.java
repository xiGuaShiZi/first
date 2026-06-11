package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 平台信息数据传输对象
 * <p>用于更新平台信息时的请求参数封装</p>
 */
@Data
public class CompanyInfoDTO {
    /** 平台名称（对应数据库company_name字段） */
    @NotBlank(message = "平台名称不能为空")
    private String companyName;

    /** 平台简介 */
    @NotBlank(message = "平台简介不能为空")
    private String intro;

    /** 平台介绍/关于我们 */
    private String culture;
    /** 联系电话 */
    private String phone;
    /** 联系邮箱 */
    private String email;
    /** 平台地址 */
    private String address;
    /** 平台Logo地址 */
    private String logo;
    /** 平台网站 */
    private String website;
    /** 服务时间 */
    private String serviceTime;
}
