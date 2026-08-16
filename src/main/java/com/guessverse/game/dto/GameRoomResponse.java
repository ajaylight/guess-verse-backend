package com.guessverse.game.dto;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.game.enums.GameState;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameRoomResponse {

    private Long id;

    private String roomCode;

    private ArenaType arenaType;

    private Difficulty difficulty;

    private GameState gameState;

    private Integer maxPlayers;

    private Integer currentPlayers;

    private String hostUsername;

    private List<String> players;
}