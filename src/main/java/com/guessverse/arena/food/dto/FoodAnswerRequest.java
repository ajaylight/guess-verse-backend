package com.guessverse.arena.food.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodAnswerRequest {

    private Long questionId;

    private String answer;
}