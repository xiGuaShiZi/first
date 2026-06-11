package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.dto.CustomerLoginDTO;
import com.example.enterprise.dto.CustomerRegisterDTO;
import com.example.enterprise.dto.LoginDTO;
import com.example.enterprise.dto.MerchantLoginDTO;
import com.example.enterprise.dto.MerchantRegisterDTO;
import com.example.enterprise.entity.Customer;
import com.example.enterprise.entity.Merchant;
import com.example.enterprise.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * <p>提供管理员登录、统一登录、验证码、客户注册登录和注销接口</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    /** 认证服务 */
    private final AuthService authService;

    /** 管理员登录，返回Token */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(Map.of("token", authService.login(dto)));
    }

    /** 统一登录，自动识别管理员/客户角色，需验证码 */
    @PostMapping("/unified-login")
    public Result<Map<String, String>> unifiedLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.unifiedLogin(dto));
    }

    /** 获取验证码，返回captchaId和SVG图片 */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.success(authService.captcha());
    }

    /** 客户注册 */
    @PostMapping("/customer/register")
    public Result<Customer> register(@Valid @RequestBody CustomerRegisterDTO dto) {
        Customer customer = authService.register(dto);
        customer.setPassword(null);
        return Result.success(customer);
    }

    /** 客户登录，需验证码 */
    @PostMapping("/customer/login")
    public Result<Map<String, String>> customerLogin(@Valid @RequestBody CustomerLoginDTO dto) {
        return Result.success(Map.of("token", authService.customerLogin(dto)));
    }

    /** 商家注册 */
    @PostMapping("/merchant/register")
    public Result<Merchant> registerMerchant(@Valid @RequestBody MerchantRegisterDTO dto) {
        Merchant merchant = authService.registerMerchant(dto);
        merchant.setPassword(null);
        return Result.success(merchant);
    }

    /** 商家登录，需验证码 */
    @PostMapping("/merchant/login")
    public Result<Map<String, String>> merchantLogin(@Valid @RequestBody MerchantLoginDTO dto) {
        return Result.success(Map.of("token", authService.merchantLogin(dto)));
    }

    /** 注销登录 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization == null ? null : authorization.replace("Bearer ", "");
        authService.logout(token);
        return Result.success();
    }
}
