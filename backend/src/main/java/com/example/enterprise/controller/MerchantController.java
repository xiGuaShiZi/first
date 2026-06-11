package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.dto.CustomerPasswordDTO;
import com.example.enterprise.dto.ProductDTO;
import com.example.enterprise.dto.ShopInfoDTO;
import com.example.enterprise.entity.Merchant;
import com.example.enterprise.entity.MerchantWallet;
import com.example.enterprise.entity.Product;
import com.example.enterprise.entity.ProductOrder;
import com.example.enterprise.entity.WalletTransaction;
import com.example.enterprise.service.AuthService;
import com.example.enterprise.service.ContentService;
import com.example.enterprise.service.MerchantWalletService;
import com.example.enterprise.service.CommerceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 商家控制器
 * <p>提供商家专属功能接口，包括个人资料查看、密码修改、商品管理、店铺信息等</p>
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {
    /** 认证服务 */
    private final AuthService authService;
    /** 内容管理服务 */
    private final ContentService contentService;
    /** 交易服务（用于商家发货等操作） */
    private final CommerceService commerceService;
    /** 商家钱包管理服务 */
    private final MerchantWalletService walletService;

    /** 获取商家个人资料 */
    @GetMapping("/profile")
    public Result<Merchant> profile(@RequestHeader("Authorization") String authorization) {
        Merchant merchant = authService.getMerchantProfile(authService.current(token(authorization)));
        merchant.setPassword(null);
        return Result.success(merchant);
    }

    /** 修改商家密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authorization,
                                       @Valid @RequestBody CustomerPasswordDTO dto) {
        authService.changeMerchantPassword(authService.current(token(authorization)), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /** 获取商家店铺信息 */
    @GetMapping("/shop")
    public Result<Merchant> getShopInfo(@RequestHeader("Authorization") String authorization) {
        Merchant merchant = authService.getMerchantProfile(authService.current(token(authorization)));
        merchant.setPassword(null);
        return Result.success(merchant);
    }

    /** 更新商家店铺信息 */
    @PutMapping("/shop")
    public Result<Merchant> updateShopInfo(@RequestHeader("Authorization") String authorization,
                                           @Valid @RequestBody ShopInfoDTO dto) {
        Long merchantId = authService.current(token(authorization)).id();
        return Result.success(contentService.updateShopInfo(merchantId, dto));
    }

    /** 商家发布闲置物品 */
    @PostMapping("/products")
    public Result<Product> publishProduct(@RequestHeader("Authorization") String authorization,
                                          @Valid @RequestBody ProductDTO dto) {
        return Result.success(contentService.publishProductByMerchant(authService.current(token(authorization)).id(), dto));
    }

    /** 商家查询自己发布的闲置物品列表 */
    @GetMapping("/products")
    public Result<Page<Product>> myProducts(@RequestHeader("Authorization") String authorization,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") @Max(value = 100, message = "每页数量不能超过100") int size) {
        return Result.success(contentService.listMerchantProducts(authService.current(token(authorization)).id(), page, size));
    }

    /** 商家更新自己发布的闲置物品 */
    @PutMapping("/products/{id}")
    public Result<Product> updateMyProduct(@RequestHeader("Authorization") String authorization,
                                           @PathVariable Long id,
                                           @Valid @RequestBody ProductDTO dto) {
        return Result.success(contentService.updateMerchantProduct(authService.current(token(authorization)).id(), id, dto));
    }

    /** 商家删除自己发布的闲置物品 */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteMyProduct(@RequestHeader("Authorization") String authorization,
                                        @PathVariable Long id) {
        contentService.deleteMerchantProduct(authService.current(token(authorization)).id(), id);
        return Result.success();
    }

    /** 商家上下架商品 */
    @PutMapping("/products/{id}/status")
    public Result<Product> updateProductStatus(@RequestHeader("Authorization") String authorization,
                                               @PathVariable Long id,
                                               @RequestParam Integer status) {
        return Result.success(contentService.updateProductStatus(authService.current(token(authorization)).id(), id, status));
    }

    /** 查询商家钱包信息 */
    @GetMapping("/wallet")
    public Result<MerchantWallet> getWallet(@RequestHeader("Authorization") String authorization) {
        Long merchantId = authService.current(token(authorization)).id();
        return Result.success(walletService.getWallet(merchantId));
    }

    /** 分页查询商家交易流水 */
    @GetMapping("/wallet/transactions")
    public Result<Page<WalletTransaction>> getWalletTransactions(@RequestHeader("Authorization") String authorization,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        Long merchantId = authService.current(token(authorization)).id();
        return Result.success(walletService.getTransactions(merchantId, page, size));
    }

    /** 分页查询商家自己的订单 */
    @GetMapping("/orders")
    public Result<Page<com.example.enterprise.entity.ProductOrder>> orders(@RequestHeader("Authorization") String authorization,
                                                                           @RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "10") int size) {
        com.example.enterprise.service.AuthService.TokenUser user = authService.current(token(authorization));
        return Result.success(commerceService.merchantOrders(user, page, size));
    }

    /** 商家发货（填写物流单号） */
    @PutMapping("/orders/{id}/ship")
    public Result<ProductOrder> shipOrder(@RequestHeader("Authorization") String authorization,
                                          @PathVariable Long id,
                                          @RequestParam(required = false) String logisticsNo) {
        com.example.enterprise.service.AuthService.TokenUser user = authService.current(token(authorization));
        return Result.success(commerceService.merchantShipOrder(user, id, logisticsNo));
    }

    /** 商家评价买家（针对已完成或售后完成的订单） */
    @PostMapping("/orders/{id}/review-buyer")
    public Result<com.example.enterprise.entity.BuyerReview> reviewBuyer(@RequestHeader("Authorization") String authorization,
                                                                          @PathVariable Long id,
                                                                          @RequestParam Integer rating,
                                                                          @RequestParam(required = false) String content) {
        com.example.enterprise.service.AuthService.TokenUser user = authService.current(token(authorization));
        return Result.success(commerceService.merchantReviewBuyer(user, id, rating, content));
    }

    /** 商家注销登录 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization == null ? null : authorization.replace("Bearer ", "");
        authService.logout(token);
        return Result.success();
    }

    /** 从Authorization头中提取Token */
    private String token(String authorization) {
        return authorization == null ? null : authorization.replace("Bearer ", "");
    }
}
