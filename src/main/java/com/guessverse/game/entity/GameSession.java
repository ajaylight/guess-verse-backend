package com.guessverse.game.entity;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.game.enums.GameState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID roomId;

    @Enumerated(EnumType.STRING)
    private ArenaType arena;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private GameState state;

    private Integer totalQuestions;

    private Integer currentQuestion;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

}