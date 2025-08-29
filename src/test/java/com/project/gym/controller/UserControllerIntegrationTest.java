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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMe_WhenUserAuthenticated_ThenReturnOkWithUserInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "99999999")
    void getMe_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_WhenValidPassword_ThenReturnOkWithMessage() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully."));
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_WhenInvalidPassword_ThenReturnBadRequest() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("ABC123");

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void deleteMe_WithNoPassword_ThenReturnBadRequest() throws Exception
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO(null);

        mockMvc.perform(delete("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }



    /*

           Membership Controllers Tests

     */



    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void createMembershipByDni_WhenValidRequest_ThenReturnOkWithMembership() throws Exception
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/user/membership")
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
    void createMembershipByDni_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/user/membership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMembershipsByDni_WhenUserHasMemberships_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value("87654321"))
                .andExpect(jsonPath("$[1].user_dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "11111111")
    void getMembershipsByDni_WhenUserHasNoMemberships_ThenReturnEmptyList() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getLastMembershipByDni_WhenMembershipExists_ThenReturnOkWithMembership() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/membership/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "88888888")
    void getLastMembershipByDni_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/membership/last"))
                .andExpect(status().isNotFound());
    }



        /*

       Unauthorized and Forbidden Tests

     */



    @ParameterizedTest
    @CsvSource({
            "/api/v1/user, GET",
            "/api/v1/user, DELETE",
            "/api/v1/user/membership, POST",
            "/api/v1/user/membership/last, GET",
            "/api/v1/user/memberships, GET",
    })
    void accessUserUrls_WhenAnonymousUser_ThenReturnUnauthorized(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/user, GET",
            "/api/v1/user, DELETE",
            "/api/v1/user/membership, POST",
            "/api/v1/user/membership/last, GET",
            "/api/v1/user/memberships, GET",
    })
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void accessUserUrls_WhenUserDoesNotHaveUserfRole_ThenReturnForbidden(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isForbidden());
    }

}
