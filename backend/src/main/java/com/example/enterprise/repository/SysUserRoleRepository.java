package com.example.enterprise.repository;

import com.example.enterprise.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户-角色关联数据访问接口
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {
    /** 根据用户ID查询用户-角色关联列表 */
    List<SysUserRole> findByUserId(Long userId);
}
