package com.guessverse.game.util;

import java.security.SecureRandom;

public class RoomCodeGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(
                    CHARACTERS.charAt(
                            RANDOM.nextInt(CHARACTERS.length())
                    )
            );
        }

        return sb.toString();
    }
}