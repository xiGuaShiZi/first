package com.example.enterprise.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单数据传输对象
 * <p>用于单物品下单时的请求参数封装</p>
 */
@Data
public class OrderCreateDTO {
    /** 物品ID */
    @NotNull(message = "请选择物品")
    private Long productId;

    /** SKU ID，为null时默认0 */
    private Long skuId;

    /** 购买数量，至少为1 */
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity = 1;

    /** 收货地址ID，与下方地址字段二选一 */
    private Long addressId;

    /** 联系电话 */
    private String contactPhone;

    /** 完整收货地址 */
    private String address;

    /** 议价出价ID（可选，传入已接受的议价记录ID时使用议价价格） */
    private Long bargainOfferId;
}
