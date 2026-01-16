package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.dto.RoleRequestDTO;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminControllerTest
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
    void getMe_whenAdminIsAuthenticated_thenReturnOwnInfo() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("46622977"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsers_whenUsersExist_thenReturnUsersList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUsers_whenUsersDoNotExist_thenReturnOnlyAdminInfo() throws Exception
    {
        userRepository.deleteAllById(List.of(2, 3));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dni").value("46622977"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getUserByDni_whenUserExists_thenReturnUser() throws Exception
    {
        String dni = "12345678";

        mockMvc.perform(get("/api/v1/admin/users/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_whenUserExistsAndIsNotAdmin_thenReturnUserUpdated() throws Exception
    {
        String dni = "12345678";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_whenUserDoesNotExist_thenReturnNotFound() throws Exception
    {
        String dni = "99999999";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USERNAME_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_whenDTOIsInvalid_thenReturnBadRequest() throws Exception
    {
        String dni = "12345678";
        String invalidDTO = "INVALID_DTO";

        mockMvc.perform(patch("/api/v1/admin/users/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HTTP_MESSAGE_NOT_READABLE"))
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateUserRoleByDni_whenUserIsAdmin_thenReturnForbidden() throws Exception
    {
        String dni = "46622977";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.USER);

        mockMvc.perform(patch("/api/v1/admin/users/dni/{dni}/role", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("You cannot perform this action on an ADMIN user."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDni_whenUserExistsAndIsNotAdmin_thenDeleteUser() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(delete("/api/v1/admin/users/dni/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("User with dni " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserByDni_whenUserIsAdmin_thenReturnForbidden() throws Exception
    {
        String dni = "46622977";

        mockMvc.perform(delete("/api/v1/admin/users/dni/{dni}", dni))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("You cannot perform this action on an ADMIN user."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_whenUserExistsAndIsNotAdmin_thenDeleteUser() throws Exception
    {
        int id = 3;

        mockMvc.perform(delete("/api/v1/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_whenUserDoesNotExist_thenReturnNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(delete("/api/v1/admin/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USERNAME_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteUserById_whenUserIsAdmin_thenReturnForbidden() throws Exception
    {
        int id = 1;

        mockMvc.perform(delete("/api/v1/admin/users/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("You cannot delete an ADMIN user."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipByDni_whenTargetIsNotAdminOrStaff_thenReturnMembership() throws Exception
    {
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        mockMvc.perform(post("/api/v1/admin/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.type").value("ANNUALLY"))
                .andExpect(jsonPath("$.payment_method").value("CARD"))
                .andExpect(jsonPath("$.created_by").value("46622977"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipByDni_whenUserIsAdminOrStaff_thenReturnConflict() throws Exception
    {
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );

        mockMvc.perform(post("/api/v1/admin/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_MEMBERSHIP_ASSIGNMENT"))
                .andExpect(jsonPath("$.message").value("Only users with role USER can have a membership."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void createMembershipByDni_whenDTOIsInvalid_thenReturnBadRequest() throws Exception
    {
        String dni = "12345678";
        String invalidDTO = "INVALID_DTO";

        mockMvc.perform(post("/api/v1/admin/users/dni/{dni}/memberships", dni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HTTP_MESSAGE_NOT_READABLE"))
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_whenMembershipsExist_thenReturnMembershipList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users/memberships/date")
                        .param("start", "2025-01-01")
                        .param("end", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_whenMembershipsDoNotExist_thenReturnEmptyList() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users/memberships/date")
                        .param("start", "2000-01-01")
                        .param("end", "2001-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDate_whenDateIsInvalid_thenReturnBadRequest() throws Exception
    {
        mockMvc.perform(get("/api/v1/admin/users/memberships/date")
                        .param("start", "invalid-param")
                        .param("end", "invalid-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("METHOD_ARGUMENT_MISMATCH"))
                .andExpect(jsonPath("$.message").value("Invalid URL parameter type."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getMembershipsByDni_whenMembershipsExist_thenReturnMembershipsList() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/users/dni/{dni}/memberships", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user_dni").value("87654321"))
                .andExpect(jsonPath("$[1].user_dni").value("87654321"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getLastMembershipByDni_whenMembershipExists_thenReturnMembership() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(get("/api/v1/admin/users/dni/{dni}/memberships/last", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dni").value("87654321"))
                .andExpect(jsonPath("$.payment_date").value("2025-01-01"))
                .andExpect(jsonPath("$.next_payment_date").value("2099-01-01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMembership_whenActiveMembershipsExist_thenReturnMembershipList() throws Exception
    {
        // The active membership expires on 2099-01-01, ensuring it is always active.
        mockMvc.perform(get("/api/v1/admin/users/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].next_payment_date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].created_by").value("46622977"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void getActiveMemberships_whenActiveMembershipsDoNotExist_thenReturnEmptyList() throws Exception
    {
        // Delete the unique active membership
        membershipRepository.deleteById(2);

        mockMvc.perform(get("/api/v1/admin/users/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusById_whenMembershipExists_thenReturnMembershipUpdated() throws Exception
    {
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/admin/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusById_whenMembershipDoesNotExist_thenReturnNotFound() throws Exception
    {
        int id = -1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        mockMvc.perform(patch("/api/v1/admin/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("MEMBERSHIP_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Membership not found."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void updateMembershipStatusById_whenDTOIsInvalid_thenReturnBadRequest() throws Exception
    {
        int id = 1;
        String invalidDTO = "INVALID_DTO";

        mockMvc.perform(patch("/api/v1/admin/users/memberships/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HTTP_MESSAGE_NOT_READABLE"))
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipsByDni_whenMembershipsExist_thenDeleteMemberships() throws Exception
    {
        String dni = "87654321";

        mockMvc.perform(delete("/api/v1/admin/users/dni/{dni}/memberships", dni))
                .andExpect(status().isOk())
                .andExpect(content().string("Memberships of the user with dni " + dni + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipById_whenMembershipExists_thenDeleteMembership() throws Exception
    {
        int id = 1;

        mockMvc.perform(delete("/api/v1/admin/users/memberships/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Membership with id " + id + " successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void deleteMembershipById_whenMembershipDoesNotExist_thenReturnNotFound() throws Exception
    {
        int id = -1;

        mockMvc.perform(delete("/api/v1/admin/users/memberships/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("MEMBERSHIP_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Membership not found."));
    }

    @Test
    void accessAdminUrls_whenAdminIsNotAuthenticated_thenReturnUnauthorized() throws Exception
    {
        // With any HTTP method, this URL is unauthorized when the ADMIN is not logged in.
        mockMvc.perform(get("/api/v1/admin/anything"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "12345678")
    void accessAdminUrls_whenUserDoesNotHaveAdminRole_thenReturnForbidden() throws Exception
    {
        // With any HTTP method, this URL is forbidden for STAFF and USER roles.
        mockMvc.perform(get("/api/v1/admin/anything"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "'', GET",
            "'', DELETE",
            "/memberships, GET",
            "/memberships, DELETE",
            "/memberships/last, GET"
    })
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void methodEntityByDni_whenUsersOrMembershipsDoNotExist_thenReturnNotFound(String url, HttpMethod method) throws Exception
    {
        mockMvc.perform(request(method, "/api/v1/admin/users/dni/10101010" + url))
                .andExpect(status().isNotFound());
    }
}
