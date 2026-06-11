package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 议价出价实体（买家对允许议价的商品发起价格协商）
 */
@Entity
@Table(name = "bargain_offer")
@Data
public class BargainOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 商品名称（快照） */
    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "buyer_name", length = 128)
    private String buyerName;

    /** 商品发布者（商家）ID */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "merchant_name", length = 128)
    private String merchantName;

    /** 商品原价 */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    /** 买家出价 */
    @Column(name = "offer_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal offerPrice;

    /** 议价状态：PENDING-待处理，ACCEPTED-已接受，REJECTED-已拒绝，CANCELLED-已取消 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 商家回复（可选） */
    @Column(columnDefinition = "TEXT")
    private String reply;

    /** 处理时间 */
    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
