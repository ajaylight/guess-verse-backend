package com.guessverse.arena.flag.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class LetterGenerator {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int MIN_LETTER_BANK_SIZE = 13;

    private static final Random RANDOM =
            new Random();

    private LetterGenerator() {
    }

    public static List<String> generate(String answer) {

        if (answer == null) {
            throw new IllegalArgumentException(
                    "Answer cannot be null."
            );
        }

        String normalizedAnswer =
                answer
                        .toUpperCase()
                        .replaceAll("[^A-Z]", "");

        if (normalizedAnswer.isEmpty()) {
            throw new IllegalArgumentException(
                    "Answer cannot be empty."
            );
        }

        List<String> letters =
                new ArrayList<>();

        /*
         * Every answer letter must exist in the bank.
         */
        for (char character : normalizedAnswer.toCharArray()) {

            letters.add(
                    String.valueOf(character)
            );
        }

        /*
         * 13 is the minimum bank size,
         * not the maximum answer length.
         */
        int targetSize =
                Math.max(
                        MIN_LETTER_BANK_SIZE,
                        letters.size()
                );

        /*
         * Fill remaining slots with random letters.
         */
        while (letters.size() < targetSize) {

            char randomCharacter =
                    ALPHABET.charAt(
                            RANDOM.nextInt(
                                    ALPHABET.length()
                            )
                    );

            letters.add(
                    String.valueOf(randomCharacter)
            );
        }

        Collections.shuffle(
                letters,
                RANDOM
        );

        return letters;
    }
}