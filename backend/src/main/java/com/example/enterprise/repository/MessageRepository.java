package com.example.enterprise.repository;

import com.example.enterprise.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 留言数据访问接口
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    /** 根据状态分页查询留言 */
    Page<Message> findByStatus(Integer status, Pageable pageable);

    /** 根据状态统计留言数量 */
    long countByStatus(Integer status);
}
