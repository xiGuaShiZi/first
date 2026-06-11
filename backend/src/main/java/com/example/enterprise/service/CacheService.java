package com.example.enterprise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 通用 Redis 缓存服务
 * <p>
 * 封装 Redis 读写操作，统一管理缓存键前缀和 TTL。
 * 所有操作均捕获异常静默处理，Redis 不可用时自动降级到数据库查询，不影响业务。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    /** Redis 字符串操作模板 */
    private final StringRedisTemplate redisTemplate;

    /** JSON 序列化/反序列化工具 */
    private final ObjectMapper objectMapper;

    // ======================== 缓存键前缀 ========================

    /** 客户个人资料缓存键前缀，格式：cache:customer:profile:{id} */
    public static final String CUSTOMER_PROFILE_PREFIX = "cache:customer:profile:";

    /** 客户收货地址列表缓存键前缀，格式：cache:customer:addresses:{id} */
    public static final String CUSTOMER_ADDRESSES_PREFIX = "cache:customer:addresses:";

    /** 商家个人资料缓存键前缀，格式：cache:merchant:profile:{id} */
    public static final String MERCHANT_PROFILE_PREFIX = "cache:merchant:profile:";

    /** 商品详情缓存键前缀，格式：cache:product:detail:{id} */
    public static final String PRODUCT_DETAIL_PREFIX = "cache:product:detail:";

    /** 首页数据缓存键 */
    public static final String HOME_CACHE = "website:home";

    /** 平台信息缓存键 */
    public static final String COMPANY_CACHE = "website:company";

    /** 推荐位缓存键 */
    public static final String BANNER_CACHE = "website:banners";

    /** 客户实体缓存键前缀，格式：cache:customer:entity:{id} */
    public static final String CUSTOMER_ENTITY_PREFIX = "cache:customer:entity:";

    /** 系统用户缓存键前缀，格式：cache:sysuser:entity:{id} */
    public static final String SYS_USER_PREFIX = "cache:sysuser:entity:";

    /** 商家实体缓存键前缀（店铺信息），格式：cache:merchant:entity:{id} */
    public static final String MERCHANT_ENTITY_PREFIX = "cache:merchant:entity:";

    /** 商家钱包缓存键前缀，格式：cache:merchant:wallet:{id} */
    public static final String MERCHANT_WALLET_PREFIX = "cache:merchant:wallet:";

    /** 商品分类列表缓存键 */
    public static final String PRODUCT_CATEGORIES_KEY = "cache:product:categories";

    /** 收藏/意向清单缓存键前缀，格式：cache:customer:cart:{id} */
    public static final String CART_ITEMS_PREFIX = "cache:customer:cart:";

    // ======================== 默认 TTL ========================

    /** 用户资料缓存过期时间：30 分钟 */
    public static final Duration PROFILE_TTL = Duration.ofMinutes(30);

    /** 地址列表缓存过期时间：30 分钟 */
    public static final Duration ADDRESSES_TTL = Duration.ofMinutes(30);

    /** 商品详情缓存过期时间：15 分钟 */
    public static final Duration PRODUCT_TTL = Duration.ofMinutes(15);

    /** 首页/推荐位等全局缓存过期时间：60 分钟 */
    public static final Duration GLOBAL_TTL = Duration.ofMinutes(60);

    /** 实体缓存过期时间：15 分钟 */
    public static final Duration ENTITY_TTL = Duration.ofMinutes(15);

    /** 钱包缓存过期时间：5 分钟（余额变动频繁） */
    public static final Duration WALLET_TTL = Duration.ofMinutes(5);

    /** 收藏/意向清单缓存过期时间：5 分钟 */
    public static final Duration CART_TTL = Duration.ofMinutes(5);

    // ======================== 通用操作 ========================

    /**
     * 将对象序列化为 JSON 写入 Redis
     * @param key 缓存键
     * @param value 要缓存的对象
     * @param ttl 过期时间
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.debug("Redis 写入失败 [key={}]: {}", key, e.getMessage());
        }
    }

    /**
     * 从 Redis 读取缓存并反序列化为指定类型
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存对象，不存在或异常时返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.debug("Redis 读取失败 [key={}]: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 从 Redis 读取缓存并反序列化为泛型类型（如 List）
     * @param key 缓存键
     * @param typeRef 目标类型引用
     * @return 缓存对象，不存在或异常时返回 null
     */
    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.debug("Redis 读取失败 [key={}]: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 读取 Redis 中的原始字符串
     * @param key 缓存键
     * @return 字符串值，不存在或异常时返回 null
     */
    public String getString(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.debug("Redis 读取失败 [key={}]: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 写入原始字符串到 Redis
     * @param key 缓存键
     * @param value 字符串值
     * @param ttl 过期时间
     */
    public void setString(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.debug("Redis 写入失败 [key={}]: {}", key, e.getMessage());
        }
    }

    /**
     * 删除缓存键
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.debug("Redis 删除失败 [key={}]: {}", key, e.getMessage());
        }
    }

    /**
     * 按前缀批量删除缓存（模糊匹配）
     * @param pattern 匹配模式，如 "cache:customer:addresses:123*"
     */
    public void deleteByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.debug("Redis 批量删除失败 [pattern={}]: {}", pattern, e.getMessage());
        }
    }

    // ======================== 便捷方法 ========================

    /**
     * 缓存客户个人资料
     */
    public void putCustomerProfile(Long customerId, Object profile) {
        set(CUSTOMER_PROFILE_PREFIX + customerId, profile, PROFILE_TTL);
    }

    /**
     * 获取缓存的客户个人资料
     */
    public <T> T getCustomerProfile(Long customerId, Class<T> clazz) {
        return get(CUSTOMER_PROFILE_PREFIX + customerId, clazz);
    }

    /**
     * 清除客户个人资料缓存
     */
    public void evictCustomerProfile(Long customerId) {
        delete(CUSTOMER_PROFILE_PREFIX + customerId);
    }

    /**
     * 缓存客户收货地址列表
     */
    public void putCustomerAddresses(Long customerId, List<?> addresses) {
        set(CUSTOMER_ADDRESSES_PREFIX + customerId, addresses, ADDRESSES_TTL);
    }

    /**
     * 获取缓存的客户收货地址列表
     */
    public <T> T getCustomerAddresses(Long customerId, TypeReference<T> typeRef) {
        return get(CUSTOMER_ADDRESSES_PREFIX + customerId, typeRef);
    }

    /**
     * 清除客户收货地址缓存
     */
    public void evictCustomerAddresses(Long customerId) {
        delete(CUSTOMER_ADDRESSES_PREFIX + customerId);
    }

    /**
     * 缓存商家个人资料
     */
    public void putMerchantProfile(Long merchantId, Object profile) {
        set(MERCHANT_PROFILE_PREFIX + merchantId, profile, PROFILE_TTL);
    }

    /**
     * 获取缓存的商家个人资料
     */
    public <T> T getMerchantProfile(Long merchantId, Class<T> clazz) {
        return get(MERCHANT_PROFILE_PREFIX + merchantId, clazz);
    }

    /**
     * 清除商家个人资料缓存
     */
    public void evictMerchantProfile(Long merchantId) {
        delete(MERCHANT_PROFILE_PREFIX + merchantId);
    }

    /**
     * 缓存商品详情
     */
    public void putProductDetail(Long productId, Object product) {
        set(PRODUCT_DETAIL_PREFIX + productId, product, PRODUCT_TTL);
    }

    /**
     * 获取缓存的商品详情
     */
    public <T> T getProductDetail(Long productId, Class<T> clazz) {
        return get(PRODUCT_DETAIL_PREFIX + productId, clazz);
    }

    /**
     * 清除商品详情缓存
     */
    public void evictProductDetail(Long productId) {
        delete(PRODUCT_DETAIL_PREFIX + productId);
    }

    // ======================== Customer 实体 ========================

    /**
     * 缓存客户实体
     */
    public void putCustomer(Long customerId, Object customer) {
        set(CUSTOMER_ENTITY_PREFIX + customerId, customer, ENTITY_TTL);
    }

    /**
     * 获取缓存的客户实体
     */
    public <T> T getCustomer(Long customerId, Class<T> clazz) {
        return get(CUSTOMER_ENTITY_PREFIX + customerId, clazz);
    }

    /**
     * 清除客户实体缓存
     */
    public void evictCustomer(Long customerId) {
        delete(CUSTOMER_ENTITY_PREFIX + customerId);
    }

    // ======================== SysUser 实体 ========================

    /**
     * 缓存系统用户实体
     */
    public void putSysUser(Long userId, Object sysUser) {
        set(SYS_USER_PREFIX + userId, sysUser, ENTITY_TTL);
    }

    /**
     * 获取缓存的系统用户实体
     */
    public <T> T getSysUser(Long userId, Class<T> clazz) {
        return get(SYS_USER_PREFIX + userId, clazz);
    }

    /**
     * 清除系统用户实体缓存
     */
    public void evictSysUser(Long userId) {
        delete(SYS_USER_PREFIX + userId);
    }

    // ======================== Merchant 实体 ========================

    /**
     * 缓存商家实体
     */
    public void putMerchant(Long merchantId, Object merchant) {
        set(MERCHANT_ENTITY_PREFIX + merchantId, merchant, ENTITY_TTL);
    }

    /**
     * 获取缓存的商家实体
     */
    public <T> T getMerchant(Long merchantId, Class<T> clazz) {
        return get(MERCHANT_ENTITY_PREFIX + merchantId, clazz);
    }

    /**
     * 清除商家实体缓存
     */
    public void evictMerchant(Long merchantId) {
        delete(MERCHANT_ENTITY_PREFIX + merchantId);
    }

    // ======================== MerchantWallet ========================

    /**
     * 缓存商家钱包
     */
    public void putMerchantWallet(Long merchantId, Object wallet) {
        set(MERCHANT_WALLET_PREFIX + merchantId, wallet, WALLET_TTL);
    }

    /**
     * 获取缓存的商家钱包
     */
    public <T> T getMerchantWallet(Long merchantId, Class<T> clazz) {
        return get(MERCHANT_WALLET_PREFIX + merchantId, clazz);
    }

    /**
     * 清除商家钱包缓存
     */
    public void evictMerchantWallet(Long merchantId) {
        delete(MERCHANT_WALLET_PREFIX + merchantId);
    }

    // ======================== 商品分类 ========================

    /**
     * 缓存商品分类列表
     */
    public void putProductCategories(List<String> categories) {
        set(PRODUCT_CATEGORIES_KEY, categories, GLOBAL_TTL);
    }

    /**
     * 获取缓存的商品分类列表
     */
    public List<String> getProductCategories() {
        return get(PRODUCT_CATEGORIES_KEY, new TypeReference<List<String>>() {});
    }

    /**
     * 清除商品分类缓存
     */
    public void evictProductCategories() {
        delete(PRODUCT_CATEGORIES_KEY);
    }

    // ======================== 收藏/意向清单 ========================

    /**
     * 缓存用户的收藏/意向清单
     */
    public void putCartItems(Long customerId, Object items) {
        set(CART_ITEMS_PREFIX + customerId, items, CART_TTL);
    }

    /**
     * 获取缓存的收藏/意向清单
     */
    public <T> T getCartItems(Long customerId, TypeReference<T> typeRef) {
        return get(CART_ITEMS_PREFIX + customerId, typeRef);
    }

    /**
     * 清除用户的收藏/意向清单缓存
     */
    public void evictCartItems(Long customerId) {
        delete(CART_ITEMS_PREFIX + customerId);
    }
}
