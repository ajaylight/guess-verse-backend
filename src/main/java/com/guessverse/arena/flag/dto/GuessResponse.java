package com.guessverse.arena.flag.dto;

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
    private FlagQuestionDto nextQuestion;

    private QuestionRevealResponse reveal;
}
