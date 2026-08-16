package com.guessverse.auth.service;

import com.guessverse.auth.dto.LoginRequest;
import com.guessverse.auth.dto.LoginResponse;
import com.guessverse.auth.dto.RegisterRequest;
import com.guessverse.auth.dto.RegisterResponse;
import com.guessverse.security.JwtService;
import com.guessverse.user.entity.User;
import com.guessverse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.guessverse.auth.service.AuthService;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getEmail());

        return new LoginResponse(
                token,
                "Bearer"
        );
    }
    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .level(1)
                .xp(0)
                .coins(0)
                .isActive(true)
                .isOnline(false)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new RegisterResponse(token, "Bearer");
    }

}