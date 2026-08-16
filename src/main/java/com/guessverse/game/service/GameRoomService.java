package com.guessverse.game.service;

import com.guessverse.exception.BadRequestException;
import com.guessverse.exception.ResourceNotFoundException;
import com.guessverse.exception.UnauthorizedActionException;
import com.guessverse.game.dto.*;
import com.guessverse.game.entity.GameRoom;
import com.guessverse.game.entity.GameRoomPlayer;
import com.guessverse.game.enums.GameState;
import com.guessverse.game.repository.GameRoomPlayerRepository;
import com.guessverse.game.repository.GameRoomRepository;
import com.guessverse.game.util.RoomCodeGenerator;
import com.guessverse.user.entity.User;
import com.guessverse.user.repository.UserRepository;
import com.guessverse.websocket.RoomMessage;
import com.guessverse.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;
    private final GameRoomPlayerRepository gameRoomPlayerRepository;
    private final WebSocketEventPublisher eventPublisher;

    public GameRoomResponse createRoom(CreateRoomRequest request) {

        User host = getCurrentUser();

        String roomCode;

        do {
            roomCode = RoomCodeGenerator.generate();
        } while (gameRoomRepository.existsByRoomCode(roomCode));

        GameRoom room = GameRoom.builder()
                .roomCode(roomCode)
                .host(host)
                .arenaType(request.getArenaType())
                .difficulty(request.getDifficulty())
                .maxPlayers(request.getMaxPlayers())
                .currentPlayers(1)
                .gameState(GameState.WAITING)
                .finished(false)
                .build();

        room = gameRoomRepository.save(room);

        GameRoomPlayer hostPlayer = GameRoomPlayer.builder()
                .gameRoom(room)
                .user(host)
                .build();

        gameRoomPlayerRepository.save(hostPlayer);

        return GameRoomResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .hostUsername(host.getUsername())
                .arenaType(room.getArenaType())
                .difficulty(room.getDifficulty())
                .gameState(room.getGameState())
                .maxPlayers(room.getMaxPlayers())
                .currentPlayers(room.getCurrentPlayers())
                .players(List.of(host.getUsername()))
                .build();
    }

    public JoinRoomResponse joinRoom(String roomCode) {

        User user = getCurrentUser();

        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (room.getCurrentPlayers() >= room.getMaxPlayers()) {
            throw new BadRequestException("Room is full");
        }

        if (room.getGameState() == GameState.IN_PROGRESS) {
            throw new BadRequestException("Game already started");
        }

        if (gameRoomPlayerRepository.findByGameRoomAndUser(room, user).isPresent()) {
            throw new BadRequestException("You are already in this room");
        }

        GameRoomPlayer player = GameRoomPlayer.builder()
                .gameRoom(room)
                .user(user)
                .build();

        gameRoomPlayerRepository.save(player);

        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        gameRoomRepository.save(room);

        eventPublisher.publish(
                room.getRoomCode(),
                RoomMessage.builder()
                        .type("PLAYER_JOINED")
                        .payload(
                                PlayerResponse.builder()
                                        .username(user.getUsername())
                                        .displayName(user.getDisplayName())
                                        .build()
                        )
                        .build()
        );

        return JoinRoomResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .host(room.getHost().getUsername())
                .arenaType(room.getArenaType())
                .difficulty(room.getDifficulty())
                .currentPlayers(room.getCurrentPlayers())
                .gameState(room.getGameState())
                .build();


    }


    public void leaveRoom(String roomCode) {

        User user = getCurrentUser();

        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        GameRoomPlayer player = gameRoomPlayerRepository
                .findByGameRoomAndUser(room, user)
                .orElseThrow(() -> new BadRequestException("You are not in this room"));

        gameRoomPlayerRepository.delete(player);

        room.setCurrentPlayers(room.getCurrentPlayers() - 1);

        if (room.getCurrentPlayers() == 0) {
            gameRoomRepository.delete(room);
            return;
        }

        if (room.getHost().getId().equals(user.getId())) {

            GameRoomPlayer newHost = gameRoomPlayerRepository
                    .findFirstByGameRoom(room)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No players available to become host"));

            room.setHost(newHost.getUser());
            eventPublisher.publish(
                    room.getRoomCode(),
                    RoomMessage.builder()
                            .type("HOST_CHANGED")
                            .payload(
                                    PlayerResponse.builder()
                                            .username(newHost.getUser().getUsername())
                                            .displayName(newHost.getUser().getDisplayName())
                                            .build()
                            )
                            .build()
            );
        }

        gameRoomRepository.save(room);
    }

    public void startGame(String roomCode) {

        User user = getCurrentUser();

        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHost().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("Only the host can start the game");
        }

        if (room.getGameState() == GameState.IN_PROGRESS) {
            throw new BadRequestException("Game already started");
        }

        if (room.getCurrentPlayers() < 2) {
            throw new BadRequestException("At least 2 players are required");
        }

        room.setGameState(GameState.IN_PROGRESS);
        gameRoomRepository.save(room);

        eventPublisher.publish(
                room.getRoomCode(),
                RoomMessage.builder()
                        .type("PLAYER_LEFT")
                        .payload(
                                PlayerResponse.builder()
                                        .username(user.getUsername())
                                        .displayName(user.getDisplayName())
                                        .build()
                        )
                        .build()
        );
        eventPublisher.publish(
                room.getRoomCode(),
                RoomMessage.builder()
                        .type("GAME_STARTED")
                        .payload(roomCode)
                        .build()
        );
    }

    public RoomDetailsResponse getRoom(String roomCode) {

        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        List<GameRoomPlayer> roomPlayers =
                gameRoomPlayerRepository.findByGameRoom(room);

        List<PlayerResponse> players = roomPlayers.stream()
                .map(player -> PlayerResponse.builder()
                        .username(player.getUser().getUsername())
                        .displayName(player.getUser().getDisplayName())
                        .build())
                .toList();

        return RoomDetailsResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .host(room.getHost().getUsername())
                .arenaType(room.getArenaType())
                .difficulty(room.getDifficulty())
                .currentPlayers(room.getCurrentPlayers())
                .maxPlayers(room.getMaxPlayers())
                .gameState(room.getGameState())
                .players(players)
                .build();
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}