package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言实体类，对应 message 表
 * <p>存储网站访客提交的咨询留言信息，包含留言内容和管理员回复</p>
 */
@Data
@Entity
@Table(name = "message")
public class Message {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 留言人姓名 */
    private String username;

    /** 留言人电话 */
    private String phone;

    /** 留言人邮箱 */
    private String email;

    /** 留言内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 来源渠道，默认 WEB */
    private String source = "WEB";

    /** 处理状态：0-未处理，1-已处理 */
    private Integer status = 0;

    /** 管理员回复内容 */
    @Column(columnDefinition = "TEXT")
    private String reply;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
