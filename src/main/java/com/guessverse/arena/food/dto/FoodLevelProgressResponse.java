package com.guessverse.arena.food.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodLevelProgressResponse {

    private int highestUnlockedLevel;
}