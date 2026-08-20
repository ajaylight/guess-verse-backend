package com.guessverse.arena.food.repository;

import com.guessverse.arena.food.entity.FoodCategory;
import com.guessverse.arena.food.entity.FoodQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodQuestionRepository
        extends JpaRepository<FoodQuestion, Long> {

    long countByCategory(FoodCategory category);

    @Query("""
        SELECT q
        FROM FoodQuestion q
        WHERE q.active = true
          AND q.category = :category
          AND q.level = :level
        ORDER BY function('RANDOM')
        """)
    List<FoodQuestion> findRandomByCategoryAndLevel(
            @Param("category") FoodCategory category,
            @Param("level") Integer level
    );

    @Query("""
    SELECT f
    FROM FoodQuestion f
    WHERE f.category = :category
      AND f.level = :level
      AND f.active = true
      AND f.id NOT IN :usedIds
    ORDER BY RANDOM()
""")
    List<FoodQuestion> findRandomUnusedByCategoryAndLevel(
            @Param("category") FoodCategory category,
            @Param("level") int level,
            @Param("usedIds") List<Long> usedIds
    );




    Optional<FoodQuestion> findByFoodIgnoreCase(String food);
}