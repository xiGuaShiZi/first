package com.example.enterprise.repository;

import com.example.enterprise.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 平台信息数据访问接口
 */
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
}
