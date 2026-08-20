package com.guessverse.arena.food.service;

import com.guessverse.arena.food.dto.FoodGameResultResponse;
import com.guessverse.arena.food.dto.FoodLevelProgressResponse;
import com.guessverse.arena.food.dto.FoodQuestionDto;
import com.guessverse.arena.food.entity.FoodCategory;

import java.util.UUID;

public interface FoodGameService {

    UUID startGame(
            FoodCategory category,
            int level
    );

    FoodQuestionDto getQuestion(
            UUID sessionId
    );

    FoodAnswerResult submitAnswer(
            UUID sessionId,
            Long questionId,
            String answer
    );

    FoodQuestionDto continueGame(
            UUID sessionId
    );

    FoodGameResultResponse getResult(
            UUID sessionId
    );

    FoodQuestionDto replayLevel(
            UUID sessionId
    );

    int getHighestUnlockedLevel(
            FoodCategory category
    );

    FoodLevelProgressResponse getLevelProgress(
            FoodCategory category
    );
}