package com.guessverse.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String displayName;
    private Integer level;
    private Integer xp;
    private Integer coins;
}