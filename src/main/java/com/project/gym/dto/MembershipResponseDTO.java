package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;

import java.time.LocalDate;

public record MembershipResponseDTO(

        int id,

        @JsonProperty("user_id")
        int userId,

        @JsonProperty("user_dni")
        String userDni,

        MembershipStatus status,

        MembershipType type,

        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,

        @JsonProperty("payment_date")
        LocalDate paymentDate,

        @JsonProperty("next_payment_date")
        LocalDate nextPaymentDate,

        @JsonProperty("created_by")
        String createdBy,

        @JsonProperty("last_modified_by")
        String lastModifiedBy
) {
}
