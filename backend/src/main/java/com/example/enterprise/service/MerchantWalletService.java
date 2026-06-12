package com.example.enterprise.service;

import com.example.enterprise.dto.ServiceFeeVO;
import com.example.enterprise.entity.Merchant;
import com.example.enterprise.entity.MerchantWallet;
import com.example.enterprise.entity.Product;
import com.example.enterprise.entity.ProductOrder;
import com.example.enterprise.entity.ServiceFee;
import com.example.enterprise.entity.WalletTransaction;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.repository.MerchantRepository;
import com.example.enterprise.repository.MerchantWalletRepository;
import com.example.enterprise.repository.ProductOrderRepository;
import com.example.enterprise.repository.ProductRepository;
import com.example.enterprise.repository.ServiceFeeRepository;
import com.example.enterprise.repository.WalletTransactionRepository;
import com.example.enterprise.repository.MerchantLevelAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 商家钱包管理服务
 * <p>处理商家钱包的创建、结算、查询等业务逻辑</p>
 */
@Service
@RequiredArgsConstructor
public class MerchantWalletService {
    /** 商家钱包数据访问 */
    private final MerchantWalletRepository walletRepository;
    /** 钱包交易流水数据访问 */
    private final WalletTransactionRepository transactionRepository;
    /** 平台服务费数据访问 */
    private final ServiceFeeRepository serviceFeeRepository;
    /** 商家数据访问 */
    private final MerchantRepository merchantRepository;
    /** 订单数据访问 */
    private final ProductOrderRepository orderRepository;
    /** 物品数据访问 */
    private final ProductRepository productRepository;
    /** 物品评价数据访问（用于计算商家满意度） */
    private final com.example.enterprise.repository.ProductReviewRepository productReviewRepository;
    /** 等级变更审计数据访问 */
    private final com.example.enterprise.repository.MerchantLevelAuditRepository merchantLevelAuditRepository;
    /** Redis 缓存服务 */
    private final CacheService cacheService;

    /** 是否启用商家等级定时评估任务（可通过配置开启/关闭） */
    @org.springframework.beans.factory.annotation.Value("${app.merchant-level-scheduler.enabled:true}")
    private boolean merchantLevelSchedulerEnabled;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MerchantWalletService.class);

    /**
     * 商家等级对应的平台服务费率（1-5级）
     * <p>等级1：0.1%，等级2：0.2%，等级3：0.5%，等级4：0.75%，等级5：1%</p>
     */
    private static final Map<Integer, BigDecimal> FEE_RATES = new HashMap<>();

    static {
        FEE_RATES.put(1, new BigDecimal("0.001"));     // 等级1：0.1%
        FEE_RATES.put(2, new BigDecimal("0.002"));     // 等级2：0.2%
        FEE_RATES.put(3, new BigDecimal("0.005"));     // 等级3：0.5%
        FEE_RATES.put(4, new BigDecimal("0.0075"));    // 等级4：0.75%
        FEE_RATES.put(5, new BigDecimal("0.01"));      // 等级5：1%
    }

    /**
     * 为商家创建钱包（商家审核通过时调用）
     * @param merchantId 商家ID
     * @return 创建的钱包实体
     */
    @Transactional
    public MerchantWallet createWallet(Long merchantId) {
        if (walletRepository.existsByMerchantId(merchantId)) {
            throw new BusinessException("商家钱包已存在");
        }

        MerchantWallet wallet = new MerchantWallet();
        wallet.setMerchantId(merchantId);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setPendingBalance(BigDecimal.ZERO);
        wallet.setTotalIncome(BigDecimal.ZERO);
        wallet.setTotalExpense(BigDecimal.ZERO);
        wallet.setCreateTime(LocalDateTime.now());
        wallet.setUpdateTime(LocalDateTime.now());

        return walletRepository.save(wallet);
    }

    /**
     * 查询商家钱包信息
     * @param merchantId 商家ID
     * @return 钱包实体
     */
    /** * 查询商家钱包信息，若不存在则自动创建（并处理并发创建的竞态） * @param merchantId 商家ID * @return 钱包实体 */
    public MerchantWallet getWallet(Long merchantId) {
        // 优先从缓存获取
        MerchantWallet cached = cacheService.getMerchantWallet(merchantId, MerchantWallet.class);
        if (cached != null) {
            return cached;
        }
        MerchantWallet wallet = walletRepository.findByMerchantId(merchantId)
                .orElseGet(() -> {
                    try {
                        // 尝试创建钱包
                        return createWallet(merchantId);
                    } catch (BusinessException ex) {
                        // 如果并发创建导致 createWallet 抛出（已存在），则再次查询并返回；
                        // 若仍然不存在，则继续抛出原异常
                        return walletRepository.findByMerchantId(merchantId)
                                .orElseThrow(() -> ex);
                    }
                });
        // 写入缓存
        cacheService.putMerchantWallet(merchantId, wallet);
        return wallet;
    }

    /**
     * 分页查询商家交易流水
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页交易流水
     */
    public Page<WalletTransaction> getTransactions(Long merchantId, int page, int size) {
        return transactionRepository.findByMerchantIdOrderByCreateTimeDesc(
                merchantId,
                PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createTime"))
        );
    }

    /**
     * 处理订单结算（买家确认收货或超时自动确认后调用）
     * @param orderId 订单ID
     * @param isAutoConfirm 是否自动确认
     * @return 结算后的订单
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProductOrder settleOrder(Long orderId, boolean isAutoConfirm) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 检查订单状态
        if (!"TRADE_FINISHED".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，无法结算");
        }

        // 检查是否已结算
        if ("SETTLED".equals(order.getSettlementStatus())) {
            throw new BusinessException("订单已结算");
        }

        // 获取商家信息
        Long merchantId = order.getMerchantId();
        if (merchantId == null && order.getProductId() != null) {
            // 兜底：从物品表查找发布者ID
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null && product.getPublisherId() != null) {
                merchantId = product.getPublisherId();
                order.setMerchantId(merchantId);
                orderRepository.save(order);
            }
        }
        if (merchantId == null) {
            throw new BusinessException("订单未关联商家");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));

        // 获取或创建商家钱包
        final Long finalMerchantId = merchantId;
        MerchantWallet wallet = walletRepository.findByMerchantId(finalMerchantId)
                .orElseGet(() -> createWallet(finalMerchantId));

        // 计算平台服务费
        BigDecimal orderAmount = order.getTotalAmount();
        Integer merchantLevel = merchant.getMerchantLevel() != null ? merchant.getMerchantLevel() : 1;
        BigDecimal feeRate = FEE_RATES.getOrDefault(merchantLevel, new BigDecimal("0.001"));
        BigDecimal platformFee = orderAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal merchantIncome = orderAmount.subtract(platformFee);

        // 记录服务费
        ServiceFee serviceFee = new ServiceFee();
        serviceFee.setMerchantId(merchantId);
        serviceFee.setOrderId(orderId);
        serviceFee.setOrderNo(order.getOrderNo());
        serviceFee.setOrderAmount(orderAmount);
        serviceFee.setMerchantLevel(merchantLevel);
        serviceFee.setFeeRate(feeRate);
        serviceFee.setFeeAmount(platformFee);
        serviceFee.setCreateTime(LocalDateTime.now());
        serviceFeeRepository.save(serviceFee);

        // 更新钱包：从中间账户待结算金额转到可用余额
        // 先将全额转入可用余额，再显式扣除平台服务费
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        BigDecimal balanceAfterIncome = balanceBefore.add(orderAmount);
        BigDecimal balanceAfterFee = balanceAfterIncome.subtract(platformFee);

        wallet.setPendingBalance(wallet.getPendingBalance().subtract(orderAmount).max(BigDecimal.ZERO));
        wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(orderAmount).max(BigDecimal.ZERO));
        wallet.setAvailableBalance(balanceAfterFee);
        wallet.setTotalIncome(wallet.getTotalIncome().add(orderAmount));
        wallet.setTotalExpense(wallet.getTotalExpense().add(platformFee));
        wallet.setUpdateTime(LocalDateTime.now());
        walletRepository.save(wallet);
        // 钱包变更，清除缓存
        cacheService.evictMerchantWallet(merchantId);

        // 记录收入流水
        WalletTransaction incomeTx = new WalletTransaction();
        incomeTx.setMerchantId(merchantId);
        incomeTx.setTransactionType(isAutoConfirm ? "AUTO_CONFIRM" : "ORDER_INCOME");
        incomeTx.setOrderId(orderId);
        incomeTx.setOrderNo(order.getOrderNo());
        incomeTx.setAmount(orderAmount);
        incomeTx.setBalanceBefore(balanceBefore);
        incomeTx.setBalanceAfter(balanceAfterIncome);
        incomeTx.setRemark(isAutoConfirm ? "系统自动确认收货" : "买家确认收货");
        incomeTx.setCreateTime(LocalDateTime.now());
        transactionRepository.save(incomeTx);

        // 记录服务费扣减流水
        WalletTransaction feeTx = new WalletTransaction();
        feeTx.setMerchantId(merchantId);
        feeTx.setTransactionType("PLATFORM_FEE");
        feeTx.setOrderId(orderId);
        feeTx.setOrderNo(order.getOrderNo());
        feeTx.setAmount(platformFee.negate());
        feeTx.setBalanceBefore(balanceAfterIncome);
        feeTx.setBalanceAfter(balanceAfterFee);
        feeTx.setRemark("平台服务费");
        feeTx.setCreateTime(LocalDateTime.now());
        transactionRepository.save(feeTx);

        // 更新订单结算状态
        order.setPlatformFee(platformFee);
        order.setMerchantIncome(merchantIncome);
        order.setSettlementStatus("SETTLED");
        order.setSettlementTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * 订单支付时将金额存入系统中间账户（待结算）
     * <p>买家付款后，货款暂存于系统中间账户，待确认收货后再结算给商家</p>
     * @param orderId 订单ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void freezeOrderAmount(Long orderId) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        Long merchantId = order.getMerchantId();
        if (merchantId == null) {
            return;
        }

        MerchantWallet wallet = walletRepository.findByMerchantId(merchantId)
                .orElseGet(() -> createWallet(merchantId));

        // 将订单金额加入中间账户待结算金额
        BigDecimal orderAmount = order.getTotalAmount();
        wallet.setPendingBalance(wallet.getPendingBalance().add(orderAmount));
        wallet.setFrozenBalance(wallet.getFrozenBalance().add(orderAmount));
        wallet.setUpdateTime(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    /**
     * 处理退款（钱包余额变更，清除缓存）
     * @param orderId 订单ID
     */
    @Transactional
    public void processRefund(Long orderId) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 如果订单已结算，需要从商家钱包扣回
        if ("SETTLED".equals(order.getSettlementStatus())) {
            Long merchantId = order.getMerchantId();
            MerchantWallet wallet = walletRepository.findByMerchantId(merchantId)
                    .orElseThrow(() -> new BusinessException("商家钱包不存在"));

            BigDecimal refundAmount = order.getMerchantIncome();
            BigDecimal balanceBefore = wallet.getAvailableBalance();

            if (wallet.getAvailableBalance().compareTo(refundAmount) < 0) {
                throw new BusinessException("商家余额不足，无法退款");
            }

            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(refundAmount));
            wallet.setTotalExpense(wallet.getTotalExpense().add(refundAmount));
            wallet.setUpdateTime(LocalDateTime.now());
            walletRepository.save(wallet);
            // 退款后钱包变更，清除缓存
            cacheService.evictMerchantWallet(merchantId);
            
            // 记录退款流水
            WalletTransaction transaction = new WalletTransaction();
            transaction.setMerchantId(merchantId);
            transaction.setTransactionType("REFUND");
            transaction.setOrderId(orderId);
            transaction.setOrderNo(order.getOrderNo());
            transaction.setAmount(refundAmount.negate());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(wallet.getAvailableBalance());
            transaction.setRemark("订单退款");
            transaction.setCreateTime(LocalDateTime.now());
            transactionRepository.save(transaction);

            // 更新订单结算状态
            order.setSettlementStatus("REFUNDED");
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    /**
     * 获取商家等级对应的服务费率
     * @param merchantLevel 商家等级（1-5）
     * @return 费率
     */
    public BigDecimal getFeeRate(Integer merchantLevel) {
        return FEE_RATES.getOrDefault(merchantLevel != null ? merchantLevel : 1, new BigDecimal("0.001"));
    }

    /**
     * 分页查询平台服务费记录（管理员用），附带商家店铺名称
     * @param page 页码
     * @param size 每页数量
     * @return 分页服务费展示VO
     */
    public Page<ServiceFeeVO> listServiceFees(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ServiceFee> feePage = serviceFeeRepository.findAll(pageable);

        if (feePage.isEmpty()) {
            return Page.empty();
        }

        // 批量查询商家信息
        Set<Long> merchantIds = feePage.getContent().stream()
                .map(ServiceFee::getMerchantId)
                .collect(Collectors.toSet());
        List<Merchant> merchants = merchantRepository.findAllById(merchantIds);
        Map<Long, Merchant> merchantMap = merchants.stream()
                .collect(Collectors.toMap(Merchant::getId, m -> m, (a, b) -> a));

        // 构建 VO
        List<ServiceFeeVO> voList = feePage.getContent().stream().map(fee -> {
            ServiceFeeVO vo = new ServiceFeeVO();
            vo.setId(fee.getId());
            vo.setMerchantId(fee.getMerchantId());
            vo.setOrderId(fee.getOrderId());
            vo.setOrderNo(fee.getOrderNo());
            vo.setOrderAmount(fee.getOrderAmount());
            // 若 merchantLevel 为 null，则从 feeRate 反推交易时的商家等级
            Integer level = fee.getMerchantLevel();
            if (level == null || level == 0) {
                level = deriveLevelFromRate(fee.getFeeRate());
            }
            vo.setMerchantLevel(level);
            vo.setFeeRate(fee.getFeeRate());
            vo.setFeeAmount(fee.getFeeAmount());
            vo.setRemark(fee.getRemark());
            vo.setCreateTime(fee.getCreateTime());

            // 填充商家信息
            Merchant merchant = merchantMap.get(fee.getMerchantId());
            if (merchant != null) {
                vo.setShopName(merchant.getShopName());
                vo.setRealName(merchant.getRealName());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageImpl<>(voList, pageable, feePage.getTotalElements());
    }

    /**
     * 从费率反推交易时的商家等级
     * @param feeRate 费率
     * @return 商家等级（0-5），0表示无对应等级
     */
    private Integer deriveLevelFromRate(BigDecimal feeRate) {
        if (feeRate == null) return 0;
        // 精确匹配到 FEE_RATES 中的已知费率
        for (Map.Entry<Integer, BigDecimal> entry : FEE_RATES.entrySet()) {
            if (feeRate.compareTo(entry.getValue()) == 0) {
                return entry.getKey();
            }
        }
        // 费率为 0 对应 0 级
        if (feeRate.compareTo(BigDecimal.ZERO) == 0) return 0;
        return 0;
    }

    /**
     * 获取全部等级费率配置表
     * @return 等级 -> 费率 的 Map
     */
    public Map<Integer, BigDecimal> getAllFeeRates() {
        return new HashMap<>(FEE_RATES);
    }

    /**
     * 根据商家的历史成交额与用户好评率，动态调整商家等级（1-5）。
     * 返回被调整的商家ID->新等级的Map供管理员查看。
     */
    @Transactional
    public Map<Long, Integer> evaluateAndAdjustMerchantLevels() {
        Map<Long, Integer> changed = new HashMap<>();
        List<Merchant> merchants = merchantRepository.findAll();
        for (Merchant m : merchants) {
            Long merchantId = m.getId();
            // 统计总评价与好评数（质量评分>=4）
            long totalReviews = productReviewRepository.countByMerchantId(merchantId);
            long positiveReviews = productReviewRepository.countPositiveByMerchantId(merchantId);
            double positiveRate = totalReviews == 0 ? 100.0 : (double) positiveReviews / (double) totalReviews * 100.0;

            // 统计已结算的成交额
            java.math.BigDecimal settled = orderRepository.sumSettledAmountByMerchant(merchantId);
            if (settled == null) settled = BigDecimal.ZERO;

            // 决策规则（可调整阈值）：
            // 5级：好评率>=95% 且 成交额>=10000
            // 4级：好评率>=90% 且 成交额>=5000
            // 3级：好评率>=85% 且 成交额>=1000
            // 2级：好评率>=75% 且 成交额>=500
            // 1级：其他
            int newLevel = 1;
            if (positiveRate >= 95.0 && settled.compareTo(new BigDecimal("10000")) >= 0) newLevel = 5;
            else if (positiveRate >= 90.0 && settled.compareTo(new BigDecimal("5000")) >= 0) newLevel = 4;
            else if (positiveRate >= 85.0 && settled.compareTo(new BigDecimal("1000")) >= 0) newLevel = 3;
            else if (positiveRate >= 75.0 && settled.compareTo(new BigDecimal("500")) >= 0) newLevel = 2;

            Integer oldLevel = m.getMerchantLevel() == null ? 1 : m.getMerchantLevel();
            if (!Objects.equals(oldLevel, newLevel)) {
                m.setMerchantLevel(newLevel);
                m.setUpdateTime(LocalDateTime.now());
                merchantRepository.save(m);
                // 记录等级变更审计（自动评估）
                com.example.enterprise.entity.MerchantLevelAudit audit = new com.example.enterprise.entity.MerchantLevelAudit();
                audit.setMerchantId(merchantId);
                audit.setOperatorId(null);
                audit.setOperatorName("system");
                audit.setOldLevel(oldLevel);
                audit.setNewLevel(newLevel);
                audit.setReason("auto_evaluate");
                audit.setCreateTime(LocalDateTime.now());
                merchantLevelAuditRepository.save(audit);
                changed.put(merchantId, newLevel);
            }
        }
        return changed;
    }

    /**
     * 定时任务：周期性评估并调整商家等级
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "${app.merchant-level-scheduler.cron}")
    public void scheduledEvaluateMerchantLevels() {
        if (!merchantLevelSchedulerEnabled) {
            logger.debug("商家等级定时任务被禁用，跳过执行");
            return;
        }
        try {
            Map<Long, Integer> changed = evaluateAndAdjustMerchantLevels();
            if (!changed.isEmpty()) {
                logger.info("商家等级自动调整完成，变更数量：{}，详情：{}", changed.size(), changed);
            } else {
                logger.info("商家等级自动调整完成，未发现需要变更的商家。");
            }
        } catch (Exception ex) {
            logger.error("商家等级自动评估任务执行失败", ex);
        }
    }
}
