package com.project.gym.dto;

import com.project.gym.entity.enums.MembershipStatus;
import jakarta.validation.constraints.NotNull;

public record MembershipStatusRequestDTO(

        @NotNull
        MembershipStatus status
) {
}
