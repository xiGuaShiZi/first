package com.example.enterprise.config;

import com.example.enterprise.entity.SysPermission;
import com.example.enterprise.entity.SysRole;
import com.example.enterprise.entity.SysRolePermission;
import com.example.enterprise.entity.SysUser;
import com.example.enterprise.entity.SysUserRole;
import com.example.enterprise.repository.SysPermissionRepository;
import com.example.enterprise.repository.SysRolePermissionRepository;
import com.example.enterprise.repository.SysRoleRepository;
import com.example.enterprise.repository.SysUserRepository;
import com.example.enterprise.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 数据初始化器
 * <p>应用启动时自动初始化管理员账号、超级管理员角色、权限列表及绑定关系</p>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    /** 系统用户数据访问 */
    private final SysUserRepository sysUserRepository;
    /** 系统角色数据访问 */
    private final SysRoleRepository sysRoleRepository;
    /** 系统权限数据访问 */
    private final SysPermissionRepository sysPermissionRepository;
    /** 用户-角色关联数据访问 */
    private final SysUserRoleRepository sysUserRoleRepository;
    /** 角色-权限关联数据访问 */
    private final SysRolePermissionRepository sysRolePermissionRepository;
    /** 密码编码器 */
    private final PasswordEncoder passwordEncoder;

    /** 初始管理员密码，默认123456 */
    @Value("${app.initial-admin-password:123456}")
    private String initialAdminPassword;

    /**
     * 应用启动时执行数据初始化
     */
    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();
        SysUser user = seedUser(now);
        SysRole role = seedRole(now);
        seedPermissions(now);
        bindUserRole(user, role, now);
        bindRolePermissions(role, now);
    }

    /** 初始化或更新管理员账号 */
    private SysUser seedUser(LocalDateTime now) {
        SysUser user = sysUserRepository.findByUsername("admin").orElseGet(SysUser::new);
        if (user.getId() == null) {
            user.setUsername("admin");
            user.setRealName("系统管理员");
            user.setPassword(passwordEncoder.encode(initialAdminPassword));
            user.setCreateTime(now);
        } else if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(initialAdminPassword));
        }
        user.setStatus(1);
        user.setUpdateTime(now);
        return sysUserRepository.save(user);
    }

    /** 初始化超级管理员角色 */
    private SysRole seedRole(LocalDateTime now) {
        SysRole role = sysRoleRepository.findByRoleCode("SUPER_ADMIN").orElseGet(SysRole::new);
        if (role.getId() == null) {
            role.setRoleCode("SUPER_ADMIN");
            role.setRoleName("超级管理员");
            role.setDescription("拥有后台所有权限");
            role.setCreateTime(now);
        }
        role.setStatus(1);
        role.setUpdateTime(now);
        return sysRoleRepository.save(role);
    }

    /** 初始化权限列表（仅首次启动时执行） */
    private void seedPermissions(LocalDateTime now) {
        if (sysPermissionRepository.count() > 0) {
            return;
        }
        sysPermissionRepository.saveAll(List.of(
                permission("dashboard:view", "查看仪表盘", "MENU", "/api/admin/dashboard", "GET", 10, now),
                permission("company:update", "维护平台信息", "API", "/api/admin/company", "PUT", 20, now),
                permission("banner:update", "维护推荐位", "API", "/api/admin/banners", "PUT", 30, now),
                permission("news:update", "维护校园贴士", "API", "/api/admin/news", "PUT", 40, now),
                permission("product:update", "维护闲置物品", "API", "/api/admin/products", "PUT", 50, now),
                permission("order:view", "查看订单", "API", "/api/admin/orders", "GET", 60, now),
                permission("return:handle", "处理交易协商", "API", "/api/admin/returns", "PUT", 70, now),
                permission("message:handle", "处理咨询留言", "API", "/api/admin/messages", "PUT", 80, now),
                permission("rbac:update", "维护权限配置", "API", "/api/admin/rbac", "PUT", 90, now)
        ));
    }

    /** 绑定用户与角色的关联关系 */
    private void bindUserRole(SysUser user, SysRole role, LocalDateTime now) {
        if (sysUserRoleRepository.findByUserId(user.getId()).isEmpty()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setCreateTime(now);
            sysUserRoleRepository.save(userRole);
        }
    }

    /** 绑定角色与权限的关联关系（超级管理员拥有所有权限） */
    private void bindRolePermissions(SysRole role, LocalDateTime now) {
        if (!sysRolePermissionRepository.findByRoleIdIn(List.of(role.getId())).isEmpty()) {
            return;
        }
        for (SysPermission permission : sysPermissionRepository.findAll()) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setCreateTime(now);
            sysRolePermissionRepository.save(rolePermission);
        }
    }

    /** 构建权限实体 */
    private SysPermission permission(String code, String name, String type, String path, String method, int sort, LocalDateTime now) {
        SysPermission permission = new SysPermission();
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        permission.setPermissionType(type);
        permission.setPath(path);
        permission.setMethod(method);
        permission.setSort(sort);
        permission.setStatus(1);
        permission.setCreateTime(now);
        permission.setUpdateTime(now);
        return permission;
    }
}
