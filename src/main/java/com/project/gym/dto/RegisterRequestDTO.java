package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDTO(

        @NotNull
        String dni,

        @NotNull
        String password,

        @NotNull
        @JsonProperty("first_name")
        String firstName,

        @NotNull
        @JsonProperty("last_name")
        String lastName
) {
}
