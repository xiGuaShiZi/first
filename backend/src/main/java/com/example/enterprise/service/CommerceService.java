package com.example.enterprise.service;

import com.example.enterprise.dto.CartItemVO;
import com.example.enterprise.dto.OrderBatchCreateDTO;
import com.example.enterprise.dto.OrderCreateDTO;
import com.example.enterprise.dto.ProductReviewDTO;
import com.example.enterprise.dto.ReturnHandleDTO;
import com.example.enterprise.dto.ReturnRequestDTO;
import com.example.enterprise.entity.Product;
import com.example.enterprise.entity.ProductOrder;
import com.example.enterprise.entity.ProductReview;
import com.example.enterprise.entity.ReturnRequest;
import com.example.enterprise.entity.ShoppingCartItem;
import com.example.enterprise.entity.CustomerAddress;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.repository.ProductOrderRepository;
import com.example.enterprise.repository.ProductRepository;
import com.example.enterprise.repository.ProductReviewRepository;
import com.example.enterprise.repository.ReturnRequestRepository;
import com.example.enterprise.repository.ShoppingCartItemRepository;
import com.example.enterprise.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 交易服务
 * <p>提供收藏/意向、订单、评价、交易协商等校园二手交易平台核心业务逻辑</p>
 */
@Service
@RequiredArgsConstructor
public class CommerceService {
    /** 闲置物品数据访问 */
    private final ProductRepository productRepository;
    /** 订单数据访问 */
    private final ProductOrderRepository orderRepository;
    /** 评价数据访问 */
    private final ProductReviewRepository reviewRepository;
    /** 商家对买家评价数据访问 */
    private final com.example.enterprise.repository.BuyerReviewRepository buyerReviewRepository;
    /** 交易协商申请数据访问 */
    private final ReturnRequestRepository returnRequestRepository;
    /** 收藏/意向项数据访问 */
    private final ShoppingCartItemRepository cartItemRepository;
    /** 客户收货地址数据访问 */
    private final CustomerAddressRepository customerAddressRepository;
    /** 客户数据访问（用于余额扣减等操作） */
    private final com.example.enterprise.repository.CustomerRepository customerRepository;
    /** 商家钱包管理服务 */
    private final MerchantWalletService walletService;

    /**
     * 获取用户收藏/意向清单
     * @param user 当前登录用户
     * @return 收藏/意向项VO列表
     */
    public List<CartItemVO> cartItems(AuthService.TokenUser user) {
        return cartItemRepository.findByCustomerIdOrderByCreateTimeDescIdDesc(user.id()).stream()
                .map(this::toCartItemVO)
                .toList();
    }

    /**
     * 将闲置物品加入收藏/意向清单，已存在则累加数量
     * @param user 当前登录用户
     * @param productId 物品ID
     * @param skuId SKU ID
     * @param quantity 物品数量
     * @return 更新后的收藏/意向项VO
     */
    @Transactional
    public CartItemVO addCartItem(AuthService.TokenUser user, Long productId, Long skuId, Integer quantity) {
        Product product = availableProduct(productId);
        int nextQuantity = normalizeQuantity(quantity);
        Long normalizedSkuId = normalizeSkuId(skuId);
        ShoppingCartItem item = cartItemRepository.findWithLockByCustomerIdAndProductIdAndSkuId(user.id(), productId, normalizedSkuId)
                .orElseGet(() -> {
                    ShoppingCartItem created = new ShoppingCartItem();
                    created.setCustomerId(user.id());
                    created.setProductId(productId);
                    created.setSkuId(normalizedSkuId);
                    created.setQuantity(0);
                    created.setSelected(1);
                    created.setChecked(1);
                    created.setSnapshotProductName(product.getName());
                    created.setSnapshotPrice(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
                    created.setSnapshotImage(product.getImage());
                    created.setCreateTime(LocalDateTime.now());
                    return created;
                });
        int quantityTotal = item.getQuantity() + nextQuantity;
        if (product.getStock() != null && product.getStock() < quantityTotal) {
            throw new BusinessException("物品数量不足");
        }
        item.setQuantity(quantityTotal);
        item.setUpdateTime(LocalDateTime.now());
        return toCartItemVO(cartItemRepository.save(item));
    }

    /**
     * 更新收藏/意向项数量和选中状态
     * @param user 当前登录用户
     * @param id 收藏/意向项ID
     * @param quantity 新数量
     * @param selected 是否选中
     * @param checked 是否勾选结算
     * @return 更新后的收藏/意向项VO
     */
    @Transactional
    public CartItemVO updateCartItem(AuthService.TokenUser user, Long id, Integer quantity, Integer selected, Integer checked) {
        ShoppingCartItem item = cartItemRepository.findByIdAndCustomerId(id, user.id())
                .orElseThrow(() -> new BusinessException("收藏项不存在"));
        Product product = availableProduct(item.getProductId());
        int nextQuantity = normalizeQuantity(quantity);
        if (product.getStock() != null && product.getStock() < nextQuantity) {
            throw new BusinessException("物品数量不足");
        }
        item.setQuantity(nextQuantity);
        int checkedValue = Integer.valueOf(0).equals(checked != null ? checked : selected) ? 0 : 1;
        item.setSelected(checkedValue);
        item.setChecked(checkedValue);
        item.setUpdateTime(LocalDateTime.now());
        return toCartItemVO(cartItemRepository.save(item));
    }

    /**
     * 从收藏/意向清单中移除某个物品
     * @param user 当前登录用户
     * @param id 收藏/意向项ID
     */
    @Transactional
    public void deleteCartItem(AuthService.TokenUser user, Long id) {
        ShoppingCartItem item = cartItemRepository.findByIdAndCustomerId(id, user.id())
                .orElseThrow(() -> new BusinessException("收藏项不存在"));
        cartItemRepository.delete(item);
    }

    /**
     * 清空用户收藏/意向清单
     * @param user 当前登录用户
     */
    @Transactional
    public void clearCart(AuthService.TokenUser user) {
        cartItemRepository.deleteByCustomerId(user.id());
    }

    /**
     * 单物品下单
     * @param user 当前登录用户
     * @param dto 下单请求参数
     * @return 创建的订单实体
     */
    @Transactional
    public ProductOrder createOrder(AuthService.TokenUser user, OrderCreateDTO dto) {
        String mainOrderNo = "MO" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return createOrder(user, dto, mainOrderNo, 1);
    }

    /**
     * 批量下单（从收藏/意向清单结算），共用一个主订单号
     * @param user 当前登录用户
     * @param dto 批量下单请求参数
     * @return 创建的订单列表
     */
    @Transactional
    public List<ProductOrder> createOrders(AuthService.TokenUser user, OrderBatchCreateDTO dto) {
        String mainOrderNo = "MO" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        List<ProductOrder> orders = new java.util.ArrayList<>();
        for (int index = 0; index < dto.getItems().size(); index++) {
            OrderCreateDTO item = dto.getItems().get(index);
            item.setAddressId(dto.getAddressId());
            item.setContactPhone(dto.getContactPhone());
            item.setAddress(dto.getAddress());
            orders.add(createOrder(user, item, mainOrderNo, index + 1));
        }

        // 奖励积分：每消费1元计1积分（取整元数）
        int pointsTotal = orders.stream()
                .map(o -> nullToZero(o.getTotalAmount()))
                .map(amount -> amount.setScale(0, java.math.RoundingMode.DOWN))
                .mapToInt(bd -> bd.intValue())
                .sum();
        if (pointsTotal > 0) {
            com.example.enterprise.entity.Customer savedCustomer = customerRepository.findById(user.id())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            Integer current = savedCustomer.getPoints() == null ? 0 : savedCustomer.getPoints();
            savedCustomer.setPoints(current + pointsTotal);
            savedCustomer.setUpdateTime(LocalDateTime.now());
            customerRepository.save(savedCustomer);
        }

        return orders;
    }

    /**
     * 批量下单并使用用户账户余额支付（无需对接第三方支付），原子操作：创建订单->校验并扣减客户余额->标记已支付并扣减库存
     * @param user 当前登录用户
     * @param dto 批量下单请求参数
     * @return 创建并已支付的订单列表
     */
    @Transactional
    public List<ProductOrder> createAndPayOrders(AuthService.TokenUser user, OrderBatchCreateDTO dto) {
        // 先创建子订单（仍为待付款状态）
        List<ProductOrder> orders = createOrders(user, dto);

        // 计算总金额
        BigDecimal totalAmount = orders.stream()
                .map(o -> nullToZero(o.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 扣减客户余额
        com.example.enterprise.entity.Customer customer = customerRepository.findById(user.id())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (customer.getBalance() == null) {
            customer.setBalance(BigDecimal.ZERO);
        }
        if (customer.getBalance().compareTo(totalAmount) < 0) {
            throw new BusinessException("账户余额不足，请先充值或选择其他支付方式");
        }
        customer.setBalance(customer.getBalance().subtract(totalAmount));
        customer.setUpdateTime(LocalDateTime.now());
        customerRepository.save(customer);

        // 将所有子订单标记为已支付，扣减库存并触发中间账户冻结
        LocalDateTime paidTime = LocalDateTime.now();
        for (ProductOrder item : orders) {
            Product product = lockedAvailableProduct(item.getProductId());
            if (product.getStock() != null && product.getStock() < item.getQuantity()) {
                throw new BusinessException(item.getProductName() + "数量不足");
            }
            if (product.getStock() != null) {
                product.setStock(product.getStock() - item.getQuantity());
            }
            product.setSalesCount((product.getSalesCount() == null ? 0 : product.getSalesCount()) + item.getQuantity());
            productRepository.save(product);

            item.setPaidTime(paidTime);
            item.setStatus("WAIT_SELLER_SEND_GOODS");
            item.setUpdateTime(paidTime);
            orderRepository.save(item);

            // 将货款存入系统中间账户（待确认收货后结算给商家）
            try {
                walletService.freezeOrderAmount(item.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return orders;
    }

    /** 创建单个子订单，设置订单号、物品快照、交易地址等 */
    private ProductOrder createOrder(AuthService.TokenUser user, OrderCreateDTO dto, String mainOrderNo, int subIndex) {
        Product product = availableProduct(dto.getProductId());
        int quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new BusinessException("物品数量不足");
        }
        ProductOrder order = new ProductOrder();
        order.setOrderNo("PO" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        order.setMainOrderNo(mainOrderNo);
        order.setSubOrderNo(mainOrderNo + "-" + String.format("%02d", subIndex));
        order.setShopId(1L);
        order.setShopName("校园二手交易平台");
        order.setMerchantId(product.getPublisherId());
        order.setCustomerId(user.id());
        order.setCustomerName(user.username());
        order.setProductId(product.getId());
        order.setSkuId(normalizeSkuId(dto.getSkuId()));
        order.setProductName(product.getName());
        order.setPrice(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
        order.setQuantity(quantity);
        order.setProductDiscountAmount(BigDecimal.ZERO);
        order.setOrderDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getPrice().multiply(BigDecimal.valueOf(quantity)));
        OrderAddress orderAddress = resolveOrderAddress(user, dto);
        order.setContactPhone(orderAddress.phone());
        order.setAddress(orderAddress.fullAddress());
        order.setPaymentMethod("SIMULATED");
        order.setStatus("WAIT_BUYER_PAY");
        order.setIsReviewed(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * 分页查询客户订单列表
     * @param user 当前登录用户
     * @param page 页码
     * @param size 每页数量
     * @return 分页订单结果
     */
    public Page<ProductOrder> userOrders(AuthService.TokenUser user, int page, int size) {
        Pageable pageable = page(page, size);
        return orderRepository.findByCustomerId(user.id(), pageable);
    }

    /**
     * 查询客户单个订单详情，校验归属
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 订单实体
     */
    public ProductOrder userOrder(AuthService.TokenUser user, Long id) {
        ProductOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getCustomerId().equals(user.id())) {
            throw new BusinessException("不能查看他人的订单");
        }
        return order;
    }

    /**
     * 确认交易，同一主订单下所有子订单同时确认，扣减库存并增加成交次数
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 支付后的订单实体
     */
    @Transactional
    public ProductOrder payOrder(AuthService.TokenUser user, Long id) {
        ProductOrder order = userOrder(user, id);
        if (!"WAIT_BUYER_PAY".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能支付");
        }
        List<ProductOrder> payableOrders = orderRepository.findByCustomerIdAndMainOrderNoAndStatus(user.id(), order.getMainOrderNo(), "WAIT_BUYER_PAY");
        if (payableOrders.isEmpty()) {
            throw new BusinessException("没有可支付的订单");
        }
        LocalDateTime paidTime = LocalDateTime.now();
        for (ProductOrder item : payableOrders) {
            Product product = lockedAvailableProduct(item.getProductId());
            if (product.getStock() != null && product.getStock() < item.getQuantity()) {
                throw new BusinessException(item.getProductName() + "数量不足");
            }
            if (product.getStock() != null) {
                product.setStock(product.getStock() - item.getQuantity());
            }
            product.setSalesCount((product.getSalesCount() == null ? 0 : product.getSalesCount()) + item.getQuantity());
            productRepository.save(product);
            item.setPaidTime(paidTime);
            item.setStatus("WAIT_SELLER_SEND_GOODS");
            item.setUpdateTime(paidTime);
            orderRepository.save(item);

            // 将货款存入系统中间账户（待确认收货后结算给商家）
            try {
                walletService.freezeOrderAmount(item.getId());
            } catch (Exception e) {
                // 中间账户记录失败不影响支付流程
                e.printStackTrace();
            }
        }

        // 支付成功后，累计积分：每消费1元计1积分（取整元数）
        int points = payableOrders.stream()
                .map(o -> nullToZero(o.getTotalAmount()))
                .map(amount -> amount.setScale(0, java.math.RoundingMode.DOWN))
                .mapToInt(bd -> bd.intValue())
                .sum();
        if (points > 0) {
            com.example.enterprise.entity.Customer customer = customerRepository.findById(user.id())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            Integer current = customer.getPoints() == null ? 0 : customer.getPoints();
            customer.setPoints(current + points);
            customer.setUpdateTime(LocalDateTime.now());
            customerRepository.save(customer);
        }

        return orderRepository.findById(id).orElse(order);
    }

    /**
     * 取消待付款订单，同一主订单下所有子订单同时取消
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 取消后的订单实体
     */
    @Transactional
    public ProductOrder cancelOrder(AuthService.TokenUser user, Long id) {
        ProductOrder order = userOrder(user, id);
        if (!"WAIT_BUYER_PAY".equals(order.getStatus())) {
            throw new BusinessException("仅待付款订单可以取消");
        }
        LocalDateTime now = LocalDateTime.now();
        List<ProductOrder> cancelableOrders = orderRepository.findByCustomerIdAndMainOrderNoAndStatus(user.id(), order.getMainOrderNo(), "WAIT_BUYER_PAY");
        for (ProductOrder item : cancelableOrders) {
            item.setStatus("TRADE_CLOSED");
            item.setUpdateTime(now);
            orderRepository.save(item);
        }
        return orderRepository.findById(id).orElse(order);
    }

    /**
     * 管理员发货，填写物流单号
     * @param id 订单ID
     * @param logisticsNo 物流单号
     * @return 发货后的订单实体
     */
    @Transactional
    public ProductOrder shipOrder(Long id, String logisticsNo) {
        ProductOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"WAIT_SELLER_SEND_GOODS".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可以发货");
        }
        order.setLogisticsNo(logisticsNo);
        order.setStatus("WAIT_BUYER_CONFIRM_GOODS");
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /** 商家自身为其订单发货（校验归属、状态） */
    @Transactional
    public ProductOrder merchantShipOrder(AuthService.TokenUser merchant, Long id, String logisticsNo) {
        ProductOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!merchant.id().equals(order.getMerchantId())) {
            throw new BusinessException("无权为他人订单发货");
        }
        if (!"WAIT_SELLER_SEND_GOODS".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可以发货");
        }
        order.setLogisticsNo(logisticsNo);
        order.setStatus("WAIT_BUYER_CONFIRM_GOODS");
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * 商家评价买家（针对购买或退货等订单）
     * @param merchant 当前登录商家
     * @param orderId 订单ID
     * @param rating 评分 1-5
     * @param content 可选评价内容
     * @return 保存的BuyerReview实体
     */
    @Transactional
    public com.example.enterprise.entity.BuyerReview merchantReviewBuyer(AuthService.TokenUser merchant, Long orderId, Integer rating, String content) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!merchant.id().equals(order.getMerchantId())) {
            throw new BusinessException("无权评价非自己订单的买家");
        }
        // 允许在交易完成或售后完成后评价买家
        if (!"TRADE_FINISHED".equals(order.getStatus()) && !"AFTER_SALES_FINISHED".equals(order.getStatus())) {
            throw new BusinessException("仅在交易完成或售后完成后可评价买家");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分需为1到5之间的整数");
        }
        if (buyerReviewRepository.existsByOrderIdAndMerchantId(order.getId(), merchant.id())) {
            throw new BusinessException("该订单已评价过买家，不能重复评价");
        }

        com.example.enterprise.entity.BuyerReview review = new com.example.enterprise.entity.BuyerReview();
        review.setOrderId(order.getId());
        review.setOrderNo(order.getOrderNo());
        review.setMerchantId(merchant.id());
        review.setMerchantName(merchant.username());
        review.setBuyerId(order.getCustomerId());
        review.setBuyerName(order.getCustomerName());
        review.setRating(rating);
        review.setReviewType("PURCHASE");
        review.setContent(content);
        review.setCreateTime(java.time.LocalDateTime.now());
        return buyerReviewRepository.save(review);
    }

    /**
     * 客户确认收货，订单状态变为交易完成
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 确认收货后的订单实体
     */
    @Transactional
    public ProductOrder confirmReceipt(AuthService.TokenUser user, Long id) {
        ProductOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getCustomerId().equals(user.id())) {
            throw new BusinessException("不能操作他人的订单");
        }
        if (!"WAIT_BUYER_CONFIRM_GOODS".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能确认收货");
        }
        order.setStatus("TRADE_FINISHED");
        order.setFinishTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        // 触发钱包结算
        try {
            walletService.settleOrder(order.getId(), false);
        } catch (Exception e) {
            // 结算失败不影响确认收货，记录日志即可
            e.printStackTrace();
        }

        return order;
    }

    /**
     * 查询客户对指定订单的最新评价
     * @param user 当前登录用户
     * @param orderId 订单ID
     * @return 评价实体，不存在则返回null
     */
    public ProductReview orderReview(AuthService.TokenUser user, Long orderId) {
        return reviewRepository.findFirstByOrderIdAndCustomerIdOrderByCreateTimeDesc(orderId, user.id()).orElse(null);
    }

    /**
     * 创建物品评价，支持主评价和追加评价，含时效性校验
     * @param user 当前登录用户
     * @param dto 评价请求参数
     * @return 创建的评价实体
     */
    @Transactional
    public ProductReview createReview(AuthService.TokenUser user, ProductReviewDTO dto) {
        ProductOrder order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getCustomerId().equals(user.id())) {
            throw new BusinessException("不能评价他人的订单");
        }
        if (!"TRADE_FINISHED".equals(order.getStatus())) {
            throw new BusinessException("确认收货后才能评价");
        }
        String reviewType = normalizeReviewType(dto.getReviewType());
        LocalDateTime finishedTime = order.getFinishTime() == null ? order.getUpdateTime() : order.getFinishTime();
        if ("MAIN".equals(reviewType) && finishedTime != null && finishedTime.plusDays(15).isBefore(LocalDateTime.now())) {
            throw new BusinessException("主评价需在交易成功15天内提交");
        }
        if ("ADDITIONAL".equals(reviewType) && finishedTime != null && finishedTime.plusDays(180).isBefore(LocalDateTime.now())) {
            throw new BusinessException("追加评价需在180天内提交");
        }
        if ("ADDITIONAL".equals(reviewType) && !reviewRepository.existsByOrderIdAndReviewType(order.getId(), "MAIN")) {
            throw new BusinessException("追加评价前需先完成主评价");
        }
        if (reviewRepository.existsByOrderIdAndReviewType(order.getId(), reviewType)) {
            throw new BusinessException("该类型评价已提交，提交后不可修改或删除");
        }
        ProductReview review = new ProductReview();
        review.setOrderId(order.getId());
        review.setOrderNo(order.getOrderNo());
        review.setCustomerId(user.id());
        review.setCustomerName(user.username());
        review.setProductId(order.getProductId());
        review.setProductName(order.getProductName());
        review.setReviewType(reviewType);
        review.setQualityRating(dto.getQualityRating());
        review.setServiceRating(dto.getServiceRating());
        review.setLogisticsRating(dto.getLogisticsRating());
        review.setServiceAttitudeRating(dto.getServiceAttitudeRating());
        review.setContent(dto.getContent());
        review.setMediaUrls(dto.getMediaUrls());
        review.setAnonymous(Integer.valueOf(1).equals(dto.getAnonymous()) ? 1 : 0);
        review.setCreateTime(LocalDateTime.now());
        if ("MAIN".equals(reviewType)) {
            order.setIsReviewed(1);
        }
        orderRepository.save(order);
        return reviewRepository.save(review);
    }

    /**
     * 创建交易协商申请，根据类型校验订单状态
     * @param user 当前登录用户
     * @param dto 交易协商申请请求参数
     * @return 创建的交易协商申请实体
     */
    @Transactional
    public ReturnRequest createReturn(AuthService.TokenUser user, ReturnRequestDTO dto) {
        ProductOrder order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getCustomerId().equals(user.id())) {
            throw new BusinessException("不能为他人订单发起协商");
        }
        String requestType = normalizeReturnType(dto.getRequestType());
        if ("ONLY_REFUND".equals(requestType) && !"WAIT_SELLER_SEND_GOODS".equals(order.getStatus())) {
            throw new BusinessException("仅退款只适用于已付款未发货订单");
        }
        if ("RETURN_REFUND".equals(requestType) || "EXCHANGE".equals(requestType) || "REPAIR".equals(requestType)) {
            // 退货/换货/维修仅在买家确认收货后申请，并且需在收货后24小时内提交申请
            if (!"TRADE_FINISHED".equals(order.getStatus())) {
                throw new BusinessException("仅在确认收货后才能申请退货或换货");
            }
            LocalDateTime finishTime = order.getFinishTime();
            if (finishTime == null) finishTime = order.getUpdateTime();
            if (finishTime == null || finishTime.plusHours(24).isBefore(LocalDateTime.now())) {
                throw new BusinessException("发起退货申请已超过收货后24小时，无法申请退货");
            }
        }
        ReturnRequest request = new ReturnRequest();
        request.setOrderId(order.getId());
        request.setOrderNo(order.getOrderNo());
        request.setCustomerId(user.id());
        request.setCustomerName(user.username());
        request.setProductName(order.getProductName());
        request.setRequestType(requestType);
        request.setReason(dto.getReason());
        request.setEvidenceMedia(dto.getEvidenceMedia());
        request.setStatus("APPLY_PENDING");
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        order.setStatus("ONLY_REFUND".equals(requestType) ? "REFUNDING" : "RETURNING");
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
        return returnRequestRepository.save(request);
    }

    /**
     * 分页查询用户的交易协商申请
     * @param user 当前登录用户
     * @param page 页码
     * @param size 每页数量
     * @return 分页交易协商申请结果
     */
    public Page<ReturnRequest> userReturns(AuthService.TokenUser user, int page, int size) {
        return returnRequestRepository.findByCustomerId(user.id(), page(page, size));
    }

    /**
     * 分页查询所有订单（管理员用）
     * @param page 页码
     * @param size 每页数量
     * @return 分页订单结果
     */
    public Page<ProductOrder> allOrders(int page, int size) {
        return orderRepository.findAll(page(page, size));
    }

    /** 分页查询商家自己的订单（商家中心使用） */
    public Page<ProductOrder> merchantOrders(AuthService.TokenUser merchant, int page, int size) {
        Pageable pageable = page(page, size);
        return orderRepository.findByMerchantId(merchant.id(), pageable);
    }

    /** 分页查询某个买家收到的商家评价（供买家查看自己被评价的记录） */
    public Page<com.example.enterprise.entity.BuyerReview> userBuyerReviews(Long buyerId, int page, int size) {
        Pageable pageable = page(page, size);
        return buyerReviewRepository.findByBuyerId(buyerId, pageable);
    }

    /**
     * 订单统计，支持按日期范围查询，返回总金额、订单数、成交数量、分类和日期维度统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据Map
     */
    public Map<String, Object> orderStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        if (end.isBefore(start)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        BigDecimal totalAmount = nullToZero(orderRepository.sumAmountBetween(startTime, endTime));
        long orderCount = nullToZero(orderRepository.countBetween(startTime, endTime));
        long quantityTotal = nullToZero(orderRepository.sumQuantityBetween(startTime, endTime));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", start);
        data.put("endDate", end);
        data.put("totalAmount", totalAmount);
        data.put("orderCount", orderCount);
        data.put("quantityTotal", quantityTotal);
        data.put("averageOrderAmount", orderCount == 0 ? BigDecimal.ZERO : totalAmount.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP));
        data.put("byCategory", orderRepository.sumByCategory(startTime, endTime).stream().map(this::statRow).toList());
        data.put("byDate", orderRepository.sumByDate(startTime, endTime).stream().map(this::statRow).toList());
        return data;
    }

    /**
     * 分页查询所有交易协商申请（管理员用），可按状态筛选
     * @param status 状态筛选，为null时查询全部
     * @param page 页码
     * @param size 每页数量
     * @return 分页交易协商申请结果
     */
    public Page<ReturnRequest> allReturns(String status, int page, int size) {
        Pageable pageable = page(page, size);
        return status == null || status.isBlank()
                ? returnRequestRepository.findAll(pageable)
                : returnRequestRepository.findByStatus(status, pageable);
    }

    /**
     * 管理员处理交易协商申请，同步更新关联订单状态
     * @param id 交易协商申请ID
     * @param dto 处理请求参数
     * @return 处理后的交易协商申请实体
     */
    @Transactional
    public ReturnRequest handleReturn(Long id, ReturnHandleDTO dto) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交易协商申请不存在"));
        if (!validReturnStatus(dto.getStatus())) {
            throw new BusinessException("协商状态不合法");
        }
        request.setStatus(dto.getStatus());
        request.setReply(dto.getReply());
        request.setHandleTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        orderRepository.findById(request.getOrderId()).ifPresent(order -> {
            order.setStatus(returnOrderStatus(dto.getStatus(), order.getStatus()));
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);
        });
        return returnRequestRepository.save(request);
    }

    /** 查询订单总数 */
    public long orderTotal() {
        return orderRepository.count();
    }

    /** 查询待处理的交易协商申请数量 */
    public long returnPendingTotal() {
        return returnRequestRepository.countByStatus("APPLY_PENDING");
    }

    /** 构建分页请求，页码从1开始，按创建时间倒序 */
    private Pageable page(int page, int size) {
        return PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createTime"));
    }

    /** 查询可用物品（在售），不存在或已下架则抛异常 */
    private Product availableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("物品已下架");
        }
        return product;
    }

    /** 查询可用物品并加悲观写锁（用于库存扣减等并发场景） */
    private Product lockedAvailableProduct(Long productId) {
        Product product = productRepository.findWithLockById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("物品已下架");
        }
        return product;
    }

    /** 数量标准化：null或小于1时默认为1 */
    private int normalizeQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    /** 将统计查询结果行转换为Map */
    private Map<String, Object> statRow(Object[] row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("label", String.valueOf(row[0]));
        data.put("orderCount", ((Number) row[1]).longValue());
        data.put("quantity", ((Number) row[2]).longValue());
        data.put("amount", row[3] instanceof BigDecimal amount ? amount : new BigDecimal(String.valueOf(row[3])));
        return data;
    }

    /** BigDecimal空值转零 */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** Long空值转零 */
    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    /** 解析订单收货地址，优先使用地址ID，其次使用请求中传入的地址信息 */
    private OrderAddress resolveOrderAddress(AuthService.TokenUser user, OrderCreateDTO dto) {
        if (dto.getAddressId() != null) {
            CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(dto.getAddressId(), user.id())
                    .orElseThrow(() -> new BusinessException("收货地址不存在"));
            return new OrderAddress(address.getPhone(), fullAddress(address));
        }
        if (dto.getContactPhone() == null || dto.getContactPhone().isBlank() || dto.getAddress() == null || dto.getAddress().isBlank()) {
            throw new BusinessException("请选择收货地址");
        }
        return new OrderAddress(dto.getContactPhone(), dto.getAddress());
    }

    /** 拼接完整地址（省+市+区+详细地址） */
    private String fullAddress(CustomerAddress address) {
        return String.join("", List.of(
                blankToEmpty(address.getProvince()),
                blankToEmpty(address.getCity()),
                blankToEmpty(address.getDistrict()),
                blankToEmpty(address.getDetailAddress())
        ));
    }

    /** 空白字符串转空字符串 */
    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** 将收藏/意向项实体转换为VO，包含当前价格与快照价格比对 */
    private CartItemVO toCartItemVO(ShoppingCartItem item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new BusinessException("收藏项不存在"));
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setProductId(product.getId());
        vo.setSkuId(item.getSkuId());
        vo.setProductName(product.getName());
        vo.setImage(product.getImage());
        BigDecimal currentPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        vo.setPrice(currentPrice);
        vo.setSnapshotPrice(item.getSnapshotPrice());
        vo.setSnapshotProductName(item.getSnapshotProductName());
        vo.setSnapshotImage(item.getSnapshotImage());
        vo.setLowerPrice(item.getSnapshotPrice() != null && currentPrice.compareTo(item.getSnapshotPrice()) < 0);
        vo.setUnit(product.getUnit());
        vo.setStock(product.getStock());
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());
        vo.setChecked(item.getChecked() == null ? item.getSelected() : item.getChecked());
        vo.setSubtotal(vo.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }

    /** 交易协商类型标准化，兼容前端传入的简写 */
    private String normalizeReturnType(String requestType) {
        if ("RETURN".equals(requestType)) return "RETURN_REFUND";
        if ("SUPPLEMENT".equals(requestType) || "OTHER".equals(requestType)) return "REPAIR";
        if ("ONLY_REFUND".equals(requestType) || "RETURN_REFUND".equals(requestType) || "EXCHANGE".equals(requestType) || "REPAIR".equals(requestType)) return requestType;
        throw new BusinessException("交易协商类型不合法");
    }

    /** SKU ID标准化，null时默认为0 */
    private Long normalizeSkuId(Long skuId) {
        return skuId == null ? 0L : skuId;
    }

    /** 评价类型标准化，非ADDITIONAL则默认为MAIN */
    private String normalizeReviewType(String reviewType) {
        return "ADDITIONAL".equals(reviewType) ? "ADDITIONAL" : "MAIN";
    }

    /** 校验交易协商状态是否合法 */
    private boolean validReturnStatus(String status) {
        return "APPLY_PENDING".equals(status)
                || "APPROVED".equals(status)
                || "REJECTED".equals(status)
                || "BUYER_RETURNING".equals(status)
                || "SELLER_RECEIVED".equals(status)
                || "REFUNDED".equals(status)
                || "RESHIPPED".equals(status)
                || "COMPLETED".equals(status)
                || "CLOSED".equals(status);
    }

    /** 根据交易协商处理状态计算关联订单的新状态 */
    private String returnOrderStatus(String returnStatus, String currentStatus) {
        if ("REJECTED".equals(returnStatus) || "CLOSED".equals(returnStatus)) {
            return "REFUNDING".equals(currentStatus) ? "WAIT_SELLER_SEND_GOODS" : "WAIT_BUYER_CONFIRM_GOODS";
        }
        if ("COMPLETED".equals(returnStatus) || "REFUNDED".equals(returnStatus)) {
            return "AFTER_SALES_FINISHED";
        }
        return currentStatus;
    }

    /** 订单交易地址内部记录 */
    private record OrderAddress(String phone, String fullAddress) {}
}
