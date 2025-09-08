package com.project.gym.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.WhatsappMessageBodyDTO;
import com.project.gym.dto.WhatsappRequestMessageDTO;
import com.project.gym.dto.WhatsappRequestMessageTextDTO;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class WhatsappService
{
    private final RestClient restClient;

    public WhatsappService(
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

    private WhatsappRequestMessageDTO createRequestMessage(WhatsappMessageBodyDTO messageBody)
    {
        return new WhatsappRequestMessageDTO(
                "whatsapp",
                messageBody.number(),
                new WhatsappRequestMessageTextDTO(messageBody.message())
        );
    }

    private void createResponseMessage(WhatsappRequestMessageDTO message) throws JsonProcessingException
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

    private WhatsappMessageBodyDTO createWhatsappMessage(MembershipMessageType type, MembershipEntity membership, UserEntity user)
    {
        long daysToExpire = ChronoUnit.DAYS.between(LocalDate.now(), membership.getNextPaymentDate());
        return new WhatsappMessageBodyDTO(
                user.getPhoneNumber(),
                type.format(user, membership, daysToExpire)
        );
    }

    private void sendMembershipMessage(MembershipMessageType type, MembershipEntity membership, UserEntity user)
    {
        try
        {
            WhatsappMessageBodyDTO messageBody = createWhatsappMessage(type, membership, user);
            WhatsappRequestMessageDTO requestMessageDTO = createRequestMessage(messageBody);
            createResponseMessage(requestMessageDTO);
        }
        catch (JsonProcessingException e)
        {
            log.error("Failed to send Whatsapp {} message for user {} and membership {}", type.name(), user.getDni(), membership.getId(), e);
        }
    }
}
