package com.guessverse.arena.flag.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StartGameResponse {

    private UUID gameId;

    private FlagQuestionDto question;
}
