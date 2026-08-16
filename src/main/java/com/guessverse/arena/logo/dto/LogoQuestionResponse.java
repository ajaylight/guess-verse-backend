package com.guessverse.arena.logo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LogoQuestionResponse {

    private Long questionId;

    private String imageId;

    private int answerLength;

    private List<String> letters;

    private int score;

    private int questionNumber;

    private Integer level;
}