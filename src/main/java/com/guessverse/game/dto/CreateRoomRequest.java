package com.guessverse.game.dto;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoomRequest {

    @NotNull
    private ArenaType arenaType;

    @NotNull
    private Difficulty difficulty;

    @Min(2)
    @Max(8)
    private Integer maxPlayers = 2;
}