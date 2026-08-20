package com.guessverse.arena.food.service;

import com.guessverse.arena.food.dto.FoodQuestionDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodAnswerResult {

    private boolean correct;

    private int score;

    private int totalScore;

    private boolean gameCompleted;

    private FoodQuestionDto nextQuestion;

    private String correctAnswer;
}