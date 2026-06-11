package com.example.enterprise.dto;

import com.example.enterprise.entity.Product;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 商家店铺展示VO
 * <p>用于展示店铺信息和商品列表</p>
 */
@Data
public class ShopVO {
    /** 商家ID */
    private Long merchantId;

    /** 店铺名称 */
    private String shopName;

    /** 店铺描述 */
    private String shopDescription;

    /** 店铺Logo */
    private String shopLogo;

    /** 商品总数 */
    private Long productTotal;

    /** 在售商品数 */
    private Long onSaleProductCount;

    /** 总销量 */
    private Integer totalSales;

    /** 商品列表 */
    private Page<Product> products;

    /** 店铺评分（基于评价） */
    private Double shopRating;

    /** 评价总数 */
    private Long reviewCount;

    /** 商家等级（1-5级） */
    private Integer merchantLevel;

    /** 服务态度评分（基于评价） */
    private Double serviceAttitudeRating;

    /** 好评率（4星及以上评价占比） */
    private Double positiveRate;
}
