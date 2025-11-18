package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record MembershipRequestDTO(

        @NotNull(message = "Membership type is required.")
        MembershipType type,

        @NotNull(message = "Payment method is required.")
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod
) {
}
