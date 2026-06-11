package com.example.enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台服务费展示VO
 * <p>用于管理员服务费管理页面，包含服务费记录及关联的商家信息</p>
 */
@Data
public class ServiceFeeVO {
    /** 主键ID */
    private Long id;

    /** 商家ID */
    private Long merchantId;

    /** 商家店铺名称 */
    private String shopName;

    /** 商家真实姓名 */
    private String realName;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 商家等级（1-5级，决定费率） */
    private Integer merchantLevel;

    /** 费率（如0.05表示5%） */
    private BigDecimal feeRate;

    /** 服务费金额 */
    private BigDecimal feeAmount;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
