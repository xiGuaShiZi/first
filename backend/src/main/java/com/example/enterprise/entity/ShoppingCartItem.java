package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏/意向项实体类，对应 shopping_cart_item 表
 * <p>存储用户收藏/意向清单中的物品信息，包含物品快照（防止物品信息变更影响清单）</p>
 */
@Data
@Entity
@Table(name = "shopping_cart_item")
public class ShoppingCartItem {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户ID，关联 customer 表 */
    private Long customerId;

    /** 物品ID，关联 product 表 */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer quantity = 1;

    /** 是否选中 */
    private Integer selected = 1;

    /** 是否勾选结算 */
    @Column(name = "checked")
    private Integer checked = 1;

    /** 物品名称快照 */
    private String snapshotProductName;

    /** 物品价格快照 */
    private BigDecimal snapshotPrice;

    /** 物品图片快照 */
    private String snapshotImage;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 乐观锁版本号，防止并发修改冲突 */
    @Version
    private Long version;
}
