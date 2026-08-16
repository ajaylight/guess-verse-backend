package com.guessverse.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String profilePictureUrl;

    private Integer level;
    private Integer xp;
    private Integer coins;

    private Boolean isOnline;
}