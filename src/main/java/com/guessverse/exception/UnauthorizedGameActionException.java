package com.guessverse.exception;

public class UnauthorizedGameActionException extends RuntimeException {

    public UnauthorizedGameActionException(String message) {
        super(message);
    }

}