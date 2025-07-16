package com.project.gym.dto;

import com.project.gym.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequestDTO(

        @NotNull(message = "Invalid role type.")
        Role role
){}
