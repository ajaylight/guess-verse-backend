package com.guessverse.arena.flag.repository;

import com.guessverse.arena.flag.entity.FlagGameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FlagGameSessionRepository extends JpaRepository<FlagGameSession, UUID> {
}
