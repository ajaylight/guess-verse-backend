package com.guessverse.arena.flag.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FlagAnswerRules {

    private FlagAnswerRules() {
    }

    public record AnswerRule(
            String displayAnswer,
            String initiallyRevealed
    ) {
    }

    private static final Map<String, AnswerRule> RULES = createRules();

    private static Map<String, AnswerRule> createRules() {

        Map<String, AnswerRule> rules = new HashMap<>();

        rules.put(
                "United Arab Emirates",
                new AnswerRule("UAE", "")
        );

        rules.put(
                "Cote d'Ivoire",
                new AnswerRule("COTE D'IVOIRE", "D'IVOIRE")
        );

        rules.put(
                "Antigua and Barbuda",
                new AnswerRule("ANTIGUA AND BARBUDA", "AND")
        );

        rules.put(
                "Bosnia and Herzegovina",
                new AnswerRule("BOSNIA", "")
        );

        rules.put(
                "Central African Republic",
                new AnswerRule("CAR", "")
        );

        rules.put(
                "Democratic Republic of the Congo",
                new AnswerRule("CONGO", "")
        );

        rules.put(
                "Equatorial Guinea",
                new AnswerRule("EQUATORIAL GUINEA", "GUINEA")
        );

        rules.put(
                "Guinea-Bissau",
                new AnswerRule("GUINEA BISSAU", "GUINEA")
        );

        rules.put(
                "Saint Kitts and Nevis",
                new AnswerRule(
                        "SAINT KITTS AND NEVIS",
                        "AND NEVIS"
                )
        );

        rules.put(
                "Saint Vincent and the Grenadines",
                new AnswerRule(
                        "SAINT VINCENT AND THE GRENADINES",
                        "AND THE GRENADINES"
                )
        );

        rules.put(
                "Sao Tome and Principe",
                new AnswerRule(
                        "SAO TOME AND PRINCIPE",
                        "AND PRINCIPE"
                )
        );

        rules.put(
                "Timor Leste",
                new AnswerRule("TIMOR LESTE", "")
        );

        return Collections.unmodifiableMap(rules);
    }

    public static AnswerRule getRule(String canonicalAnswer) {

        return RULES.getOrDefault(
                canonicalAnswer,
                new AnswerRule(
                        canonicalAnswer.toUpperCase(),
                        ""
                )
        );
    }
}