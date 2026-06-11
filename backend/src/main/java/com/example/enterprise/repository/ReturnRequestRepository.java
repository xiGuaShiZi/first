package com.example.enterprise.repository;

import com.example.enterprise.entity.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 交易协商申请数据访问接口
 */
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    /** 根据客户ID分页查询交易协商申请 */
    Page<ReturnRequest> findByCustomerId(Long customerId, Pageable pageable);
    /** 根据状态分页查询交易协商申请 */
    Page<ReturnRequest> findByStatus(String status, Pageable pageable);
    /** 根据状态统计交易协商申请数量 */
    long countByStatus(String status);
}
