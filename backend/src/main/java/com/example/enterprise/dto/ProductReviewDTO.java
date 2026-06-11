package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 物品评价数据传输对象
 * <p>用于提交物品评价时的请求参数封装，支持主评价和追加评价</p>
 */
@Data
public class ProductReviewDTO {
    /** 订单ID */
    @NotNull
    private Long orderId;

    /** 物品描述评分（1-5） */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer qualityRating;

    /** 服务评分（1-5） */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer serviceRating;

    /** 物流评分（1-5） */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer logisticsRating;

    /** 商家服务态度评分（1-5） */
    @Min(1)
    @Max(5)
    private Integer serviceAttitudeRating;

    /** 评价类型：MAIN-主评价，ADDITIONAL-追加评价 */
    private String reviewType = "MAIN";

    /** 评价内容，最长1000个字符 */
    @Size(max = 1000)
    private String content;

    /** 媒体文件URL列表，多个用逗号分隔，最长2000个字符 */
    @Size(max = 2000)
    private String mediaUrls;

    /** 是否匿名评价：0-否，1-是 */
    private Integer anonymous = 0;
}
