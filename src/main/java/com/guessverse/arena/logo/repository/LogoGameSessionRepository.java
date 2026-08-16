package com.guessverse.arena.logo.repository;

import com.guessverse.arena.logo.entity.LogoGameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogoGameSessionRepository extends JpaRepository<LogoGameSession, UUID> {
}
