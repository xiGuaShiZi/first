package com.example.enterprise.repository;

import com.example.enterprise.entity.ShoppingCartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 收藏/意向项数据访问接口，支持悲观锁防止并发冲突
 */
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {
    /** 根据客户ID查询收藏/意向项，按创建时间和ID倒序 */
    List<ShoppingCartItem> findByCustomerIdOrderByCreateTimeDescIdDesc(Long customerId);
    /** 根据客户ID、物品ID和SKU ID查询收藏/意向项 */
    Optional<ShoppingCartItem> findByCustomerIdAndProductIdAndSkuId(Long customerId, Long productId, Long skuId);
    /** 根据客户ID、物品ID和SKU ID查询收藏/意向项并加悲观写锁 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from ShoppingCartItem item where item.customerId = :customerId and item.productId = :productId and item.skuId = :skuId")
    Optional<ShoppingCartItem> findWithLockByCustomerIdAndProductIdAndSkuId(@Param("customerId") Long customerId, @Param("productId") Long productId, @Param("skuId") Long skuId);
    /** 根据客户ID和物品ID查询收藏/意向项 */
    Optional<ShoppingCartItem> findByCustomerIdAndProductId(Long customerId, Long productId);
    /** 根据ID和客户ID查询收藏/意向项并加悲观写锁 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ShoppingCartItem> findByIdAndCustomerId(Long id, Long customerId);
    /** 根据客户ID删除所有收藏/意向项 */
    void deleteByCustomerId(Long customerId);
}
