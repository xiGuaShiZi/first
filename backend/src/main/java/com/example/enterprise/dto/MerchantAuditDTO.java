package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商家审核请求DTO
 * <p>包含管理员审核商家注册申请的信息</p>
 */
@Data
public class MerchantAuditDTO {
    /** 审核状态：1-通过，2-拒绝 */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态只能为1或2")
    @Max(value = 2, message = "审核状态只能为1或2")
    private Integer auditStatus;

    /** 审核备注 */
    @NotNull(message = "审核备注不能为空")
    private String auditRemark;
}
