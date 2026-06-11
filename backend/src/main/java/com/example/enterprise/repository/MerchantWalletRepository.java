package com.example.enterprise.repository;

import com.example.enterprise.entity.MerchantWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 商家钱包数据访问接口
 */
public interface MerchantWalletRepository extends JpaRepository<MerchantWallet, Long> {
    /** 根据商家ID查询钱包 */
    Optional<MerchantWallet> findByMerchantId(Long merchantId);

    /** 检查商家是否已有钱包 */
    boolean existsByMerchantId(Long merchantId);
}
