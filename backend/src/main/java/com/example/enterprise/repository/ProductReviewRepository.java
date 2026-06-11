package com.example.enterprise.repository;

import com.example.enterprise.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 物品评价数据访问接口
 */
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    /** 检查指定订单是否已有评价 */
    boolean existsByOrderId(Long orderId);
    /** 检查指定订单是否已有某种类型的评价 */
    boolean existsByOrderIdAndReviewType(Long orderId, String reviewType);
    /** 查找指定订单和客户的最新评价 */
    Optional<ProductReview> findFirstByOrderIdAndCustomerIdOrderByCreateTimeDesc(Long orderId, Long customerId);
    /** 根据物品ID查询评价列表，按创建时间倒序 */
    List<ProductReview> findByProductIdOrderByCreateTimeDesc(Long productId);

    /** 根据商家ID统计评价总数（通过JOIN product表） */
    @Query(value = "SELECT COUNT(*) FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId", nativeQuery = true)
    long countByMerchantId(@Param("merchantId") Long merchantId);

    /** 根据商家ID统计好评数（质量评分>=4） */
    @Query(value = "SELECT COUNT(*) FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId AND r.quality_rating >= 4", nativeQuery = true)
    long countPositiveByMerchantId(@Param("merchantId") Long merchantId);

    /** 根据商家ID计算平均质量评分 */
    @Query(value = "SELECT COALESCE(AVG(r.quality_rating), 0) FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId", nativeQuery = true)
    double avgQualityRatingByMerchantId(@Param("merchantId") Long merchantId);

    /** 根据商家ID计算平均服务态度评分 */
    @Query(value = "SELECT COALESCE(AVG(r.service_attitude_rating), 0) FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId", nativeQuery = true)
    double avgServiceAttitudeByMerchantId(@Param("merchantId") Long merchantId);

    /** 根据商家ID分页查询评价列表（通过JOIN product表） */
    @Query(value = "SELECT r.* FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId ORDER BY r.create_time DESC",
           countQuery = "SELECT COUNT(*) FROM product_review r JOIN product p ON r.product_id = p.id WHERE p.publisher_id = :merchantId",
           nativeQuery = true)
    Page<ProductReview> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);
}
