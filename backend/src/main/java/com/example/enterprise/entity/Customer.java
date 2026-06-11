package com.example.enterprise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 客户实体类，对应 customer 表
 * <p>存储前端注册用户的基本信息，包括用户名、密码、联系方式等</p>
 */
@Data
@Entity
@Table(name = "customer")
public class Customer {
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

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Size(max = 50, message = "电话不能超过50个字符")
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别：MALE-男，FEMALE-女 */
    @Column(length = 10)
    private String gender;

    /** 银行账号，16位数字 */
    @Column(length = 16)
    private String bankAccount;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 审核状态：0-待审核，1-已通过，2-已拒绝 */
    @Column(name = "audit_status")
    private Integer auditStatus = 0;

    /** 审核备注 */
    @Column(name = "audit_remark", length = 255)
    private String auditRemark;

    /** 审核时间 */
    @Column(name = "audit_time")
    private java.time.LocalDateTime auditTime;

    /** 账户可用余额（系统内部余额，单位：元） */
    private BigDecimal balance = BigDecimal.ZERO;

    /** 积分（每消费1元累计1积分） */
    private Integer points = 0;
    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
