package com.guessverse.user.service;

import com.guessverse.user.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUser();

    UserProfileResponse getProfile(String name);
}