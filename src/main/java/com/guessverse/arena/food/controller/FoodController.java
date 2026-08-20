package com.guessverse.arena.food.controller;

import com.guessverse.arena.food.dto.FoodAnswerRequest;
import com.guessverse.arena.food.dto.FoodGameResultResponse;
import com.guessverse.arena.food.dto.FoodLevelProgressResponse;
import com.guessverse.arena.food.dto.FoodQuestionDto;
import com.guessverse.arena.food.entity.FoodCategory;
import com.guessverse.arena.food.service.FoodAnswerResult;
import com.guessverse.arena.food.service.FoodGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodGameService foodGameService;

    @PostMapping("/start/{category}/{level}")
    public UUID startGame(
            @PathVariable FoodCategory category,
            @PathVariable int level
    ) {
        return foodGameService.startGame(
                category,
                level
        );
    }

    @GetMapping("/{sessionId}/question")
    public FoodQuestionDto getQuestion(
            @PathVariable UUID sessionId
    ) {
        return foodGameService.getQuestion(
                sessionId
        );
    }


    @PostMapping("/{sessionId}/answer")
    public FoodAnswerResult submitAnswer(
            @PathVariable UUID sessionId,
            @RequestBody FoodAnswerRequest request
    ) {
        return foodGameService.submitAnswer(
                sessionId,
                request.getQuestionId(),
                request.getAnswer()
        );
    }

    @PostMapping("/{sessionId}/continue")
    public FoodQuestionDto continueGame(
            @PathVariable UUID sessionId
    ) {
        return foodGameService.continueGame(sessionId);
    }

    @GetMapping("/{sessionId}/result")
    public FoodGameResultResponse getResult(
            @PathVariable UUID sessionId
    ) {
        return foodGameService.getResult(sessionId);
    }

    @PostMapping("/{sessionId}/replay")
    public FoodQuestionDto replayLevel(
            @PathVariable UUID sessionId
    ) {
        return foodGameService.replayLevel(sessionId);
    }

    @GetMapping("/levels/{category}")
    public FoodLevelProgressResponse getLevelProgress(
            @PathVariable FoodCategory category
    ) {
        return FoodLevelProgressResponse.builder()
                .highestUnlockedLevel(
                        foodGameService
                                .getHighestUnlockedLevel(category)
                )
                .build();
    }
}