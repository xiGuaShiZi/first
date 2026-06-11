package com.example.enterprise.repository;

import com.example.enterprise.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 闲置物品数据访问接口，支持动态条件查询和悲观锁
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    /** 根据物品名称模糊查询（忽略大小写） */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    /** 根据物品名称模糊查询和状态筛选 */
    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, Integer status, Pageable pageable);
    /** 根据状态分页查询物品 */
    Page<Product> findByStatus(Integer status, Pageable pageable);

    /** 根据商家ID分页查询物品 */
    Page<Product> findByPublisherId(Long publisherId, Pageable pageable);

    /** 根据商家ID和状态分页查询物品 */
    Page<Product> findByPublisherIdAndStatus(Long publisherId, Integer status, Pageable pageable);

    /** 根据商家ID查询所有物品总数 */
    long countByPublisherId(Long publisherId);

    /** 根据商家ID查询在售物品总数 */
    long countByPublisherIdAndStatus(Long publisherId, Integer status);

    /** 根据ID查询物品并加悲观写锁，防止并发修改冲突 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findWithLockById(@Param("id") Long id);

    /** 查询所有在售物品的去重分类列表 */
    @Query("""
            select distinct p.category
            from Product p
            where p.status = 1 and p.category is not null and p.category <> ''
            order by p.category
            """)
    List<String> findPublicCategories();
}
