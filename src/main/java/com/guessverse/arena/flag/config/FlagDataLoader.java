package com.guessverse.arena.flag.config;

import com.guessverse.arena.flag.entity.FlagQuestion;
import com.guessverse.arena.flag.repository.FlagQuestionRepository;
import com.guessverse.game.enums.Difficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class FlagDataLoader implements CommandLineRunner {

    private final FlagQuestionRepository repository;

    private Map<String, String> loadFlagInfo() throws Exception {

        Map<String, String> infoMap =
                new HashMap<>();

        InputStream input =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "data/flags/flag-info.csv"
                        );

        if (input == null) {
            throw new IllegalStateException(
                    "Flag info manifest not found."
            );
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     input,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns =
                        line.split(",", 2);

                if (columns.length < 2) {
                    continue;
                }

                String country =
                        columns[0]
                                .trim()
                                .replaceAll("^\"|\"$", "");

                String info =
                        columns[1].trim();

                if (info.startsWith("\"") &&
                        info.endsWith("\"")) {

                    info = info.substring(
                            1,
                            info.length() - 1
                    );
                }

                infoMap.put(
                        country,
                        info
                );
            }
        }

        return infoMap;
    }

    @Override
    public void run(String... args) throws Exception {


        Map<String, String> infoMap =
                loadFlagInfo();

        InputStream input =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "data/flags/flag-level-manifest.csv"
                        );

        if (input == null) {
            throw new IllegalStateException(
                    "Flag manifest not found."
            );
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     input,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            // Skip CSV header.
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns =
                        line.split(",", 8);

                if (columns.length < 6) {
                    continue;
                }

                int level =
                        Integer.parseInt(columns[0]);

                String country =
                        columns[2];

                Difficulty difficulty =
                        Difficulty.valueOf(columns[4]);

                String imageName =
                        columns[5];

                String info =
                        infoMap.get(country);

                if (info != null &&
                        info.length() >= 2 &&
                        info.startsWith("\"") &&
                        info.endsWith("\"")) {

                    info = info.substring(
                            1,
                            info.length() - 1
                    );
                }

                FlagQuestion question =
                        repository
                                .findByImageName(imageName)
                                .orElseGet(FlagQuestion::new);

                question.setImageName(imageName);
                question.setAnswer(country);
                question.setDifficulty(difficulty);
                question.setLevel(level);
                question.setActive(true);
                question.setInfo(info);

                repository.save(question);
            }
        }
    }
}