package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台信息实体类，对应 company_info 表
 * <p>存储平台展示的基本信息，如平台名称、简介、联系方式等</p>
 */
@Data
@Entity
@Table(name = "company_info")
public class CompanyInfo {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 平台名称 */
    private String companyName;

    /** 平台简介 */
    @Column(columnDefinition = "TEXT")
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

    /** 更新时间 */
    private LocalDateTime updateTime;
}
