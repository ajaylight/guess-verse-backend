package com.guessverse.arena.flag.controller;

import com.guessverse.arena.flag.dto.*;
import com.guessverse.arena.flag.service.FlagGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/flag")
@RequiredArgsConstructor
public class FlagController {

    private final FlagGameService service;

    @GetMapping("/start")
    public StartGameResponse startGame() {
        return service.startGame();
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<FlagQuestionResponse> getQuestion(
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
    public ResponseEntity<FlagQuestionResponse> continueGame(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.continueGame(sessionId)
        );
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<FlagGameResultResponse> getResult(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.getResult(sessionId)
        );
    }

    @PostMapping("/{sessionId}/replay")
    public ResponseEntity<FlagQuestionResponse> replayLevel(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
                service.replayLevel(sessionId)
        );
    }

    @GetMapping("/levels")
    public ResponseEntity<FlagLevelProgressResponse> getLevelProgress() {

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