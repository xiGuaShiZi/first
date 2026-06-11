package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易协商申请实体类，对应 return_request 表
 * <p>存储用户提交的交易协商申请信息，包括仅退款、退货退款、换货等类型</p>
 */
@Data
@Entity
@Table(name = "return_request")
public class ReturnRequest {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 关联订单编号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户姓名 */
    private String customerName;

    /** 物品名称 */
    private String productName;

    /** 申请类型：ONLY_REFUND-仅退款，RETURN_REFUND-退货退款，EXCHANGE-换货 */
    private String requestType = "ONLY_REFUND";

    /** 申请原因 */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** 凭证图片 */
    private String evidenceImage;

    /** 凭证媒体文件URL列表 */
    @Column(length = 2000)
    private String evidenceMedia;

    /** 申请状态：APPLY_PENDING / APPROVED / REJECTED / BUYER_RETURNING /
     *  SELLER_RECEIVED / REFUNDED / RESHIPPED / COMPLETED / CLOSED */
    private String status = "APPLY_PENDING";

    /** 管理员回复内容 */
    @Column(columnDefinition = "TEXT")
    private String reply;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
