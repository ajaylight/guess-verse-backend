package com.guessverse.arena.flag.service;

import com.guessverse.arena.flag.dto.*;
import com.guessverse.arena.flag.entity.FlagGameSession;
import com.guessverse.arena.flag.entity.FlagGameUsedQuestion;
import com.guessverse.arena.flag.entity.FlagPlayerProgress;
import com.guessverse.arena.flag.entity.FlagQuestion;
import com.guessverse.arena.flag.repository.FlagGameSessionRepository;
import com.guessverse.arena.flag.repository.FlagGameUsedQuestionRepository;
import com.guessverse.arena.flag.repository.FlagPlayerProgressRepository;
import com.guessverse.arena.flag.repository.FlagQuestionRepository;
import com.guessverse.arena.flag.util.FlagAnswerRules;
import com.guessverse.arena.flag.util.LetterGenerator;
import com.guessverse.game.enums.Difficulty;
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
public class FlagGameServiceImpl implements FlagGameService {

    private final FlagQuestionRepository questionRepository;
    private final FlagGameSessionRepository sessionRepository;
    private final FlagGameUsedQuestionRepository usedQuestionRepository;
    private final UserRepository userRepository;
    private final FlagPlayerProgressRepository FlagPlayerProgressRepository;
    private final Random random = new Random();

    // =========================================================
    // START GAME
    // =========================================================

    @Override
    public StartGameResponse startGame() {

        FlagQuestion question =
                findQuestionForLevel(1, List.of());


        List<String> letterBank =
                LetterGenerator.generate(question.getAnswer());

        FlagGameSession session =
                sessionRepository.save(
                        FlagGameSession.builder()
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


                                .totalInfoHints(0)
                                .totalLettersRevealed(0)
                                .totalAnswersRevealed(0)

                                .currentLetterBank(
                                        serializeLetterBank(letterBank)
                                )
                                .revealedPositions(
                                        buildInitialRevealedPositions(
                                                question.getId()
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

        FlagQuestionDto dto =
                FlagQuestionDto.builder()
                        .questionId(question.getId())
                        .imageId("flag-" + question.getId())
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

            FlagPlayerProgress progress =
                    FlagPlayerProgressRepository
                            .findByUserId(user.getId())
                            .orElseGet(() ->
                                    FlagPlayerProgressRepository.save(
                                            FlagPlayerProgress.builder()
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

        FlagQuestion question =
                findQuestionForLevel(
                        level,
                        List.of()
                );



        List<String> letterBank =
                LetterGenerator.generate(question.getAnswer());

        FlagGameSession session =
                sessionRepository.save(
                        FlagGameSession.builder()
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


                                .totalInfoHints(0)
                                .totalLettersRevealed(0)
                                .totalAnswersRevealed(0)

                                .currentLetterBank(
                                        serializeLetterBank(
                                                letterBank
                                        )
                                )
                                .revealedPositions(
                                        buildInitialRevealedPositions(
                                                question.getId()
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

        FlagQuestionDto dto =
                FlagQuestionDto.builder()
                        .questionId(question.getId())
                        .imageId("flag-" + question.getId())
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
    public FlagQuestionResponse getCurrentQuestion(
            UUID sessionId
    ) {

        FlagGameSession session =
                getSession(sessionId);

        FlagQuestion question =
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

        FlagGameSession session =
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

        FlagQuestion question =
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

        boolean gameComplete =
                levelComplete &&
                        session.getLevel() >= 20;

        if (gameComplete) {

            session.setCompleted(true);
            session.setAwaitingContinue(false);
            session.setCompletedAt(
                    java.time.LocalDateTime.now()
            );
        }

        sessionRepository.save(session);

        QuestionRevealResponse reveal =
                QuestionRevealResponse.builder()
                        .answer(question.getAnswer())
                        .imageId("flag-" + question.getId())
                        .info(question.getInfo())
                        .questionReward(awardedPoints)
                        .totalScore(session.getTotalScore())
                        .questionNumber(
                                session.getCurrentQuestion()
                        )
                        .correct(true)
                        .answerRevealed(false)
                        .gameCompleted(gameComplete)
                        .build();

        return GuessResponse.builder()
                .correct(true)
                .score(session.getTotalScore())
                .message(
                        gameComplete
                                ? "Game complete!"
                                : levelComplete
                                ? "Level complete!"
                                : "Correct!"
                )
                .gameCompleted(gameComplete)
                .reveal(reveal)
                .nextQuestion(null)
                .build();
    }

    // =========================================================
    // CONTINUE
    // =========================================================

    @Override
    public FlagQuestionResponse continueGame(
            UUID sessionId
    ) {

        FlagGameSession session =
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

            // Level 20 is the final level.
            if (session.getLevel() >= 20) {

                session.setAwaitingContinue(false);
                session.setCompleted(true);

                if (session.getCompletedAt() == null) {
                    session.setCompletedAt(
                            java.time.LocalDateTime.now()
                    );
                }

                sessionRepository.save(session);

                return buildQuestionResponse(
                        getQuestion(session.getQuestionId()),
                        session
                );
            }

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

            FlagQuestion nextQuestion =
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

        FlagQuestion nextQuestion =
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
    public FlagQuestionResponse replayLevel(
            UUID sessionId
    ) {

        FlagGameSession session =
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

        session.setRevealedPositions(
                buildInitialRevealedPositions(
                        session.getQuestionId()
                )
        );
        session.setTotalInfoHints(0);
        session.setTotalLettersRevealed(0);
        session.setTotalAnswersRevealed(0);

        session.setAwaitingContinue(false);
        session.setCompleted(false);

        FlagQuestion question =
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

        FlagGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        FlagQuestion question =
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
        FlagGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        FlagQuestion question =
                getQuestion(session.getQuestionId());

        FlagAnswerRules.AnswerRule rule =
                FlagAnswerRules.getRule(
                        question.getAnswer()
                );

        String displayAnswer =
                rule.displayAnswer();

        int position =
                request.getPosition();

        if (position < 0 ||
                position >= displayAnswer.length()) {

            throw new IllegalArgumentException(
                    "Invalid letter position."
            );
        }

        char selectedCharacter =
                displayAnswer.charAt(position);

        /*
         * Spaces and punctuation are automatically revealed.
         * They are never valid letter-hint positions.
         */
        if (!Character.isLetter(selectedCharacter)) {

            throw new IllegalArgumentException(
                    "Selected position is not a letter."
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
                selectedCharacter;

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

        FlagGameSession session =
                getSession(sessionId);

        validateActiveQuestion(session);

        FlagQuestion question =
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

        sessionRepository.save(session);

        QuestionRevealResponse reveal =
                QuestionRevealResponse.builder()
                        .answer(question.getAnswer())
                        .imageId("flag-" + question.getId())
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
    public FlagGameResultResponse getResult(
            UUID sessionId
    ) {

        FlagGameSession session =
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

        return FlagGameResultResponse.builder()
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
    public FlagLevelProgressResponse getLevelProgress() {

        User user = getAuthenticatedUser();

        // Guest users always start from Level 1.
        if (user == null) {
            return FlagLevelProgressResponse.builder()
                    .highestUnlockedLevel(1)
                    .build();
        }

        FlagPlayerProgress progress =
                FlagPlayerProgressRepository
                        .findByUserId(user.getId())
                        .orElseGet(() ->
                                FlagPlayerProgressRepository.save(
                                        FlagPlayerProgress.builder()
                                                .user(user)
                                                .highestUnlockedLevel(1)
                                                .build()
                                )
                        );

        return FlagLevelProgressResponse.builder()
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

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return null;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            return null;
        }

        return userRepository
                .findByEmail(userDetails.getUsername())
                .orElse(null);
    }

    // =========================================================
    // LEVEL / DIFFICULTY
    // =========================================================

    private FlagQuestion findQuestionForLevel(
            int level,
            List<Long> usedIds
    ) {

        if (usedIds == null || usedIds.isEmpty()) {

            return questionRepository
                    .findRandomByLevel(level)
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No active flag questions available for level "
                                            + level
                            )
                    );
        }

        return questionRepository
                .findRandomUnusedByLevel(level, usedIds)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No unused flag questions available for level "
                                        + level
                        )
                );
    }

    // =========================================================
    // QUESTION STATE
    // =========================================================

    private void setNewQuestion(
            FlagGameSession session,
            FlagQuestion question
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
            FlagGameSession session
    ) {

        session.setCurrentQuestionReward(100);

        session.setCurrentQuestionLetterHints(0);

        session.setInfoUsed(false);

        session.setAnswerRevealed(false);

        session.setRevealedPositions(
                buildInitialRevealedPositions(
                        session.getQuestionId()
                )
        );

        session.setAwaitingContinue(false);
    }

    // =========================================================
    // USED QUESTIONS
    // =========================================================
    private String buildInitialRevealedPositions(
            Long questionId
    ) {

        FlagQuestion question =
                getQuestion(questionId);

        FlagAnswerRules.AnswerRule rule =
                FlagAnswerRules.getRule(
                        question.getAnswer()
                );

        String displayAnswer =
                rule.displayAnswer();

        String initiallyRevealed =
                rule.initiallyRevealed();

        if (initiallyRevealed == null ||
                initiallyRevealed.isBlank()) {

            return "";
        }

        List<Integer> positions =
                new ArrayList<>();

        /*
         * Reveal every occurrence of each character sequence
         * specified by the rule.
         *
         * Spaces and punctuation are ignored when matching.
         */
        String normalizedDisplay =
                displayAnswer
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z]",
                                ""
                        );

        String normalizedReveal =
                initiallyRevealed
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z]",
                                ""
                        );

        if (normalizedReveal.isEmpty()) {
            return "";
        }

        int searchFrom = 0;

        while (searchFrom <= normalizedDisplay.length()) {

            int match =
                    normalizedDisplay.indexOf(
                            normalizedReveal,
                            searchFrom
                    );

            if (match < 0) {
                break;
            }

            /*
             * Map normalized positions back to the actual
             * display-string positions.
             */
            int normalizedIndex = 0;

            for (int i = 0;
                 i < displayAnswer.length();
                 i++) {

                char c =
                        displayAnswer
                                .charAt(i);

                if (!Character.isLetter(c)) {
                    continue;
                }

                if (normalizedIndex >= match &&
                        normalizedIndex <
                                match + normalizedReveal.length()) {

                    positions.add(i);
                }

                normalizedIndex++;
            }

            searchFrom =
                    match + normalizedReveal.length();
        }

        return positions.stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private String buildRevealedText(
            FlagQuestion question,
            String revealedPositions
    ) {

        if (revealedPositions == null ||
                revealedPositions.isBlank()) {

            return null;
        }

        FlagAnswerRules.AnswerRule rule =
                FlagAnswerRules.getRule(
                        question.getAnswer()
                );

        String displayAnswer =
                rule.displayAnswer();

        String[] positionValues =
                revealedPositions.split(",");

        StringBuilder result =
                new StringBuilder();

        for (String value : positionValues) {

            if (value == null || value.isBlank()) {
                continue;
            }

            int position;

            try {
                position = Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                continue;
            }

            if (position >= 0 &&
                    position < displayAnswer.length()) {

                result.append(
                        displayAnswer.charAt(position)
                );
            }
        }

        return result.length() == 0
                ? null
                : result.toString();
    }

    private List<Long> getUsedQuestionIds(
            UUID sessionId
    ) {

        return usedQuestionRepository
                .findBySessionId(sessionId)
                .stream()
                .map(FlagGameUsedQuestion::getQuestionId)
                .toList();
    }

    private void markQuestionUsed(
            UUID sessionId,
            Long questionId
    ) {

        usedQuestionRepository.save(
                FlagGameUsedQuestion.builder()
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

        FlagPlayerProgress progress =
                FlagPlayerProgressRepository
                        .findByUserId(user.getId())
                        .orElseGet(() ->
                                FlagPlayerProgress.builder()
                                        .user(user)
                                        .highestUnlockedLevel(1)
                                        .build()
                        );

        if (nextLevel >
                progress.getHighestUnlockedLevel()) {

            progress.setHighestUnlockedLevel(
                    nextLevel
            );

            FlagPlayerProgressRepository.save(
                    progress
            );
        }
    }
    private void validateActiveQuestion(
            FlagGameSession session
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

    private FlagQuestionResponse buildQuestionResponse(
            FlagQuestion question,
            FlagGameSession session
    ) {

        return FlagQuestionResponse.builder()
                .questionId(question.getId())
                .imageId("flag-" + question.getId())
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
                .revealedText(
                        buildRevealedText(
                                question,
                                session.getRevealedPositions()
                        )
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
            FlagQuestion question,
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

         * from the database/entity.
         */


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

    private FlagGameSession getSession(
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

    private FlagQuestion getQuestion(
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
