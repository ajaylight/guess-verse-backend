package com.guessverse.arena.logo.service;

import com.guessverse.arena.logo.dto.*;
import com.guessverse.arena.logo.entity.LogoGameSession;
import com.guessverse.arena.logo.entity.LogoGameUsedQuestion;
import com.guessverse.arena.logo.entity.LogoPlayerProgress;
import com.guessverse.arena.logo.entity.LogoQuestion;
import com.guessverse.arena.logo.repository.LogoGameSessionRepository;
import com.guessverse.arena.logo.repository.LogoGameUsedQuestionRepository;
import com.guessverse.arena.logo.repository.LogoPlayerProgressRepository;
import com.guessverse.arena.logo.repository.LogoQuestionRepository;
import com.guessverse.arena.logo.util.LetterGenerator;
import com.guessverse.game.enums.Difficulty;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.guessverse.user.entity.User;
import com.guessverse.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogoGameServiceImpl implements LogoGameService {

    private final LogoQuestionRepository questionRepository;
    private final LogoGameSessionRepository sessionRepository;
    private final LogoGameUsedQuestionRepository usedQuestionRepository;
    private final UserRepository userRepository;
    private final LogoPlayerProgressRepository logoPlayerProgressRepository;
    private final Random random = new Random();

    // =========================================================
    // START GAME
    // =========================================================

    @Override
    public StartGameResponse startGame() {

        LogoQuestion question =
                findQuestionForLevel(1, List.of());

        List<String> letterBank =
                LetterGenerator.generate(question.getAnswer());

        LogoGameSession session =
                sessionRepository.save(
                        LogoGameSession.builder()
                                .questionId(question.getId())

                                .totalScore(0)
                                .currentQuestionReward(100)

                                .attempts(0)
                                .hintsUsed(0)

                                .currentQuestion(1)
                                .level(1)
                                .correctAnswers(0)
                                .totalQuestions(10)

                                .currentQuestionLetterHints(0)
                                .infoUsed(false)
                                .answerRevealed(false)
                                .revealedPositions(null)

                                .totalInfoHints(0)
                                .totalLettersRevealed(0)
                                .totalAnswersRevealed(0)

                                .currentLetterBank(
                                        serializeLetterBank(letterBank)
                                )

                                .awaitingContinue(false)
                                .completed(false)
                                .build()
                );

        markQuestionUsed(
                session.getId(),
                question.getId()
        );

        LogoQuestionDto dto =
                LogoQuestionDto.builder()
                        .questionId(question.getId())
                        .imageId("logo-" + question.getId())
                        .answerLength(
                                normalizeAnswer(
                                        question.getAnswer()
                                ).length()
                        )
                        .difficulty(
                                question.getDifficulty().name()
                        )
                        .letters(letterBank)
                        .build();

        return StartGameResponse.builder()
                .gameId(session.getId())
                .question(dto)
                .build();
    }

    @Override
    public StartGameResponse startGameAtLevel(int level) {

        if (level < 1) {
            throw new IllegalArgumentException(
                    "Level must be at least 1."
            );
        }

        // Guests can only start Level 1.
        User user = getAuthenticatedUser();

        if (user == null && level != 1) {
            throw new IllegalArgumentException(
                    "Guests can only play Level 1."
            );
        }

        // Logged-in users can only start unlocked levels.
        if (user != null) {

            LogoPlayerProgress progress =
                    logoPlayerProgressRepository
                            .findByUserId(user.getId())
                            .orElseGet(() ->
                                    logoPlayerProgressRepository.save(
                                            LogoPlayerProgress.builder()
                                                    .user(user)
                                                    .highestUnlockedLevel(1)
                                                    .build()
                                    )
                            );

            if (level >
                    progress.getHighestUnlockedLevel()) {

                throw new IllegalArgumentException(
                        "This level is locked."
                );
            }
        }

        LogoQuestion question =
                findQuestionForLevel(
                        level,
                        List.of()
                );

        List<String> letterBank =
                LetterGenerator.generate(question.getAnswer());

        LogoGameSession session =
                sessionRepository.save(
                        LogoGameSession.builder()
                                .questionId(question.getId())

                                .totalScore(0)
                                .currentQuestionReward(100)

                                .attempts(0)
                                .hintsUsed(0)

                                .currentQuestion(1)
                                .level(level)
                                .correctAnswers(0)
                                .totalQuestions(10)

                                .currentQuestionLetterHints(0)
                                .infoUsed(false)
                                .answerRevealed(false)
                                .revealedPositions(null)

                                .totalInfoHints(0)
                                .totalLettersRevealed(0)
                                .totalAnswersRevealed(0)

                                .currentLetterBank(
                                        serializeLetterBank(
                                                letterBank
                                        )
                                )

                                .awaitingContinue(false)
                                .completed(false)
                                .build()
                );

        markQuestionUsed(
                session.getId(),
                question.getId()
        );

        LogoQuestionDto dto =
                LogoQuestionDto.builder()
                        .questionId(question.getId())
                        .imageId("logo-" + question.getId())
                        .answerLength(
                                normalizeAnswer(
                                        question.getAnswer()
                                ).length()
                        )
                        .difficulty(
                                question.getDifficulty().name()
                        )
                        .letters(letterBank)
                        .build();

        return StartGameResponse.builder()
                .gameId(session.getId())
                .question(dto)
                .build();
    }
    // =========================================================
    // GET CURRENT QUESTION
    // =========================================================

    @Override
    public LogoQuestionResponse getCurrentQuestion(
            UUID sessionId
    ) {

        LogoGameSession session =
                getSession(sessionId);

        LogoQuestion question =
                getQuestion(session.getQuestionId());

        return buildQuestionResponse(
                question,
                session
        );
    }

    // =========================================================
    // SUBMIT GUESS
    // =========================================================

    @Override
    public GuessResponse submitGuess(
            UUID sessionId,
            GuessRequest request
    ) {

        LogoGameSession session =
                getSession(sessionId);

        if (Boolean.TRUE.equals(session.getCompleted())) {
            throw new IllegalStateException(
                    "Game is already completed."
            );
        }

        if (Boolean.TRUE.equals(
                session.getAwaitingContinue()
        )) {
            throw new IllegalStateException(
                    "Finish the question review before submitting another guess."
            );
        }

        LogoQuestion question =
                getQuestion(session.getQuestionId());

        session.setAttempts(
                session.getAttempts() + 1
        );

        String guess =
                normalizeAnswer(request.getGuess());

        // -----------------------------------------------------
        // WRONG ANSWER
        // -----------------------------------------------------

        if (!isCorrectAnswer(question, guess)) {

            sessionRepository.save(session);

            return GuessResponse.builder()
                    .correct(false)
                    .score(session.getTotalScore())
                    .message("Wrong answer")
                    .gameCompleted(false)
                    .reveal(null)
                    .nextQuestion(null)
                    .build();
        }

        // -----------------------------------------------------
        // CORRECT ANSWER
        // -----------------------------------------------------

        int awardedPoints =
                Math.max(
                        20,
                        session.getCurrentQuestionReward()
                );

        session.setCurrentQuestionReward(
                awardedPoints
        );

        session.setTotalScore(
                session.getTotalScore()
                        + awardedPoints
        );

        session.setCorrectAnswers(
                session.getCorrectAnswers() + 1
        );

        session.setAwaitingContinue(true);

        /*
         * IMPORTANT:
         *
         * Q10 completes the CURRENT LEVEL,
         * not the entire game.
         */
        boolean levelComplete =
                session.getCurrentQuestion()
                        >= session.getTotalQuestions();

// Unlock next level immediately when Q10
// completes the current level.
        if (levelComplete) {
            unlockNextLevel(session.getLevel());
        }

        sessionRepository.save(session);

        QuestionRevealResponse reveal =
                QuestionRevealResponse.builder()
                        .answer(question.getAnswer())
                        .imageId("logo-" + question.getId())
                        .info(question.getInfo())
                        .questionReward(awardedPoints)
                        .totalScore(session.getTotalScore())
                        .questionNumber(
                                session.getCurrentQuestion()
                        )
                        .correct(true)
                        .answerRevealed(false)
                        .gameCompleted(false)
                        .build();

        return GuessResponse.builder()
                .correct(true)
                .score(session.getTotalScore())
                .message(
                        levelComplete
                                ? "Level complete!"
                                : "Correct!"
                )
                .gameCompleted(false)
                .reveal(reveal)
                .nextQuestion(null)
                .build();
    }

    // =========================================================
    // CONTINUE
    // =========================================================

    @Override
    @Transactional
    public LogoQuestionResponse continueGame(UUID sessionId) {

        LogoGameSession session =
                getSession(sessionId);

        if (Boolean.TRUE.equals(
                session.getCompleted()
        )) {
            throw new IllegalStateException(
                    "Game is already completed."
            );
        }

        if (!Boolean.TRUE.equals(
                session.getAwaitingContinue()
        )) {
            throw new IllegalStateException(
                    "Current question has not been completed yet."
            );
        }

        // =====================================================
        // CURRENT LEVEL COMPLETE
        // =====================================================

        if (session.getCurrentQuestion()
                >= session.getTotalQuestions()) {

            unlockNextLevel(session.getLevel());

            int nextLevel =
                    session.getLevel() + 1;

            session.setLevel(nextLevel);
            session.setCurrentQuestion(1);

            /*
             * Important:
             *
             * Score is NOT reset.
             * Correct-answer statistics continue
             * for the current run.
             *
             * Used questions remain attached to the
             * session, so a question from Level 1
             * will not accidentally repeat in Level 2.
             */

            LogoQuestion nextQuestion =
                    findQuestionForLevel(
                            nextLevel,
                            getUsedQuestionIds(sessionId)
                    );

            setNewQuestion(
                    session,
                    nextQuestion
            );

            sessionRepository.save(session);

            return buildQuestionResponse(
                    nextQuestion,
                    session
            );
        }

        // =====================================================
        // NORMAL NEXT QUESTION
        // =====================================================

        List<Long> usedIds =
                getUsedQuestionIds(sessionId);

        LogoQuestion nextQuestion =
                findQuestionForLevel(
                        session.getLevel(),
                        usedIds
                );

        session.setCurrentQuestion(
                session.getCurrentQuestion() + 1
        );

        setNewQuestion(
                session,
                nextQuestion
        );

        sessionRepository.save(session);

        return buildQuestionResponse(
                nextQuestion,
                session
        );
    }

    // =========================================================
    // REPLAY CURRENT LEVEL
    // =========================================================

    @Override
    public LogoQuestionResponse replayLevel(
            UUID sessionId
    ) {

        LogoGameSession session =
                getSession(sessionId);

        int level =
                session.getLevel();

        /*
         * Remove questions from the current attempt.
         */
        usedQuestionRepository.deleteBySessionId(
                sessionId
        );

        /*
         * Fresh attempt:
         *
         * score = 0
         * question = 1
         * correct = 0
         * hints = 0
         */

        session.setCurrentQuestion(1);

        session.setCurrentQuestionReward(100);

        session.setHintsUsed(0);

        session.setCurrentQuestionLetterHints(0);

        session.setInfoUsed(false);
        session.setAnswerRevealed(false);

        session.setRevealedPositions(null);

        session.setTotalInfoHints(0);
        session.setTotalLettersRevealed(0);
        session.setTotalAnswersRevealed(0);

        session.setAwaitingContinue(false);
        session.setCompleted(false);

        LogoQuestion question =
                findQuestionForLevel(
                        level,
                        List.of()
                );

        setNewQuestion(
                session,
                question
        );

        sessionRepository.save(session);

        return buildQuestionResponse(
                question,
                session
        );
    }

    // =========================================================
    // INFO HINT
    // =========================================================

    @Override
    public HintResponse revealInfo(
            UUID sessionId
    ) {

        LogoGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        LogoQuestion question =
                getQuestion(session.getQuestionId());

        if (!Boolean.TRUE.equals(
                session.getInfoUsed()
        )) {

            int newReward =
                    Math.max(
                            20,
                            session.getCurrentQuestionReward()
                                    - 30
                    );

            session.setCurrentQuestionReward(
                    newReward
            );

            session.setInfoUsed(true);

            session.setHintsUsed(
                    session.getHintsUsed() + 1
            );

            session.setTotalInfoHints(
                    session.getTotalInfoHints() + 1
            );

            sessionRepository.save(session);
        }

        return HintResponse.builder()
                .score(
                        session.getCurrentQuestionReward()
                )
                .info(question.getInfo())
                .answerRevealed(false)
                .build();
    }

    // =========================================================
    // REVEAL LETTER
    // =========================================================

    @Override
    public HintResponse revealLetter(
            UUID sessionId,
            RevealLetterRequest request
    ) {

        LogoGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        LogoQuestion question =
                getQuestion(session.getQuestionId());

        String answer =
                normalizeAnswer(question.getAnswer());

        int position =
                request.getPosition();

        if (position < 0 ||
                position >= answer.length()) {

            throw new IllegalArgumentException(
                    "Invalid letter position."
            );
        }

        List<Integer> revealedPositions =
                parseRevealedPositions(
                        session.getRevealedPositions()
                );

        if (revealedPositions.contains(position)) {

            throw new IllegalStateException(
                    "This letter is already revealed."
            );
        }

        char revealedLetter =
                answer.charAt(position);

        revealedPositions.add(position);

        session.setRevealedPositions(
                revealedPositions.stream()
                        .map(String::valueOf)
                        .collect(
                                Collectors.joining(",")
                        )
        );

        // Every letter = -10.
        // Minimum reward = 20.

        session.setCurrentQuestionReward(
                Math.max(
                        20,
                        session.getCurrentQuestionReward()
                                - 10
                )
        );

        session.setCurrentQuestionLetterHints(
                session.getCurrentQuestionLetterHints()
                        + 1
        );

        session.setHintsUsed(
                session.getHintsUsed() + 1
        );

        session.setTotalLettersRevealed(
                session.getTotalLettersRevealed() + 1
        );

        sessionRepository.save(session);

        return HintResponse.builder()
                .score(
                        session.getCurrentQuestionReward()
                )
                .revealedPosition(position)
                .revealedLetter(revealedLetter)
                .lettersRevealed(
                        session.getCurrentQuestionLetterHints()
                )
                .answerRevealed(false)
                .build();
    }

    // =========================================================
    // REVEAL ANSWER
    // =========================================================

    @Override
    public HintResponse revealAnswer(
            UUID sessionId
    ) {

        LogoGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        LogoQuestion question =
                getQuestion(session.getQuestionId());

        session.setCurrentQuestionReward(20);

        session.setAnswerRevealed(true);

        session.setHintsUsed(
                session.getHintsUsed() + 1
        );

        session.setTotalAnswersRevealed(
                session.getTotalAnswersRevealed() + 1
        );

        session.setTotalScore(
                session.getTotalScore() + 20
        );

        /*
         * Reveal Answer is not a correct answer.
         */
        session.setAwaitingContinue(true);

        /*
         * Q10 means current LEVEL is complete,
         * not the entire run.
         */
        boolean levelComplete =
                session.getCurrentQuestion()
                        >= session.getTotalQuestions();

        if (levelComplete) {
            unlockNextLevel(session.getLevel());
        }

        sessionRepository.save(session);

        QuestionRevealResponse reveal =
                QuestionRevealResponse.builder()
                        .answer(question.getAnswer())
                        .imageId("logo-" + question.getId())
                        .info(question.getInfo())
                        .questionReward(20)
                        .totalScore(session.getTotalScore())
                        .questionNumber(
                                session.getCurrentQuestion()
                        )
                        .correct(false)
                        .answerRevealed(true)
                        .gameCompleted(false)
                        .build();

        return HintResponse.builder()
                .score(20)
                .answerRevealed(true)
                .answer(question.getAnswer())
                .reveal(reveal)
                .build();
    }

    // =========================================================
    // RESULT
    // =========================================================

    @Override
    public LogoGameResultResponse getResult(
            UUID sessionId
    ) {

        LogoGameSession session =
                getSession(sessionId);

        int totalQuestions =
                session.getTotalQuestions();

        double accuracy =
                totalQuestions == 0
                        ? 0.0
                        : (
                        (double)
                                session.getCorrectAnswers()
                                / totalQuestions
                ) * 100.0;

        int maximumScore =
                totalQuestions * 100;

        return LogoGameResultResponse.builder()
                .totalScore(
                        session.getTotalScore()
                )
                .maximumScore(maximumScore)
                .correctAnswers(
                        session.getCorrectAnswers()
                )
                .totalQuestions(totalQuestions)
                .accuracy(
                        Math.round(
                                accuracy * 100.0
                        ) / 100.0
                )
                .hintsUsed(
                        session.getHintsUsed()
                )
                .infoHintsUsed(
                        session.getTotalInfoHints()
                )
                .lettersRevealed(
                        session.getTotalLettersRevealed()
                )
                .answersRevealed(
                        session.getTotalAnswersRevealed()
                )
                .completed(
                        session.getCompleted()
                )
                .build();
    }

    @Override
    public LogoLevelProgressResponse getLevelProgress() {

        User user = getAuthenticatedUser();

        // Guest users always start from Level 1.
        if (user == null) {
            return LogoLevelProgressResponse.builder()
                    .highestUnlockedLevel(1)
                    .build();
        }

        LogoPlayerProgress progress =
                logoPlayerProgressRepository
                        .findByUserId(user.getId())
                        .orElseGet(() ->
                                logoPlayerProgressRepository.save(
                                        LogoPlayerProgress.builder()
                                                .user(user)
                                                .highestUnlockedLevel(1)
                                                .build()
                                )
                        );

        return LogoLevelProgressResponse.builder()
                .highestUnlockedLevel(
                        progress.getHighestUnlockedLevel()
                )
                .build();
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || "anonymousUser".equals(
                        authentication.getPrincipal()
                )
        ) {
            return null;
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElse(null);
    }

    // =========================================================
    // LEVEL / DIFFICULTY
    // =========================================================

    private LogoQuestion findQuestionForLevel(
            int level,
            List<Long> usedIds
    ) {

        /*
         * Level 1 = EASY
         * Level 2 = MEDIUM
         * Level 3 = HARD
         * Level 4+ = MIXED
         */

        if (level == 1) {
            return findUnusedByDifficulty(
                    Difficulty.EASY,
                    usedIds
            );
        }

        if (level == 2) {
            return findUnusedByDifficulty(
                    Difficulty.MEDIUM,
                    usedIds
            );
        }

        if (level == 3) {
            return findUnusedByDifficulty(
                    Difficulty.HARD,
                    usedIds
            );
        }

        /*
         * Level 4+:
         *
         * Randomly choose EASY/MEDIUM/HARD.
         */
        Difficulty[] difficulties = {
                Difficulty.EASY,
                Difficulty.MEDIUM,
                Difficulty.HARD
        };

        List<Difficulty> shuffled =
                new ArrayList<>(
                        List.of(difficulties)
                );

        Collections.shuffle(shuffled);

        for (Difficulty difficulty : shuffled) {

            try {

                return findUnusedByDifficulty(
                        difficulty,
                        usedIds
                );

            } catch (IllegalStateException ignored) {
                // Try another difficulty pool.
            }
        }

        /*
         * Final fallback.
         */
        if (usedIds.isEmpty()) {

            return questionRepository
                    .findRandomQuestion()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No active logo questions available."
                            )
                    );
        }

        return questionRepository
                .findRandomUnused(usedIds)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No unused logo questions available."
                        )
                );
    }

    private LogoQuestion findUnusedByDifficulty(
            Difficulty difficulty,
            List<Long> usedIds
    ) {

        if (usedIds.isEmpty()) {

            return questionRepository
                    .findRandomQuestionByDifficulty(
                            difficulty.name()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No "
                                            + difficulty.name()
                                            + " logo questions available."
                            )
                    );
        }

        return questionRepository
                .findRandomUnusedByDifficulty(
                        difficulty.name(),
                        usedIds
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No unused "
                                        + difficulty.name()
                                        + " logo questions available."
                        )
                );
    }

    // =========================================================
    // QUESTION STATE
    // =========================================================

    private void setNewQuestion(
            LogoGameSession session,
            LogoQuestion question
    ) {

        List<String> letterBank =
                LetterGenerator.generate(question.getAnswer());

        session.setQuestionId(
                question.getId()
        );

        session.setCurrentLetterBank(
                serializeLetterBank(letterBank)
        );

        resetQuestionState(session);

        markQuestionUsed(
                session.getId(),
                question.getId()
        );
    }

    private void resetQuestionState(
            LogoGameSession session
    ) {

        session.setCurrentQuestionReward(100);

        session.setCurrentQuestionLetterHints(0);

        session.setInfoUsed(false);

        session.setAnswerRevealed(false);

        session.setRevealedPositions(null);

        session.setAwaitingContinue(false);
    }

    // =========================================================
    // USED QUESTIONS
    // =========================================================

    private List<Long> getUsedQuestionIds(
            UUID sessionId
    ) {

        return usedQuestionRepository
                .findBySessionId(sessionId)
                .stream()
                .map(LogoGameUsedQuestion::getQuestionId)
                .toList();
    }

    private void markQuestionUsed(
            UUID sessionId,
            Long questionId
    ) {

        usedQuestionRepository.save(
                LogoGameUsedQuestion.builder()
                        .sessionId(sessionId)
                        .questionId(questionId)
                        .build()
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private void unlockNextLevel(int completedLevel) {

        User user = getAuthenticatedUser();

        // Guest → don't persist anything.
        if (user == null) {
            return;
        }

        int nextLevel =
                completedLevel + 1;

        LogoPlayerProgress progress =
                logoPlayerProgressRepository
                        .findByUserId(user.getId())
                        .orElseGet(() ->
                                LogoPlayerProgress.builder()
                                        .user(user)
                                        .highestUnlockedLevel(1)
                                        .build()
                        );

        if (nextLevel >
                progress.getHighestUnlockedLevel()) {

            progress.setHighestUnlockedLevel(
                    nextLevel
            );

            logoPlayerProgressRepository.save(
                    progress
            );
        }
    }
    private void validateActiveQuestion(
            LogoGameSession session
    ) {

        if (Boolean.TRUE.equals(
                session.getCompleted()
        )) {

            throw new IllegalStateException(
                    "Game is already completed."
            );
        }

        if (Boolean.TRUE.equals(
                session.getAwaitingContinue()
        )) {

            throw new IllegalStateException(
                    "Current question is already completed."
            );
        }
    }

    // =========================================================
    // BUILD QUESTION RESPONSE
    // =========================================================

    private LogoQuestionResponse buildQuestionResponse(
            LogoQuestion question,
            LogoGameSession session
    ) {

        return LogoQuestionResponse.builder()
                .questionId(question.getId())
                .imageId("logo-" + question.getId())
                .level(session.getLevel())
                .answerLength(
                        normalizeAnswer(
                                question.getAnswer()
                        ).length()
                )
                .letters(
                        deserializeLetterBank(
                                session.getCurrentLetterBank()
                        )
                )
                .score(session.getTotalScore())
                .questionNumber(
                        session.getCurrentQuestion()
                )
                .build();
    }

    // =========================================================
    // GENERATE LETTER BANK
    // =========================================================



    // =========================================================
    // ANSWER VALIDATION
    // =========================================================

    private boolean isCorrectAnswer(
            LogoQuestion question,
            String normalizedGuess
    ) {

        /*
         * Main popular name.
         */
        if (normalizeAnswer(
                question.getAnswer()
        ).equals(normalizedGuess)) {

            return true;
        }

        /*
         * Kept temporarily because the database
         * currently still contains aliases.
         *
         * We can remove this once aliases are removed
         * from the database/entity.
         */
        if (question.getAliases() != null) {

            return question.getAliases()
                    .stream()
                    .anyMatch(alias ->
                            normalizeAnswer(alias)
                                    .equals(normalizedGuess)
                    );
        }

        return false;
    }

    // =========================================================
    // NORMALIZE ANSWER
    // =========================================================

    private String normalizeAnswer(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace(" ", "")
                .toUpperCase();
    }

    // =========================================================
    // SESSION
    // =========================================================

    private LogoGameSession getSession(
            UUID sessionId
    ) {

        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"
                        )
                );
    }

    // =========================================================
    // QUESTION
    // =========================================================

    private LogoQuestion getQuestion(
            Long questionId
    ) {

        return questionRepository
                .findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found"
                        )
                );
    }

    // =========================================================
    // REVEALED POSITIONS
    // =========================================================

    private List<Integer> parseRevealedPositions(
            String revealedPositions
    ) {

        if (revealedPositions == null ||
                revealedPositions.isBlank()) {

            return new ArrayList<>();
        }

        return java.util.Arrays
                .stream(
                        revealedPositions.split(",")
                )
                .map(Integer::parseInt)
                .collect(
                        Collectors.toCollection(
                                ArrayList::new
                        )
                );
    }

    // =========================================================
    // LETTER BANK SERIALIZATION
    // =========================================================

    private String serializeLetterBank(
            List<String> letters
    ) {

        return String.join(
                ",",
                letters
        );
    }

    private List<String> deserializeLetterBank(
            String letterBank
    ) {

        if (letterBank == null ||
                letterBank.isBlank()) {

            return new ArrayList<>();
        }

        return new ArrayList<>(
                java.util.Arrays.asList(
                        letterBank.split(",")
                )
        );
    }




}
