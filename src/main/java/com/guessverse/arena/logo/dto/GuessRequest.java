package com.guessverse.arena.logo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuessRequest {

    @NotBlank
    private String guess;

}