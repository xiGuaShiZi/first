package com.example.enterprise.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家店铺信息数据传输对象
 * <p>用于商家设置和更新店铺信息</p>
 */
@Data
public class ShopInfoDTO {
    /** 店铺名称 */
    @Size(max = 100, message = "店铺名称不能超过100个字符")
    private String shopName;

    /** 店铺描述 */
    @Size(max = 500, message = "店铺描述不能超过500个字符")
    private String shopDescription;

    /** 店铺Logo */
    private String shopLogo;
}
