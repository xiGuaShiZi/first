package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户收货地址实体类，对应 customer_address 表
 * <p>存储客户的收货地址信息，支持多个地址并设置默认地址</p>
 */
@Data
@Entity
@Table(name = "customer_address")
public class CustomerAddress {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户ID，关联 customer 表 */
    private Long customerId;

    /** 收货人姓名 */
    private String receiverName;

    /** 联系电话 */
    private String phone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 是否为默认地址：1-默认，0-非默认 */
    private Integer isDefault = 0;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
