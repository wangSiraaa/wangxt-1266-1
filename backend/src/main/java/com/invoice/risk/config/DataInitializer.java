package com.invoice.risk.config;

import com.invoice.risk.entity.SysUser;
import com.invoice.risk.enums.RoleEnum;
import com.invoice.risk.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfNotExists("tax01", "税务专员01", RoleEnum.TAX_SPECIALIST, "13800000001", "tax01@company.com");
        createUserIfNotExists("proc01", "采购负责人01", RoleEnum.PROCUREMENT_HEAD, "13800000002", "proc01@company.com");
        createUserIfNotExists("fin01", "财务经理01", RoleEnum.FINANCE_MANAGER, "13800000003", "fin01@company.com");
        log.info("初始化用户数据完成");
    }

    private void createUserIfNotExists(String username, String realName, RoleEnum role, String phone, String email) {
        if (!sysUserRepository.existsByUsername(username)) {
            SysUser user = SysUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode("123456"))
                    .realName(realName)
                    .role(role)
                    .phone(phone)
                    .email(email)
                    .enabled(true)
                    .build();
            sysUserRepository.save(user);
            log.info("创建默认用户: {} / 123456, 角色: {}", username, role.getDescription());
        }
    }
}
