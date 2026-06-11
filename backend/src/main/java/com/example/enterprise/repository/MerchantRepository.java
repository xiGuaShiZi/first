package com.example.enterprise.repository;

import com.example.enterprise.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商家数据访问接口
 * <p>提供商家实体的数据库操作方法</p>
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    /**
     * 根据用户名查找商家
     * @param username 用户名
     * @return 商家实体
     */
    Optional<Merchant> findByUsername(String username);

    /**
     * 根据用户名和状态查找商家
     * @param username 用户名
     * @param status 状态
     * @return 商家实体
     */
    Optional<Merchant> findByUsernameAndStatus(String username, Integer status);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据手机号查找商家
     * @param phone 手机号
     * @return 商家实体
     */
    Optional<Merchant> findByPhone(String phone);

    /**
     * 检查手机号是否已注册
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据审核状态分页查询商家列表
     * @param auditStatus 审核状态
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    Page<Merchant> findByAuditStatus(Integer auditStatus, Pageable pageable);

    /**
     * 根据审核状态查询商家总数
     * @param auditStatus 审核状态
     * @return 商家数量
     */
    long countByAuditStatus(Integer auditStatus);
}
