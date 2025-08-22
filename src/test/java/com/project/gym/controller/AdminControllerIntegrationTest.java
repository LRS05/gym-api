package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.dto.RoleRequestDTO;
import com.project.gym.entity.MembershipEntity;
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
public class AdminControllerIntegrationTest
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
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsersTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsersEmptyListTest() throws Exception
    {
        userRepository.deleteAll();
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUsersUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUsersNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUserByDniTest() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/user/dni/{dni}", dni)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUserByDniNotFoundTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(get("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getUserByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMeTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("46622977"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getMeUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void getMeNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDniTest() throws Exception
    {
        String dni = "87654321";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.STAFF);

        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleUserNotFoundTest() throws Exception
    {
        String dni = "99999999";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.STAFF);

        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDniInvalidDTOTest() throws Exception
    {
        String dni = "87654321";
        RoleRequestDTO requestDTO = new RoleRequestDTO(null);

        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRoleUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "87654321")
    void updateUserRoleNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDniTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("User with dni " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDniNotFoundTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserByDniUnauthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "87654321")
    void deleteUserByDniNotAuthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByIdTest() throws Exception
    {
        int id = 2;
        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByIdNotFoundTest() throws Exception
    {
        int id = -1;
        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserByIdUnauthorizedTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "87654321")
    void deleteUserByIdNotAuthorizedTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isForbidden());
    }

    /*

           /api/v1/user/membership TESTS

     */

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipTest() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/admin/membership/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.payment_method").value("CASH"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipUserNotAllowedTest() throws Exception
    {
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/admin/membership/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipInvalidDTOTest() throws Exception
    {
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/admin/membership/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMembershipUnauthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(post("/api/v1/admin/membership/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void createMembershipNotAuthorizedTest() throws Exception
    {
        String dni = "99999999";
        mockMvc.perform(post("/api/v1/admin/membership/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDateTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDateEmptyListTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2023-01-01")
                        .param("end", "2023-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDateInvalidRequestParametersTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "invalid")
                        .param("end", "invalid"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getMembershipsByDateUnauthoriedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDateNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDniTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/memberships/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value(dni))
                .andExpect(jsonPath("$[1].user_dni").value(dni));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDniEmptyListTest() throws Exception
    {
        String dni = "88888888";
        mockMvc.perform(get("/api/v1/admin/memberships/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMembershipsByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/memberships/all/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getMembershipsByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/memberships/all/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipByDniTest() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/membership/last/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"))
                .andExpect(jsonPath("$.next_payment_date").value("2025-12-01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipByDniNotFoundTest() throws Exception
    {
        String dni = "88888888";
        mockMvc.perform(get("/api/v1/admin/membership/last/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastMembershipByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/membership/last/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getLastMembershipByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(get("/api/v1/admin/membership/last/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMembershipsTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMembershipsEmptyListTest() throws Exception
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByStatus(MembershipStatus.ACTIVE);
        membershipRepository.deleteAll(memberships);

        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveMembershipsUnauthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void getActiveMembershipsNotAuthorizedTest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusByIdTest() throws Exception
    {
        int id = 2;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusByIdUserNotFoundTest() throws Exception
    {
        int id = -1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusByIdInvalidDTOTest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(null);
        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMembershipStatusByIdUnauthorizedTest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
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
        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteAllMembershipsByDniTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(delete("/api/v1/admin/memberships/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("Memberships with DNI " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteAllMembershipsByDniEmptyListTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(delete("/api/v1/admin/memberships/{dni}", dni))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllMembershipsByDniUnauthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(delete("/api/v1/admin/membership/all/{dni}", dni))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void deleteAllMembershipsByDniNotAuthorizedTest() throws Exception
    {
        String dni = "87654321";
        mockMvc.perform(delete("/api/v1/admin/membership/all/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipByIdTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Membership with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipByIdNotFoundTest() throws Exception
    {
        int id = -1;
        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMembershipByIdUnauthorizedTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void deleteMembershipByIdNotAuthorizedTest() throws Exception
    {
        int id = 1;
        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isForbidden());
    }
}
