package com.guessverse.game.repository;

import com.guessverse.game.entity.GameRoom;
import com.guessverse.game.entity.GameRoomPlayer;
import com.guessverse.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRoomPlayerRepository extends JpaRepository<GameRoomPlayer, Long> {

    List<GameRoomPlayer> findByGameRoom(GameRoom gameRoom);

    Optional<GameRoomPlayer> findByGameRoomAndUser(GameRoom gameRoom, User user);

    Optional<GameRoomPlayer> findFirstByGameRoom(GameRoom gameRoom);

    long countByGameRoom(GameRoom gameRoom);

    boolean existsByGameRoomAndUser(GameRoom gameRoom, User user);

    void deleteByGameRoomAndUser(GameRoom gameRoom, User user);
}