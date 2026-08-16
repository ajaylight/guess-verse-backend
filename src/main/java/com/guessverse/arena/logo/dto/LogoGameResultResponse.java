package com.guessverse.arena.logo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoGameResultResponse {

    private int totalScore;
    private int maximumScore;

    private int correctAnswers;
    private int totalQuestions;

    private double accuracy;

    private int hintsUsed;
    private int infoHintsUsed;
    private int lettersRevealed;
    private int answersRevealed;

    private boolean completed;
}