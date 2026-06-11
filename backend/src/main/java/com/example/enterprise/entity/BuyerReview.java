package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家对买家的评价实体（buyer review created by merchant）
 */
@Entity
@Table(name = "buyer_review_by_merchant")
@Data
public class BuyerReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "merchant_name", length = 128)
    private String merchantName;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "buyer_name", length = 128)
    private String buyerName;

    /** 评分 1-5 */
    @Column(nullable = false)
    private Integer rating;

    /** 评价类型，例如 PURCHASE 或 RETURN */
    @Column(name = "review_type", length = 40)
    private String reviewType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}

