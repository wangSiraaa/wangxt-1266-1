package com.invoice.risk.service;

import com.invoice.risk.dto.auth.LoginRequest;
import com.invoice.risk.dto.auth.LoginResponse;
import com.invoice.risk.entity.SysUser;
import com.invoice.risk.exception.BusinessException;
import com.invoice.risk.repository.SysUserRepository;
import com.invoice.risk.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!user.getEnabled()) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name(), user.getRealName());

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole().name())
                .roleDescription(user.getRole().getDescription())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userInfo(userInfo)
                .build();
    }
}
