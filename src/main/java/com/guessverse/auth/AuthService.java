package com.guessverse.auth.service;

import com.guessverse.auth.dto.LoginRequest;
import com.guessverse.auth.dto.LoginResponse;
import com.guessverse.auth.dto.RegisterRequest;
import com.guessverse.auth.dto.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}