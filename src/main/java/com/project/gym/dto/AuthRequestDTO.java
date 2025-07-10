package com.project.gym.dto;

import jakarta.validation.constraints.NotNull;

public record AuthRequestDTO(

        @NotNull
        String dni,

        @NotNull
        String password
) {
}
