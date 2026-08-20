package com.guessverse.arena.food.service;

import com.guessverse.arena.food.dto.FoodAnswerOption;
import com.guessverse.arena.food.dto.FoodGameResultResponse;
import com.guessverse.arena.food.dto.FoodLevelProgressResponse;
import com.guessverse.arena.food.dto.FoodQuestionDto;
import com.guessverse.arena.food.entity.*;
import com.guessverse.arena.food.repository.FoodGameSessionRepository;
import com.guessverse.arena.food.repository.FoodGameUsedQuestionRepository;
import com.guessverse.arena.food.repository.FoodPlayerProgressRepository;
import com.guessverse.arena.food.repository.FoodQuestionRepository;
import com.guessverse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodGameServiceImpl
        implements FoodGameService {

    private static final int TOTAL_QUESTIONS = 10;
    private static final int QUESTION_TIME_SECONDS = 15;
    private static final int MAX_SCORE = 100;
    private static final int MIN_SCORE = 20;

    private final FoodQuestionRepository foodQuestionRepository;
    private final FoodGameSessionRepository sessionRepository;
    private final FoodGameUsedQuestionRepository usedQuestionRepository;
    private final FoodPlayerProgressRepository playerProgressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UUID startGame(
            FoodCategory category,
            int level
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Food category is required."
            );
        }

        if (level < 1) {
            throw new IllegalArgumentException(
                    "Level must be at least 1."
            );
        }
        int highestUnlocked =
                getHighestUnlockedLevel(category);

        if (level > highestUnlocked) {
            throw new IllegalStateException(
                    "Level "
                            + level
                            + " is locked. Highest unlocked level: "
                            + highestUnlocked
            );
        }

        List<FoodQuestion> available =
                foodQuestionRepository
                        .findRandomByCategoryAndLevel(
                                category,
                                level
                        );

        if (available.size() < TOTAL_QUESTIONS) {
            throw new IllegalStateException(
                    "Level "
                            + level
                            + " is not available for "
                            + category
                            + "."
            );
        }

        UUID sessionId =
                UUID.randomUUID();

        FoodGameSession session =
                FoodGameSession.builder()
                        .id(sessionId)
                        .category(category)
                        .level(level)
                        .currentQuestion(1)
                        .totalScore(0)
                        .correctAnswers(0)
                        .completed(false)
                        .currentQuestionStartedAt(null)
                        .currentQuestionId(null)
                        .createdAt(LocalDateTime.now())
                        .build();

        sessionRepository.save(session);

        return sessionId;
    }

    @Override
    @Transactional
    public FoodQuestionDto getQuestion(
            UUID sessionId
    ) {

        FoodGameSession session =
                getSession(sessionId);

        if (Boolean.TRUE.equals(
                session.getCompleted()
        )) {
            throw new IllegalStateException(
                    "Game already completed."
            );
        }

        /*
         * If a question is currently active, return the
         * existing question instead of generating another one.
         *
         * This is important because the timer starts when
         * the question is created. Calling GET /question
         * repeatedly must NOT reset the timer.
         */
        if (
                session.getCurrentQuestionId() != null
                        && session.getCurrentQuestionStartedAt() != null
        ) {

            FoodQuestion currentQuestion =
                    foodQuestionRepository
                            .findById(
                                    session.getCurrentQuestionId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Current food question not found."
                                    )
                            );

            return buildQuestion(
                    currentQuestion,
                    session
            );
        }

        /*
         * If a question has already been answered,
         * the client must use /continue instead of
         * calling /question directly.
         */
        if (
                session.getCurrentQuestionId() != null
                        && session.getCurrentQuestionStartedAt() == null
        ) {

            throw new IllegalStateException(
                    "Current question has already been answered. "
                            + "Call /continue to get the next question."
            );
        }

        return createNextQuestion(session);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodLevelProgressResponse getLevelProgress(
            FoodCategory category
    ) {
        int highestUnlockedLevel =
                getHighestUnlockedLevel(category);

        return FoodLevelProgressResponse.builder()
                .highestUnlockedLevel(
                        highestUnlockedLevel
                )
                .build();
    }

    @Override
    @Transactional
    public FoodAnswerResult submitAnswer(
            UUID sessionId,
            Long questionId,
            String answer
    ) {

        FoodGameSession session =
                getSession(sessionId);

        if (Boolean.TRUE.equals(
                session.getCompleted()
        )) {
            throw new IllegalStateException(
                    "Game already completed."
            );
        }

        if (questionId == null) {
            throw new IllegalArgumentException(
                    "Question ID is required."
            );
        }

        /*
         * The current question ID is authoritative.
         * A client cannot submit an older question.
         */
        if (
                session.getCurrentQuestionId() == null
                        || !session.getCurrentQuestionId()
                        .equals(questionId)
        ) {

            throw new IllegalArgumentException(
                    "This is not the current question."
            );
        }

        /*
         * A null start timestamp means the current question
         * has already been answered.
         *
         * This prevents duplicate answer submissions.
         */
        if (session.getCurrentQuestionStartedAt() == null) {

            throw new IllegalStateException(
                    "This question has already been answered."
            );
        }

        FoodQuestion question =
                foodQuestionRepository
                        .findById(questionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Question not found."
                                )
                        );

        LocalDateTime started =
                session.getCurrentQuestionStartedAt();

        long elapsedSeconds =
                Duration.between(
                        started,
                        LocalDateTime.now()
                ).getSeconds();

        boolean timedOut =
                elapsedSeconds >= QUESTION_TIME_SECONDS;

        boolean correct =
                !timedOut
                        && question.getFood()
                        .equalsIgnoreCase(
                                answer == null
                                        ? ""
                                        : answer.trim()
                        );

        int score;

        if (correct) {
            score = calculateScore(elapsedSeconds);
        } else {
            score = MIN_SCORE;
        }



        session.setTotalScore(
                session.getTotalScore() + score
        );

        if (correct) {
            session.setCorrectAnswers(
                    session.getCorrectAnswers() + 1
            );
        }

        boolean completed =
                session.getCurrentQuestion()
                        >= TOTAL_QUESTIONS;

        /*
         * Mark the current question as answered.
         *
         * We intentionally keep currentQuestionId so that
         * the session still knows which question was answered.
         *
         * The null timestamp is our server-side ANSWERED state.
         */
        session.setCurrentQuestionStartedAt(null);

        if (completed) {

            session.setCompleted(true);

            session.setCompletedAt(
                    LocalDateTime.now()
            );

            unlockNextLevel(
                    session.getCategory(),
                    session.getLevel()
            );

        } else {

            session.setCurrentQuestion(
                    session.getCurrentQuestion() + 1
            );
        }

        sessionRepository.save(session);

        return FoodAnswerResult.builder()
                .correct(correct)
                .score(score)
                .totalScore(
                        session.getTotalScore()
                )
                .gameCompleted(completed)
                .correctAnswer(
                        question.getFood()
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public int getHighestUnlockedLevel(
            FoodCategory category
    ) {
        String playerId = getCurrentPlayerId();

        // Guests always start from Level 1.
        if (playerId == null) {
            return 1;
        }

        return playerProgressRepository
                .findByPlayerIdAndCategory(
                        playerId,
                        category
                )
                .map(
                        FoodPlayerProgress::getHighestUnlockedLevel
                )
                .orElse(1);
    }



    private int calculateScore(long elapsedSeconds) {

        long safeElapsed =
                Math.max(
                        0,
                        Math.min(
                                elapsedSeconds,
                                QUESTION_TIME_SECONDS
                        )
                );

        double ratio =
                (double) safeElapsed
                        / QUESTION_TIME_SECONDS;

        return Math.max(
                MIN_SCORE,
                (int) Math.round(
                        MAX_SCORE
                                - (
                                ratio
                                        * (
                                        MAX_SCORE
                                                - MIN_SCORE
                                )
                        )
                )
        );
    }

    @Override
    @Transactional
    public FoodQuestionDto replayLevel(
            UUID sessionId
    ) {

        FoodGameSession session =
                getSession(sessionId);

        usedQuestionRepository.deleteBySessionId(
                sessionId
        );

        session.setCurrentQuestion(1);
        session.setTotalScore(0);
        session.setCorrectAnswers(0);
        session.setCompleted(false);
        session.setCompletedAt(null);

        /*
         * Clear both current-question fields before
         * generating the first question of the replay.
         */
        session.setCurrentQuestionId(null);
        session.setCurrentQuestionStartedAt(null);

        sessionRepository.save(session);

        return createNextQuestion(session);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodGameResultResponse getResult(
            UUID sessionId
    ) {

        FoodGameSession session =
                getSession(sessionId);

        int totalQuestions =
                TOTAL_QUESTIONS;

        double accuracy =
                totalQuestions == 0
                        ? 0.0
                        : (
                        (double)
                                session.getCorrectAnswers()
                                / totalQuestions
                ) * 100.0;

        return FoodGameResultResponse.builder()
                .totalScore(
                        session.getTotalScore()
                )
                .maximumScore(
                        TOTAL_QUESTIONS * MAX_SCORE
                )
                .correctAnswers(
                        session.getCorrectAnswers()
                )
                .totalQuestions(
                        totalQuestions
                )
                .accuracy(accuracy)
                .completed(
                        Boolean.TRUE.equals(
                                session.getCompleted()
                        )
                )
                .build();
    }

    @Override
    @Transactional
    public FoodQuestionDto continueGame(
            UUID sessionId
    ) {

        FoodGameSession session =
                getSession(sessionId);

        if (Boolean.TRUE.equals(
                session.getCompleted()
        )) {
            throw new IllegalStateException(
                    "Game already completed."
            );
        }

        if (
                session.getCurrentQuestion()
                        > TOTAL_QUESTIONS
        ) {
            throw new IllegalStateException(
                    "No more questions available."
            );
        }

        /*
         * Continue is only valid after the previous
         * question has been answered.
         */
        if (
                session.getCurrentQuestionId() == null
                        || session.getCurrentQuestionStartedAt() != null
        ) {

            throw new IllegalStateException(
                    "Current question must be answered before continuing."
            );
        }

        return createNextQuestion(session);
    }

    private FoodQuestionDto createNextQuestion(
            FoodGameSession session
    ) {

        List<Long> usedIds =
                usedQuestionRepository
                        .findBySessionId(
                                session.getId()
                        )
                        .stream()
                        .map(
                                FoodGameUsedQuestion::getQuestionId
                        )
                        .toList();

        FoodQuestion question =
                findQuestion(
                        session.getCategory(),
                        session.getLevel(),
                        usedIds
                );

        /*
         * Store this question as used.
         */
        usedQuestionRepository.save(
                FoodGameUsedQuestion.builder()
                        .sessionId(session.getId())
                        .questionId(question.getId())
                        .build()
        );

        /*
         * Start the authoritative server timer NOW.
         */
        session.setCurrentQuestionId(
                question.getId()
        );

        session.setCurrentQuestionStartedAt(
                LocalDateTime.now()
        );

        sessionRepository.save(session);

        return buildQuestion(
                question,
                session
        );
    }

    private FoodQuestion findQuestion(
            FoodCategory category,
            int level,
            List<Long> usedIds
    ) {

        List<FoodQuestion> questions;

        if (usedIds.isEmpty()) {

            questions =
                    foodQuestionRepository
                            .findRandomByCategoryAndLevel(
                                    category,
                                    level
                            );

        } else {

            questions =
                    foodQuestionRepository
                            .findRandomUnusedByCategoryAndLevel(
                                    category,
                                    level,
                                    usedIds
                            );
        }

        if (questions.isEmpty()) {

            throw new IllegalStateException(
                    "No unused food questions available for "
                            + category
                            + " level "
                            + level
            );
        }

        return questions.get(0);
    }

    private FoodGameSession getSession(
            UUID sessionId
    ) {

        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Game session not found."
                        )
                );
    }

    private FoodQuestionDto buildQuestion(
            FoodQuestion question,
            FoodGameSession session
    ) {

        String correctFamily =
                getFoodFamily(question.getFood());

        /*
         * First try to get distractors from DIFFERENT food families.
         *
         * Example:
         *
         * Hyderabadi Biryani
         *     ↓
         * avoid Biryani variants
         *
         * Instead use:
         * Butter Chicken
         * Paneer Tikka
         * Chole Bhature
         */
        List<FoodQuestion> candidates =
                foodQuestionRepository
                        .findRandomByCategoryAndLevel(
                                session.getCategory(),
                                session.getLevel()
                        )
                        .stream()
                        .filter(
                                q ->
                                        !q.getId()
                                                .equals(
                                                        question.getId()
                                                )
                        )
                        .filter(
                                q ->
                                        !q.getFood()
                                                .equalsIgnoreCase(
                                                        question.getFood()
                                                )
                        )
                        .toList();

        List<FoodQuestion> distractors =
                new ArrayList<>();

        /*
         * Prefer one distractor from each different family.
         */
        for (FoodQuestion candidate : candidates) {

            if (
                    distractors.size() >= 3
            ) {
                break;
            }

            String candidateFamily =
                    getFoodFamily(
                            candidate.getFood()
                    );

            boolean familyAlreadyUsed =
                    candidateFamily.equals(
                            correctFamily
                    );

            for (
                    FoodQuestion selected :
                    distractors
            ) {

                if (
                        getFoodFamily(
                                selected.getFood()
                        ).equals(
                                candidateFamily
                        )
                ) {
                    familyAlreadyUsed = true;
                    break;
                }
            }

            if (!familyAlreadyUsed) {
                distractors.add(candidate);
            }
        }

        /*
         * Fallback:
         * If there aren't enough different families,
         * fill remaining slots with other foods.
         */
        if (distractors.size() < 3) {

            for (FoodQuestion candidate : candidates) {

                if (
                        distractors.size() >= 3
                ) {
                    break;
                }

                boolean alreadySelected =
                        distractors.stream()
                                .anyMatch(
                                        q ->
                                                q.getId()
                                                        .equals(
                                                                candidate.getId()
                                                        )
                                );

                if (!alreadySelected) {
                    distractors.add(candidate);
                }
            }
        }

        if (distractors.size() < 3) {

            throw new IllegalStateException(
                    "Not enough food choices for "
                            + session.getCategory()
                            + " level "
                            + session.getLevel()
            );
        }

        List<FoodAnswerOption> options =
                new ArrayList<>();

        /*
         * Correct answer.
         */
        options.add(
                FoodAnswerOption.builder()
                        .id(
                                question.getId()
                                        .toString()
                        )
                        .text(
                                question.getFood()
                        )
                        .build()
        );

        /*
         * Three distractors.
         */
        for (
                FoodQuestion distractor :
                distractors
        ) {

            options.add(
                    FoodAnswerOption.builder()
                            .id(
                                    distractor.getId()
                                            .toString()
                            )
                            .text(
                                    distractor.getFood()
                            )
                            .build()
            );
        }

        Collections.shuffle(options);

        return FoodQuestionDto.builder()
                .questionId(
                        question.getId()
                )
                .imageId(
                        question.getImageName()
                )
                .options(options)
                .questionNumber(
                        session.getCurrentQuestion()
                )
                .totalQuestions(
                        TOTAL_QUESTIONS
                )
                .level(
                        session.getLevel()
                )
                .build();
    }


    private int calculateLevel(int index) {
        return ((index - 1) / 10) + 1;
    }


    private String getFoodFamily(
            String food
    ) {

        String name =
                food
                        .toLowerCase()
                        .trim();

        /*
         * BIRYANI
         */
        if (name.contains("biryani")) {
            return "biryani";
        }

        /*
         * DOSA
         */
        if (name.contains("dosa")) {
            return "dosa";
        }

        /*
         * PARATHA
         */
        if (name.contains("paratha")) {
            return "paratha";
        }

        /*
         * NAAN / ROTI / KULCHA
         */
        if (
                name.contains("naan")
                        || name.contains("roti")
                        || name.contains("kulcha")
        ) {
            return "bread";
        }

        /*
         * PANEER
         */
        if (name.contains("paneer")) {
            return "paneer";
        }

        /*
         * CHICKEN
         */
        if (name.contains("chicken")) {
            return "chicken";
        }

        /*
         * MUTTON / GOAT
         */
        if (
                name.contains("mutton")
                        || name.contains("goat")
                        || name.contains("rogan josh")
                        || name.contains("laal maas")
                        || name.contains("kosha mangsho")
        ) {
            return "mutton";
        }

        /*
         * FISH
         */
        if (
                name.contains("fish")
                        || name.contains("ilish")
                        || name.contains("macher")
        ) {
            return "fish";
        }

        /*
         * PRAWN
         */
        if (
                name.contains("prawn")
                        || name.contains("shrimp")
        ) {
            return "prawn";
        }

        /*
         * RICE DISHES
         */
        if (
                name.contains("rice")
                        || name.contains("pulao")
                        || name.contains("pongal")
                        || name.contains("khichdi")
                        || name.contains("khichdi")
        ) {
            return "rice";
        }

        /*
         * DAL
         */
        if (
                name.startsWith("dal ")
                        || name.equals("dal")
                        || name.contains("dal makhani")
                        || name.contains("dal tadka")
                        || name.contains("dal fry")
                        || name.contains("dal bukhara")
                        || name.contains("dal baati")
                        || name.contains("dal dhokli")
        ) {
            return "dal";
        }

        /*
         * CHAAT / STREET CHAAT
         */
        if (
                name.contains("chaat")
                        || name.contains("puri")
                        || name.contains("pattice")
                        || name.contains("pani puri")
        ) {
            return "chaat";
        }

        /*
         * FRIED SNACKS
         */
        if (
                name.contains("pakora")
                        || name.contains("pakoda")
                        || name.contains("samosa")
                        || name.contains("kachori")
                        || name.contains("vada")
                        || name.contains("bhaji")
        ) {
            return "snack";
        }

        /*
         * NOODLES / INDO-CHINESE
         */
        if (
                name.contains("noodle")
                        || name.contains("chow mein")
                        || name.contains("manchurian")
                        || name.contains("spring roll")
                        || name.contains("chilli")
        ) {
            return "indo-chinese";
        }

        /*
         * BURGER
         */
        if (name.contains("burger")) {
            return "burger";
        }

        /*
         * SANDWICH
         */
        if (name.contains("sandwich")) {
            return "sandwich";
        }

        /*
         * PIZZA
         */
        if (name.contains("pizza")) {
            return "pizza";
        }

        /*
         * PASTA
         */
        if (name.contains("pasta")) {
            return "pasta";
        }

        /*
         * SWEETS
         */
        if (
                name.contains("halwa")
                        || name.contains("ladoo")
                        || name.contains("laddu")
                        || name.contains("barfi")
                        || name.contains("burfi")
                        || name.contains("jamun")
                        || name.contains("jalebi")
                        || name.contains("rasgulla")
                        || name.contains("rasmalai")
                        || name.contains("peda")
                        || name.contains("sandesh")
                        || name.contains("kalakand")
                        || name.contains("ghevar")
                        || name.contains("balushahi")
                        || name.contains("imarti")
                        || name.contains("petha")
                        || name.contains("gujia")
                        || name.contains("anarsa")
                        || name.contains("rajbhog")
                        || name.contains("malai sandwich")
                        || name.contains("kheer")
                        || name.contains("payasam")
                        || name.contains("phirni")
                        || name.contains("kulfi")
                        || name.contains("rabri")
                        || name.contains("shrikhand")
                        || name.contains("basundi")
                        || name.contains("mysore pak")
                        || name.contains("soan papdi")
                        || name.contains("cham cham")
                        || name.contains("sutarfeni")
        ) {
            return "sweet";
        }

        /*
         * Generic fallback.
         */
        return name;
    }

    private String getCurrentPlayerId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        System.out.println("========== FOOD AUTH DEBUG ==========");
        System.out.println("Authentication = " + authentication);
        System.out.println(
                "Principal = " +
                        (authentication == null
                                ? null
                                : authentication.getPrincipal())
        );
        System.out.println(
                "Name = " +
                        (authentication == null
                                ? null
                                : authentication.getName())
        );
        System.out.println(
                "Authenticated = " +
                        (authentication != null &&
                                authentication.isAuthenticated())
        );
        System.out.println("=====================================");

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            return null;
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .map(user -> String.valueOf(user.getId()))
                .orElse(null);
    }

    private void unlockNextLevel(
            FoodCategory category,
            int completedLevel
    ) {

        String playerId =
                getCurrentPlayerId();

        if (playerId == null) {
            return;
        }

        int maxLevel =
                category == FoodCategory.INDIAN
                        ? 27
                        : 36;

        if (completedLevel >= maxLevel) {
            return;
        }

        FoodPlayerProgress progress =
                playerProgressRepository
                        .findByPlayerIdAndCategory(
                                playerId,
                                category
                        )
                        .orElseGet(() ->
                                FoodPlayerProgress.builder()
                                        .playerId(playerId)
                                        .category(category)
                                        .highestUnlockedLevel(1)
                                        .updatedAt(
                                                LocalDateTime.now()
                                        )
                                        .build()
                        );

        int currentHighest =
                progress.getHighestUnlockedLevel() == null
                        ? 1
                        : progress.getHighestUnlockedLevel();

        int nextLevel =
                completedLevel + 1;

        if (nextLevel > currentHighest) {

            progress.setHighestUnlockedLevel(
                    nextLevel
            );

            progress.setUpdatedAt(
                    LocalDateTime.now()
            );

            playerProgressRepository.save(
                    progress
            );
        }
    }
}