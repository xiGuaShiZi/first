package com.example.enterprise.service;

import com.example.enterprise.dto.CustomerAddressDTO;
import com.example.enterprise.dto.CustomerLoginDTO;
import com.example.enterprise.dto.CustomerProfileVO;
import com.example.enterprise.dto.CustomerRegisterDTO;
import com.example.enterprise.dto.LoginDTO;
import com.example.enterprise.dto.MerchantLoginDTO;
import com.example.enterprise.dto.MerchantRegisterDTO;
import com.example.enterprise.entity.Customer;
import com.example.enterprise.entity.CustomerAddress;
import com.example.enterprise.entity.Merchant;
import com.example.enterprise.entity.SysPermission;
import com.example.enterprise.entity.SysRole;
import com.example.enterprise.entity.SysUser;
import com.example.enterprise.entity.SysUserRole;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.repository.CustomerAddressRepository;
import com.example.enterprise.repository.CustomerRepository;
import com.example.enterprise.repository.MerchantRepository;
import com.example.enterprise.repository.MerchantWalletRepository;
import com.example.enterprise.repository.SysPermissionRepository;
import com.example.enterprise.repository.SysRolePermissionRepository;
import com.example.enterprise.repository.SysRoleRepository;
import com.example.enterprise.repository.SysUserRepository;
import com.example.enterprise.repository.SysUserRoleRepository;
import com.example.enterprise.service.MerchantWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 认证与授权服务
 * <p>提供管理员和客户的登录、注册、Token管理、验证码、权限校验等功能</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    /** 系统用户数据访问 */
    private final SysUserRepository sysUserRepository;
    /** 客户数据访问 */
    private final CustomerRepository customerRepository;
    /** 商家对买家评价访问 */
    private final com.example.enterprise.repository.BuyerReviewRepository buyerReviewRepository;
    /** 商家数据访问 */
    private final MerchantRepository merchantRepository;
    /** 商家钱包数据访问 */
    private final MerchantWalletRepository merchantWalletRepository;
    /** 商家钱包管理服务 */
    private final MerchantWalletService merchantWalletService;
    /** 客户收货地址数据访问 */
    private final CustomerAddressRepository customerAddressRepository;
    /** 用户-角色关联数据访问 */
    private final SysUserRoleRepository sysUserRoleRepository;
    /** 系统角色数据访问 */
    private final SysRoleRepository sysRoleRepository;
    /** 角色-权限关联数据访问 */
    private final SysRolePermissionRepository sysRolePermissionRepository;
    /** 系统权限数据访问 */
    private final SysPermissionRepository sysPermissionRepository;
    /** 密码编码器 */
    private final PasswordEncoder passwordEncoder;
    /** 内存Token存储，线程安全 */
    private final Map<String, TokenSession> tokenStore = new ConcurrentHashMap<>();

    /** Token过期时间（分钟），默认480分钟（8小时） */
    @Value("${app.auth.token-ttl-minutes:480}")
    private long tokenTtlMinutes;

    /** 验证码过期时间（分钟），默认5分钟 */
    @Value("${app.auth.captcha-ttl-minutes:5}")
    private long captchaTtlMinutes;

    /**
     * 管理员登录，验证用户名密码后生成Token
     * @param dto 登录请求参数
     * @return 生成的Token字符串
     */
    public String login(LoginDTO dto) {
        String username = normalizeUsername(dto.getUsername());
        SysUser user = sysUserRepository.findByUsernameAndStatus(username, 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        Set<String> roles = loadRoleCodes(user.getId());
        if (roles.isEmpty()) {
            throw new BusinessException(403, "用户未分配角色");
        }
        Set<String> permissions = loadPermissionCodes(user.getId());

        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserRepository.save(user);

        String token = newToken();
        putSession(token, new TokenUser(user.getId(), user.getUsername(), "ADMIN", roles, permissions), tokenTtlMinutes);
        return token;
    }

    /**
     * 统一登录，先尝试管理员登录，失败后尝试客户登录，并校验验证码
     * @param dto 登录请求参数（含验证码）
     * @return 包含token、角色和用户名的Map
     */
    public Map<String, String> unifiedLogin(LoginDTO dto) {
        String username = normalizeUsername(dto.getUsername());
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptcha());
        var admin = sysUserRepository.findByUsernameAndStatus(username, 1);
        if (admin.isPresent()) {
            SysUser user = admin.get();
            if (!matches(dto.getPassword(), user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }
            Set<String> roles = loadRoleCodes(user.getId());
            if (roles.isEmpty()) {
                throw new BusinessException(403, "用户未分配角色");
            }
            Set<String> permissions = loadPermissionCodes(user.getId());
            user.setLastLoginTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserRepository.save(user);
            String token = newToken();
            putSession(token, new TokenUser(user.getId(), user.getUsername(), "ADMIN", roles, permissions), tokenTtlMinutes);
            return Map.of("token", token, "role", "ADMIN", "username", username);
        }

        Customer customer = customerRepository.findByUsernameAndStatus(username, 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!matches(dto.getPassword(), customer.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (customer.getAuditStatus() == null || customer.getAuditStatus() != 1) {
            throw new BusinessException("您的账号尚未通过审核或已被拒绝，请联系管理员");
        }
        String token = newToken();
        putSession(token, new TokenUser(customer.getId(), customer.getUsername(), "USER", Set.of("USER"), Set.of()), tokenTtlMinutes);
        return Map.of("token", token, "role", "USER", "username", customer.getUsername());
    }

    /**
     * 客户注册，创建新客户账号
     * @param dto 注册请求参数
     * @return 注册成功的客户实体
     */
    @Transactional
    public Customer register(CustomerRegisterDTO dto) {
        String username = normalizeUsername(dto.getUsername());
        if (customerRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (customerRepository.existsByPhone(dto.getPhone())) {
            throw new BusinessException("手机号已注册");
        }
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
        customer.setGender(dto.getGender());
        customer.setBankAccount(dto.getBankAccount());
        customer.setStatus(1);
        // 新注册用户默认为待审核，管理员需审核后方可登录
        customer.setAuditStatus(0);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        
        // 注册时创建默认地址记录
        CustomerAddress defaultAddress = new CustomerAddress();
        defaultAddress.setCustomerId(savedCustomer.getId());
        defaultAddress.setReceiverName(username);
        defaultAddress.setPhone(dto.getPhone());
        defaultAddress.setProvince("");
        defaultAddress.setCity(dto.getCity());
        defaultAddress.setDistrict("");
        defaultAddress.setDetailAddress("");
        defaultAddress.setIsDefault(1);
        defaultAddress.setCreateTime(LocalDateTime.now());
        defaultAddress.setUpdateTime(LocalDateTime.now());
        customerAddressRepository.save(defaultAddress);
        
        return savedCustomer;
    }

    /**
     * 客户登录，校验验证码后生成Token
     * @param dto 客户登录请求参数（含验证码）
     * @return 生成的Token字符串
     */
    public String customerLogin(CustomerLoginDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptcha());
        Customer customer = customerRepository.findByUsernameAndStatus(normalizeUsername(dto.getUsername()), 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!matches(dto.getPassword(), customer.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (customer.getAuditStatus() == null || customer.getAuditStatus() != 1) {
            throw new BusinessException("您的账号尚未通过审核或已被拒绝，请联系管理员");
        }
        String token = newToken();
        putSession(token, new TokenUser(customer.getId(), customer.getUsername(), "USER", Set.of("USER"), Set.of()), tokenTtlMinutes);
        return token;
    }

    /**
     * 商家注册，创建新商家账号（待审核状态）
     * @param dto 注册请求参数
     * @return 注册成功的商家实体
     */
    @Transactional
    public Merchant registerMerchant(MerchantRegisterDTO dto) {
        String username = normalizeUsername(dto.getUsername());
        if (merchantRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (merchantRepository.existsByPhone(dto.getPhone())) {
            throw new BusinessException("手机号已注册");
        }
        Merchant merchant = new Merchant();
        merchant.setUsername(username);
        merchant.setPassword(passwordEncoder.encode(dto.getPassword()));
        merchant.setRealName(dto.getRealName());
        merchant.setGender(dto.getGender());
        merchant.setPhone(dto.getPhone());
        merchant.setIdCard(dto.getIdCard());
        merchant.setIdCardImage(dto.getIdCardImage());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setBankAccount(dto.getBankAccount());
        merchant.setAuditStatus(0); // 待审核
        merchant.setStatus(1);
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    /**
     * 商家登录，校验验证码和审核状态后生成Token
     * @param dto 商家登录请求参数（含验证码）
     * @return 生成的Token字符串
     */
    public String merchantLogin(MerchantLoginDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptcha());
        Merchant merchant = merchantRepository.findByUsernameAndStatus(normalizeUsername(dto.getUsername()), 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!matches(dto.getPassword(), merchant.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (merchant.getAuditStatus() != 1) {
            throw new BusinessException("您的账号尚未通过审核，请耐心等待");
        }
        String token = newToken();
        putSession(token, new TokenUser(merchant.getId(), merchant.getUsername(), "MERCHANT", Set.of("MERCHANT"), Set.of()), tokenTtlMinutes);
        return token;
    }

    /**
     * 审核商家注册申请
     * @param merchantId 商家ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditRemark 审核备注
     * @return 审核后的商家实体
     */
    @Transactional
    public Merchant auditMerchant(Long merchantId, Integer auditStatus, String auditRemark) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("商家不存在"));
        if (merchant.getAuditStatus() != 0) {
            throw new BusinessException("该商家已审核，无法重复审核");
        }
        if (auditStatus != 1 && auditStatus != 2) {
            throw new BusinessException("审核状态无效");
        }
        merchant.setAuditStatus(auditStatus);
        merchant.setAuditRemark(auditRemark);
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        Merchant savedMerchant = merchantRepository.save(merchant);

        // 如果审核通过，自动创建钱包
        if (auditStatus == 1) {
            try {
                merchantWalletService.createWallet(savedMerchant.getId());
            } catch (Exception e) {
                // 钱包创建失败不影响审核，可能已经存在
            }
        }

        return savedMerchant;
    }

    /**
     * 审核客户注册申请
     * @param customerId 客户ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditRemark 审核备注
     * @return 审核后的客户实体
     */
    @Transactional
    public Customer auditCustomer(Long customerId, Integer auditStatus, String auditRemark) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));
        if (customer.getAuditStatus() != null && customer.getAuditStatus() != 0) {
            throw new BusinessException("该客户已审核，无法重复审核");
        }
        if (auditStatus != 1 && auditStatus != 2) {
            throw new BusinessException("审核状态无效");
        }
        customer.setAuditStatus(auditStatus);
        customer.setAuditRemark(auditRemark);
        customer.setAuditTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    /**
     * 获取商家个人资料，隐藏密码字段
     * @param tokenUser 当前登录用户
     * @return 商家实体（隐藏密码）
     */
    public Merchant getMerchantProfile(TokenUser tokenUser) {
        if (!"MERCHANT".equals(tokenUser.role())) {
            throw new BusinessException(403, "无权访问");
        }
        Merchant merchant = merchantRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        return merchant;
    }

    /**
     * 修改商家密码
     * @param tokenUser 当前登录用户
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void changeMerchantPassword(TokenUser tokenUser, String oldPassword, String newPassword) {
        if (!"MERCHANT".equals(tokenUser.role())) {
            throw new BusinessException(403, "无权修改密码");
        }
        Merchant merchant = merchantRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        if (!matches(oldPassword, merchant.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        merchant.setPassword(passwordEncoder.encode(newPassword));
        merchant.setUpdateTime(LocalDateTime.now());
        merchantRepository.save(merchant);
    }

    /**
     * 获取客户个人资料，隐藏密码字段，包含默认收货地址
     * @param tokenUser 当前登录用户
     * @return 客户个人资料（包含默认地址）
     */
    public CustomerProfileVO customerProfile(TokenUser tokenUser) {
        Customer customer = customerRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        
        CustomerProfileVO profile = new CustomerProfileVO();
        profile.setId(customer.getId());
        profile.setUsername(customer.getUsername());
        profile.setPhone(customer.getPhone());
        profile.setEmail(customer.getEmail());
        profile.setGender(customer.getGender());
        profile.setBankAccount(customer.getBankAccount());
        profile.setStatus(customer.getStatus());
        profile.setCreateTime(customer.getCreateTime() != null ? customer.getCreateTime().toString() : null);
        profile.setUpdateTime(customer.getUpdateTime() != null ? customer.getUpdateTime().toString() : null);
        
        // 查询默认地址
        List<CustomerAddress> addresses = customerAddressRepository
                .findByCustomerIdOrderByIsDefaultDescUpdateTimeDesc(tokenUser.id());
        CustomerAddress defaultAddress = addresses.stream()
                .filter(addr -> Integer.valueOf(1).equals(addr.getIsDefault()))
                .findFirst()
                .orElse(null);
        profile.setDefaultAddress(defaultAddress);
        // 账户余额
        profile.setBalance(customer.getBalance());
        // 积分
        profile.setPoints(customer.getPoints());

        // 统计商家对该用户的评价（好评率）
        try {
            long total = buyerReviewRepository.countByBuyerId(tokenUser.id());
            long positive = buyerReviewRepository.countPositiveByBuyerId(tokenUser.id());
            profile.setBuyerMerchantReviewCount(total);
            double rate = total == 0 ? 0.0 : ((double) positive / (double) total) * 100.0;
            // 保留一位小数
            profile.setBuyerMerchantPositiveRate(Math.round(rate * 10.0) / 10.0);
        } catch (Exception e) {
            // 若查询失败，默认0
            profile.setBuyerMerchantReviewCount(0L);
            profile.setBuyerMerchantPositiveRate(0.0);
        }

        return profile;
    }

    /**
     * 管理员为指定客户充值（仅管理员可调用）
     * @param tokenUser 当前登录用户（必须为ADMIN角色）
     * @param customerId 客户ID
     * @param amount 充值金额（单位：元，必须大于0）
     * @return 更新后的客户实体
     */
    @Transactional
    public Customer rechargeCustomer(TokenUser tokenUser, Long customerId, java.math.BigDecimal amount) {
        if (!"ADMIN".equals(tokenUser.role())) {
            throw new BusinessException(403, "无权操作");
        }
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额需大于0");
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));
        if (customer.getBalance() == null) customer.setBalance(java.math.BigDecimal.ZERO);
        customer.setBalance(customer.getBalance().add(amount));
        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    /**
     * 用户使用积分兑换现金（每100积分=1元）。积分按整百兑换，多余积分不兑换。
     * @param tokenUser 当前登录用户
     * @param pointsToUse 要兑换的积分数（必须为正整数）
     * @return 更新后的客户实体
     */
    @Transactional
    public Customer redeemPoints(TokenUser tokenUser, int pointsToUse) {
        if (pointsToUse <= 0) {
            throw new BusinessException("兑换积分需为正整数");
        }
        Customer customer = customerRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        int current = customer.getPoints() == null ? 0 : customer.getPoints();
        if (current < pointsToUse) {
            throw new BusinessException("积分不足");
        }
        int convertibleYuan = pointsToUse / 100; // 每100积分兑换1元
        if (convertibleYuan <= 0) {
            throw new BusinessException("每次最少兑换100积分");
        }
        int actualPointsUsed = convertibleYuan * 100;
        java.math.BigDecimal amount = java.math.BigDecimal.valueOf(convertibleYuan);
        if (customer.getBalance() == null) customer.setBalance(java.math.BigDecimal.ZERO);
        customer.setPoints(current - actualPointsUsed);
        customer.setBalance(customer.getBalance().add(amount));
        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    /**
     * 修改客户密码
     * @param tokenUser 当前登录用户
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void changeCustomerPassword(TokenUser tokenUser, String oldPassword, String newPassword) {
        Customer customer = customerRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        if (!matches(oldPassword, customer.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setUpdateTime(LocalDateTime.now());
        customerRepository.save(customer);
    }

    /**
     * 修改管理员密码
     * @param tokenUser 当前登录用户（必须为ADMIN角色）
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void changeAdminPassword(TokenUser tokenUser, String oldPassword, String newPassword) {
        if (!"ADMIN".equals(tokenUser.role())) {
            throw new BusinessException(403, "无权修改管理员密码");
        }
        SysUser user = sysUserRepository.findById(tokenUser.id())
                .orElseThrow(() -> new BusinessException(401, "请先登录"));
        if (!matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserRepository.save(user);
    }

    /**
     * 查询客户的所有收货地址
     * @param tokenUser 当前登录用户
     * @return 收货地址列表
     */
    public List<CustomerAddress> customerAddresses(TokenUser tokenUser) {
        return customerAddressRepository.findByCustomerIdOrderByIsDefaultDescUpdateTimeDesc(tokenUser.id());
    }

    /**
     * 保存客户收货地址，支持新建和更新，自动设置默认地址
     * @param tokenUser 当前登录用户
     * @param id 地址ID，为null时新建
     * @param dto 地址请求参数
     * @return 保存后的地址实体
     */
    @Transactional
    public CustomerAddress saveCustomerAddress(TokenUser tokenUser, Long id, CustomerAddressDTO dto) {
        CustomerAddress address = id == null
                ? new CustomerAddress()
                : customerAddressRepository.findByIdAndCustomerId(id, tokenUser.id())
                        .orElseThrow(() -> new BusinessException("收货地址不存在"));
        if (id == null) {
            address.setCustomerId(tokenUser.id());
            address.setCreateTime(LocalDateTime.now());
        }
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(Integer.valueOf(1).equals(dto.getIsDefault()) || customerAddressRepository.countByCustomerId(tokenUser.id()) == 0 ? 1 : 0);
        address.setUpdateTime(LocalDateTime.now());
        CustomerAddress saved = customerAddressRepository.save(address);
        if (Integer.valueOf(1).equals(saved.getIsDefault())) {
            setDefaultAddress(tokenUser, saved.getId());
            saved.setIsDefault(1);
        }
        return saved;
    }

    /**
     * 设置默认收货地址，将其他地址设为非默认
     * @param tokenUser 当前登录用户
     * @param id 目标地址ID
     * @return 设为默认的地址实体
     */
    @Transactional
    public CustomerAddress setDefaultAddress(TokenUser tokenUser, Long id) {
        CustomerAddress target = customerAddressRepository.findByIdAndCustomerId(id, tokenUser.id())
                .orElseThrow(() -> new BusinessException("收货地址不存在"));
        customerAddressRepository.findByCustomerIdOrderByIsDefaultDescUpdateTimeDesc(tokenUser.id()).forEach(address -> {
            address.setIsDefault(address.getId().equals(id) ? 1 : 0);
            address.setUpdateTime(LocalDateTime.now());
            customerAddressRepository.save(address);
        });
        target.setIsDefault(1);
        return target;
    }

    /**
     * 删除客户收货地址
     * @param tokenUser 当前登录用户
     * @param id 要删除的地址ID
     */
    public void deleteCustomerAddress(TokenUser tokenUser, Long id) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(id, tokenUser.id())
                .orElseThrow(() -> new BusinessException("收货地址不存在"));
        customerAddressRepository.delete(address);
    }

    /**
     * 校验Token是否有效
     * @param token 待校验的Token
     * @return 是否有效
     */
    public boolean valid(String token) {
        return currentOrNull(token) != null;
    }

    /**
     * 校验Token是否为有效管理员Token
     * @param token 待校验的Token
     * @return 是否为有效管理员
     */
    public boolean validAdmin(String token) {
        TokenUser user = currentOrNull(token);
        return user != null && "ADMIN".equals(user.role()) && !user.roles().isEmpty();
    }

    /**
     * 校验Token是否为有效客户Token
     * @param token 待校验的Token
     * @return 是否为有效客户
     */
    public boolean validUser(String token) {
        TokenUser user = currentOrNull(token);
        return user != null && "USER".equals(user.role());
    }

    /**
     * 校验Token是否具有指定权限（超级管理员自动拥有所有权限）
     * @param token 待校验的Token
     * @param permissionCode 权限编码
     * @return 是否拥有该权限
     */
    public boolean hasPermission(String token, String permissionCode) {
        TokenUser user = currentOrNull(token);
        return user != null && ("ADMIN".equals(user.role()) && user.roles().contains("SUPER_ADMIN")
                || user.permissions().contains(permissionCode));
    }

    /**
     * 根据Token获取当前登录用户，Token无效时抛出异常
     * @param token 待解析的Token
     * @return 当前登录用户信息
     * @throws BusinessException Token无效时抛出401异常
     */
    public TokenUser current(String token) {
        TokenUser user = currentOrNull(token);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    /**
     * 根据Token获取当前登录用户，Token无效时返回null
     * @param token 待解析的Token
     * @return 当前登录用户信息，或null
     */
    public TokenUser currentOrNull(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenSession session = tokenStore.get(token);
        if (session == null) {
            return null;
        }
        if (session.isExpired()) {
            tokenStore.remove(token);
            return null;
        }
        return session.user();
    }

    /**
     * 注销登录，移除Token
     * @param token 待注销的Token
     */
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokenStore.remove(token);
    }

    /**
     * 生成验证码，返回验证码ID和SVG图片
     * @return 包含captchaId和captchaImage的Map
     */
    public Map<String, String> captcha() {
        String captchaId = newToken();
        int code = 1000 + new java.security.SecureRandom().nextInt(9000);
        String captchaText = String.valueOf(code);
        putSession("CAPTCHA:" + captchaId, new TokenUser(null, captchaText, "CAPTCHA", Set.of(), Set.of()), captchaTtlMinutes);
        return Map.of("captchaId", captchaId, "captchaImage", captchaImage(captchaText));
    }

    /** 生成验证码SVG图片，返回Base64编码的Data URI */
    private String captchaImage(String text) {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="120" height="42" viewBox="0 0 120 42">
                  <rect width="120" height="42" rx="6" fill="#eef8fa"/>
                  <path d="M0 30 C20 12, 36 48, 58 24 S95 8, 120 26" fill="none" stroke="#78c7d0" stroke-width="2"/>
                  <path d="M10 8 L112 34 M18 36 L104 6" stroke="#c7dce8" stroke-width="1"/>
                  <text x="60" y="29" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" font-weight="800" letter-spacing="4" fill="#0e1a27">%s</text>
                </svg>
                """.formatted(text);
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    /** 校验验证码，校验后立即删除（一次性使用） */
    private void verifyCaptcha(String captchaId, String captcha) {
        TokenSession session = tokenStore.remove("CAPTCHA:" + captchaId);
        TokenUser stored = session == null || session.isExpired() ? null : session.user();
        if (stored == null || captcha == null || !stored.username().equalsIgnoreCase(captcha.trim())) {
            throw new BusinessException("验证码错误");
        }
    }

    /** 将Token-用户会话存入内存存储 */
    private void putSession(String token, TokenUser user, long ttlMinutes) {
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(Math.max(ttlMinutes, 1)));
        tokenStore.put(token, new TokenSession(user, expiresAt));
    }

    /** 生成随机Token（UUID去横线） */
    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 用户名标准化：去除前后空格 */
    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    /** 密码匹配校验，异常时返回false */
    private boolean matches(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /** 加载用户的所有角色编码 */
    private Set<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return sysRoleRepository.findByIdInAndStatus(roleIds, 1).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** 加载用户的所有权限编码（通过角色关联） */
    private Set<String> loadPermissionCodes(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        List<Long> permissionIds = sysRolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rolePermission -> rolePermission.getPermissionId())
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }
        return sysPermissionRepository.findByIdInAndStatus(permissionIds, 1).stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Token会话记录，包含用户信息和过期时间 */
    private record TokenSession(TokenUser user, Instant expiresAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    /** 当前登录用户信息，包含ID、用户名、角色、角色集合和权限集合 */
    public record TokenUser(Long id, String username, String role, Set<String> roles, Set<String> permissions) {}
}
