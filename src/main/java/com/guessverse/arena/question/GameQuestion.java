package com.guessverse.arena.question;

import com.guessverse.game.enums.ArenaType;
import com.guessverse.game.enums.Difficulty;

import java.util.UUID;

public interface GameQuestion {

    UUID getId();

    String getAnswer();

    Difficulty getDifficulty();

    ArenaType getArena();
}