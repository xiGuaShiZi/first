package com.example.enterprise.repository;

import com.example.enterprise.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 客户数据访问接口
 */
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    /** 根据用户名查找客户 */
    Optional<Customer> findByUsername(String username);

    /** 根据用户名和状态查找客户 */
    Optional<Customer> findByUsernameAndStatus(String username, Integer status);

    /** 检查用户名是否已存在 */
    boolean existsByUsername(String username);

    /** 检查手机号是否已注册 */
    boolean existsByPhone(String phone);

    /** 根据审核状态分页查询客户列表 */
    org.springframework.data.domain.Page<com.example.enterprise.entity.Customer> findByAuditStatus(Integer auditStatus, org.springframework.data.domain.Pageable pageable);

    /** 根据审核状态查询客户数量 */
    long countByAuditStatus(Integer auditStatus);
}
