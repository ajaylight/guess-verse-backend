package com.guessverse.arena.logo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HintResponse {

    private int score;

    private String info;

    private Integer revealedPosition;

    private Character revealedLetter;

    private Integer lettersRevealed;

    private boolean answerRevealed;

    private String answer;

    private String imageId;

    private Integer totalScore;

    private Integer questionNumber;

    private Boolean correct;

    private Boolean gameCompleted;

    private QuestionRevealResponse reveal;
}