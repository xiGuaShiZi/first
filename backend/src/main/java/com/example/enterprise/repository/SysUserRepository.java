package com.example.enterprise.repository;

import com.example.enterprise.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 系统用户数据访问接口
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    /** 根据用户名查询系统用户 */
    Optional<SysUser> findByUsername(String username);
    /** 根据用户名和状态查询系统用户 */
    Optional<SysUser> findByUsernameAndStatus(String username, Integer status);
}
