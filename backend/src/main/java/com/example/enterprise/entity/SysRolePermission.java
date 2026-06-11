package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联实体类，对应 sys_role_permission 表
 * <p>建立角色与权限的多对多关联关系</p>
 */
@Data
@Entity
@Table(name = "sys_role_permission")
public class SysRolePermission {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色ID，关联 sys_role 表 */
    @Column(nullable = false)
    private Long roleId;

    /** 权限ID，关联 sys_permission 表 */
    @Column(nullable = false)
    private Long permissionId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
