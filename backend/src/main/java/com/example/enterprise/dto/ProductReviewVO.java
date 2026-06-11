package com.example.enterprise.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品评价视图对象
 * <p>用于返回物品评价信息给前端，匿名评价时隐藏用户姓名</p>
 */
@Data
public class ProductReviewVO {
    /** 评价ID */
    private Long id;

    /** 客户姓名（匿名时显示 "匿名用户"） */
    private String customerName;

    /** 物品ID */
    private Long productId;

    /** 物品名称 */
    private String productName;

    /** 评价类型：MAIN / ADDITIONAL */
    private String reviewType;

    /** 物品描述评分（1-5） */
    private Integer qualityRating;

    /** 服务评分（1-5） */
    private Integer serviceRating;

    /** 物流评分（1-5） */
    private Integer logisticsRating;

    /** 商家服务态度评分（1-5） */
    private Integer serviceAttitudeRating;

    /** 评价内容 */
    private String content;

    /** 媒体文件URL列表 */
    private String mediaUrls;

    /** 是否匿名：0-否，1-是 */
    private Integer anonymous;

    /** 创建时间 */
    private LocalDateTime createTime;
}
