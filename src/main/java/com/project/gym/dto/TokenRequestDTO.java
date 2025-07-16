package com.project.gym.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestDTO(

        @NotBlank(message = "Refresh token is required.")
        String token
) {
}
