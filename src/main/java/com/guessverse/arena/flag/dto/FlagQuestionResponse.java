package com.guessverse.arena.flag.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlagQuestionResponse {

    private Long questionId;

    private String imageId;

    private int answerLength;

    private List<String> letters;

    private int score;

    private int questionNumber;

    private Integer level;

    private String revealedText;

    private List<Integer> spacePositions;
}
