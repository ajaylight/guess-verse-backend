package com.guessverse.arena.flag.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlagLevelProgressResponse {

    private int highestUnlockedLevel;
}
