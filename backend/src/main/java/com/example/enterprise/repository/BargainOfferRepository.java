package com.example.enterprise.repository;

import com.example.enterprise.entity.BargainOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 议价出价数据访问接口
 */
public interface BargainOfferRepository extends JpaRepository<BargainOffer, Long> {

    /** 查询买家对某商品的出价记录 */
    Page<BargainOffer> findByBuyerIdAndProductId(Long buyerId, Long productId, Pageable pageable);

    /** 查询买家的所有出价记录 */
    Page<BargainOffer> findByBuyerId(Long buyerId, Pageable pageable);

    /** 查询商家收到的所有议价出价 */
    Page<BargainOffer> findByMerchantId(Long merchantId, Pageable pageable);

    /** 检查买家对某商品是否已有待处理的出价 */
    boolean existsByBuyerIdAndProductIdAndStatus(Long buyerId, Long productId, String status);
}
