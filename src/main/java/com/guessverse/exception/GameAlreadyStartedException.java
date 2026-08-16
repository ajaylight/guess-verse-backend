package com.guessverse.exception;

public class GameAlreadyStartedException extends RuntimeException {

    public GameAlreadyStartedException() {
        super("Game has already started");
    }

}