package com.qdx.bishe.service;

import com.qdx.bishe.dto.LoginRequest;
import com.qdx.bishe.dto.LoginResponse;
import com.qdx.bishe.dto.UserProfileDto;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserProfileDto getCurrentUser(Long userId);
}

