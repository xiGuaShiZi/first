package com.example.enterprise.service;

import com.example.enterprise.common.PageUtil;
import com.example.enterprise.dto.*;
import com.example.enterprise.entity.*;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 内容管理服务
 * <p>提供校园贴士、闲置物品、推荐位、平台信息、咨询反馈、首页等CMS内容管理功能，支持Redis缓存</p>
 */
@Service
@RequiredArgsConstructor
public class ContentService {
    /** 平台信息缓存键 */
    private static final String COMPANY_CACHE = "website:company";
    /** 推荐位缓存键 */
    private static final String BANNER_CACHE = "website:banners";
    /** 首页数据缓存键 */
    private static final String HOME_CACHE = "website:home";
    /** 校园贴士HTML安全白名单，允许的富文本标签和属性 */
    private static final Safelist NEWS_HTML_SAFELIST = Safelist.relaxed()
            .addTags("figure", "figcaption")
            .addAttributes("img", "alt", "title")
            .addAttributes("a", "target", "rel")
            .addProtocols("img", "src", "http", "https", "data", "/")
            .addProtocols("a", "href", "http", "https", "mailto");

    /** 校园贴士数据访问 */
    private final NewsRepository newsRepository;
    /** 闲置物品数据访问 */
    private final ProductRepository productRepository;
    /** 闲置物品图片数据访问 */
    private final ProductImageRepository productImageRepository;
    /** 留言数据访问 */
    private final MessageRepository messageRepository;
    /** 平台信息数据访问 */
    private final CompanyInfoRepository companyInfoRepository;
    /** 推荐位数据访问 */
    private final BannerRepository bannerRepository;
    /** 订单数据访问 */
    private final ProductOrderRepository productOrderRepository;
    /** 评价数据访问 */
    private final ProductReviewRepository productReviewRepository;
    /** 交易协商申请数据访问 */
    private final ReturnRequestRepository returnRequestRepository;
    /** 商家数据访问 */
    private final MerchantRepository merchantRepository;
    /** 商家等级变更审计数据访问 */
    private final com.example.enterprise.repository.MerchantLevelAuditRepository merchantLevelAuditRepository;
    /** 客户数据访问 */
    private final CustomerRepository customerRepository;
    /** Redis操作模板 */
    private final StringRedisTemplate redisTemplate;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;
    /** Redis缓存服务 */
    private final CacheService cacheService;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    /** 图片地址前缀，用于校验上传图片路径 */
    @Value("${app.public-url-prefix:/uploads/}")
    private String publicUploadPrefix;

    /**
     * 分页查询校园贴士列表（不带状态筛选）
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @param publicOnly 是否只查询已发布贴士
     * @return 分页校园贴士结果
     */
    public Page<News> listNews(String keyword, int page, int size, boolean publicOnly) {
        return listNews(keyword, null, page, size, publicOnly);
    }

    /**
     * 分页查询校园贴士列表（带状态筛选），支持关键词多字段模糊搜索
     * @param keyword 关键词
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     * @param publicOnly 是否只查询已发布贴士
     * @return 分页校园贴士结果
     */
    public Page<News> listNews(String keyword, Integer status, int page, int size, boolean publicOnly) {
        Pageable pageable = PageUtil.of(page, size,
                Sort.by(Sort.Direction.ASC, "sort").and(Sort.by(Sort.Direction.DESC, "createTime")));
        Specification<News> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (publicOnly) {
                predicates.add(builder.equal(root.get("status"), 1));
            } else if (status != null) {
                predicates.add(builder.equal(root.get("status"), normalizeStatus(status, 1)));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), like),
                        builder.like(builder.lower(root.get("summary")), like),
                        builder.like(builder.lower(root.get("content")), like),
                        builder.like(builder.lower(root.get("author")), like),
                        builder.like(builder.lower(root.get("source")), like),
                        builder.like(builder.lower(root.get("tags")), like)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return newsRepository.findAll(specification, pageable);
    }

    /**
     * 保存校园贴士（创建或更新），内容经HTML安全过滤后存储
     * @param id 贴士ID，为null时新建
     * @param dto 贴士请求参数
     * @return 保存后的贴士实体
     */
    public News saveNews(Long id, NewsDTO dto) {
        News news = id == null ? new News() : newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("贴士不存在"));
        news.setTitle(dto.getTitle());
        news.setSummary(dto.getSummary());
        news.setContent(sanitizeNewsHtml(dto.getContent()));
        news.setAuthor(dto.getAuthor());
        news.setCoverImage(dto.getCoverImage());
        news.setTags(dto.getTags());
        news.setSource(dto.getSource());
        news.setViewCount(dto.getViewCount() == null ? 0 : dto.getViewCount());
        news.setSort(dto.getSort() == null ? 0 : dto.getSort());
        news.setStatus(normalizeStatus(dto.getStatus(), 1));
        if (news.getCreateTime() == null) {
            news.setCreateTime(LocalDateTime.now());
        }
        news.setUpdateTime(LocalDateTime.now());
        News saved = newsRepository.save(news);
        deleteCache(HOME_CACHE);
        return saved;
    }

    /**
     * 删除校园贴士
     * @param id 贴士ID
     */
    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new BusinessException("贴士不存在");
        }
        newsRepository.deleteById(id);
        deleteCache(HOME_CACHE);
    }

    /**
     * 获取已发布校园贴士详情
     * @param id 贴士ID
     * @return 贴士实体
     */
    public News getPublicNews(Long id) {
        News news = newsRepository.findById(id).orElseThrow(() -> new BusinessException("贴士不存在"));
        if (!Integer.valueOf(1).equals(news.getStatus())) {
            throw new BusinessException("贴士已下线");
        }
        return news;
    }

    /**
     * 分页查询闲置物品列表（不带分类和排序）
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @param publicOnly 是否只查询在售物品
     * @return 分页闲置物品结果
     */
    public Page<Product> listProducts(String keyword, int page, int size, boolean publicOnly) {
        return listProducts(keyword, "", "newest", page, size, publicOnly);
    }

    /**
     * 分页查询闲置物品列表（带分类和排序），支持关键词模糊搜索，结果附带图片列表
     * @param keyword 关键词
     * @param category 分类筛选
     * @param sort 排序方式：newest/priceAsc/priceDesc/salesDesc
     * @param page 页码
     * @param size 每页数量
     * @param publicOnly 是否只查询在售物品
     * @return 分页闲置物品结果
     */
    public Page<Product> listProducts(String keyword, String category, String sort, int page, int size, boolean publicOnly) {
        // Special handling for sorting by positive rate (好评率) because it requires aggregation on reviews.
        if ("positiveRateDesc".equals(sort)) {
            int pageIndex = Math.max(page - 1, 0);
            int pageSize = normalizeSize(size);
            int offset = pageIndex * pageSize;

            // Total count of matching products
            String countSql = "SELECT COUNT(DISTINCT p.id) FROM product p WHERE p.status = 1";
            List<Object> params = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                countSql += " AND LOWER(p.name) LIKE ?";
                params.add("%" + keyword.trim().toLowerCase() + "%");
            }
            if (StringUtils.hasText(category)) {
                countSql += " AND p.category = ?";
                params.add(category.trim());
            }
            jakarta.persistence.Query countQuery = entityManager.createNativeQuery(countSql);
            for (int i = 0; i < params.size(); i++) {
                countQuery.setParameter(i + 1, params.get(i));
            }
            Number totalCountNum = ((Number) countQuery.getSingleResult());
            long total = totalCountNum == null ? 0L : totalCountNum.longValue();

            if (total == 0) {
                return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(), PageRequest.of(pageIndex, pageSize), 0);
            }

            // Query product ids ordered by positive rate desc (positive_count/total_count). Products without reviews get 0.
            StringBuilder idsSql = new StringBuilder();
            idsSql.append("SELECT p.id FROM product p LEFT JOIN product_review r ON p.id = r.product_id WHERE p.status = 1");
            List<Object> qparams = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                idsSql.append(" AND LOWER(p.name) LIKE ?");
                qparams.add("%" + keyword.trim().toLowerCase() + "%");
            }
            if (StringUtils.hasText(category)) {
                idsSql.append(" AND p.category = ?");
                qparams.add(category.trim());
            }
            idsSql.append(" GROUP BY p.id ORDER BY (SUM(CASE WHEN r.quality_rating >= 4 THEN 1 ELSE 0 END) / NULLIF(COUNT(r.id),0)) DESC, p.create_time DESC");

            jakarta.persistence.Query idsQuery = entityManager.createNativeQuery(idsSql.toString());
            for (int i = 0; i < qparams.size(); i++) idsQuery.setParameter(i + 1, qparams.get(i));
            idsQuery.setFirstResult(offset);
            idsQuery.setMaxResults(pageSize);
            @SuppressWarnings("unchecked")
            List<Number> idResults = idsQuery.getResultList();
            List<Long> ids = idResults.stream().map(Number::longValue).toList();

            // Load full product entities and preserve order
            List<Product> products = new ArrayList<>();
            if (!ids.isEmpty()) {
                List<Product> fetched = productRepository.findAllById(ids);
                // map by id
                java.util.Map<Long, Product> map = new java.util.HashMap<>();
                for (Product p : fetched) map.put(p.getId(), p);
                for (Long id : ids) {
                    Product p = map.get(id);
                    if (p != null) products.add(p);
                }

                // compute positiveRate for the batch via one aggregated query
                String aggSql = "SELECT r.product_id, SUM(CASE WHEN r.quality_rating >= 4 THEN 1 ELSE 0 END) AS positive_count, COUNT(r.id) AS total_count FROM product_review r WHERE r.product_id IN (:productIds) GROUP BY r.product_id";
                jakarta.persistence.Query aggQuery = entityManager.createNativeQuery(aggSql);
                aggQuery.setParameter("productIds", ids);
                @SuppressWarnings("unchecked")
                List<Object[]> aggRows = aggQuery.getResultList();
                java.util.Map<Long, double[]> aggMap = new java.util.HashMap<>();
                for (Object[] row : aggRows) {
                    Long pid = ((Number) row[0]).longValue();
                    double positive = ((Number) row[1]).doubleValue();
                    double tot = ((Number) row[2]).doubleValue();
                    aggMap.put(pid, new double[]{positive, tot});
                }
                // attach images, publisherInfo and set positiveRate
                for (Product p : products) {
                    attachProductImages(p);
                    attachPublisherInfo(p);
                    double pr = 0.0;
                    double[] a = aggMap.get(p.getId());
                    if (a != null && a[1] > 0) pr = a[0] / a[1] * 100.0;
                    p.setPositiveRate(Math.round(pr * 10.0) / 10.0); // 保留1位小数（百分比）
                }
            }

            return new org.springframework.data.domain.PageImpl<>(products, PageRequest.of(pageIndex, pageSize), total);
        }

        // default handling for other sorts
        Pageable pageable = PageUtil.of(page, size, productSort(sort));
        Specification<Product> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (publicOnly) {
                predicates.add(builder.equal(root.get("status"), 1));
            }
            if (StringUtils.hasText(keyword)) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(builder.equal(root.get("category"), category.trim()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        List<Product> content = products.getContent();
        content.forEach(this::attachProductImages);
        content.forEach(this::attachPublisherInfo);
        // compute positiveRate for the visible page to support UI display
        if (!content.isEmpty()) {
            List<Long> ids = content.stream().map(Product::getId).toList();
            String aggSql = "SELECT r.product_id, SUM(CASE WHEN r.quality_rating >= 4 THEN 1 ELSE 0 END) AS positive_count, COUNT(r.id) AS total_count FROM product_review r WHERE r.product_id IN (:productIds) GROUP BY r.product_id";
            jakarta.persistence.Query aggQuery = entityManager.createNativeQuery(aggSql);
            aggQuery.setParameter("productIds", ids);
            @SuppressWarnings("unchecked")
            List<Object[]> aggRows = aggQuery.getResultList();
            java.util.Map<Long, double[]> aggMap = new java.util.HashMap<>();
            for (Object[] row : aggRows) {
                Long pid = ((Number) row[0]).longValue();
                double positive = ((Number) row[1]).doubleValue();
                double tot = ((Number) row[2]).doubleValue();
                aggMap.put(pid, new double[]{positive, tot});
            }
            for (Product p : content) {
                double pr = 0.0;
                double[] a = aggMap.get(p.getId());
                if (a != null && a[1] > 0) pr = a[0] / a[1] * 100.0;
                p.setPositiveRate(Math.round(pr * 10.0) / 10.0);
            }
        }
        return products;
    }

    /**
     * 获取所有在售物品的去重分类列表
     * @return 分类名称列表
     */
    public List<String> publicProductCategories() {
        // 优先从缓存获取
        List<String> cached = cacheService.getProductCategories();
        if (cached != null) {
            return cached;
        }
        List<String> categories = productRepository.findPublicCategories();
        // 写入缓存
        cacheService.putProductCategories(categories);
        return categories;
    }

    /**
     * 学生发布闲置物品，自动关联发布者ID，状态默认为在售
     * @param customerId 发布者（客户）ID
     * @param dto 物品请求参数
     * @return 保存后的物品实体
     */
    @Transactional
    public Product publishProduct(Long customerId, ProductDTO dto) {
        Product product = new Product();
        product.setPublisherId(customerId);
        // 如果SKU为空，自动生成唯一SKU
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis() + "-" + customerId + "-" + System.nanoTime();
        }
        product.setSku(sku.trim());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(validateServerImagePath(dto.getImage(), "物品封面图片"));
        product.setCategory(dto.getCategory());
        product.setTags(dto.getTags());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setUnit(dto.getUnit());
        product.setSalesCount(dto.getSalesCount() == null ? 0 : dto.getSalesCount());
        product.setWeightGrams(dto.getWeightGrams());
        product.setDetail(sanitizeContentHtml(dto.getDetail()));
        product.setStatus(1); // 学生发布的物品默认在售
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        saveProductImages(saved.getId(), dto.getImages());
        attachProductImages(saved);
        deleteCache(HOME_CACHE);
        // 新商品可能新增分类，清除分类缓存
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 学生查询自己发布的闲置物品列表
     * @param customerId 客户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页闲置物品结果
     */
    public Page<Product> listMyProducts(Long customerId, int page, int size) {
        Pageable pageable = PageUtil.of(page, size);
        Specification<Product> specification = (root, query, builder) ->
                builder.equal(root.get("publisherId"), customerId);
        Page<Product> products = productRepository.findAll(specification, pageable);
        products.getContent().forEach(this::attachProductImages);
        return products;
    }

    /**
     * 学生删除自己发布的闲置物品，仅允许删除自己发布的物品
     * @param customerId 客户ID
     * @param productId 物品ID
     */
    @Transactional
    public void deleteMyProduct(Long customerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!customerId.equals(product.getPublisherId())) {
            throw new BusinessException("无权删除该物品");
        }
        productImageRepository.deleteByProductId(productId);
        productRepository.deleteById(productId);
        cacheService.evictProductDetail(productId);
        deleteCache(HOME_CACHE);
        // 商品删除可能影响分类列表，清除分类缓存
        evictProductCategoriesCache();
    }

    /**
     * 学生更新自己发布的闲置物品，仅允许更新自己发布的物品
     * @param customerId 客户ID
     * @param productId 物品ID
     * @param dto 物品请求参数
     * @return 更新后的物品实体
     */
    @Transactional
    public Product updateMyProduct(Long customerId, Long productId, ProductDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!customerId.equals(product.getPublisherId())) {
            throw new BusinessException("无权修改该物品");
        }
        // 如果SKU为空，自动生成唯一SKU
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis() + "-" + productId + "-" + System.nanoTime();
        }
        product.setSku(sku.trim());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(validateServerImagePath(dto.getImage(), "物品封面图片"));
        product.setCategory(dto.getCategory());
        product.setTags(dto.getTags());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setUnit(dto.getUnit());
        product.setSalesCount(dto.getSalesCount() == null ? 0 : dto.getSalesCount());
        product.setWeightGrams(dto.getWeightGrams());
        product.setSize(dto.getSize());
        product.setAllowBargain(dto.getAllowBargain() == null ? 0 : dto.getAllowBargain());
        product.setConditionLevel(dto.getConditionLevel());
        product.setUsageInstructions(sanitizeContentHtml(dto.getUsageInstructions()));
        product.setDetail(sanitizeContentHtml(dto.getDetail()));
        product.setStatus(normalizeStatus(dto.getStatus(), 1));
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        saveProductImages(saved.getId(), dto.getImages());
        attachProductImages(saved);
        cacheService.evictProductDetail(productId);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 商家发布闲置物品，自动关联发布者ID（商家ID），状态默认为在售
     * @param merchantId 商家ID
     * @param dto 物品请求参数
     * @return 保存后的物品实体
     */
    @Transactional
    public Product publishProductByMerchant(Long merchantId, ProductDTO dto) {
        Product product = new Product();
        product.setPublisherId(merchantId);
        // 如果SKU为空，自动生成唯一SKU
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis() + "-" + merchantId + "-" + System.nanoTime();
        }
        product.setSku(sku.trim());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(validateServerImagePath(dto.getImage(), "物品封面图片"));
        product.setCategory(dto.getCategory());
        product.setTags(dto.getTags());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setUnit(dto.getUnit());
        product.setSalesCount(dto.getSalesCount() == null ? 0 : dto.getSalesCount());
        product.setWeightGrams(dto.getWeightGrams());
        product.setSize(dto.getSize());
        product.setAllowBargain(dto.getAllowBargain() == null ? 0 : dto.getAllowBargain());
        product.setConditionLevel(dto.getConditionLevel());
        product.setUsageInstructions(sanitizeContentHtml(dto.getUsageInstructions()));
        product.setDetail(sanitizeContentHtml(dto.getDetail()));
        // 商家发布的物品需要管理员审核，初始置为待审核且不可见
        product.setAuditStatus(0);
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        saveProductImages(saved.getId(), dto.getImages());
        attachProductImages(saved);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 商家查询自己发布的闲置物品列表
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页闲置物品结果
     */
    public Page<Product> listMerchantProducts(Long merchantId, int page, int size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Product> products = productRepository.findByPublisherId(merchantId, pageable);
        products.getContent().forEach(this::attachProductImages);
        return products;
    }

    /**
     * 商家更新自己发布的闲置物品，仅允许更新自己发布的物品
     * @param merchantId 商家ID
     * @param productId 物品ID
     * @param dto 物品请求参数
     * @return 更新后的物品实体
     */
    @Transactional
    public Product updateMerchantProduct(Long merchantId, Long productId, ProductDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!merchantId.equals(product.getPublisherId())) {
            throw new BusinessException("无权修改该物品");
        }
        // 如果SKU为空，自动生成唯一SKU
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis() + "-" + productId + "-" + System.nanoTime();
        }
        product.setSku(sku.trim());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(validateServerImagePath(dto.getImage(), "物品封面图片"));
        product.setCategory(dto.getCategory());
        product.setTags(dto.getTags());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setUnit(dto.getUnit());
        product.setSalesCount(dto.getSalesCount() == null ? 0 : dto.getSalesCount());
        product.setWeightGrams(dto.getWeightGrams());
        product.setSize(dto.getSize());
        product.setAllowBargain(dto.getAllowBargain() == null ? 0 : dto.getAllowBargain());
        product.setConditionLevel(dto.getConditionLevel());
        product.setUsageInstructions(sanitizeContentHtml(dto.getUsageInstructions()));
        product.setDetail(sanitizeContentHtml(dto.getDetail()));
        // 商家更新商品后需要重新走审核流程
        product.setAuditStatus(0);
        product.setStatus(normalizeStatus(dto.getStatus(), 0));
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        saveProductImages(saved.getId(), dto.getImages());
        attachProductImages(saved);
        cacheService.evictProductDetail(productId);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 商家删除自己发布的闲置物品，仅允许删除自己发布的物品
     * @param merchantId 商家ID
     * @param productId 物品ID
     */
    @Transactional
    public void deleteMerchantProduct(Long merchantId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!merchantId.equals(product.getPublisherId())) {
            throw new BusinessException("无权删除该物品");
        }
        productImageRepository.deleteByProductId(productId);
        productRepository.deleteById(productId);
        cacheService.evictProductDetail(productId);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
    }

    /**
     * 商家上下架商品
     * @param merchantId 商家ID
     * @param productId 物品ID
     * @param status 状态：1-上架，0-下架
     * @return 更新后的物品实体
     */
    @Transactional
    public Product updateProductStatus(Long merchantId, Long productId, Integer status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        if (!merchantId.equals(product.getPublisherId())) {
            throw new BusinessException("无权操作该物品");
        }
        // 商家上架商品必须已通过管理员审核
        if (Integer.valueOf(1).equals(status) && !Integer.valueOf(1).equals(product.getAuditStatus())) {
            throw new BusinessException("该商品尚未通过管理员审核，无法上架");
        }
        product.setStatus(normalizeStatus(status, 1));
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        cacheService.evictProductDetail(productId);
        return saved;
    }

    /**
     * 获取商家店铺信息
     * @param merchantId 商家ID
     * @return 商家实体
     */
    public Merchant getMerchantShopInfo(Long merchantId) {
        // 优先从缓存获取
        Merchant cached = cacheService.getMerchant(merchantId, Merchant.class);
        if (cached != null) {
            return cached;
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));
        // 写入缓存
        cacheService.putMerchant(merchantId, merchant);
        return merchant;
    }

    /**
     * 更新商家店铺信息
     * @param merchantId 商家ID
     * @param dto 店铺信息请求参数
     * @return 更新后的商家实体
     */
    @Transactional
    public Merchant updateShopInfo(Long merchantId, ShopInfoDTO dto) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));
        if (dto.getShopName() != null) {
            merchant.setShopName(dto.getShopName());
        }
        if (dto.getShopDescription() != null) {
            merchant.setShopDescription(dto.getShopDescription());
        }
        if (dto.getShopLogo() != null) {
            merchant.setShopLogo(validateServerImagePath(dto.getShopLogo(), "店铺Logo"));
        }
        merchant.setUpdateTime(LocalDateTime.now());
        Merchant saved = merchantRepository.save(merchant);
        // 店铺信息变更，清除商家实体缓存
        cacheService.evictMerchant(merchantId);
        return saved;
    }

    /**
     * 管理员设置商家等级（1-5级）
     * <p>等级决定平台收取的交易费率：1级0.1%、2级0.2%、3级0.5%、4级0.75%、5级1%</p>
     * @param merchantId 商家ID
     * @param level 等级（1-5）
     * @return 更新后的商家实体
     */
    @Transactional
    public Merchant setMerchantLevel(Long merchantId, Integer level) {
        // 保持向后兼容：无操作员信息时视为系统或未知来源操作
        return setMerchantLevel(merchantId, level, null);
    }

    @Transactional
    public Merchant setMerchantLevel(Long merchantId, Integer level, com.example.enterprise.service.AuthService.TokenUser operator) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));
        if (level == null || level < 1 || level > 5) {
            throw new BusinessException("商家等级必须在1-5之间");
        }
        Integer oldLevel = merchant.getMerchantLevel() == null ? 1 : merchant.getMerchantLevel();
        merchant.setMerchantLevel(level);
        merchant.setUpdateTime(LocalDateTime.now());
        Merchant saved = merchantRepository.save(merchant);

        // 记录等级变更审计（管理员手动设置）
        try {
            com.example.enterprise.entity.MerchantLevelAudit audit = new com.example.enterprise.entity.MerchantLevelAudit();
            audit.setMerchantId(merchantId);
            if (operator != null) {
                audit.setOperatorId(operator.id());
                audit.setOperatorName(operator.username());
            } else {
                audit.setOperatorName("system");
            }
            audit.setOldLevel(oldLevel);
            audit.setNewLevel(level);
            audit.setReason("manual_admin");
            audit.setCreateTime(LocalDateTime.now());
            merchantLevelAuditRepository.save(audit);
        } catch (Exception ignored) {
            // 审计记录不应阻塞主流程，失败则忽略
        }

        // 等级变更，清除商家实体缓存
        cacheService.evictMerchant(merchantId);
        return saved;
    }

    /**
     * 获取公开店铺展示信息（含商品列表）
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 店铺展示VO
     */
    public ShopVO getPublicShop(Long merchantId, int page, int size) {
        // 优先从缓存获取商家实体
        Merchant merchant = cacheService.getMerchant(merchantId, Merchant.class);
        if (merchant == null) {
            merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));
        }
        if (!Integer.valueOf(1).equals(merchant.getAuditStatus())) {
            throw new BusinessException("店铺未通过审核");
        }

        ShopVO shopVO = new ShopVO();
        shopVO.setMerchantId(merchant.getId());
        shopVO.setShopName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getRealName() + "的店");
        shopVO.setShopDescription(merchant.getShopDescription());
        shopVO.setShopLogo(merchant.getShopLogo());

        // 查询商品统计
        long productTotal = productRepository.countByPublisherId(merchantId);
        long onSaleProductCount = productRepository.countByPublisherIdAndStatus(merchantId, 1);
        shopVO.setProductTotal(productTotal);
        shopVO.setOnSaleProductCount(onSaleProductCount);

        // 计算总销量（使用聚合查询，避免全表加载）
        long totalSales = productRepository.sumSalesCountByPublisherId(merchantId);
        shopVO.setTotalSales((int) totalSales);

        // 查询在售商品列表（只查询该商家的商品）
        Pageable pageable = PageUtil.of(page, size);
        Page<Product> merchantProducts = productRepository.findByPublisherIdAndStatus(merchantId, 1, pageable);
        merchantProducts.getContent().forEach(this::attachProductImages);
        shopVO.setProducts(merchantProducts);

        // 计算店铺评分和评价总数（使用聚合查询，避免逐商品加载）
        long reviewCount = productReviewRepository.countByMerchantId(merchantId);
        shopVO.setReviewCount(reviewCount);
        if (reviewCount > 0) {
            long positiveCount = productReviewRepository.countPositiveByMerchantId(merchantId);
            double avgRating = productReviewRepository.avgQualityRatingByMerchantId(merchantId);
            double avgServiceAttitude = productReviewRepository.avgServiceAttitudeByMerchantId(merchantId);
            shopVO.setShopRating(Math.round(avgRating * 10.0) / 10.0);
            shopVO.setServiceAttitudeRating(Math.round(avgServiceAttitude * 10.0) / 10.0);
            double positiveRate = (double) positiveCount / reviewCount * 100;
            shopVO.setPositiveRate(Math.round(positiveRate * 10.0) / 10.0);
        }

        // 设置商家等级
        shopVO.setMerchantLevel(merchant.getMerchantLevel());

        return shopVO;
    }

    /** 在商品发布/更新/删除时清除分类缓存 */
    private void evictProductCategoriesCache() {
        cacheService.evictProductCategories();
    }

    /**
     * 保存闲置物品（创建或更新），含图片列表全量替换、HTML内容过滤和路径校验
     * @param id 物品ID，为null时新建
     * @param dto 物品请求参数
     * @return 保存后的物品实体
     */
    @Transactional
    public Product saveProduct(Long id, ProductDTO dto) {
        Product product = id == null ? new Product() : productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物品不存在"));
        // 如果SKU为空，自动生成唯一SKU
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis() + "-" + (id == null ? System.nanoTime() : id) + "-" + System.nanoTime();
        }
        product.setSku(sku.trim());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(validateServerImagePath(dto.getImage(), "物品封面图片"));
        product.setCategory(dto.getCategory());
        product.setTags(dto.getTags());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setUnit(dto.getUnit());
        product.setSalesCount(dto.getSalesCount() == null ? 0 : dto.getSalesCount());
        product.setWeightGrams(dto.getWeightGrams());
        product.setSize(dto.getSize());
        product.setAllowBargain(dto.getAllowBargain() == null ? 0 : dto.getAllowBargain());
        product.setConditionLevel(dto.getConditionLevel());
        product.setUsageInstructions(sanitizeContentHtml(dto.getUsageInstructions()));
        product.setDetail(sanitizeContentHtml(dto.getDetail()));
        // 管理员更新已有商品时：未通过审核的商品不允许直接上架，必须走审核流程
        if (id != null) {
            Integer currentAuditStatus = product.getAuditStatus() == null ? 0 : product.getAuditStatus();
            int newStatus = normalizeStatus(dto.getStatus(), 1);
            if (!Integer.valueOf(1).equals(currentAuditStatus) && newStatus == 1) {
                throw new BusinessException("该商品尚未通过审核，无法直接上架，请先在商品列表中完成审核");
            }
            product.setStatus(newStatus);
        } else {
            // 管理员直接创建的商品视为已通过审核，无需走审核流程
            product.setAuditStatus(1);
            product.setStatus(normalizeStatus(dto.getStatus(), 1));
        }
        if (product.getCreateTime() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        saveProductImages(saved.getId(), dto.getImages());
        attachProductImages(saved);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 删除闲置物品及其关联图片
     * @param id 物品ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("物品不存在");
        }
        productImageRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
    }

    /**
     * 获取在售物品详情，附带图片列表
     * @param id 物品ID
     * @return 物品实体（含图片）
     */
    public Product getPublicProduct(Long id) {
        // 优先从缓存获取
        Product cached = cacheService.getProductDetail(id, Product.class);
        if (cached != null) {
            attachProductImages(cached);
            attachPublisherInfo(cached);
            return cached;
        }
        Product product = productRepository.findById(id).orElseThrow(() -> new BusinessException("物品不存在"));
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("物品已下架");
        }
        attachProductImages(product);
        attachPublisherInfo(product);
        // 写入缓存
        cacheService.putProductDetail(id, product);
        return product;
    }

    /**
     * 管理员审核商家发布的商品
     * @param productId 商品ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditRemark 审核备注
     * @return 更新后的商品实体
     */
    @Transactional
    public Product auditProduct(Long productId, Integer auditStatus, String auditRemark) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException("物品不存在"));
        if (product.getAuditStatus() == null) product.setAuditStatus(0);
        if (product.getAuditStatus() != 0) {
            throw new BusinessException("该商品已审核，无法重复审核");
        }
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            throw new BusinessException("审核状态无效");
        }
        product.setAuditStatus(auditStatus);
        product.setAuditRemark(auditRemark);
        product.setAuditTime(LocalDateTime.now());
        // 审核通过则上架（公开），否则保持下架
        product.setStatus(auditStatus == 1 ? 1 : 0);
        product.setUpdateTime(LocalDateTime.now());
        Product saved = productRepository.save(product);
        deleteCache(HOME_CACHE);
        evictProductCategoriesCache();
        return saved;
    }

    /**
     * 获取物品的公开评价列表，匿名评价隐藏用户名
     * @param productId 物品ID
     * @return 评价VO列表
     */
    public List<ProductReviewVO> publicProductReviews(Long productId) {
        getPublicProduct(productId);
        return productReviewRepository.findByProductIdOrderByCreateTimeDesc(productId).stream()
                .map(this::toProductReviewVO)
                .toList();
    }

    /**
     * 分页查询店铺的公开评价列表
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页评价VO结果
     */
    public Page<ProductReviewVO> publicShopReviews(Long merchantId, int page, int size) {
        // 使用无排序的分页，因为 native query 已包含 ORDER BY
        Pageable pageable = PageRequest.of(PageUtil.normalizePage(page), PageUtil.normalizeSize(size));
        return productReviewRepository.findByMerchantId(merchantId, pageable)
                .map(this::toProductReviewVO);
    }

    /**
     * 提交咨询留言
     * @param dto 留言请求参数
     * @return 保存后的留言实体
     */
    public Message submitMessage(MessageSubmitDTO dto) {
        Message message = new Message();
        message.setUsername(dto.getUsername());
        message.setPhone(dto.getPhone());
        message.setEmail(dto.getEmail());
        message.setContent(dto.getContent());
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * 分页查询留言列表，可按状态筛选
     * @param status 状态筛选，为null时查询全部
     * @param page 页码
     * @param size 每页数量
     * @return 分页留言结果
     */
    public Page<Message> listMessages(Integer status, int page, int size) {
        Pageable pageable = PageUtil.of(page, size);
        return status == null ? messageRepository.findAll(pageable) : messageRepository.findByStatus(status, pageable);
    }

    /**
     * 更新留言处理状态
     * @param id 留言ID
     * @param status 新状态
     * @return 更新后的留言实体
     */
    public Message updateMessageStatus(Long id, Integer status) {
        Message message = messageRepository.findById(id).orElseThrow(() -> new BusinessException("留言不存在"));
        message.setStatus(normalizeStatus(status, 0));
        return messageRepository.save(message);
    }

    /**
     * 删除留言
     * @param id 留言ID
     */
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new BusinessException("留言不存在");
        }
        messageRepository.deleteById(id);
    }

    /**
     * 获取平台信息，优先从缓存读取
     * @return 平台信息实体
     */
    public CompanyInfo getCompanyInfo() {
        String cached = readCache(COMPANY_CACHE);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, CompanyInfo.class);
            } catch (JsonProcessingException ignored) {
                deleteCache(COMPANY_CACHE);
            }
        }
        CompanyInfo companyInfo = companyInfoRepository.findById(1L).orElseGet(CompanyInfo::new);
        writeCache(COMPANY_CACHE, companyInfo);
        return companyInfo;
    }

    /**
     * 更新平台信息，清除相关缓存
     * @param dto 平台信息请求参数
     * @return 更新后的平台信息实体
     */
    public CompanyInfo updateCompanyInfo(CompanyInfoDTO dto) {
        CompanyInfo info = companyInfoRepository.findById(1L).orElseGet(CompanyInfo::new);
        info.setCompanyName(dto.getCompanyName());
        info.setIntro(dto.getIntro());
        info.setCulture(dto.getCulture());
        info.setPhone(dto.getPhone());
        info.setEmail(dto.getEmail());
        info.setAddress(dto.getAddress());
        info.setLogo(dto.getLogo());
        info.setWebsite(dto.getWebsite());
        info.setServiceTime(dto.getServiceTime());
        info.setUpdateTime(LocalDateTime.now());
        CompanyInfo saved = companyInfoRepository.save(info);
        deleteCache(COMPANY_CACHE);
        deleteCache(HOME_CACHE);
        return saved;
    }

    /**
     * 获取已启用的推荐位列表（公开接口），优先从缓存读取
     * @return 推荐位列表
     */
    public List<Banner> publicBanners() {
        String cached = readCache(BANNER_CACHE);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<Banner>>() {});
            } catch (JsonProcessingException ignored) {
                deleteCache(BANNER_CACHE);
            }
        }
        List<Banner> banners = bannerRepository.findByStatus(1, Sort.by("sort").ascending());
        writeCache(BANNER_CACHE, banners);
        return banners;
    }

    /**
     * 获取所有推荐位列表（管理员接口）
     * @return 推荐位列表
     */
    public List<Banner> allBanners() {
        return bannerRepository.findAll(Sort.by("sort").ascending());
    }

    /**
     * 保存推荐位（创建或更新），清除相关缓存
     * @param id 推荐位ID，为null时新建
     * @param dto 推荐位请求参数
     * @return 保存后的推荐位实体
     */
    public Banner saveBanner(Long id, BannerDTO dto) {
        Banner banner = id == null ? new Banner() : bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("推荐位不存在"));
        banner.setTitle(dto.getTitle());
        banner.setSubtitle(dto.getSubtitle());
        banner.setImage(dto.getImage());
        banner.setLink(dto.getLink());
        banner.setSort(dto.getSort());
        banner.setStatus(normalizeStatus(dto.getStatus(), 1));
        if (banner.getCreateTime() == null) {
            banner.setCreateTime(LocalDateTime.now());
        }
        banner.setUpdateTime(LocalDateTime.now());
        Banner saved = bannerRepository.save(banner);
        deleteCache(BANNER_CACHE);
        deleteCache(HOME_CACHE);
        return saved;
    }

    /**
     * 删除推荐位，清除相关缓存
     * @param id 推荐位ID
     */
    public void deleteBanner(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new BusinessException("推荐位不存在");
        }
        bannerRepository.deleteById(id);
        deleteCache(BANNER_CACHE);
        deleteCache(HOME_CACHE);
    }

    /**
     * 获取首页聚合数据（平台信息+推荐位+闲置物品+校园贴士+仪表盘），优先从缓存读取
     * @return 首页数据Map
     */
    public Map<String, Object> home() {
        String cached = readCache(HOME_CACHE);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException ignored) {
                deleteCache(HOME_CACHE);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("company", getCompanyInfo());
        data.put("banners", publicBanners());
        data.put("products", listProducts("", 1, 3, true).getContent());
        data.put("news", listNews("", 1, 4, true).getContent());
        data.put("dashboard", dashboard());
        writeCache(HOME_CACHE, data);
        return data;
    }

    /**
     * 获取管理仪表盘统计数据
     * @return 各模块数量统计Map
     */
    public Map<String, Long> dashboard() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("newsTotal", newsRepository.count());
        data.put("productTotal", productRepository.count());
        data.put("messageTotal", messageRepository.count());
        data.put("pendingMessageTotal", messageRepository.countByStatus(0));
        data.put("orderTotal", productOrderRepository.count());
        data.put("returnPendingTotal", returnRequestRepository.countByStatus("APPLY_PENDING"));
        data.put("merchantPendingTotal", merchantRepository.countByAuditStatus(0));
        data.put("customerPendingTotal", customerRepository.countByAuditStatus(0));
        data.put("productPendingAudit", productRepository.countByAuditStatus(0));
        return data;
    }

    /**
     * 分页查询商家列表（支持按审核状态筛选）
     * @param auditStatus 审核状态筛选，为null时查询全部
     * @param page 页码
     * @param size 每页数量
     * @return 分页商家结果
     */
    public Page<Merchant> listMerchants(Integer auditStatus, int page, int size) {
        Pageable pageable = PageUtil.of(page, size);
        if (auditStatus == null) {
            return merchantRepository.findAll(pageable);
        }
        return merchantRepository.findByAuditStatus(auditStatus, pageable);
    }

    /**
     * 分页查询客户列表（支持按审核状态筛选）
     * @param auditStatus 审核状态筛选，为null时查询全部
     * @param page 页码
     * @param size 每页数量
     * @return 分页客户结果
     */
    public org.springframework.data.domain.Page<com.example.enterprise.entity.Customer> listCustomers(Integer auditStatus, int page, int size) {
        Pageable pageable = PageUtil.of(page, size);
        if (auditStatus == null) {
            return (org.springframework.data.domain.Page<com.example.enterprise.entity.Customer>) ((org.springframework.data.jpa.repository.JpaRepository) customerRepository).findAll(pageable);
        }
        return customerRepository.findByAuditStatus(auditStatus, pageable);
    }

    /** 分页大小标准化，限制在1-100之间 */
    private int normalizeSize(int size) {
        return PageUtil.normalizeSize(size);
    }

    /** 闲置物品排序方式映射 */
    private Sort productSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.DESC, "createTime"));
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.DESC, "createTime"));
            case "salesDesc" -> Sort.by(Sort.Direction.DESC, "salesCount").and(Sort.by(Sort.Direction.DESC, "createTime"));
            default -> Sort.by(Sort.Direction.DESC, "createTime");
        };
    }

    /** 状态值标准化，非0即1，非法值抛异常 */
    private int normalizeStatus(Integer status, int defaultValue) {
        if (status == null) {
            return defaultValue;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能为0或1");
        }
        return status;
    }

    /** 写入缓存（当前禁用，确保数据库为准） */
    private void writeCache(String key, Object value) {
        cacheService.set(key, value, CacheService.GLOBAL_TTL);
    }

    /** 读取缓存，Redis 不可用时返回 null */
    private String readCache(String key) {
        return cacheService.getString(key);
    }

    /** 校园贴士HTML安全过滤（等同于通用内容过滤） */
    private String sanitizeNewsHtml(String html) {
        return sanitizeContentHtml(html);
    }

    /** HTML内容安全过滤，使用jsoup白名单防止XSS */
    private String sanitizeContentHtml(String html) {
        return Jsoup.clean(html == null ? "" : html, NEWS_HTML_SAFELIST);
    }

    /** 为闲置物品实体附加图片列表 */
    private void attachProductImages(Product product) {
        if (product != null && product.getId() != null) {
            product.setImages(productImageRepository.findByProductIdOrderBySortAscIdAsc(product.getId()));
        }
    }

    /** 保存闲置物品图片列表（全量删除后重新插入），校验图片路径必须为服务器上传地址 */
    private void saveProductImages(Long productId, List<ProductImageDTO> images) {
        productImageRepository.deleteByProductId(productId);
        if (images == null) {
            return;
        }
        int sort = 0;
        for (ProductImageDTO item : images) {
            if (item == null || !StringUtils.hasText(item.getImageUrl())) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(validateServerImagePath(item.getImageUrl(), "物品详情图片"));
            image.setCaption(item.getCaption());
            image.setSort(item.getSort() == null ? sort : item.getSort());
            image.setCreateTime(LocalDateTime.now());
            productImageRepository.save(image);
            sort++;
        }
    }

    /** 校验图片路径必须为服务器上传接口返回的地址，禁止网络图片和Data URI */
    private String validateServerImagePath(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:")) {
            throw new BusinessException(fieldName + "必须上传到服务器，不能使用网络图片地址");
        }
        String prefix = StringUtils.hasText(publicUploadPrefix) ? publicUploadPrefix : "/uploads/";
        if (!trimmed.startsWith(prefix)) {
            throw new BusinessException(fieldName + "必须使用服务器上传接口返回的图片地址");
        }
        return trimmed;
    }

    /** 将评价实体转换为VO，匿名评价时隐藏用户姓名 */
    private ProductReviewVO toProductReviewVO(ProductReview review) {
        ProductReviewVO vo = new ProductReviewVO();
        vo.setId(review.getId());
        vo.setCustomerName(Integer.valueOf(1).equals(review.getAnonymous()) ? "匿名用户" : review.getCustomerName());
        vo.setProductId(review.getProductId());
        vo.setProductName(review.getProductName());
        vo.setReviewType(review.getReviewType());
        vo.setQualityRating(review.getQualityRating());
        vo.setServiceRating(review.getServiceRating());
        vo.setLogisticsRating(review.getLogisticsRating());
        vo.setServiceAttitudeRating(review.getServiceAttitudeRating());
        vo.setContent(review.getContent());
        vo.setMediaUrls(review.getMediaUrls());
        vo.setAnonymous(review.getAnonymous());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }

    /**
     * 填充发布者信息到商品实体（商家店铺名/学生用户名）
     */
    private void attachPublisherInfo(Product product) {
        if (product.getPublisherId() == null) return;
        // 优先尝试查找商家
        merchantRepository.findById(product.getPublisherId()).ifPresentOrElse(merchant -> {
            product.setPublisherName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getRealName() + "的店");
            product.setPublisherType("merchant");
            product.setPublisherAvatar(merchant.getShopLogo());
        }, () -> {
            // 不是商家则查找学生用户
            customerRepository.findById(product.getPublisherId()).ifPresent(customer -> {
                product.setPublisherName(customer.getUsername());
                product.setPublisherType("customer");
                product.setPublisherAvatar(null);
            });
        });
    }

    /** 删除缓存键，Redis不可用时静默忽略 */
    private void deleteCache(String key) {
        cacheService.delete(key);
    }
}
