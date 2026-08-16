package com.guessverse.game.entity;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.game.enums.GameState;
import com.guessverse.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 8)
    private String roomCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Enumerated(EnumType.STRING)
    @Column(name = "arena_type", nullable = false)
    private ArenaType arenaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    @Builder.Default
    @Column(name = "current_players", nullable = false)
    private Integer currentPlayers = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "game_state", nullable = false)
    private GameState gameState = GameState.WAITING;

    @Builder.Default
    @Column(nullable = false)
    private Boolean finished = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}