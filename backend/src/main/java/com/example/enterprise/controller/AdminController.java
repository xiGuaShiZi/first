package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.dto.*;
import com.example.enterprise.entity.*;
import com.example.enterprise.service.CommerceService;
import com.example.enterprise.service.ContentService;
import com.example.enterprise.service.AuthService;
import com.example.enterprise.service.MerchantWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
/**
 * 管理员控制器
 * <p>提供后台管理功能，包括仪表盘、校园贴士、闲置物品、咨询反馈、平台信息、推荐位、订单、交易协商等管理接口</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    /** 内容管理服务 */
    private final ContentService contentService;
    /** 交易服务 */
    private final CommerceService commerceService;
    /** 认证服务 */
    private final AuthService authService;
    /** 商家钱包管理服务 */
    private final MerchantWalletService walletService;
    /** 商家等级变更审计数据访问 */
    private final com.example.enterprise.repository.MerchantLevelAuditRepository merchantLevelAuditRepository;

    /** 获取管理仪表盘统计数据 */
    @GetMapping("/dashboard")
    public Result<Map<String, Long>> dashboard() {
        return Result.success(contentService.dashboard());
    }

    /** 修改管理员密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authorization,
                                       @Valid @RequestBody CustomerPasswordDTO dto) {
        authService.changeAdminPassword(authService.current(token(authorization)), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /** 分页查询校园贴士列表（管理员，支持状态筛选） */
    @GetMapping("/news")
    public Result<Page<News>> news(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listNews(keyword, status, page, size, false));
    }

    /** 创建校园贴士 */
    @PostMapping("/news")
    public Result<News> createNews(@Valid @RequestBody NewsDTO dto) {
        return Result.success(contentService.saveNews(null, dto));
    }

    /** 更新校园贴士 */
    @PutMapping("/news/{id}")
    public Result<News> updateNews(@PathVariable Long id, @Valid @RequestBody NewsDTO dto) {
        return Result.success(contentService.saveNews(id, dto));
    }

    /** 删除校园贴士 */
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        contentService.deleteNews(id);
        return Result.success();
    }

    /** 分页查询闲置物品列表（管理员） */
    @GetMapping("/products")
    public Result<Page<Product>> products(@RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listProducts(keyword, page, size, false));
    }

    /** 创建闲置物品 */
    @PostMapping("/products")
    public Result<Product> createProduct(@Valid @RequestBody ProductDTO dto) {
        return Result.success(contentService.saveProduct(null, dto));
    }

    /** 更新闲置物品 */
    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return Result.success(contentService.saveProduct(id, dto));
    }

    /** 审核商家发布的商品 */
    @PutMapping("/products/{id}/audit")
    public Result<Product> auditProduct(@PathVariable Long id, @Valid @RequestBody com.example.enterprise.dto.MerchantAuditDTO dto) {
        return Result.success(contentService.auditProduct(id, dto.getAuditStatus(), dto.getAuditRemark()));
    }

    /** 删除闲置物品 */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        contentService.deleteProduct(id);
        return Result.success();
    }

    /** 分页查询留言列表 */
    @GetMapping("/messages")
    public Result<Page<Message>> messages(@RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listMessages(status, page, size));
    }

    /** 更新留言处理状态 */
    @PutMapping("/messages/{id}/status")
    public Result<Message> updateMessageStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(contentService.updateMessageStatus(id, status));
    }

    /** 删除留言 */
    @DeleteMapping("/messages/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        contentService.deleteMessage(id);
        return Result.success();
    }

    /** 获取平台信息 */
    @GetMapping("/company")
    public Result<CompanyInfo> company() {
        return Result.success(contentService.getCompanyInfo());
    }

    /**
     * 管理员为指定客户充值（后台充值）
     * @param authorization 管理员Authorization头
     * @param id 客户ID
     * @param amount 充值金额（元）
     */
    @PostMapping("/customers/{id}/recharge")
    public Result<com.example.enterprise.entity.Customer> rechargeCustomer(@RequestHeader("Authorization") String authorization,
                                                                           @PathVariable Long id,
                                                                           @RequestParam java.math.BigDecimal amount) {
        return Result.success(authService.rechargeCustomer(authService.current(token(authorization)), id, amount));
    }

    /** 更新平台信息 */
    @PutMapping("/company")
    public Result<CompanyInfo> updateCompany(@Valid @RequestBody CompanyInfoDTO dto) {
        return Result.success(contentService.updateCompanyInfo(dto));
    }

    /** 获取所有推荐位列表（管理员） */
    @GetMapping("/banners")
    public Result<List<Banner>> banners() {
        return Result.success(contentService.allBanners());
    }

    /** 创建推荐位 */
    @PostMapping("/banners")
    public Result<Banner> createBanner(@Valid @RequestBody BannerDTO dto) {
        return Result.success(contentService.saveBanner(null, dto));
    }

    /** 更新推荐位 */
    @PutMapping("/banners/{id}")
    public Result<Banner> updateBanner(@PathVariable Long id, @Valid @RequestBody BannerDTO dto) {
        return Result.success(contentService.saveBanner(id, dto));
    }

    /** 删除推荐位 */
    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        contentService.deleteBanner(id);
        return Result.success();
    }

    /** 分页查询所有订单（管理员） */
    @GetMapping("/orders")
    public Result<Page<ProductOrder>> orders(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(commerceService.allOrders(page, size));
    }

    /** 订单发货 */
    @PutMapping("/orders/{id}/ship")
    public Result<ProductOrder> shipOrder(@PathVariable Long id, @RequestParam(required = false) String logisticsNo) {
        return Result.success(commerceService.shipOrder(id, logisticsNo));
    }

    /** 订单统计 */
    @GetMapping("/order-statistics")
    public Result<Map<String, Object>> orderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(commerceService.orderStatistics(startDate, endDate));
    }

    /** 分页查询交易协商申请列表 */
    @GetMapping("/returns")
    public Result<Page<ReturnRequest>> returns(@RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(commerceService.allReturns(status, page, size));
    }

    /** 处理交易协商申请 */
    @PutMapping("/returns/{id}")
    public Result<ReturnRequest> handleReturn(@PathVariable Long id, @Valid @RequestBody ReturnHandleDTO dto) {
        return Result.success(commerceService.handleReturn(id, dto));
    }

    /** 分页查询商家列表（支持按审核状态筛选） */
    @GetMapping("/merchants")
    public Result<Page<Merchant>> merchants(@RequestParam(required = false) Integer auditStatus,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listMerchants(auditStatus, page, size));
    }

    /** 分页查询客户列表（支持按审核状态筛选） */
    @GetMapping("/customers")
    public Result<org.springframework.data.domain.Page<com.example.enterprise.entity.Customer>> customers(@RequestParam(required = false) Integer auditStatus,
                                                                                                             @RequestParam(defaultValue = "1") int page,
                                                                                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listCustomers(auditStatus, page, size));
    }

    /** 审核商家注册申请 */
    @PutMapping("/merchants/{id}/audit")
    public Result<Merchant> auditMerchant(@PathVariable Long id, @Valid @RequestBody MerchantAuditDTO dto) {
        return Result.success(authService.auditMerchant(id, dto.getAuditStatus(), dto.getAuditRemark()));
    }

    /** 审核客户注册申请 */
    @PutMapping("/customers/{id}/audit")
    public Result<com.example.enterprise.entity.Customer> auditCustomer(@PathVariable Long id, @Valid @RequestBody com.example.enterprise.dto.MerchantAuditDTO dto) {
        return Result.success(authService.auditCustomer(id, dto.getAuditStatus(), dto.getAuditRemark()));
    }

    /** 设置商家等级（1-5级，等级越高平台交易费率越高），记录操作人员审计 */
    @PutMapping("/merchants/{id}/level")
    public Result<Merchant> setMerchantLevel(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @PathVariable Long id,
                                             @Valid @RequestBody MerchantLevelDTO dto) {
        com.example.enterprise.service.AuthService.TokenUser operator = authService.current(token(authorization));
        return Result.success(contentService.setMerchantLevel(id, dto.getMerchantLevel(), operator));
    }

    /** 分页查询商家等级变更审计 */
    @GetMapping("/merchant-level-audits")
    public Result<org.springframework.data.domain.Page<com.example.enterprise.entity.MerchantLevelAudit>> merchantLevelAudits(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(page - 1, 0), size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
        if (merchantId == null) {
            return Result.success(merchantLevelAuditRepository.findAll(pageable));
        }
        return Result.success(merchantLevelAuditRepository.findByMerchantId(merchantId, pageable));
    }

    /** 分页查询平台服务费记录 */
    @GetMapping("/service-fees")
    public Result<Page<ServiceFee>> serviceFees(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(walletService.listServiceFees(page, size));
    }

    /** 获取商家等级费率配置表 */
    @GetMapping("/fee-rates")
    public Result<Map<Integer, BigDecimal>> feeRates() {
        return Result.success(walletService.getAllFeeRates());
    }

    /** 手动触发：根据成交额和好评率调整商家等级 */
    @PostMapping("/merchants/adjust-levels")
    public Result<Map<Long, Integer>> adjustMerchantLevels() {
        return Result.success(walletService.evaluateAndAdjustMerchantLevels());
    }

    /** 从Authorization头中提取Token */
    private String token(String authorization) {
        return authorization == null ? null : authorization.replace("Bearer ", "");
    }
}
