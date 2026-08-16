package com.guessverse.game.dto;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.game.enums.GameState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRoomResponse {

    private Long id;
    private String roomCode;
    private String host;
    private ArenaType arenaType;
    private Difficulty difficulty;
    private Integer currentPlayers;
    private GameState gameState;
}