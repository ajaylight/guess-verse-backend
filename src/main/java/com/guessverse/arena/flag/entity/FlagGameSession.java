package com.guessverse.arena.flag.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "flag_game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagGameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long questionId;

    @Builder.Default
    private Integer totalScore = 0;

    @Builder.Default
    private Integer currentQuestionReward = 100;

    @Builder.Default
    private Integer hintsUsed = 0;

    @Builder.Default
    private Integer attempts = 0;

    @Builder.Default
    private Integer currentQuestion = 1;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Integer correctAnswers = 0;

    @Builder.Default
    private Integer totalQuestions = 10;

    @Builder.Default
    private Boolean completed = false;

    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Builder.Default
    private Integer currentQuestionLetterHints = 0;

    @Builder.Default
    private Boolean infoUsed = false;

    @Builder.Default
    private Boolean answerRevealed = false;

    private String revealedPositions;

    @Builder.Default
    private Boolean awaitingContinue = false;

    @Builder.Default
    private Integer totalInfoHints = 0;

    @Builder.Default
    private Integer totalLettersRevealed = 0;

    @Builder.Default
    private Integer totalAnswersRevealed = 0;

    private String currentLetterBank;
}