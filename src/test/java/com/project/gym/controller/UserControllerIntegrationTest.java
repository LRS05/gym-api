package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMeTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("Franco"))
                .andExpect(jsonPath("$.last_name").value("Cataldi"))
                .andExpect(jsonPath("$.dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMeForbiddenTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMeUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMeTest() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMeNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(delete("/api/v1/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMeInvalidPasswordTest() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("ABC123");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMeNoBodyTest() throws Exception
    {
        mockMvc.perform(delete("/api/v1/user"))
                .andExpect(status().isBadRequest());
    }

    /*

           /api/v1/user/membership TESTS

     */

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void createMembershipTest() throws Exception
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/user/membership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(post("/api/v1/user/membership"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void createMembershipInvalidDTOTest() throws Exception
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/user/membership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMembershipsTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dni").value("87654321"))
                .andExpect(jsonPath("$[1].dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getLastMembershipTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/membership/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "88888888")
    void getLastMembershipNotFoundTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/membership/last"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/membership/last"))
                .andExpect(status().isForbidden());
    }

}
