package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "Dni is required.")
        @Pattern(regexp = "^[0-9]{7,8}$", message = "Dni must have 7 or 8 digits.")
        String dni,

        @NotBlank(message = "Password is required.")
        String password,

        @JsonProperty("first_name")
        @NotBlank(message = "First name is required.")
        @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters.")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$", message = "First name can only contain letters.")
        String firstName,

        @JsonProperty("last_name")
        @NotBlank(message = "Last name is required.")
        @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters.")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$", message = "Last name can only contain letters.")
        String lastName
) {
}
