package com.guessverse.arena.logo.service;

import com.guessverse.arena.logo.dto.*;

import java.util.UUID;
import java.util.UUID;
import com.guessverse.arena.logo.dto.HintResponse;
import com.guessverse.arena.logo.dto.RevealLetterRequest;

public interface LogoGameService {

    StartGameResponse startGame();
    GuessResponse submitGuess(UUID sessionId, GuessRequest request);
    LogoQuestionResponse getCurrentQuestion(UUID sessionId);
    HintResponse revealInfo(UUID sessionId);

    HintResponse revealLetter(UUID sessionId, RevealLetterRequest request);

    HintResponse revealAnswer(UUID sessionId);

    LogoQuestionResponse continueGame(UUID sessionId);

    LogoQuestionResponse replayLevel(UUID sessionId);

    LogoGameResultResponse getResult(UUID sessionId);

    LogoLevelProgressResponse getLevelProgress();

    StartGameResponse startGameAtLevel(int level);


}
