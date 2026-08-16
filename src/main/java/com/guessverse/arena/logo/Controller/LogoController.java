package com.guessverse.arena.logo.controller;

import com.guessverse.arena.logo.dto.*;
import com.guessverse.arena.logo.service.LogoGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@RestController
@RequestMapping("/api/logo")
@RequiredArgsConstructor


public class LogoController {

    private final LogoGameService service;

    @GetMapping("/start")
    public StartGameResponse startGame() {
        return service.startGame();
    }
    @GetMapping("/{sessionId}/question")
    public ResponseEntity<LogoQuestionResponse> getQuestion(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.getCurrentQuestion(sessionId)
        );
    }
    @PostMapping("/{sessionId}/guess")
    public ResponseEntity<GuessResponse> submitGuess(
            @PathVariable UUID sessionId,
            @Valid @RequestBody GuessRequest request) {

        return ResponseEntity.ok(
                service.submitGuess(sessionId, request)
        );
    }
    @PostMapping("/{sessionId}/hint/info")
    public ResponseEntity<HintResponse> revealInfo(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.revealInfo(sessionId)
        );
    }
    @PostMapping("/{sessionId}/hint/letter")
    public ResponseEntity<HintResponse> revealLetter(
            @PathVariable UUID sessionId,
            @RequestBody RevealLetterRequest request) {

        return ResponseEntity.ok(
                service.revealLetter(sessionId, request)
        );
    }
    @PostMapping("/{sessionId}/hint/answer")
    public ResponseEntity<HintResponse> revealAnswer(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.revealAnswer(sessionId)
        );
    }
    @PostMapping("/{sessionId}/continue")
    public ResponseEntity<LogoQuestionResponse> continueGame(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.continueGame(sessionId)
        );
    }
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<LogoGameResultResponse> getResult(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.getResult(sessionId)
        );
    }
    @PostMapping("/{sessionId}/replay")
    public ResponseEntity<LogoQuestionResponse> replayLevel(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.replayLevel(sessionId)
        );
    }
    @GetMapping("/levels")
    public ResponseEntity<LogoLevelProgressResponse> getLevelProgress() {

        return ResponseEntity.ok(
                service.getLevelProgress()
        );
    }
    @PostMapping("/start/level/{level}")
    public ResponseEntity<StartGameResponse> startGameAtLevel(
            @PathVariable int level) {

        return ResponseEntity.ok(
                service.startGameAtLevel(level)
        );
    }
}