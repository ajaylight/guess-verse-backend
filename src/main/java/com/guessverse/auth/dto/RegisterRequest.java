package com.guessverse.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String displayName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}