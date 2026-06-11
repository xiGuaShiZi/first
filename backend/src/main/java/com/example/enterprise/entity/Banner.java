package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐位实体类，对应 banner 表
 * <p>用于首页推荐位管理，包含标题、图片、链接、排序等信息</p>
 */
@Data
@Entity
@Table(name = "banner")
public class Banner {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 推荐位标题 */
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 图片地址 */
    private String image;

    /** 跳转链接 */
    private String link;

    /** 排序序号，值越小越靠前 */
    private Integer sort = 0;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
