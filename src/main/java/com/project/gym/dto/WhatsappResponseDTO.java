package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsappResponseDTO(

        @JsonProperty("messaging_product")
        String messagingProduct,

        List<WhatsAppContactResponseDTO> contacts,

        List<WhatsAppMessageResponseDTO> messages
) {
}
