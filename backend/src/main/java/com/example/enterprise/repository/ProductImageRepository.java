package com.example.enterprise.repository;

import com.example.enterprise.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 闲置物品图片数据访问接口
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    /** 根据物品ID查找图片，按排序和ID升序 */
    List<ProductImage> findByProductIdOrderBySortAscIdAsc(Long productId);

    /** 根据物品ID删除所有图片 */
    void deleteByProductId(Long productId);
}
