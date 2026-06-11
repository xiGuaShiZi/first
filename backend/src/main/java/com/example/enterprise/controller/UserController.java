package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.dto.CartAddDTO;
import com.example.enterprise.dto.CartItemVO;
import com.example.enterprise.dto.CartUpdateDTO;
import com.example.enterprise.dto.CustomerAddressDTO;
import com.example.enterprise.dto.CustomerPasswordDTO;
import com.example.enterprise.dto.CustomerProfileVO;
import com.example.enterprise.dto.ProductDTO;
import com.example.enterprise.entity.CustomerAddress;
import com.example.enterprise.dto.OrderBatchCreateDTO;
import com.example.enterprise.dto.OrderCreateDTO;
import com.example.enterprise.dto.ProductReviewDTO;
import com.example.enterprise.dto.ReturnRequestDTO;
// removed unused import
import com.example.enterprise.entity.Product;
import com.example.enterprise.entity.ProductOrder;
import com.example.enterprise.entity.ProductReview;
import com.example.enterprise.entity.ReturnRequest;
import com.example.enterprise.service.AuthService;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.service.CommerceService;
import com.example.enterprise.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户控制器
 * <p>提供已登录用户的个人资料、密码修改、收货地址、收藏/意向清单、订单、评价和交易协商等接口</p>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    /** 认证服务 */
    private final AuthService authService;
    /** 交易服务 */
    private final CommerceService commerceService;
    /** 内容管理服务 */
    private final ContentService contentService;

    /** 获取客户个人资料（包含默认地址） */
    @GetMapping("/profile")
    public Result<CustomerProfileVO> profile(@RequestHeader("Authorization") String authorization) {
        return Result.success(authService.customerProfile(authService.current(token(authorization))));
    }

    /** 修改客户密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authorization,
                                       @Valid @RequestBody CustomerPasswordDTO dto) {
        authService.changeCustomerPassword(authService.current(token(authorization)), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /** 查询客户收货地址列表 */
    @GetMapping("/addresses")
    public Result<List<CustomerAddress>> addresses(@RequestHeader("Authorization") String authorization) {
        return Result.success(authService.customerAddresses(authService.current(token(authorization))));
    }

    /** 创建收货地址 */
    @PostMapping("/addresses")
    public Result<CustomerAddress> createAddress(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody CustomerAddressDTO dto) {
        return Result.success(authService.saveCustomerAddress(authService.current(token(authorization)), null, dto));
    }

    /** 更新收货地址 */
    @PutMapping("/addresses/{id}")
    public Result<CustomerAddress> updateAddress(@RequestHeader("Authorization") String authorization,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody CustomerAddressDTO dto) {
        return Result.success(authService.saveCustomerAddress(authService.current(token(authorization)), id, dto));
    }

    /** 设置默认收货地址 */
    @PutMapping("/addresses/{id}/default")
    public Result<CustomerAddress> setDefaultAddress(@RequestHeader("Authorization") String authorization,
                                                     @PathVariable Long id) {
        return Result.success(authService.setDefaultAddress(authService.current(token(authorization)), id));
    }

    /** 删除收货地址 */
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(@RequestHeader("Authorization") String authorization,
                                      @PathVariable Long id) {
        authService.deleteCustomerAddress(authService.current(token(authorization)), id);
        return Result.success();
    }

    /** 客户注销登录 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(token(authorization));
        return Result.success();
    }

    /** 学生发布闲置物品 */
    @PostMapping("/products")
    public Result<Product> publishProduct(@RequestHeader("Authorization") String authorization,
                                          @Valid @RequestBody ProductDTO dto) {
        // 禁止普通用户发布商品：请使用商家账号或管理员接口上架
        throw new BusinessException(403, "普通用户无权发布商品；请使用商家账号或管理员接口进行上架操作");
    }

    /** 学生查询自己发布的闲置物品列表 */
    @GetMapping("/products")
    public Result<Page<Product>> myProducts(@RequestHeader("Authorization") String authorization,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listMyProducts(authService.current(token(authorization)).id(), page, size));
    }

    /** 学生更新自己发布的闲置物品 */
    @PutMapping("/products/{id}")
    public Result<Product> updateMyProduct(@RequestHeader("Authorization") String authorization,
                                           @PathVariable Long id,
                                           @Valid @RequestBody ProductDTO dto) {
        // 禁止普通用户更新上架商品：请使用商家账号或管理员接口进行修改
        throw new BusinessException(403, "普通用户无权修改商品；请使用商家账号或管理员接口进行修改操作");
    }

    /** 学生删除自己发布的闲置物品 */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteMyProduct(@RequestHeader("Authorization") String authorization,
                                        @PathVariable Long id) {
        // 禁止普通用户删除上架商品：请使用商家账号或管理员接口进行删除
        throw new BusinessException(403, "普通用户无权删除商品；请使用商家账号或管理员接口进行删除操作");
    }

    /** 获取收藏/意向清单列表 */
    @GetMapping("/cart")
    public Result<List<CartItemVO>> cart(@RequestHeader("Authorization") String authorization) {
        return Result.success(commerceService.cartItems(authService.current(token(authorization))));
    }

    /** 将闲置物品加入收藏/意向清单 */
    @PostMapping("/cart")
    public Result<CartItemVO> addCartItem(@RequestHeader("Authorization") String authorization,
                                          @Valid @RequestBody CartAddDTO dto) {
        return Result.success(commerceService.addCartItem(authService.current(token(authorization)), dto.getProductId(), dto.getSkuId(), dto.getQuantity()));
    }

    /** 更新收藏/意向项数量和状态 */
    @PutMapping("/cart/{id}")
    public Result<CartItemVO> updateCartItem(@RequestHeader("Authorization") String authorization,
                                             @PathVariable Long id,
                                             @Valid @RequestBody CartUpdateDTO dto) {
        return Result.success(commerceService.updateCartItem(authService.current(token(authorization)), id, dto.getQuantity(), dto.getSelected(), dto.getChecked()));
    }

    /** 从收藏/意向清单中移除物品 */
    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCartItem(@RequestHeader("Authorization") String authorization,
                                       @PathVariable Long id) {
        commerceService.deleteCartItem(authService.current(token(authorization)), id);
        return Result.success();
    }

    /** 清空收藏/意向清单 */
    @DeleteMapping("/cart")
    public Result<Void> clearCart(@RequestHeader("Authorization") String authorization) {
        commerceService.clearCart(authService.current(token(authorization)));
        return Result.success();
    }

    /** 单物品下单 */
    @PostMapping("/orders")
    public Result<ProductOrder> createOrder(@RequestHeader("Authorization") String authorization,
                                            @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(commerceService.createOrder(authService.current(token(authorization)), dto));
    }

    /** 批量下单 */
    @PostMapping("/orders/batch")
    public Result<List<ProductOrder>> createOrders(@RequestHeader("Authorization") String authorization,
                                                   @Valid @RequestBody OrderBatchCreateDTO dto) {
        return Result.success(commerceService.createOrders(authService.current(token(authorization)), dto));
    }

    /**
     * 一键下单并使用用户账户余额支付（后台无需对接支付接口）
     */
    @PostMapping("/orders/batch/checkout")
    public Result<List<ProductOrder>> checkoutOrders(@RequestHeader("Authorization") String authorization,
                                                     @Valid @RequestBody OrderBatchCreateDTO dto) {
        return Result.success(commerceService.createAndPayOrders(authService.current(token(authorization)), dto));
    }

    /** 分页查询客户订单列表 */
    @GetMapping("/orders")
    public Result<Page<ProductOrder>> orders(@RequestHeader("Authorization") String authorization,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(commerceService.userOrders(authService.current(token(authorization)), page, size));
    }

    /** 查询订单详情 */
    @GetMapping("/orders/{id}")
    public Result<ProductOrder> order(@RequestHeader("Authorization") String authorization,
                                      @PathVariable Long id) {
        return Result.success(commerceService.userOrder(authService.current(token(authorization)), id));
    }

    /** 确认交易订单 */
    @PutMapping("/orders/{id}/pay")
    public Result<ProductOrder> payOrder(@RequestHeader("Authorization") String authorization,
                                         @PathVariable Long id) {
        return Result.success(commerceService.payOrder(authService.current(token(authorization)), id));
    }

    /** 取消订单 */
    @PutMapping("/orders/{id}/cancel")
    public Result<ProductOrder> cancelOrder(@RequestHeader("Authorization") String authorization,
                                            @PathVariable Long id) {
        return Result.success(commerceService.cancelOrder(authService.current(token(authorization)), id));
    }

    /** 确认收货 */
    @PutMapping("/orders/{id}/confirm-receipt")
    public Result<ProductOrder> confirmReceipt(@RequestHeader("Authorization") String authorization,
                                               @PathVariable Long id) {
        return Result.success(commerceService.confirmReceipt(authService.current(token(authorization)), id));
    }

    /**
     * 使用积分兑换现金（每100积分=1元），积分按整百兑换
     * @param authorization Authorization头
     * @param points 要兑换的积分数
     * @return 更新后的客户资料
     */
    @PostMapping("/points/redeem")
    public Result<CustomerProfileVO> redeemPoints(@RequestHeader("Authorization") String authorization,
                                                  @RequestParam int points) {
        authService.redeemPoints(authService.current(token(authorization)), points);
        return Result.success(authService.customerProfile(authService.current(token(authorization))));
    }

    /** 查询订单评价 */
    @GetMapping("/orders/{id}/review")
    public Result<ProductReview> orderReview(@RequestHeader("Authorization") String authorization,
                                             @PathVariable Long id) {
        return Result.success(commerceService.orderReview(authService.current(token(authorization)), id));
    }

    /** 分页查询当前用户收到的商家评价（商家对买家的评价） */
    @GetMapping("/buyer-reviews")
    public Result<Page<com.example.enterprise.entity.BuyerReview>> buyerReviews(@RequestHeader("Authorization") String authorization,
                                                                                @RequestParam(defaultValue = "1") int page,
                                                                                @RequestParam(defaultValue = "10") int size) {
        com.example.enterprise.service.AuthService.TokenUser user = authService.current(token(authorization));
        return Result.success(commerceService.userBuyerReviews(user.id(), page, size));
    }

    /** 提交物品评价 */
    @PostMapping("/reviews")
    public Result<ProductReview> createReview(@RequestHeader("Authorization") String authorization,
                                              @Valid @RequestBody ProductReviewDTO dto) {
        return Result.success(commerceService.createReview(authService.current(token(authorization)), dto));
    }

    /** 提交交易协商申请 */
    @PostMapping("/returns")
    public Result<ReturnRequest> createReturn(@RequestHeader("Authorization") String authorization,
                                              @Valid @RequestBody ReturnRequestDTO dto) {
        return Result.success(commerceService.createReturn(authService.current(token(authorization)), dto));
    }

    /** 分页查询用户交易协商申请列表 */
    @GetMapping("/returns")
    public Result<Page<ReturnRequest>> returns(@RequestHeader("Authorization") String authorization,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(commerceService.userReturns(authService.current(token(authorization)), page, size));
    }

    /** 买家发起议价 */
    @PostMapping("/bargain-offers")
    public Result<com.example.enterprise.entity.BargainOffer> createBargainOffer(@RequestHeader("Authorization") String authorization,
                                                                                  @RequestParam Long productId,
                                                                                  @RequestParam java.math.BigDecimal offerPrice) {
        return Result.success(commerceService.createBargainOffer(authService.current(token(authorization)), productId, offerPrice));
    }

    /** 分页查询买家议价记录 */
    @GetMapping("/bargain-offers")
    public Result<Page<com.example.enterprise.entity.BargainOffer>> myBargainOffers(@RequestHeader("Authorization") String authorization,
                                                                                      @RequestParam(defaultValue = "1") int page,
                                                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(commerceService.userBargainOffers(authService.current(token(authorization)).id(), page, size));
    }

    /** 买家取消议价 */
    @PutMapping("/bargain-offers/{id}/cancel")
    public Result<com.example.enterprise.entity.BargainOffer> cancelBargainOffer(@RequestHeader("Authorization") String authorization,
                                                                                  @PathVariable Long id) {
        return Result.success(commerceService.cancelBargainOffer(authService.current(token(authorization)), id));
    }

    /** 从Authorization头中提取Token */
    private String token(String authorization) {
        return authorization == null ? null : authorization.replace("Bearer ", "");
    }
}
