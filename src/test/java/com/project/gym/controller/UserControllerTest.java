package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMe_whenUserIsAuthenticated_thenReturnOwnInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_whenPasswordIsValid_thenDeleteUser() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("Gordomono8!");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_whenPasswordIsInvalid_thenReturnBadRequest() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("ABC123");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.message").value("Invalid password."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_whenPasswordIsEmpty_thenReturnBadRequest() throws Exception
    {
        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HTTP_MESSAGE_NOT_READABLE"))
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "99999999")
    void deleteMe_whenUserDoesNotExist_thenReturnNotFound() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("ABC123");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USERNAME_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void createMembershipByDni_whenDTOIsValid_thenReturnMembership() throws Exception
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/user/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void createMembershipByDni_whenDTOIsInvalid_thenReturnBadRequest() throws Exception
    {
        String invalidDTO = "INVALID_DTO";

        mockMvc.perform(post("/api/v1/user/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HTTP_MESSAGE_NOT_READABLE"))
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMembershipsByDni_whenMembershipsExist_thenReturnMembershipList() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value("87654321"))
                .andExpect(jsonPath("$[1].user_dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getLastMembershipByDni_whenMembershipExists_thenReturnMembership() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"));
    }

    @Test
    void accessUserUrls_whenUserIsNotAuthenticated_thenReturnUnauthorized() throws Exception
    {
        // With any HTTP method, this URL is unauthorized when the USER is not logged in.
        mockMvc.perform(get("/api/v1/user/anything"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void accessUserUrls_whenUserDoesNotHaveUserRole_thenReturnForbidden() throws Exception
    {
        // With any HTTP method, this URL is forbidden for ADMIN and STAFF roles.
        mockMvc.perform(get("/api/v1/user/anything"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "'', GET",
            "/memberships, GET",
            "/memberships/last, GET"
    })
    @WithMockUser(roles = "USER", username = "10101010")
    void methodEntityByDni_whenUsersOrMembershipsDoNotExist_thenReturnNotFound(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(request(method, "/api/v1/user" + url))
                .andExpect(status().isNotFound());
    }
}
