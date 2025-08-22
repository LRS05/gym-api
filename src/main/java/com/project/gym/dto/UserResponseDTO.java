package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.Gender;
import com.project.gym.entity.enums.Role;

import java.time.LocalDate;

public record UserResponseDTO(

        int id,

        Role role,

        Gender gender,

        String dni,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("creation_date")
        LocalDate creationDate
) {
}
