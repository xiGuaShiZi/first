package com.example.enterprise.repository;

import com.example.enterprise.entity.Banner;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 推荐位数据访问接口
 */
public interface BannerRepository extends JpaRepository<Banner, Long> {
    /** 根据状态查找推荐位并排序 */
    List<Banner> findByStatus(Integer status, Sort sort);
}
