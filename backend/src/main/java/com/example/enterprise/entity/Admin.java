package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类，对应 admin 表
 * <p>存储后台管理员的基本信息，包括用户名、密码、状态等</p>
 */
@Data
@Entity
@Table(name = "admin")
public class Admin {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名，唯一且不可为空 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 密码（加密存储） */
    @Column(nullable = false)
    private String password;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
