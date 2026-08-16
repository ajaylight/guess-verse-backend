package com.guessverse.arena.flag.service;

import com.guessverse.arena.flag.dto.*;

import java.util.UUID;
import java.util.UUID;
import com.guessverse.arena.flag.dto.HintResponse;
import com.guessverse.arena.flag.dto.RevealLetterRequest;

public interface FlagGameService {

    StartGameResponse startGame();
    GuessResponse submitGuess(UUID sessionId, GuessRequest request);
    FlagQuestionResponse getCurrentQuestion(UUID sessionId);
    HintResponse revealInfo(UUID sessionId);

    HintResponse revealLetter(UUID sessionId, RevealLetterRequest request);

    HintResponse revealAnswer(UUID sessionId);

    FlagQuestionResponse continueGame(UUID sessionId);

    FlagQuestionResponse replayLevel(UUID sessionId);

    FlagGameResultResponse getResult(UUID sessionId);

    FlagLevelProgressResponse getLevelProgress();

    StartGameResponse startGameAtLevel(int level);


}
