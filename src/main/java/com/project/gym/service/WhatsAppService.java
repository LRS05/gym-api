package com.project.gym.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.whatsapp.WhatsAppMessageBodyDTO;
import com.project.gym.dto.whatsapp.WhatsAppRequestMessageDTO;
import com.project.gym.dto.whatsapp.WhatsAppRequestMessageTextDTO;
import com.project.gym.dto.whatsapp.WhatsappResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class WhatsAppService
{
    private final RestClient restClient;

    public WhatsAppService(
            @Value("${whatsapp.identification}") String identification,
            @Value("${whatsapp.token}") String token
    )
    {
        restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com/v22.0/" + identification + "/messages")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    public void sendMessage(String phoneNumber, String message)
    {
        try
        {
            WhatsAppMessageBodyDTO messageBody = createMessage(phoneNumber, message);
            WhatsAppRequestMessageDTO requestMessageDTO = createRequestMessage(messageBody);
            createResponseMessage(requestMessageDTO);
        }
        catch (Exception e)
        {
            log.error("Failed to send {} message to {} number", message, phoneNumber);
        }
    }

    private WhatsAppMessageBodyDTO createMessage(String phoneNumber, String message)
    {
        return new WhatsAppMessageBodyDTO(
                phoneNumber,
                message
        );
    }

    private WhatsAppRequestMessageDTO createRequestMessage(WhatsAppMessageBodyDTO messageBody)
    {
        return new WhatsAppRequestMessageDTO(
                "whatsapp",
                messageBody.number(),
                new WhatsAppRequestMessageTextDTO(messageBody.message())
        );
    }

    private void createResponseMessage(WhatsAppRequestMessageDTO message) throws JsonProcessingException
    {
        String response = restClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(message)
                .retrieve()
                .body(String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(response, WhatsappResponseDTO.class);
    }
}
