package com.example.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 交易协商处理数据传输对象
 * <p>用于管理员处理交易协商申请时的请求参数封装</p>
 */
@Data
public class ReturnHandleDTO {
    /** 处理状态，如 APPROVED / REJECTED / COMPLETED 等 */
    @NotBlank(message = "状态不能为空")
    private String status;

    /** 管理员回复内容 */
    private String reply;
}
