package com.guessverse.arena.logo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class LetterGenerator {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int LETTER_BANK_SIZE = 13;

    private static final Random RANDOM =
            new Random();

    private LetterGenerator() {
        // Utility class
    }

    public static List<String> generate(
            String answer
    ) {

        if (answer == null) {
            throw new IllegalArgumentException(
                    "Answer cannot be null."
            );
        }

        String normalizedAnswer =
                answer
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z]",
                                ""
                        );

        if (normalizedAnswer.isEmpty()) {
            throw new IllegalArgumentException(
                    "Answer cannot be empty."
            );
        }

        /*
         * The answer itself must always be completely
         * represented inside the bank.
         */
        List<String> letters =
                new ArrayList<>();

        for (
                char character :
                normalizedAnswer.toCharArray()
        ) {
            letters.add(
                    String.valueOf(character)
            );
        }

        /*
         * If an answer itself is longer than 13,
         * don't silently remove answer letters.
         */
        if (
                letters.size() >
                        LETTER_BANK_SIZE
        ) {
            throw new IllegalArgumentException(
                    "Answer is longer than "
                            + LETTER_BANK_SIZE
                            + " letters: "
                            + answer
            );
        }

        /*
         * Fill the remaining slots with random
         * alphabet letters.
         */
        while (
                letters.size() <
                        LETTER_BANK_SIZE
        ) {
            char randomCharacter =
                    ALPHABET.charAt(
                            RANDOM.nextInt(
                                    ALPHABET.length()
                            )
                    );

            letters.add(
                    String.valueOf(
                            randomCharacter
                    )
            );
        }

        /*
         * Shuffle the complete bank.
         */
        Collections.shuffle(
                letters,
                RANDOM
        );

        return letters;
    }
}