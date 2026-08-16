package com.guessverse.auth.controller;

import com.guessverse.auth.dto.LoginRequest;
import com.guessverse.auth.dto.LoginResponse;
import com.guessverse.auth.dto.RegisterRequest;
import com.guessverse.auth.dto.RegisterResponse;
import com.guessverse.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService AuthService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthService.login(request);
    }
    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return AuthService.register(request);
    }
    @GetMapping("/test")
    public String test() {
        return "Auth controller working";
    }

}