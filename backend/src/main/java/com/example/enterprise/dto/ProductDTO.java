package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 闲置物品数据传输对象
 * <p>用于创建和更新闲置物品时的请求参数封装，包含物品图片列表</p>
 */
@Data
public class ProductDTO {
    /** 物品编码 */
    private String sku;

    /** 物品名称 */
    @NotBlank(message = "物品名称不能为空")
    private String name;

    /** 物品简介 */
    @NotBlank(message = "物品简介不能为空")
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
    /** 计量单位 */
    private String unit = "件";
    /** 成交次数 */
    private Integer salesCount = 0;
    /** 重量（克） */
    private Integer weightGrams;

    /** 尺寸大小 */
    private String size;

    /** 是否允许议价：0-不允许，1-允许 */
    private Integer allowBargain = 0;

    /** 新旧程度：BRAND_NEW-全新，LIKE_NEW-九成新，GOOD-八成新，FAIR-七成新，POOR-六成新及以下 */
    private String conditionLevel;

    /** 使用说明 */
    private String usageInstructions;

    /** 物品详情描述（富文本） */
    private String detail;
    /** 物品图片列表 */
    private List<ProductImageDTO> images = new ArrayList<>();

    /** 状态：1-在售，0-已下架 */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status = 1;
}
