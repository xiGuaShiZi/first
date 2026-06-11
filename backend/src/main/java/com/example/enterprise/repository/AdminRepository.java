package com.example.enterprise.repository;

import com.example.enterprise.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 管理员数据访问接口
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {
    /** 根据用户名查找管理员 */
    Optional<Admin> findByUsername(String username);

    /** 根据用户名和状态查找管理员 */
    Optional<Admin> findByUsernameAndStatus(String username, Integer status);
}
