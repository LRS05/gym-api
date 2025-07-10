package com.project.gym.dto;

import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record MembershipRequestDTO(

        @NotNull
        MembershipType type,

        @NotNull
        PaymentMethod paymentMethod
) {
}
