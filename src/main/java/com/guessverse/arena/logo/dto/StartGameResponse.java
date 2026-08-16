package com.guessverse.arena.logo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StartGameResponse {

    private UUID gameId;

    private LogoQuestionDto question;
}
