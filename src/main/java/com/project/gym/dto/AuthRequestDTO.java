package com.project.gym.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(

        @NotBlank(message = "Dni is required.")
        String dni,

        @NotBlank(message = "Password is required.")
        String password
) {
}
