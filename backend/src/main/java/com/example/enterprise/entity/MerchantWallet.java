package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家钱包实体类，对应 merchant_wallet 表
 * <p>存储商家的账户余额、冻结金额等钱包信息</p>
 */
@Data
@Entity
@Table(name = "merchant_wallet")
public class MerchantWallet {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商家ID，关联 merchant 表 */
    @Column(name = "merchant_id", unique = true, nullable = false)
    private Long merchantId;

    /** 可用余额 */
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /** 冻结金额（交易中未完成订单的金额） */
    @Column(name = "frozen_balance", nullable = false)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    /** 中间账户待结算金额（买家已付款但尚未确认收货的金额，暂存于系统中间账户） */
    @Column(name = "pending_balance", nullable = false)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    /** 累计收入 */
    @Column(name = "total_income", nullable = false)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    /** 累计支出（提现等） */
    @Column(name = "total_expense", nullable = false)
    private BigDecimal totalExpense = BigDecimal.ZERO;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
