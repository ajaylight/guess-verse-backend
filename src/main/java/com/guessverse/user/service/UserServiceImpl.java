package com.guessverse.user.service;

import com.guessverse.user.dto.UserProfileResponse;
import com.guessverse.user.entity.User;
import com.guessverse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .level(user.getLevel())
                .xp(user.getXp())
                .coins(user.getCoins())
                .isOnline(user.getIsOnline())
                .build();
    }

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .level(user.getLevel())
                .xp(user.getXp())
                .coins(user.getCoins())
                .isOnline(user.getIsOnline())
                .build();
    }
}