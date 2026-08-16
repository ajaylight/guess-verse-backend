package com.guessverse.arena.flag.dto;

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

    private QuestionRevealResponse reveal;
}
