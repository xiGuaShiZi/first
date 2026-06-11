package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 推荐位数据传输对象
 * <p>用于创建和更新推荐位时的请求参数封装</p>
 */
@Data
public class BannerDTO {
    /** 推荐位标题 */
    @NotBlank(message = "推荐位标题不能为空")
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 图片地址 */
    @NotBlank(message = "推荐位图片不能为空")
    private String image;

    /** 跳转链接 */
    private String link;

    /** 排序序号，值越小越靠前 */
    private Integer sort = 0;

    /** 状态：1-启用，0-禁用 */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status = 1;
}
