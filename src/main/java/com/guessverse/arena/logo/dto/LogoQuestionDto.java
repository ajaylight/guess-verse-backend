package com.guessverse.arena.logo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LogoQuestionDto {

    private Long questionId;

    private String imageId;

    private int answerLength;

    private List<String> letters;

    private String difficulty;
}