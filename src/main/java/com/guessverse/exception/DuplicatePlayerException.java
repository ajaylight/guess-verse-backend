package com.guessverse.exception;

public class DuplicatePlayerException extends RuntimeException {

    public DuplicatePlayerException() {
        super("You are already in this room");
    }

}