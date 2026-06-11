package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台服务费实体类，对应 service_fee 表
 * <p>记录平台从交易中收取的服务费</p>
 */
@Data
@Entity
@Table(name = "service_fee")
public class ServiceFee {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商家ID，关联 merchant 表 */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** 订单ID，关联 product_order 表 */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 订单编号 */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** 订单金额 */
    @Column(name = "order_amount", nullable = false)
    private BigDecimal orderAmount;

    /** 商家等级（1-5级，决定费率） */
    @Column(name = "merchant_level")
    private Integer merchantLevel;

    /** 费率（如0.05表示5%） */
    @Column(name = "fee_rate", nullable = false)
    private BigDecimal feeRate;

    /** 服务费金额 */
    @Column(name = "fee_amount", nullable = false)
    private BigDecimal feeAmount;

    /** 备注（可记录特殊行业适用、临时优惠等扩展信息） */
    @Column(length = 255)
    private String remark;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
