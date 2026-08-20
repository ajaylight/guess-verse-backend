package com.guessverse.arena.logo.service;

import com.guessverse.arena.logo.dto.*;
import com.guessverse.arena.logo.entity.*;
import com.guessverse.arena.logo.repository.*;
import com.guessverse.arena.logo.util.LetterGenerator;
import com.guessverse.game.enums.Difficulty;
import com.guessverse.user.entity.User;
import com.guessverse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogoGameServiceImpl implements LogoGameService {

    private final LogoGameSessionRepository sessionRepository;
    private final LogoQuestionRepository questionRepository;
    private final LogoGameUsedQuestionRepository usedQuestionRepository;
    private final LogoPlayerProgressRepository logoPlayerProgressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public HintResponse revealAnswer(UUID sessionId) {
        LogoGameSession session = getSession(sessionId);
        validateActiveQuestion(session);
        LogoQuestion question = getQuestion(session.getQuestionId());

        if (!Boolean.TRUE.equals(session.getAnswerRevealed())) {
            session.setAnswerRevealed(true);
            session.setCurrentQuestionReward(20);
            session.setHintsUsed(session.getHintsUsed() + 1);
            session.setTotalAnswersRevealed(session.getTotalAnswersRevealed() + 1);
        }

        // Reveal Answer is a completed question. Continue is now allowed.
        session.setAwaitingContinue(true);
        sessionRepository.save(session);

        QuestionRevealResponse reveal = QuestionRevealResponse.builder()
                .answer(question.getAnswer())
                .imageId("logo-" + question.getId())
                .info(question.getInfo())
                .questionReward(session.getCurrentQuestionReward())
                .totalScore(session.getTotalScore())
                .questionNumber(session.getCurrentQuestion())
                .correct(false)
                .answerRevealed(true)
                .gameCompleted(Boolean.TRUE.equals(session.getCompleted()))
                .build();

        return HintResponse.builder()
                .score(session.getCurrentQuestionReward())
                .answerRevealed(true)
                .answer(question.getAnswer())
                .imageId("logo-" + question.getId())
                .totalScore(session.getTotalScore())
                .questionNumber(session.getCurrentQuestion())
                .correct(false)
                .gameCompleted(Boolean.TRUE.equals(session.getCompleted()))
                .reveal(reveal)
                .build();
    }

    private void validateActiveQuestion(LogoGameSession session) {
        if (Boolean.TRUE.equals(session.getCompleted())) throw new IllegalStateException("Game is already completed.");
        if (Boolean.TRUE.equals(session.getAwaitingContinue())) throw new IllegalStateException("Current question is already completed.");
    }

    private LogoQuestion getQuestion(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
    }

    private LogoGameSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
    }
}
