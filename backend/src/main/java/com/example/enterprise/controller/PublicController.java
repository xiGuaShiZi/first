package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.dto.MessageSubmitDTO;
import com.example.enterprise.dto.ProductReviewVO;
import com.example.enterprise.dto.ShopVO;
import com.example.enterprise.entity.*;
import com.example.enterprise.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公开接口控制器
 * <p>提供无需登录即可访问的公开接口，包括平台信息、首页、推荐位、校园贴士、闲置物品、评价和咨询反馈</p>
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    /** 内容管理服务 */
    private final ContentService contentService;

    /** 获取平台信息 */
    @GetMapping("/company")
    public Result<CompanyInfo> company() {
        return Result.success(contentService.getCompanyInfo());
    }

    /** 获取首页聚合数据 */
    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        return Result.success(contentService.home());
    }

    /** 获取已启用的推荐位列表 */
    @GetMapping("/banners")
    public Result<List<Banner>> banners() {
        return Result.success(contentService.publicBanners());
    }

    /** 分页查询已发布校园贴士列表 */
    @GetMapping("/news")
    public Result<Page<News>> news(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listNews(keyword, page, size, true));
    }

    /** 获取已发布校园贴士详情 */
    @GetMapping("/news/{id}")
    public Result<News> newsDetail(@PathVariable Long id) {
        return Result.success(contentService.getPublicNews(id));
    }

    /** 分页查询在售闲置物品列表，支持分类和排序 */
    @GetMapping("/products")
    public Result<Page<Product>> products(@RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(defaultValue = "") String category,
                                          @RequestParam(defaultValue = "newest") String sort,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.listProducts(keyword, category, sort, page, size, true));
    }

    /** 获取所有在售物品的去重分类列表 */
    @GetMapping("/product-categories")
    public Result<List<String>> productCategories() {
        return Result.success(contentService.publicProductCategories());
    }

    /** 获取在售闲置物品详情 */
    @GetMapping("/products/{id}")
    public Result<Product> productDetail(@PathVariable Long id) {
        return Result.success(contentService.getPublicProduct(id));
    }

    /** 获取物品评价列表 */
    @GetMapping("/products/{id}/reviews")
    public Result<List<ProductReviewVO>> productReviews(@PathVariable Long id) {
        return Result.success(contentService.publicProductReviews(id));
    }

    /** 提交咨询留言 */
    @PostMapping("/messages")
    public Result<Message> submitMessage(@Valid @RequestBody MessageSubmitDTO dto) {
        return Result.success(contentService.submitMessage(dto));
    }

    /** 获取商家店铺展示信息（公开接口） */
    @GetMapping("/shops/{merchantId}")
    public Result<ShopVO> shopDetail(@PathVariable Long merchantId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.getPublicShop(merchantId, page, size));
    }

    /** 分页查询店铺的公开评价列表 */
    @GetMapping("/shops/{merchantId}/reviews")
    public Result<Page<ProductReviewVO>> shopReviews(@PathVariable Long merchantId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(contentService.publicShopReviews(merchantId, page, size));
    }
}
