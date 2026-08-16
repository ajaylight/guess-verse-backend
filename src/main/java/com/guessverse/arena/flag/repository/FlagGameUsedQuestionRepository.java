package com.guessverse.arena.flag.repository;

import com.guessverse.arena.flag.entity.FlagGameUsedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FlagGameUsedQuestionRepository
        extends JpaRepository<FlagGameUsedQuestion, UUID> {

    List<FlagGameUsedQuestion> findBySessionId(UUID sessionId);

    long countBySessionId(UUID sessionId);

    void deleteBySessionId(UUID sessionId);
}
