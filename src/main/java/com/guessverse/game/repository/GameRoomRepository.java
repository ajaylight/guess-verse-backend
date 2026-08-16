package com.guessverse.game.repository;

import com.guessverse.game.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {



    boolean existsByRoomCode(String roomCode);

    Optional<GameRoom> findByRoomCode(String roomCode);
}