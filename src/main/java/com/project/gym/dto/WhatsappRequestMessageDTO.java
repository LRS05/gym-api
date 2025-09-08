package com.project.gym.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsappRequestMessageDTO(

        @JsonProperty("messaging_product")
        String messagingProduct,

        String to,

        WhatsappRequestMessageTextDTO text
) {
}
