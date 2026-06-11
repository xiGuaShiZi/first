package com.example.enterprise.config;

import com.example.enterprise.entity.ProductOrder;
import com.example.enterprise.repository.ProductOrderRepository;
import com.example.enterprise.service.CommerceService;
import com.example.enterprise.service.MerchantWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单结算定时任务
 * <p>处理超过7天未确认收货的订单自动确认并结算</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSettlementScheduler {
    /** 订单数据访问 */
    private final ProductOrderRepository orderRepository;
    /** 钱包管理服务 */
    private final MerchantWalletService walletService;

    /**
     * 每1小时执行一次
     * 查询已发货超过7天但未确认收货的订单，自动确认并结算
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void autoConfirmAndSettle() {
        log.info("开始执行订单自动确认结算任务");

        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            // 查询已发货超过7天且状态为等待买家确认的订单
            List<ProductOrder> orders = orderRepository.findAll().stream()
                    .filter(order -> "WAIT_BUYER_CONFIRM_GOODS".equals(order.getStatus()))
                    .filter(order -> order.getPaidTime() != null)
                    .filter(order -> order.getPaidTime().isBefore(sevenDaysAgo))
                    .filter(order -> !"SETTLED".equals(order.getSettlementStatus()))
                    .toList();

            if (orders.isEmpty()) {
                log.info("没有需要自动确认的订单");
                return;
            }

            log.info("找到 {} 个需要自动确认的订单", orders.size());

            int successCount = 0;
            int failCount = 0;

            for (ProductOrder order : orders) {
                try {
                    // 自动确认收货
                    order.setStatus("TRADE_FINISHED");
                    order.setFinishTime(LocalDateTime.now());
                    order.setUpdateTime(LocalDateTime.now());
                    orderRepository.save(order);

                    log.info("订单 {} 自动确认收货成功", order.getOrderNo());

                    // 执行结算
                    walletService.settleOrder(order.getId(), true);
                    log.info("订单 {} 结算成功", order.getOrderNo());

                    successCount++;
                } catch (Exception e) {
                    log.error("订单 {} 自动确认或结算失败: {}", order.getOrderNo(), e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("订单自动确认结算任务完成: 成功 {} 个, 失败 {} 个", successCount, failCount);

        } catch (Exception e) {
            log.error("订单自动确认结算任务执行失败: {}", e.getMessage(), e);
        }
    }
}
