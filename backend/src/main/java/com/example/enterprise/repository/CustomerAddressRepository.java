package com.example.enterprise.repository;

import com.example.enterprise.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 客户收货地址数据访问接口
 */
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    /** 根据客户ID查找地址，按默认状态和更新时间排序 */
    List<CustomerAddress> findByCustomerIdOrderByIsDefaultDescUpdateTimeDesc(Long customerId);

    /** 根据ID和客户ID查找地址（确保归属正确） */
    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);

    /** 统计客户地址数量 */
    long countByCustomerId(Long customerId);
}
