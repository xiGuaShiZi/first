package com.example.enterprise.dto;

import lombok.Data;

/**
 * 闲置物品图片数据传输对象
 * <p>用于闲置物品图片的请求参数封装</p>
 */
@Data
public class ProductImageDTO {
    /** 图片URL地址 */
    private String imageUrl;
    /** 图片说明/描述 */
    private String caption;
    /** 排序序号，值越小越靠前 */
    private Integer sort = 0;
}
