package com.guessverse.arena.logo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuessResponse {

    private boolean correct;

    private int score;

    private String message;

    private boolean gameCompleted;

    // NEW
    private LogoQuestionDto nextQuestion;

    private QuestionRevealResponse reveal;
}