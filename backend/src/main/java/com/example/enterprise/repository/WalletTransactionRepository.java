package com.example.enterprise.repository;

import com.example.enterprise.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 钱包交易流水数据访问接口
 */
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    /** 根据商家ID分页查询交易流水 */
    Page<WalletTransaction> findByMerchantIdOrderByCreateTimeDesc(Long merchantId, Pageable pageable);

    /** 根据商家ID和交易类型分页查询交易流水 */
    Page<WalletTransaction> findByMerchantIdAndTransactionTypeOrderByCreateTimeDesc(Long merchantId, String transactionType, Pageable pageable);

    /** 根据订单ID查询交易流水 */
    WalletTransaction findByOrderIdAndTransactionType(Long orderId, String transactionType);
}
