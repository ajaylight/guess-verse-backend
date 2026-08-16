package com.guessverse.arena.logo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRevealResponse {

    private String answer;
    private String imageId;
    private String info;

    private int questionReward;
    private int totalScore;
    private int questionNumber;

    private boolean correct;
    private boolean answerRevealed;
    private boolean gameCompleted;
}