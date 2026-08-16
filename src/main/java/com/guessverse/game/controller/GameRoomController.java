package com.guessverse.game.controller;

import com.guessverse.game.dto.CreateRoomRequest;
import com.guessverse.game.dto.GameRoomResponse;
import com.guessverse.game.service.GameRoomService;
import com.guessverse.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game-rooms")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/create")
    public GameRoomResponse createRoom(
            @Valid @RequestBody CreateRoomRequest request
    ) {
        return gameRoomService.createRoom(request);
    }

}