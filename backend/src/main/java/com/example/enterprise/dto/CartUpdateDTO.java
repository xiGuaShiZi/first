package com.example.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 更新收藏/意向项数据传输对象
 * <p>用于修改收藏/意向清单中物品数量和选中状态时的请求参数封装</p>
 */
@Data
public class CartUpdateDTO {
    /** 物品数量，至少为1 */
    @Min(value = 1, message = "物品数量至少为1")
    private Integer quantity = 1;

    /** 是否选中 */
    private Integer selected = 1;

    /** 是否勾选结算 */
    private Integer checked = 1;
}
