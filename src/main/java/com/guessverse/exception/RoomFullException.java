package com.guessverse.exception;

public class RoomFullException extends RuntimeException {

    public RoomFullException() {
        super("Room is full");
    }

}