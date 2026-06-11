package com.example.enterprise.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏/意向项数据传输对象
 * <p>用于将闲置物品加入收藏/意向清单时的请求参数封装</p>
 */
@Data
public class CartAddDTO {
    /** 物品ID */
    @NotNull(message = "请选择物品")
    private Long productId;

    /** SKU ID，为null时默认0 */
    private Long skuId;

    /** 物品数量，至少为1 */
    @Min(value = 1, message = "物品数量至少为1")
    private Integer quantity = 1;
}
