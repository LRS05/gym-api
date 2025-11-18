package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import com.project.gym.repository.MembershipRepository;
import com.project.gym.repository.UserRepository;
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
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaffControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMe_whenStaffIsAuthenticated_thenReturnOwnInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsers_whenUsersExist_thenReturnUserList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsers_whenUsersDoNotExist_thenReturnEmptyList() throws Exception
    {
        userRepository.deleteById(3);

        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_whenUserExists_thenReturnUser() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_whenUserDoesNotExist_thenThrowNotFound() throws Exception
    {
        String dni = "10101010";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}", dni))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_whenUserIsAdminOrStaff_thenThrowForbidden() throws Exception
    {
        String dni = "46622977";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}", dni))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not allowed to see this user."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_whenUserExists_thenReturnUser() throws Exception
    {
        int id = 3;

        mockMvc.perform(get("/api/v1/staff/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_whenUserDoesNotExist_thenThrowNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(get("/api/v1/staff/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_whenUserIsAdminOrStaff_thenThrowForbidden() throws Exception
    {
        int id = 1;

        mockMvc.perform(get("/api/v1/staff/users/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not allowed to see this user."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipByDni_whenTargetIsNotAdminOrStaff_thenReturnMembership() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/staff/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipByDni_whenUserIsAdminOrStaff_thenThrowForbidden() throws Exception
    {
        String dni = "46622977";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/staff/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only users with role USER can have a membership."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipByDni_whenDTOIsInvalid_thenThrowBadRequest() throws Exception
    {
        String dni = "87654321";
        String invalidDTO = "invalid-dto";

        mockMvc.perform(post("/api/v1/staff/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDate_whenMembershipsExist_thenReturnMembershipList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDate_whenMembershipsDoNotExist_thenReturnEmptyList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users/memberships/date")
                        .param("start", "2000-01-01")
                        .param("end", "2001-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDate_whenDateIsInvalid_thenThrowBadRequest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users/memberships/date")
                        .param("start", "invalid-param")
                        .param("end", "invalid-param"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDni_whenMembershipsExist_thenReturnMembershipList() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}/memberships", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value("87654321"))
                .andExpect(jsonPath("$[1].user_dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDni_whenMembershipsDoNotExist_thenThrowNotFound() throws Exception
    {
        String dni = "10101010";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}/memberships", dni))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Memberships not found."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDni_whenMembershipExists_thenReturnMembership() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}/memberships/last", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDni_whenMembershipDoesNotExist_thenThrowNotFound() throws Exception
    {
        String dni = "10101010";

        mockMvc.perform(get("/api/v1/staff/users/dni/{dni}/memberships/last", dni))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Membership not found."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getActiveMemberships_whenActiveMembershipsExist_thenReturnMembershipList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getActiveMemberships_whenActiveMembershipsDoNotExist_thenReturnEmptyList() throws Exception
    {
        membershipRepository.deleteById(2);

        mockMvc.perform(get("/api/v1/staff/users/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void updateMembershipStatusById_whenMembershipExists_thenReturnMembershipUpdated() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/staff/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void updateMembershipStatusById_whenMembershipDoesNotExist_thenThrowNotFound() throws Exception
    {
        int id = -1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/staff/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Membership not found."));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void updateMembershipStatusById_whenDTOIsInvalid_thenThrowBadRequest() throws Exception
    {
        int id = 1;
        String invalidDTO = "invalid-dto";

        mockMvc.perform(patch("/api/v1/staff/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDTO))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/admin, GET",
            "/api/v1/admin/users, GET",
            "/api/v1/admin/users/2, DELETE",
            "/api/v1/admin/users/dni/87654321, GET",
            "/api/v1/admin/users/dni/87654321, DELETE",
            "/api/v1/admin/users/dni/87654321/role, PATCH",
            "/api/v1/admin/users/memberships/active, GET",
            "/api/v1/admin/users/dni/87654321/memberships, GET",
            "/api/v1/admin/users/dni/87654321/memberships, POST",
            "/api/v1/admin/users/dni/87654321/memberships, DELETE",
            "/api/v1/admin/users/dni/87654321/memberships/last, GET",
            "/api/v1/admin/users/memberships/date, GET",
            "/api/v1/admin/users/memberships/1, GET",
            "/api/v1/admin/users/memberships/1, DELETE",
            "/api/v1/admin/users/memberships/1/status, PATCH"
    })
    @WithMockUser(roles = "USER", username = "87654321")
    void accessStaffUrls_whenUserIsNotStaff_thenThrowForbidden(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(request(method, url))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/admin, GET",
            "/api/v1/admin/users, GET",
            "/api/v1/admin/users/2, DELETE",
            "/api/v1/admin/users/dni/87654321, GET",
            "/api/v1/admin/users/dni/87654321, DELETE",
            "/api/v1/admin/users/dni/87654321/role, PATCH",
            "/api/v1/admin/users/memberships/active, GET",
            "/api/v1/admin/users/dni/87654321/memberships, GET",
            "/api/v1/admin/users/dni/87654321/memberships, POST",
            "/api/v1/admin/users/dni/87654321/memberships, DELETE",
            "/api/v1/admin/users/dni/87654321/memberships/last, GET",
            "/api/v1/admin/users/memberships/date, GET",
            "/api/v1/admin/users/memberships/1, GET",
            "/api/v1/admin/users/memberships/1, DELETE",
            "/api/v1/admin/users/memberships/1/status, PATCH"
    })
    void accessStaffUrls_whenStaffIsNotAuthenticated_thenThrowUnauthorized(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(request(method, url))
                .andExpect(status().isUnauthorized());
    }
}

