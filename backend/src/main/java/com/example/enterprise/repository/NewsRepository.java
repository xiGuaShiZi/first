package com.example.enterprise.repository;

import com.example.enterprise.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 校园贴士数据访问接口，支持动态条件查询
 */
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    /** 检查标题是否已存在 */
    boolean existsByTitle(String title);

    /** 根据标题模糊查询（忽略大小写） */
    Page<News> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /** 根据标题和状态模糊查询 */
    Page<News> findByTitleContainingIgnoreCaseAndStatus(String title, Integer status, Pageable pageable);

    /** 根据状态分页查询 */
    Page<News> findByStatus(Integer status, Pageable pageable);
}
