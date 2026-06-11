package com.example.enterprise.repository;

import com.example.enterprise.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 操作日志数据访问接口
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
}
