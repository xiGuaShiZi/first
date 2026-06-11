package com.example.enterprise.repository;

import com.example.enterprise.entity.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单数据访问接口，支持订单统计查询
 */
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    /** 根据客户ID分页查询订单 */
    Page<ProductOrder> findByCustomerId(Long customerId, Pageable pageable);
    /** 根据商家ID分页查询订单 */
    Page<ProductOrder> findByMerchantId(Long merchantId, Pageable pageable);
    /** 根据客户ID、主订单号和状态查询订单列表 */
    List<ProductOrder> findByCustomerIdAndMainOrderNoAndStatus(Long customerId, String mainOrderNo, String status);
    /** 根据状态统计订单数量 */
    long countByStatus(String status);

    /** 统计指定时间范围内的订单总金额 */
    @Query(value = """
            SELECT COALESCE(SUM(total_amount), 0)
            FROM product_order
            WHERE create_time >= :startTime AND create_time < :endTime
            """, nativeQuery = true)
    BigDecimal sumAmountBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 统计指定时间范围内的物品总成交数量 */
    @Query(value = """
            SELECT COALESCE(SUM(quantity), 0)
            FROM product_order
            WHERE create_time >= :startTime AND create_time < :endTime
            """, nativeQuery = true)
    Long sumQuantityBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 统计指定时间范围内的订单数量 */
    @Query(value = """
            SELECT COUNT(*)
            FROM product_order
            WHERE create_time >= :startTime AND create_time < :endTime
            """, nativeQuery = true)
    Long countBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 按物品分类统计指定时间范围内的订单数量、成交数量和金额 */
    @Query(value = """
            SELECT COALESCE(p.category, '未分类') AS label,
                   COUNT(o.id) AS order_count,
                   COALESCE(SUM(o.quantity), 0) AS quantity,
                   COALESCE(SUM(o.total_amount), 0) AS amount
            FROM product_order o
            LEFT JOIN product p ON p.id = o.product_id
            WHERE o.create_time >= :startTime AND o.create_time < :endTime
            GROUP BY COALESCE(p.category, '未分类')
            ORDER BY amount DESC
            """, nativeQuery = true)
    List<Object[]> sumByCategory(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 按日期统计指定时间范围内的订单数量、销量和金额 */
    @Query(value = """
            SELECT DATE(o.create_time) AS label,
                   COUNT(o.id) AS order_count,
                   COALESCE(SUM(o.quantity), 0) AS quantity,
                   COALESCE(SUM(o.total_amount), 0) AS amount
            FROM product_order o
            WHERE o.create_time >= :startTime AND o.create_time < :endTime
            GROUP BY DATE(o.create_time)
            ORDER BY label
            """, nativeQuery = true)
    List<Object[]> sumByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定商家已结算的订单总金额（已结算状态）
     */
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM product_order WHERE merchant_id = :merchantId AND settlement_status = 'SETTLED'", nativeQuery = true)
    BigDecimal sumSettledAmountByMerchant(@Param("merchantId") Long merchantId);
}
