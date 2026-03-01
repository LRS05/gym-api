package com.project.gym.controller;

import com.project.gym.dto.*;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.*;
import com.project.gym.repository.MembershipRepository;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdminControllerTest
{
    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @BeforeEach
    void setUp()
    {
        UserEntity admin = userRepository.findById(1)
                .orElse(null);

        ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(admin);

        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultCookie("access-token", accessTokenCookie.getValue())
                .build();
    }

    @Test
    void getMe_whenAdminIsAuthenticated_thenReturnOwnInfo()
    {
        restTestClient.get()
                .uri("/api/v1/admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("46622977")
                .jsonPath("$.role").isEqualTo("ADMIN");
    }

    @Test
    void getUsers_whenUsersExist_thenReturnUsersList()
    {
        restTestClient.get()
                .uri("/api/v1/admin/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getUsers_whenUsersAndStaffDoNotExist_thenReturnAdminList()
    {
        // Delete users and staff.
        userRepository.deleteAllById(List.of(2, 3));

        restTestClient.get()
                .uri("/api/v1/admin/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].role").isEqualTo("ADMIN")
                .jsonPath("$[0].dni").isEqualTo("46622977");
    }

    @Test
    void getUserByDni_whenUserExists_thenReturnUser()
    {
        String dni = "87654321";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("87654321")
                .jsonPath("$.role").isEqualTo("USER");
    }

    @Test
    void getUserByDni_whenUserDoesNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("USERNAME_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("User not found.");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUserRoleByDni_whenUserExistsAndIsNotAdmin_thenReturnUserUpdated()
    {
        String dni = "12345678";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.ADMIN);

        restTestClient.patch()
                .uri("/api/v1/admin/users/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("12345678")
                .jsonPath("$.role").isEqualTo("ADMIN");
    }
    @Test
    void updateUserRoleByDni_whenUserIsAdmin_thenReturnForbidden()
    {
        String dni = "46622977";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.USER);

        restTestClient.patch()
                .uri("/api/v1/admin/users/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo("ACCESS_DENIED")
                .jsonPath("$.message").isEqualTo("You cannot perform this action on an ADMIN user.");
    }

    @Test
    void updateUserRoleByDni_whenUserDoesNotExist_thenReturnNotFound()
    {
        String dni = "99999999";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.USER);

        restTestClient.patch()
                .uri("/api/v1/admin/users/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("USERNAME_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("User not found.");
    }

    @Test
    void updateUserRoleByDni_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String dni = "12345678";
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.patch()
                .uri("/api/v1/admin/users/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void updateUserRoleByDni_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {
        String dni = "87654321";

        restTestClient.patch()
                .uri("/api/v1/admin/users/dni/{dni}/role", dni)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.role").isEqualTo("Role is required.");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteUserByDni_whenUserExistsAndIsNotAdmin_thenDeleteUser()
    {
        String dni = "87654321";

        restTestClient.delete()
                .uri("/api/v1/admin/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User with dni " + dni + " successfully deleted.");
    }

    @Test
    void deleteUserByDni_whenUserIsAdmin_thenReturnForbidden()
    {
        String dni = "46622977";

        restTestClient.delete()
                .uri("/api/v1/admin/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo("ACCESS_DENIED")
                .jsonPath("$.message").isEqualTo("You cannot perform this action on an ADMIN user.");
    }

    @Test
    void deleteUserByDni_whenUserDoesNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.delete()
                .uri("/api/v1/admin/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("USERNAME_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("User not found.");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteUserById_whenUserExistsAndIsNotAdmin_thenDeleteUser()
    {
        int id = 2;

        restTestClient.delete()
                .uri("/api/v1/admin/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User with id " + id + " successfully deleted.");
    }

    @Test
    void deleteUserById_whenUserIsAdmin_thenReturnForbidden()
    {
        int id = 1;

        restTestClient.delete()
                .uri("/api/v1/admin/users/{id}", id)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo("ACCESS_DENIED")
                .jsonPath("$.message").isEqualTo("You cannot delete an ADMIN user.");
    }

    @Test
    void deleteUserById_whenUserDoesNotExist_thenReturnNotFound()
    {
        int id = -1;

        restTestClient.delete()
                .uri("/api/v1/admin/users/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("USERNAME_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("User not found.");
    }

    @Test
    void createMembership_whenUserIsNotAdminOrStaff_thenReturnMembership()
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                "87654321",
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        restTestClient.post()
                .uri("/api/v1/admin/users/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.user_dni").isEqualTo("87654321")
                .jsonPath("$.type").isEqualTo("ANNUALLY")
                .jsonPath("$.payment_method").isEqualTo("CARD");
    }

    @Test
    void createMembership_whenUserIsAdminOrStaff_thenReturnConflict()
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                "46622977",
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        restTestClient.post()
                .uri("/api/v1/admin/users/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.error").isEqualTo("INVALID_MEMBERSHIP_ASSIGNMENT")
                .jsonPath("$.message").isEqualTo("Only users with role USER can have a membership.");
    }

    @Test
    void createMembership_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.post()
                .uri("/api/v1/admin/users/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void createMembership_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {
        restTestClient.post()
                .uri("/api/v1/admin/users/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.type").isEqualTo("Membership type is required.")
                .jsonPath("$.paymentMethod").isEqualTo("Payment method is required.");
    }



    @Test
    void getMembershipsByPaymentDateBetween_whenMembershipsExist_thenReturnMembershipsList()
    {
        restTestClient.get()
                .uri("/api/v1/admin/users/memberships/date?start=2025-01-01&end=2026-01-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void getMembershipsByPaymentDateBetween_whenMembershipsDoNotExist_thenReturnEmptyList()
    {
        restTestClient.get()
                .uri("/api/v1/admin/users/memberships/date?start=2020-01-01&end=2021-01-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getMembershipsByPaymentDateBetween_whenDatesAreInvalid_thenReturnBadRequest()
    {
        restTestClient.get()
                .uri("/api/v1/admin/users/memberships/date?start=start&end=end")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("METHOD_ARGUMENT_MISMATCH")
                .jsonPath("$.message").isEqualTo("Invalid URL parameter type.");
    }

    @Test
    void getMembershipsByDni_whenMembershipsExist_thenReturnMemberships()
    {
        String dni = "87654321";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}/memberships", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].user_dni").isEqualTo("87654321")
                .jsonPath("$[1].user_dni").isEqualTo("87654321");
    }

    @Test
    void getMembershipsByDni_whenMembershipsDoNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}/memberships", dni)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Memberships not found.");
    }

    @Test
    void getLastMembershipByDni_whenMembershipExists_thenReturnMembership()
    {
        String dni = "99999999";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}/memberships/last", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user_dni").isEqualTo("99999999")
                .jsonPath("$.payment_date").isEqualTo("2025-01-01");
    }

    @Test
    void getLastMembershipsByDni_whenMembershipDoesNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.get()
                .uri("/api/v1/admin/users/dni/{dni}/memberships/last", dni)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Membership not found.");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getActiveMemberships_whenActiveMembershipsExist_thenReturnActiveMembershipsList()
    {
        restTestClient.get()
                .uri("/api/v1/admin/users/memberships/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].next_payment_date").isEqualTo("2099-01-01");
    }

    @Test
    void getActiveMemberships_whenActiveMembershipsDoNotExist_thenReturnEmptyList()
    {
        membershipRepository.deleteById(2);

        restTestClient.get()
                .uri("/api/v1/admin/users/memberships/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void updateMembershipStatusById_whenMembershipExists_thenReturnMembershipUpdated()
    {
        int id = 3;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        restTestClient.patch()
                .uri("/api/v1/admin/users/memberships/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("3")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    void updateMembershipStatusById_whenMembershipDoesNotExist_thenReturnNotFound()
    {
        int id = -1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        restTestClient.patch()
                .uri("/api/v1/admin/users/memberships/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Membership not found.");
    }

    @Test
    void updateMembershipStatusById_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        int id = 3;
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.patch()
                .uri("/api/v1/admin/users/memberships/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void updateMembershipStatusById_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {
        int id = 3;

        restTestClient.patch()
                .uri("/api/v1/admin/users/memberships/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("Membership status is required.");
    }

    @Test
    void deleteMembershipsByDni_whenMembershipsExist_thenDeleteMemberships()
    {
        String dni = "87654321";

        restTestClient.delete()
                .uri("/api/v1/admin/users/dni/{dni}/memberships", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Memberships of the user with dni " + dni + " successfully deleted.");
    }

    @Test
    void deleteMembershipsByDni_whenMembershipsDoNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.delete()
                .uri("/api/v1/admin/users/dni/{dni}/memberships", dni)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Memberships not found.");
    }

    @Test
    void deleteMembershipById_whenMembershipExists_thenDeleteMembership()
    {
        int id = 1;

        restTestClient.delete()
                .uri("/api/v1/admin/users/memberships/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Membership with id " + id + " successfully deleted.");
    }

    @Test
    void deleteMembershipById_whenMembershipDoesNotExist_thenReturnNotFound()
    {
        int id = -1;

        restTestClient.delete()
                .uri("/api/v1/admin/users/memberships/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Membership not found.");
    }

    @Test
    void accessAdminUrls_whenAdminIsNotAuthenticated_thenReturnUnauthorized()
    {
        // Create a new RestTestClient without any cookies (i.e. not authenticated)
        RestTestClient clientWithoutCookie = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // We can test with any HTTP method; it always returns unauthorized
        clientWithoutCookie
                .get()
                .uri("/api/v1/admin")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void accessAdminUrls_whenUserDoesNotHaveAdminRole_thenReturnForbidden()
    {
        UserEntity user = userRepository.findById(3)
                .orElse(null);

        ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(user);

        RestTestClient clientWithUserCookie = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultCookie("access-token", accessTokenCookie.getValue())
                .build();

        clientWithUserCookie.get()
                .uri("/api/v1/admin")
                .exchange()
                .expectStatus().isForbidden();
    }
}
