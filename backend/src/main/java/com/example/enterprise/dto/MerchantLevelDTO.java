package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商家等级设置请求DTO
 * <p>管理员设置商家等级（1-5级），等级越高平台收取的交易费率越高</p>
 */
@Data
public class MerchantLevelDTO {
    /** 商家等级：1-5级，对应费率分别为0.1%、0.2%、0.5%、0.75%、1% */
    @NotNull(message = "商家等级不能为空")
    @Min(value = 1, message = "商家等级最小为1")
    @Max(value = 5, message = "商家等级最大为5")
    private Integer merchantLevel;
}
