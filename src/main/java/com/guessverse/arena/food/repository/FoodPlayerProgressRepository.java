package com.guessverse.arena.food.repository;

import com.guessverse.arena.food.entity.FoodCategory;
import com.guessverse.arena.food.entity.FoodPlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodPlayerProgressRepository
        extends JpaRepository<FoodPlayerProgress, Long> {

    Optional<FoodPlayerProgress>
    findByPlayerIdAndCategory(
            String playerId,
            FoodCategory category
    );
}