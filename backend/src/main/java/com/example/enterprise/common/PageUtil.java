package com.example.enterprise.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 分页工具类
 * <p>统一封装分页请求构建逻辑，避免各 Service 重复编写分页参数处理</p>
 */
public final class PageUtil {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private PageUtil() {}

    /**
     * 构建分页请求（按创建时间倒序）
     * @param page 页码（从1开始）
     * @param size 每页数量（自动限制在1-100之间）
     * @return Pageable 实例
     */
    public static Pageable of(int page, int size) {
        return of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    /**
     * 构建分页请求（自定义排序）
     * @param page 页码（从1开始）
     * @param size 每页数量（自动限制在1-100之间）
     * @param sort 排序规则
     * @return Pageable 实例
     */
    public static Pageable of(int page, int size, Sort sort) {
        return PageRequest.of(normalizePage(page), normalizeSize(size), sort);
    }

    /**
     * 标准化页码：将前端 1-based 页码转为 0-based
     */
    public static int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    /**
     * 标准化每页数量，限制在 1-100 之间
     */
    public static int normalizeSize(int size) {
        return Math.min(Math.max(size, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
    }
}
