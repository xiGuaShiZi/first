package com.example.enterprise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类，对应 merchant 表
 * <p>存储商家用户的基本信息、证件信息和审核状态</p>
 */
@Data
@Entity
@Table(name = "merchant")
public class Merchant {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名，唯一且不可为空 */
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    /** 密码（加密存储） */
    @Column(nullable = false)
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    /** 性别：MALE-男，FEMALE-女 */
    @NotBlank(message = "性别不能为空")
    @Column(length = 10)
    private String gender;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Size(max = 20, message = "手机号不能超过20个字符")
    @Column(nullable = false, length = 20)
    private String phone;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号不能超过18个字符")
    @Column(name = "id_card", nullable = false, length = 18)
    private String idCard;

    /** 身份证图片URL */
    @Column(name = "id_card_image", length = 255)
    private String idCardImage;

    /** 营业执照图片URL */
    @Column(name = "business_license", length = 255)
    private String businessLicense;

    /** 银行账号 */
    @NotBlank(message = "银行账号不能为空")
    @Size(max = 30, message = "银行账号不能超过30个字符")
    @Column(name = "bank_account", nullable = false, length = 30)
    private String bankAccount;

    /** 店铺名称 */
    @Size(max = 100, message = "店铺名称不能超过100个字符")
    @Column(name = "shop_name", length = 100)
    private String shopName;

    /** 店铺描述 */
    @Column(name = "shop_description", columnDefinition = "TEXT")
    private String shopDescription;

    /** 店铺Logo */
    @Column(name = "shop_logo", length = 255)
    private String shopLogo;

    /** 审核状态：0-待审核，1-已通过，2-已拒绝 */
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 0;

    /** 商家等级：1-5级，数字越大费率越高，对应交易费率分别为0.1%、0.2%、0.5%、0.75%、1% */
    @Column(name = "merchant_level")
    private Integer merchantLevel = 1;

    /** 审核备注 */
    @Column(name = "audit_remark", length = 255)
    private String auditRemark;

    /** 审核时间 */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
