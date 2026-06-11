package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 闲置物品实体类，对应 product 表
 * <p>存储闲置物品基本信息，包括编码、名称、价格、库存、详情等，支持多图展示</p>
 */
@Data
@Entity
@Table(name = "product")
public class Product {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物品编码 */
    @Column(unique = true, nullable = false)
    private String sku;

    /** 物品名称 */
    private String name;

    /** 物品简介 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 封面图片地址 */
    private String image;

    /** 物品分类 */
    private String category;

    /** 标签，多个标签用逗号分隔 */
    private String tags;

    /** 交易价格 */
    private BigDecimal price;

    /** 原价/划线价 */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock = 0;

    /** 计量单位，默认 "件" */
    private String unit = "件";

    /** 成交次数 */
    private Integer salesCount = 0;

    /** 重量（克） */
    private Integer weightGrams;

    /** 尺寸大小 */
    private String size;

    /** 是否允许议价：0-不允许，1-允许 */
    @Column(name = "allow_bargain")
    private Integer allowBargain = 0;

    /** 新旧程度：BRAND_NEW-全新，LIKE_NEW-九成新，GOOD-八成新，FAIR-七成新，POOR-六成新及以下 */
    @Column(name = "condition_level")
    private String conditionLevel;

    /** 使用说明 */
    @Column(columnDefinition = "TEXT")
    private String usageInstructions;

    /** 物品详情描述（富文本） */
    @Column(columnDefinition = "TEXT")
    private String detail;

    /** 发布者ID，关联 customer 表 */
    @Column(name = "publisher_id")
    private Long publisherId;

    /** 状态：1-在售，0-已下架 */
    private Integer status = 1;

    /** 审核状态：0-待审核，1-已通过，2-已拒绝（用于商家发布商品的审核流程） */
    @Column(name = "audit_status")
    private Integer auditStatus = 1;

    /** 审核备注 */
    @Column(name = "audit_remark", length = 255)
    private String auditRemark;

    /** 审核时间 */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 物品图片列表（非持久化，运行时关联查询） */
    @Transient
    private List<ProductImage> images = new ArrayList<>();

    /** 运行时计算的好评率（百分比，非持久化） */
    @Transient
    private Double positiveRate;
}
