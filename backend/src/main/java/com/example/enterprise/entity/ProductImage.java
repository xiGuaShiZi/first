package com.example.enterprise.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闲置物品图片实体类，对应 product_image 表
 * <p>存储闲置物品的详情图片，每个物品可有多张图片，支持排序</p>
 */
@Data
@Entity
@Table(name = "product_image")
public class ProductImage {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属物品ID，关联 product 表 */
    private Long productId;

    /** 图片URL地址 */
    private String imageUrl;

    /** 图片说明/描述 */
    private String caption;

    /** 排序序号，值越小越靠前 */
    private Integer sort = 0;

    /** 创建时间 */
    private LocalDateTime createTime;
}
