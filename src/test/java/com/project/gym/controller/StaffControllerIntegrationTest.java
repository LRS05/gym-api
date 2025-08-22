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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
    void getMeTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.first_name").value("Matias"))
                .andExpect(jsonPath("$.last_name").value("Freccero"));
    }

    @Test
    void getMeUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMeNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsersTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsersEmptyListTest() throws Exception
    {
        List<UserEntity> users = userRepository.findAllByRole(Role.USER);
        userRepository.deleteAll(users);

        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUsersUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getUsersNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDniTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDniNotFoundTest() throws Exception
    {
        String dni = "99999999";

        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDniUserNotAllowedTest() throws Exception
    {
        String dni = "46622977";
        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getUserByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByIdTest() throws Exception
    {
        int id = 3;
        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByIdNotFoundTest() throws Exception
    {
        int id = -1;
        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByIdUserNotAllowedTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByIdUnauthorizedTest() throws Exception
    {
        int id = 3;
        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getUserByIdNotAuthorizedTest() throws Exception
    {
        int id = 3;
        mockMvc.perform(get("/api/v1/staff/user/{id}", id))
                .andExpect(status().isForbidden());
    }



    /*

           Membership Controllers

     */



    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipTest() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/staff/membership/{dni}", dni)
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
    void createMembershipUserNotAllowedTest() throws Exception
    {
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/staff/membership/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void createMembershipInvalidDTOTest() throws Exception
    {
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/staff/membership/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMembershipUnauthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(post("/api/v1/staff/membership/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "12345678")
    void createMembershipNotAuthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(post("/api/v1/staff/membership/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDateTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDateEmptyListTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2023-01-01")
                        .param("end", "2023-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDateInvalidRequestParametersTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "invalid")
                        .param("end", "invalid"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getMembershipsByDateUnauthoriedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "12345678")
    void getMembershipsByDateNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDniTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/memberships/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value(dni))
                .andExpect(jsonPath("$[1].user_dni").value(dni));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getMembershipsByDniEmptyListTest() throws Exception
    {
        String dni = "88888888";
        mockMvc.perform(get("/api/v1/staff/memberships/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMembershipsByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/memberships/all/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "12345678")
    void getMembershipsByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/membership/all/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDniTest() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/staff/membership/last/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"))
                .andExpect(jsonPath("$.next_payment_date").value("2025-12-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDniNotFoundTest() throws Exception
    {
        String dni = "88888888";
        mockMvc.perform(get("/api/v1/staff/membership/last/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastMembershipByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/membership/last/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "12345678")
    void getLastMembershipByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/staff/membership/last/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getActiveMembershipsTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void getActiveMembershipsEmptyListTest() throws Exception
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByStatus(MembershipStatus.ACTIVE);
        membershipRepository.deleteAll(memberships);

        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveMembershipsUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "12345678")
    void getActiveMembershipsNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/staff/memberships/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "46622977")
    void updateMembershipStatusByIdTest() throws Exception
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
    void updateMembershipStatusByIdUserNotFoundTest() throws Exception
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
    void updateMembershipStatusByIdInvalidDTOTest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(null);
        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMembershipStatusByIdUnauthorizedTest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void updateMembershipStatusByIdNotAuthorizedTest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.INACTIVE);
        mockMvc.perform(patch("/api/v1/staff/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }




}
