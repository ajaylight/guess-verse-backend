package com.guessverse.arena.flag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuessRequest {

    @NotBlank
    private String guess;

}
