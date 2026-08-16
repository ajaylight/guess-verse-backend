package com.guessverse.game.dto;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.game.enums.GameState;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomDetailsResponse {

    private Long id;

    private String roomCode;

    private String host;

    private ArenaType arenaType;
    private Difficulty difficulty;

    private Integer currentPlayers;

    private Integer maxPlayers;

    private GameState gameState;

    private List<PlayerResponse> players;
}