package com.example.enterprise.repository;

import com.example.enterprise.entity.BuyerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 商家对买家的评价数据访问
 */
public interface BuyerReviewRepository extends JpaRepository<BuyerReview, Long> {
    boolean existsByOrderIdAndMerchantId(Long orderId, Long merchantId);

    Page<BuyerReview> findByBuyerId(Long buyerId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM buyer_review_by_merchant r WHERE r.buyer_id = :buyerId", nativeQuery = true)
    long countByBuyerId(@Param("buyerId") Long buyerId);

    @Query(value = "SELECT COUNT(*) FROM buyer_review_by_merchant r WHERE r.buyer_id = :buyerId AND r.rating >= 4", nativeQuery = true)
    long countPositiveByBuyerId(@Param("buyerId") Long buyerId);
}

