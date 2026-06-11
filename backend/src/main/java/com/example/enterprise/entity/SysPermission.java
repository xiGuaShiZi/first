package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体类，对应 sys_permission 表
 * <p>存储后台管理系统的权限配置，支持菜单和API两种权限类型</p>
 */
@Data
@Entity
@Table(name = "sys_permission")
public class SysPermission {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 权限编码，唯一标识，如 "dashboard:view" */
    @Column(nullable = false, unique = true, length = 100)
    private String permissionCode;

    /** 权限名称 */
    @Column(nullable = false, length = 100)
    private String permissionName;

    /** 权限类型：MENU-菜单权限，API-接口权限 */
    @Column(nullable = false, length = 20)
    private String permissionType;

    /** 父级权限ID */
    private Long parentId;

    /** API路径 */
    @Column(length = 200)
    private String path;

    /** HTTP方法 */
    @Column(length = 20)
    private String method;

    /** 排序序号 */
    private Integer sort = 0;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
