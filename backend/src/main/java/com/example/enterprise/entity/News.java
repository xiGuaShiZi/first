package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校园贴士实体类，对应 news 表
 * <p>存储校园贴士/公告信息，支持富文本内容、封面图、标签等</p>
 */
@Data
@Entity
@Table(name = "news")
public class News {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 贴士标题 */
    private String title;

    /** 贴士摘要 */
    private String summary;

    /** 贴士正文内容（富文本） */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 作者 */
    private String author;

    /** 封面图片地址 */
    private String coverImage;

    /** 标签，多个标签用逗号分隔 */
    private String tags;

    /** 来源，默认 "校园二手交易平台" */
    private String source = "校园二手交易平台";

    /** 浏览次数 */
    private Integer viewCount = 0;

    /** 排序序号 */
    private Integer sort = 0;

    /** 状态：1-发布，0-下线 */
    private Integer status = 1;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
