package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品评价实体类，对应 product_review 表
 * <p>存储用户对已交易物品的评价信息，支持主评价和追加评价，
 * 同一订单的同一评价类型唯一</p>
 */
@Data
@Entity
@Table(name = "product_review", uniqueConstraints = @UniqueConstraint(name = "uk_product_review_order_type", columnNames = {"order_id", "review_type"}))
public class ProductReview {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户姓名 */
    private String customerName;

    /** 物品ID */
    private Long productId;

    /** 物品名称 */
    private String productName;

    /** 评价类型：MAIN-主评价，ADDITIONAL-追加评价 */
    private String reviewType = "MAIN";

    /** 物品描述评分（1-5） */
    private Integer qualityRating;

    /** 服务评分（1-5） */
    private Integer serviceRating;

    /** 物流评分（1-5） */
    private Integer logisticsRating;

    /** 商家服务态度评分（1-5） */
    @Column(name = "service_attitude_rating")
    private Integer serviceAttitudeRating;

    /** 评价内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 媒体文件URL列表，多个用逗号分隔 */
    @Column(length = 2000)
    private String mediaUrls;

    /** 是否匿名评价：0-否，1-是 */
    private Integer anonymous = 0;

    /** 创建时间 */
    private LocalDateTime createTime;
}
