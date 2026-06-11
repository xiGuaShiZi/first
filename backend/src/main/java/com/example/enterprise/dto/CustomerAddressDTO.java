package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户收货地址数据传输对象
 * <p>用于创建和更新收货地址时的请求参数封装</p>
 */
@Data
public class CustomerAddressDTO {
    /** 收货人姓名 */
    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区/县 */
    private String district;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /** 是否为默认地址：1-默认，0-非默认 */
    private Integer isDefault = 0;
}
