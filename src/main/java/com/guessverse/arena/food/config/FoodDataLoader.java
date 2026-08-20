package com.guessverse.arena.food.config;

import com.guessverse.arena.food.entity.FoodCategory;
import com.guessverse.arena.food.entity.FoodQuestion;
import com.guessverse.arena.food.repository.FoodQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodDataLoader implements CommandLineRunner {

    private final FoodQuestionRepository foodQuestionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (foodQuestionRepository.countByCategory(
                FoodCategory.INDIAN
        ) == 0) {
            loadIndianFood();
        }

        if (foodQuestionRepository.countByCategory(
                FoodCategory.INTERNATIONAL
        ) == 0) {
            loadInternationalFood();
        }
    }

    private void loadIndianFood() throws Exception {

        Path path = Path.of(
                "data",
                "guessverse_indian_food.csv"
        );

        if (!Files.exists(path)) {
            System.out.println(
                    "Indian food dataset not found: " + path
            );
            return;
        }

        List<String> lines =
                Files.readAllLines(path);

        List<FoodQuestion> questions =
                new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",", 2);

            if (parts.length < 2) {
                continue;
            }

            String food =
                    parts[1].trim();

            if (food.isEmpty()) {
                continue;
            }

            if (foodQuestionRepository
                    .findByFoodIgnoreCase(food)
                    .isPresent()) {
                continue;
            }

            questions.add(
                    FoodQuestion.builder()
                            .food(food)
                            .category(
                                    FoodCategory.INDIAN
                            )
                            .level(
                                    calculateLevel(
                                            questions.size() + 1
                                    )
                            )
                            .active(true)
                            .build()
            );
        }

        if (!questions.isEmpty()) {
            foodQuestionRepository.saveAll(
                    questions
            );
        }

        System.out.println(
                "Indian food loaded: "
                        + questions.size()
        );
    }

    private int calculateLevel(int index) {

        if (index <= 10) {
            return 1;
        }

        return Math.min(
                20,
                ((index - 1) / 10) + 1
        );
    }
    private void loadInternationalFood() {

        Path path = Path.of(
                "data",
                "guessverse_international_food.csv"
        );

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "International food dataset not found: "
                            + path.toAbsolutePath()
            );
        }

        try {
            List<String> lines =
                    Files.readAllLines(path);

            List<FoodQuestion> foods =
                    new ArrayList<>();

            int dataIndex = 0;

            for (String line : lines) {

                if (line == null ||
                        line.isBlank()) {
                    continue;
                }

                // Skip CSV header
                if (line.trim().equalsIgnoreCase(
                        "index,food,images"
                )) {
                    continue;
                }

                String[] parts =
                        line.split(",", 3);

                if (parts.length < 2) {
                    continue;
                }

                String food =
                        parts[1].trim();

                if (food.isBlank()) {
                    continue;
                }

                dataIndex++;

                /*
                 * 10 questions = 1 level.
                 *
                 * 361 records gives:
                 * 36 complete levels
                 * + 1 leftover record
                 *
                 * The final incomplete record is ignored.
                 */
                int level =
                        ((dataIndex - 1) / 10) + 1;

                if (level > 36) {
                    break;
                }

                foods.add(
                        FoodQuestion.builder()
                                .food(food)
                                .category(
                                        FoodCategory.INTERNATIONAL
                                )
                                .imageName(null)
                                .level(level)
                                .active(true)
                                .build()
                );
            }

            if (foods.size() < 360) {
                throw new IllegalStateException(
                        "International dataset must contain "
                                + "at least 360 valid foods. Found: "
                                + foods.size()
                );
            }

            foodQuestionRepository.saveAll(
                    foods
            );

            System.out.println(
                    "Loaded "
                            + foods.size()
                            + " international foods."
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to load International food dataset.",
                    e
            );
        }
    }
}