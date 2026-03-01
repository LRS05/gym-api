package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MembershipRequestDTO(

        @NotBlank(message = "Dni is required.")
        @Pattern(regexp = "^[0-9]{7,8}$", message = "Dni must have 7 or 8 digits.")
        String dni,

        @NotNull(message = "Membership type is required.")
        MembershipType type,

        @NotNull(message = "Payment method is required.")
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod
) {
}
