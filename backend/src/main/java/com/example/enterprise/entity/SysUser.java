package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类，对应 sys_user 表
 * <p>存储后台管理系统的管理员账号信息</p>
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUser {
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

    /** 真实姓名 */
    @Column(length = 80)
    private String realName;

    /** 状态：1-启用，0-禁用 */
    private Integer status = 1;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
