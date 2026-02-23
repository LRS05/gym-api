package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.gym.entity.enums.CountryCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberRequestDTO(

        @NotNull(message = "Country code is required.")
        @JsonProperty("country-code")
        CountryCode countryCode,

        @JsonProperty("phone_number")
        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^\\d{10,14}$", message = "Invalid phone number, Only digits are allowed, without spaces or special characters, and do not include the country code.")
        String phoneNumber
) {
}
