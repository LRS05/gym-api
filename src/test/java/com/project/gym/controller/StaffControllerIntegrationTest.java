package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import com.project.gym.entity.enums.Role;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaffControllerIntegrationTest
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
    void getMe_WhenStaffAuthenticated_ThenReturnOkWithStaffInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "99999999")
    void getMe_WhenStaffDoesNotExist_ThenReturnNotFound() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsers_WhenUsersExist_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsers_WhenNoUsersExist_ThenReturnOkWithEmptyList() throws Exception
    {
        userRepository.deleteAll(userRepository.findAllByRole(Role.USER));

        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_WhenUserExists_ThenReturnOkWithUser() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        String dni = "99999999";

        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDni_WhenUserDoesNotHaveUserRole_ThenReturnForbidden() throws Exception
    {
        String dni = "46622977";

        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_WhenUserExists_ThenReturnOkWithUser() throws Exception
    {
        int id = 3;

        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserById_WhenUserDoesNotHaveUserRole_ThenReturnForbidden() throws Exception
    {
        int id = 1;

        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isForbidden());
    }



    /*

           Membership Controllers Tests

     */



    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipByDni_WhenValidRequest_ThenReturnOkWithMembership() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/staff/membership/dni/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.payment_method").value("CASH"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void createMembershipByDni_WhenUserNotAllowed_ThenReturnBadRequest() throws Exception
    {
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/staff/membership/dni/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void createMembershipByDni_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/staff/membership/dni/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDate_WhenValidDateRange_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDate_WhenNoMembershipsInRange_ThenReturnOkWithEmptyList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2023-01-01")
                        .param("end", "2023-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDate_WhenInvalidParameters_ThenReturnBadRequest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "invalid")
                        .param("end", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDni_WhenUserHasMemberships_ThenReturnOkWithList() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/memberships/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value(dni))
                .andExpect(jsonPath("$[1].user_dni").value(dni));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDni_WhenUserHasNoMemberships_ThenReturnEmptyList() throws Exception
    {
        String dni = "88888888";

        mockMvc.perform(get("/api/v1/staff/memberships/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDni_WhenMembershipExists_ThenReturnOkWithMembership() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/membership/dni/{dni}/last", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"))
                .andExpect(jsonPath("$.next_payment_date").value("2025-12-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDni_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
    {
        String dni = "88888888";

        mockMvc.perform(get("/api/v1/staff/membership/dni/{dni}/last", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getActiveMemberships_WhenActiveMembershipsExist_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getActiveMemberships_WhenNoActiveMemberships_ThenReturnOkWithEmptyList() throws Exception
    {
        membershipRepository.deleteAll(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void updateMembershipStatusById_WhenMembershipExists_ThenReturnOkWithUpdatedStatus() throws Exception
    {
        int id = 2;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void updateMembershipStatusById_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
    {
        int id = -1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void updateMembershipStatusById_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(null);

        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }



    /*

       Unauthorized and Forbidden Tests

     */



    @ParameterizedTest
    @CsvSource({
            "/api/v1/staff, GET",
            "/api/v1/staff/users, GET",
            "/api/v1/staff/user/2, DELETE",
            "/api/v1/staff/user/dni/87654321, GET",
            "/api/v1/staff/memberships/active, GET",
            "/api/v1/staff/memberships/dni/87654321, GET",
            "/api/v1/staff/membership/dni/87654321, POST",
            "/api/v1/staff/membership/dni/87654321/last, GET",
            "/api/v1/staff/memberships/dni/date, GET",
            "/api/v1/staff/membership/1, GET",
            "/api/v1/staff/membership/1/status, PATCH"
    })
    void accessStaffUrls_WhenAnonymousUser_ThenReturnUnauthorized(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/staff, GET",
            "/api/v1/staff/users, GET",
            "/api/v1/staff/user/2, DELETE",
            "/api/v1/staff/user/dni/87654321, GET",
            "/api/v1/staff/memberships/active, GET",
            "/api/v1/staff/memberships/dni/87654321, GET",
            "/api/v1/staff/membership/dni/87654321, POST",
            "/api/v1/staff/membership/dni/87654321/last, GET",
            "/api/v1/staff/memberships/dni/date, GET",
            "/api/v1/staff/membership/1, GET",
            "/api/v1/staff/membership/1/status, PATCH"
    })
    @WithMockUser(roles = "USER", username = "87654321")
    void accessStaffUrls_WhenUserDoesNotHaveStaffRole_ThenReturnForbidden(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isForbidden());
    }

}
