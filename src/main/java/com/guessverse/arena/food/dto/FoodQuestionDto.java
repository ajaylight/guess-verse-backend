package com.guessverse.arena.food.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodQuestionDto {

    private Long questionId;

    private String imageId;

    private List<FoodAnswerOption> options;

    private Integer questionNumber;

    private Integer totalQuestions;

    private Integer level;
}