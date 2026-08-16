package com.guessverse.arena.logo.repository;

import com.guessverse.arena.logo.entity.LogoPlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogoPlayerProgressRepository
        extends JpaRepository<LogoPlayerProgress, Long> {

    Optional<LogoPlayerProgress> findByUserId(Long userId);
}