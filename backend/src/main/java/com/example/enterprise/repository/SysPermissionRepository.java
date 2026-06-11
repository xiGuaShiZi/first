package com.example.enterprise.repository;

import com.example.enterprise.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 系统权限数据访问接口
 */
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    /** 根据ID集合和状态查询权限列表 */
    List<SysPermission> findByIdInAndStatus(Collection<Long> ids, Integer status);
}
