package com.guessverse.exception;

public class UserNotInRoomException extends RuntimeException {

    public UserNotInRoomException() {
        super("You are not in this room");
    }

}