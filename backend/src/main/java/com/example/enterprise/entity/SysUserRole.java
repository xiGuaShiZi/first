package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-角色关联实体类，对应 sys_user_role 表
 * <p>建立用户与角色的多对多关联关系</p>
 */
@Data
@Entity
@Table(name = "sys_user_role")
public class SysUserRole {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID，关联 sys_user 表 */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 角色ID，关联 sys_role 表 */
    @Column(nullable = false)
    private Long roleId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
