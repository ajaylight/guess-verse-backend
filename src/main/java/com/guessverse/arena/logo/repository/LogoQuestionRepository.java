package com.guessverse.arena.logo.repository;

import com.guessverse.arena.logo.entity.LogoQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LogoQuestionRepository
        extends JpaRepository<LogoQuestion, Long> {

    // -----------------------------------------
    // Any active question
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM logo_questions
            WHERE active = true
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<LogoQuestion> findRandomQuestion();


    // -----------------------------------------
    // Question by difficulty
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM logo_questions
            WHERE active = true
              AND difficulty = :difficulty
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<LogoQuestion> findRandomQuestionByDifficulty(
            @Param("difficulty") String difficulty
    );


    // -----------------------------------------
    // Unused question
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM logo_questions
            WHERE active = true
              AND id NOT IN (:ids)
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<LogoQuestion> findRandomUnused(
            @Param("ids") List<Long> ids
    );


    // -----------------------------------------
    // Unused question by difficulty
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM logo_questions
            WHERE active = true
              AND difficulty = :difficulty
              AND id NOT IN (:ids)
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<LogoQuestion> findRandomUnusedByDifficulty(
            @Param("difficulty") String difficulty,
            @Param("ids") List<Long> ids
    );
}