package com.guessverse.arena.food.repository;

import com.guessverse.arena.food.entity.FoodGameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodGameSessionRepository
        extends JpaRepository<FoodGameSession, UUID> {
}