package com.guessverse.arena.flag.repository;

import com.guessverse.arena.flag.entity.FlagPlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlagPlayerProgressRepository
        extends JpaRepository<FlagPlayerProgress, Long> {

    Optional<FlagPlayerProgress> findByUserId(Long userId);
}
