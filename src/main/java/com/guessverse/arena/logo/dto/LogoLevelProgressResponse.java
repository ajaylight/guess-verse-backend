package com.guessverse.arena.logo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoLevelProgressResponse {

    private int highestUnlockedLevel;
}