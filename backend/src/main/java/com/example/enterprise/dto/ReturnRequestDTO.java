package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交易协商申请数据传输对象
 * <p>用于用户提交交易协商申请时的请求参数封装</p>
 */
@Data
public class ReturnRequestDTO {
    /** 关联订单ID */
    @NotNull(message = "请选择订单")
    private Long orderId;

    /** 申请类型：ONLY_REFUND / RETURN_REFUND / EXCHANGE / REPAIR */
    @NotBlank(message = "请选择协商类型")
    private String requestType;

    /** 申请原因 */
    @NotBlank(message = "请填写交易协商原因")
    private String reason;

    /** 凭证媒体文件URL列表 */
    private String evidenceMedia;
}
