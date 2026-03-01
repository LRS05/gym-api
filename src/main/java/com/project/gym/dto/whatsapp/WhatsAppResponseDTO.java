package com.project.gym.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsAppResponseDTO(

        @JsonProperty("messaging_product")
        String messagingProduct,

        List<WhatsAppContactResponseDTO> contacts,

        List<WhatsAppMessageResponseDTO> messages
) {
}
