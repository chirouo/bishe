package com.qdx.bishe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.JwtUtils;
import com.qdx.bishe.dto.LoginRequest;
import com.qdx.bishe.dto.LoginResponse;
import com.qdx.bishe.dto.UserProfileDto;
import com.qdx.bishe.entity.User;
import com.qdx.bishe.mapper.UserMapper;
import com.qdx.bishe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("当前账号已被禁用");
        }

        return LoginResponse.builder()
                .token(jwtUtils.generateToken(user.getId(), user.getRole()))
                .userInfo(toUserProfile(user))
                .build();
    }

    @Override
    public UserProfileDto getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserProfile(user);
    }

    private UserProfileDto toUserProfile(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build();
    }
}

