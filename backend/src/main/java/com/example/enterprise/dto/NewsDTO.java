package com.example.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 校园贴士数据传输对象
 * <p>用于创建和更新校园贴士时的请求参数封装</p>
 */
@Data
public class NewsDTO {
    /** 贴士标题 */
    @NotBlank(message = "贴士标题不能为空")
    private String title;

    /** 贴士摘要 */
    private String summary;

    /** 贴士正文内容（富文本） */
    @NotBlank(message = "贴士内容不能为空")
    private String content;

    /** 作者 */
    private String author;
    /** 封面图片地址 */
    private String coverImage;
    /** 标签，多个标签用逗号分隔 */
    private String tags;
    /** 来源 */
    private String source;
    /** 浏览次数 */
    private Integer viewCount = 0;
    /** 排序序号 */
    private Integer sort = 0;

    /** 状态：1-发布，0-下线 */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status = 1;
}
