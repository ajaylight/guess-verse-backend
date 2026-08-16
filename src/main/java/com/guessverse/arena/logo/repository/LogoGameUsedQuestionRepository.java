package com.guessverse.arena.logo.repository;

import com.guessverse.arena.logo.entity.LogoGameUsedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LogoGameUsedQuestionRepository
        extends JpaRepository<LogoGameUsedQuestion, UUID> {

    List<LogoGameUsedQuestion> findBySessionId(UUID sessionId);

    long countBySessionId(UUID sessionId);

    void deleteBySessionId(UUID sessionId);
}