package com.project.gym.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppContactResponseDTO(

        String input,

        @JsonProperty("wa_id")
        String waId
) {
}
