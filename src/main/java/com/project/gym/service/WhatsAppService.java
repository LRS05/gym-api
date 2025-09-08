package com.project.gym.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.WhatsAppMessageBodyDTO;
import com.project.gym.dto.WhatsAppRequestMessageDTO;
import com.project.gym.dto.WhatsAppRequestMessageTextDTO;
import com.project.gym.dto.WhatsappResponseDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipMessageType;
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

    public void sendMembershipCreatedMessage(MembershipEntity membership, UserEntity user)
    {
        sendMembershipMessage(MembershipMessageType.CREATED, membership, user);
    }

    public void sendMembershipExpiredMessage(MembershipEntity membership, UserEntity user)
    {
        sendMembershipMessage(MembershipMessageType.EXPIRED, membership, user);
    }

    public void sendMembershipExpiryReminderMessage(MembershipEntity membership, UserEntity user)
    {
        sendMembershipMessage(MembershipMessageType.EXPIRING, membership, user);
    }

    private void sendMembershipMessage(MembershipMessageType type, MembershipEntity membership, UserEntity user)
    {
        try
        {
            WhatsAppMessageBodyDTO messageBody = createWhatsappMessage(type, membership, user);
            WhatsAppRequestMessageDTO requestMessageDTO = createRequestMessage(messageBody);
            createResponseMessage(requestMessageDTO);
        }
        catch (Exception e)
        {
            log.error("Failed to send Whatsapp {} message for user {} and membership {}", type.name(), user.getDni(), membership.getId(), e);
        }
    }

    private WhatsAppMessageBodyDTO createWhatsappMessage(MembershipMessageType messageType, MembershipEntity membership, UserEntity user)
    {
        return new WhatsAppMessageBodyDTO(
                user.getPhoneNumber(),
                messageType.format(user, membership)
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
