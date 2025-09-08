package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsappContactResponseDTO(

        String input,

        @JsonProperty("wa_id")
        String waId
) {
}
