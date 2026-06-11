package com.example.enterprise.repository;

import com.example.enterprise.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 系统角色数据访问接口
 */
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
    /** 根据ID集合和状态查询角色列表 */
    List<SysRole> findByIdInAndStatus(Collection<Long> ids, Integer status);
    /** 根据角色编码查询角色 */
    Optional<SysRole> findByRoleCode(String roleCode);
}
