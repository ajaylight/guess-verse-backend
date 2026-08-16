package com.guessverse.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerResponse {

    private String username;
    private String displayName;
}