package com.example.enterprise.repository;

import com.example.enterprise.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 角色-权限关联数据访问接口
 */
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {
    /** 根据角色ID集合查询角色-权限关联列表 */
    List<SysRolePermission> findByRoleIdIn(Collection<Long> roleIds);
}
