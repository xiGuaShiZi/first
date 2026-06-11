package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体类，对应 sys_role 表
 * <p>存储后台管理系统的角色信息，如超级管理员等</p>
 */
@Data
@Entity
@Table(name = "sys_role")
public class SysRole {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色编码，唯一标识，如 "SUPER_ADMIN" */
    @Column(nullable = false, unique = true, length = 60)
    private String roleCode;

    /** 角色名称 */
    @Column(nullable = false, length = 80)
    private String roleName;

    /** 角色描述 */
    @Column(length = 300)
    private String description;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
