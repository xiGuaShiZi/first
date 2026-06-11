package com.example.enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏/意向项视图对象
 * <p>用于返回收藏/意向清单物品信息给前端，包含当前价格、快照价格、降价比对等</p>
 */
@Data
public class CartItemVO {
    /** 收藏/意向项ID */
    private Long id;

    /** 物品ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 物品名称 */
    private String productName;

    /** 物品图片 */
    private String image;

    /** 当前交易价格 */
    private BigDecimal price;

    /** 加入收藏时的价格快照 */
    private BigDecimal snapshotPrice;

    /** 加入收藏时的物品名称快照 */
    private String snapshotProductName;

    /** 加入收藏时的物品图片快照 */
    private String snapshotImage;

    /** 当前价格是否低于加入时的价格 */
    private Boolean lowerPrice;

    /** 计量单位 */
    private String unit;

    /** 库存数量 */
    private Integer stock;

    /** 购买数量 */
    private Integer quantity;

    /** 是否选中 */
    private Integer selected;

    /** 是否勾选结算 */
    private Integer checked;

    /** 小计金额 */
    private BigDecimal subtotal;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
