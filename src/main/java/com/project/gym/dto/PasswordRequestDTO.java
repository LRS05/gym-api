package com.project.gym.dto;

import jakarta.validation.constraints.NotEmpty;

public record PasswordRequestDTO(

        @NotEmpty(message = "Password is required.")
        String password
) {
}
