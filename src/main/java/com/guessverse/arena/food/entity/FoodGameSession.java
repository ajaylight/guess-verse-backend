package com.guessverse.arena.food.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "food_game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodGameSession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "current_question", nullable = false)
    private Integer currentQuestion;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @Column(name = "current_question_started_at")
    private LocalDateTime currentQuestionStartedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "current_question_id")
    private Long currentQuestionId;
}