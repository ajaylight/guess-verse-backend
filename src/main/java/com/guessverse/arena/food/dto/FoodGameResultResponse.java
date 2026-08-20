package com.guessverse.arena.food.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodGameResultResponse {

    private int totalScore;

    private int maximumScore;

    private int correctAnswers;

    private int totalQuestions;

    private double accuracy;

    private boolean completed;
}