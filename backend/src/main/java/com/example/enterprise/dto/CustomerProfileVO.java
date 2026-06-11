package com.example.enterprise.dto;

import com.example.enterprise.entity.CustomerAddress;
import lombok.Data;

/**
 * 客户个人资料响应对象
 * <p>包含客户基本信息和默认收货地址</p>
 */
@Data
public class CustomerProfileVO {
    /** 客户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别 */
    private String gender;

    /** 银行账号 */
    private String bankAccount;

    /** 状态 */
    private Integer status;

    /** 创建时间 */
    private String createTime;

    /** 更新时间 */
    private String updateTime;

    /** 默认收货地址 */
    private CustomerAddress defaultAddress;

    /** 账户可用余额（单位：元） */
    private java.math.BigDecimal balance;

    /** 用户积分 */
    private Integer points;

    /** 商家给该用户的评价总数（商家对买家的评价） */
    private Long buyerMerchantReviewCount;

    /** 商家给该用户的好评率（百分比，1位小数），基于商家对买家的评分（>=4视为好评） */
    private Double buyerMerchantPositiveRate;
}
