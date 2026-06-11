package com.example.enterprise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量创建订单数据传输对象
 * <p>用于从收藏/意向清单批量结算下单时的请求参数封装</p>
 */
@Data
public class OrderBatchCreateDTO {
    /** 订单项列表，至少包含一个物品 */
    @NotEmpty(message = "请选择要结算的物品")
    @Valid
    private List<OrderCreateDTO> items;

    /** 收货地址ID，与下方地址字段二选一 */
    private Long addressId;

    /** 联系电话 */
    private String contactPhone;

    /** 完整收货地址（不使用地址ID时填写） */
    private String address;
}
