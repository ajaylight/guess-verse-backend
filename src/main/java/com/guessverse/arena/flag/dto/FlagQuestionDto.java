package com.guessverse.arena.flag.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlagQuestionDto {

    private Long questionId;

    private String imageId;

    private int answerLength;

    private List<String> letters;

    private String difficulty;

    private List<Integer> spacePositions;
}
