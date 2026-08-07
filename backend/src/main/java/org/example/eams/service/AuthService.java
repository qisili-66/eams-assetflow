package org.example.eams.service;

import org.example.eams.dto.auth.LoginRequest;
import org.example.eams.vo.CurrentUserVo;
import org.example.eams.vo.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    CurrentUserVo getCurrentUser(String username);
}
