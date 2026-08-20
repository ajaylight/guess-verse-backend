package com.guessverse.arena.food.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_player_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_food_player_progress",
                        columnNames = {
                                "player_id",
                                "category"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodPlayerProgress {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "player_id",
            nullable = false,
            length = 255
    )
    private String playerId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private FoodCategory category;

    @Column(
            name = "highest_unlocked_level",
            nullable = false
    )
    private Integer highestUnlockedLevel;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}