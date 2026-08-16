package com.guessverse.game.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {

    @NotBlank
    private String roomCode;
}