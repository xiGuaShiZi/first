package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单实体类，对应 product_order 表
 * <p>存储订单的完整信息，包括订单号、物品、金额、收货地址、支付状态等</p>
 */
@Data
@Entity
@Table(name = "product_order")
public class ProductOrder {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 主订单编号（批量下单时共用） */
    private String mainOrderNo;

    /** 子订单编号 */
    private String subOrderNo;

    /** 店铺ID（保留字段） */
    private Long shopId;

    /** 店铺名称（保留字段） */
    private String shopName;

    /** 商家ID（发布者ID） */
    @Column(name = "merchant_id")
    private Long merchantId;

    /** 客户ID，关联 customer 表 */
    private Long customerId;

    /** 客户姓名 */
    private String customerName;

    /** 物品ID，关联 product 表 */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 物品名称 */
    private String productName;

    /** 物品单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 物品级优惠金额 */
    private BigDecimal productDiscountAmount = BigDecimal.ZERO;

    /** 订单级优惠金额 */
    private BigDecimal orderDiscountAmount = BigDecimal.ZERO;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 联系电话 */
    private String contactPhone;

    /** 收货地址 */
    private String address;

    /** 支付方式，默认 "SIMULATED"（模拟确认交易） */
    private String paymentMethod = "SIMULATED";

    /** 支付时间 */
    private LocalDateTime paidTime;

    /** 物流单号 */
    private String logisticsNo;

    /** 买家备注 */
    private String remark;

    /** 订单状态：WAIT_BUYER_PAY / WAIT_SELLER_SEND_GOODS / WAIT_BUYER_CONFIRM_GOODS /
     *  TRADE_FINISHED / TRADE_CLOSED / REFUNDING / RETURNING / AFTER_SALES_FINISHED */
    private String status = "WAIT_BUYER_PAY";

    /** 是否已评价：0-未评价，1-已评价 */
    private Integer isReviewed = 0;

    /** 平台服务费金额 */
    // @Transient // 如果数据库中没有该字段,取消注释此行使其不参与数据库映射
    @Column(name = "platform_fee")
    private BigDecimal platformFee = BigDecimal.ZERO;

    /** 商家实际收入（订单金额 - 平台服务费） */
    @Column(name = "merchant_income")
    private BigDecimal merchantIncome = BigDecimal.ZERO;

    /** 结算状态：UNSETTLED-未结算, SETTLED-已结算, REFUNDED-已退款 */
    @Column(name = "settlement_status")
    private String settlementStatus = "UNSETTLED";

    /** 结算时间 */
    @Column(name = "settlement_time")
    private LocalDateTime settlementTime;

    /** 交易完成时间 */
    private LocalDateTime finishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
