package com.guessverse.arena.food.repository;

import com.guessverse.arena.food.entity.FoodGameUsedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FoodGameUsedQuestionRepository
        extends JpaRepository<FoodGameUsedQuestion, Long> {

    List<FoodGameUsedQuestion>
    findBySessionId(UUID sessionId);

    boolean existsBySessionIdAndQuestionId(
            UUID sessionId,
            Long questionId
    );

    void deleteBySessionId(UUID sessionId);
}