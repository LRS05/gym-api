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
    void getMe_WhenAdminAuthenticated_ThenReturnOkWithAdminInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("46622977"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "99999999")
    void getMe_WhenAdminDoesNotExist_ThenReturnNotFound() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsers_WhenUsersExist_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsers_WhenNoUsersExist_ThenReturnOkWithEmptyList() throws Exception
    {
        userRepository.deleteAll();

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUserByDni_WhenUserExists_ThenReturnOkWithUser() throws Exception
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
    void getUserByDni_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        String dni = "99999999";

        mockMvc.perform(get("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_WhenValidRequest_ThenReturnOkWithUpdatedRole() throws Exception
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
    void updateUserRoleByDni_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
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
    void updateUserRoleByDni_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        String dni = "87654321";
        RoleRequestDTO requestDTO = new RoleRequestDTO(null);

        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_WhenUserIsAdmin_ThenReturnForbidden() throws Exception
    {
        String dni = "46622977";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.USER);

        mockMvc.perform(patch("/api/v1/admin/user/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDni_WhenUserExists_ThenReturnOkWithMessage() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("User with dni " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDni_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        String dni = "99999999";

        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDni_WhenUserIsAdmin_ThenReturnForbidden() throws Exception
    {
        String dni = "46622977";

        mockMvc.perform(delete("/api/v1/admin/user/dni/{dni}", dni))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_WhenUserExists_ThenReturnOkWithMessage() throws Exception
    {
        int id = 2;

        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_WhenUserDoesNotExist_ThenReturnNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_WhenUserIsAdmin_ThenReturnForbidden() throws Exception
    {
        int id = 1;

        mockMvc.perform(delete("/api/v1/admin/user/{id}", id))
                .andExpect(status().isForbidden());
    }



    /*

           Memberships Controllers Tests

     */



    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipByDni_WhenValidRequest_ThenReturnOkWithMembership() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/admin/membership/dni/{dni}", dni)
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
    void createMembershipByDni_WhenUserNotAllowed_ThenReturnBadRequest() throws Exception
    {
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/admin/membership/dni/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipByDni_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(null, null);

        mockMvc.perform(post("/api/v1/admin/membership/dni/{dni}", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_WhenValidDateRange_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_WhenNoMembershipsInRange_ThenReturnOkWithEmptyList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "2023-01-01")
                        .param("end", "2023-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_WhenInvalidParameters_ThenReturnBadRequest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/date")
                        .param("start", "invalid")
                        .param("end", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDni_WhenUserHasMemberships_ThenReturnOkWithList() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/memberships/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value(dni))
                .andExpect(jsonPath("$[1].user_dni").value(dni));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDni_WhenUserHasNoMemberships_ThenReturnEmptyList() throws Exception
    {
        String dni = "88888888";

        mockMvc.perform(get("/api/v1/admin/memberships/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipByDni_WhenMembershipExists_ThenReturnOkWithMembership() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/membership/dni/{dni}/last", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value(dni))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"))
                .andExpect(jsonPath("$.next_payment_date").value("2025-12-01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipByDni_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
    {
        String dni = "88888888";

        mockMvc.perform(get("/api/v1/admin/membership/dni/{dni}/last", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMemberships_WhenActiveMembershipsExist_ThenReturnOkWithList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMemberships_WhenNoActiveMemberships_ThenReturnOkWithEmptyList() throws Exception
    {
        membershipRepository.deleteAll(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/admin/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusById_WhenMembershipExists_ThenReturnOkWithUpdatedStatus() throws Exception
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
    void updateMembershipStatusById_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
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
    void updateMembershipStatusById_WhenInvalidDTO_ThenReturnBadRequest() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(null);

        mockMvc.perform(patch("/api/v1/admin/membership/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipsByDni_WhenMembershipsExist_ThenReturnOkWithMessage() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(delete("/api/v1/admin/memberships/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("Memberships with dni " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipsByDni_WhenNoMembershipsExist_ThenReturnNotFound() throws Exception
    {
        String dni = "88888888";

        mockMvc.perform(delete("/api/v1/admin/memberships/dni/{dni}", dni))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipById_WhenMembershipExists_ThenReturnOkWithMessage() throws Exception
    {
        int id = 1;

        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Membership with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipById_WhenMembershipDoesNotExist_ThenReturnNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(delete("/api/v1/admin/membership/{id}", id))
                .andExpect(status().isNotFound());
    }



    /*

           Unauthorized and Forbidden Tests

     */



    @ParameterizedTest
    @CsvSource({
            "/api/v1/admin, GET",
            "/api/v1/admin/users, GET",
            "/api/v1/admin/user/2, DELETE",
            "/api/v1/admin/user/dni/87654321, GET",
            "/api/v1/admin/user/dni/87654321, DELETE",
            "/api/v1/admin/user/dni/87654321/role, PATCH",
            "/api/v1/admin/memberships/active, GET",
            "/api/v1/admin/memberships/dni/87654321, GET",
            "/api/v1/admin/membership/dni/87654321, POST",
            "/api/v1/admin/membership/dni/87654321, DELETE",
            "/api/v1/admin/membership/dni/87654321/last, GET",
            "/api/v1/admin/membership/dni/date, GET",
            "/api/v1/admin/membership/1, GET",
            "/api/v1/admin/membership/1, DELETE",
            "/api/v1/admin/membership/1/status, PATCH"
    })
    @WithMockUser(roles = "USER", username = "87654321")
    void accessStaffUrls_WhenUserDoesNotHaveAdminRole_ThenReturnForbidden(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/admin, GET",
            "/api/v1/admin/users, GET",
            "/api/v1/admin/user/2, DELETE",
            "/api/v1/admin/user/dni/87654321, GET",
            "/api/v1/admin/user/dni/87654321, DELETE",
            "/api/v1/admin/user/dni/87654321/role, PATCH",
            "/api/v1/admin/memberships/active, GET",
            "/api/v1/admin/memberships/dni/87654321, GET",
            "/api/v1/admin/membership/dni/87654321, POST",
            "/api/v1/admin/membership/dni/87654321, DELETE",
            "/api/v1/admin/membership/dni/87654321/last, GET",
            "/api/v1/admin/membership/dni/date, GET",
            "/api/v1/admin/membership/1, GET",
            "/api/v1/admin/membership/1, DELETE",
            "/api/v1/admin/membership/1/status, PATCH"
    })
    void accessAdminUrls_WhenUserIsNotAuthenticated_ThenReturnUnauthorized(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.request(method, url))
                .andExpect(status().isUnauthorized());
    }
}
