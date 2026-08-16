package com.guessverse.game.entity;

import com.guessverse.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "game_room_players",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"game_room_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoomPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private Integer score = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean eliminated = false;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}