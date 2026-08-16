package com.guessverse.arena.flag.repository;

import com.guessverse.arena.flag.entity.FlagQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlagQuestionRepository
        extends JpaRepository<FlagQuestion, Long> {

    // -----------------------------------------
    // Any active question
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM flag_questions
            WHERE active = true
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<FlagQuestion> findRandomQuestion();


    // -----------------------------------------
    // Question by difficulty
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM flag_questions
            WHERE active = true
              AND difficulty = :difficulty
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<FlagQuestion> findRandomQuestionByDifficulty(
            @Param("difficulty") String difficulty
    );


    // -----------------------------------------
    // Unused question
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM flag_questions
            WHERE active = true
              AND id NOT IN (:ids)
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<FlagQuestion> findRandomUnused(
            @Param("ids") List<Long> ids
    );


    // -----------------------------------------
    // Unused question by difficulty
    // -----------------------------------------

    @Query(value = """
            SELECT *
            FROM flag_questions
            WHERE active = true
              AND difficulty = :difficulty
              AND id NOT IN (:ids)
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<FlagQuestion> findRandomUnusedByDifficulty(
            @Param("difficulty") String difficulty,
            @Param("ids") List<Long> ids
    );

    @Query("""
        SELECT q
        FROM FlagQuestion q
        WHERE q.active = true
          AND q.level = :level
          AND q.id NOT IN :ids
        ORDER BY function('RANDOM')
        """)
    List<FlagQuestion> findRandomUnusedByLevel(
            @Param("level") Integer level,
            @Param("ids") List<Long> ids
    );

    @Query("""
        SELECT q
        FROM FlagQuestion q
        WHERE q.active = true
          AND q.level = :level
        ORDER BY function('RANDOM')
        """)
    List<FlagQuestion> findRandomByLevel(
            @Param("level") Integer level
    );
    Optional<FlagQuestion> findByImageName(String imageName);

}


