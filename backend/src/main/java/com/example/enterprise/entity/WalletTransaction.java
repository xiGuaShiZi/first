package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易流水实体类，对应 wallet_transaction 表
 * <p>记录商家钱包的每一笔交易明细</p>
 */
@Data
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商家ID，关联 merchant 表 */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** 交易类型：ORDER_INCOME-订单收入, WITHDRAW-提现, PLATFORM_FEE-平台服务费, REFUND-退款, AUTO_CONFIRM-自动确认收货 */
    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    /** 关联订单ID */
    @Column(name = "order_id")
    private Long orderId;

    /** 关联订单编号 */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** 交易金额（正数为收入，负数为支出） */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /** 交易前余额 */
    @Column(name = "balance_before", nullable = false)
    private BigDecimal balanceBefore;

    /** 交易后余额 */
    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    /** 交易备注 */
    @Column(length = 500)
    private String remark;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
