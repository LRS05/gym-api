package com.project.gym.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppRequestMessageDTO(

        @JsonProperty("messaging_product")
        String messagingProduct,

        String to,

        WhatsAppRequestMessageTextDTO text
) {
}
