package com.example.enterprise.repository;

import com.example.enterprise.entity.MerchantLevelAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantLevelAuditRepository extends JpaRepository<MerchantLevelAudit, Long> {
    Page<MerchantLevelAudit> findByMerchantId(Long merchantId, Pageable pageable);
}

