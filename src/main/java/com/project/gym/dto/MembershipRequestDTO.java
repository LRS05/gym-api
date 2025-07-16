package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record MembershipRequestDTO(

        @NotNull
        MembershipType type,

        @NotNull
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod
) {
}
