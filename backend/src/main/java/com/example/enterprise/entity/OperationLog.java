package com.example.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类，对应 operation_log 表
 * <p>记录系统中所有关键操作的日志信息，包括操作人、请求信息、执行结果等</p>
 */
@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {
    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    @Column(length = 80)
    private String operatorName;

    /** 操作人类型：ADMIN / USER / ANONYMOUS */
    @Column(length = 20)
    private String operatorType;

    /** 操作动作：VIEW / CREATE / UPDATE / DELETE */
    @Column(length = 100)
    private String action;

    /** 请求方法：GET / POST / PUT / DELETE */
    @Column(length = 20)
    private String requestMethod;

    /** 请求URI */
    @Column(length = 300)
    private String requestUri;

    /** 所属模块 */
    @Column(length = 80)
    private String module;

    /** 操作目标类型 */
    @Column(length = 80)
    private String targetType;

    /** 操作目标ID */
    @Column(length = 80)
    private String targetId;

    /** 请求IP地址 */
    @Column(length = 80)
    private String ipAddress;

    /** 用户代理信息 */
    @Column(length = 300)
    private String userAgent;

    /** HTTP响应状态码 */
    private Integer statusCode;

    /** 结果状态：SUCCESS / FAIL */
    @Column(length = 20)
    private String resultStatus;

    /** 错误信息 */
    @Column(length = 500)
    private String errorMessage;

    /** 请求耗时（毫秒） */
    private Long durationMs;

    /** 创建时间 */
    private LocalDateTime createTime;
}
