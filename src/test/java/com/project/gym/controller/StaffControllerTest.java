package com.project.gym.controller;

import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
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
public class StaffControllerTest
{
    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MembershipRepository membershipRepository;

    @BeforeEach
    void setUp()
    {
        UserEntity staff = userRepository.findById(2)
                .orElse(null);

        ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(staff);

        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultCookie("access-token", accessTokenCookie.getValue())
                .build();
    }

    @Test
    void getMe_whenStaffIsAuthenticated_thenReturnStaffInfo()
    {
        restTestClient.get()
                .uri("/api/v1/staff")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("12345678")
                .jsonPath("$.role").isEqualTo("STAFF");
    }

    @Test
    void getUsers_whenUsersExist_thenReturnUsersList()
    {
        restTestClient.get()
                .uri("/api/v1/staff/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].role").isEqualTo("USER");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getUsers_whenUsersDoNotExist_thenReturnEmptyList()
    {
        // Deletes the only user with role USER.
        userRepository.deleteById(3);

        restTestClient.get()
                .uri("/api/v1/staff/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getUserByDni_whenUserExistsAndIsNotAdmin_thenReturnUser()
    {
        String dni = "87654321";

        restTestClient.get()
                .uri("/api/v1/staff/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("87654321")
                .jsonPath("$.role").isEqualTo("USER");
    }

    @Test
    void getUserByDni_whenUserIsAdmin_thenReturnForbidden()
    {
        String dni = "46622977";

        restTestClient.get()
                .uri("/api/v1/staff/users/dni/{dni}", dni)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo("ACCESS_DENIED")
                .jsonPath("$.message").isEqualTo("You are not allowed to see this user.");
    }

    @Test
    void getUserByDni_whenUserDoesNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.get()
                .uri("/api/v1/staff/users/dni/{dni}", dni)
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
                .uri("/api/v1/staff/users/memberships")
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
                .uri("/api/v1/staff/users/memberships")
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
                .uri("/api/v1/staff/users/memberships")
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
                .uri("/api/v1/staff/users/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("paymentMethod").isEqualTo("Payment method is required.")
                .jsonPath("type").isEqualTo("Membership type is required.");
    }

    @Test
    void getMembershipsByPaymentDateBetween_whenMembershipsExist_thenReturnMemberships()
    {
        restTestClient.get()
                .uri("/api/v1/staff/users/memberships/date?start=2025-01-01&end=2026-01-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].payment_date").isEqualTo("2025-01-01")
                .jsonPath("$[1].payment_date").isEqualTo("2025-01-01");
    }

    @Test
    void getMembershipsByPaymentDateBetween_whenMembershipsDoNotExist_thenReturnEmptyList()
    {
        restTestClient.get()
                .uri("/api/v1/staff/users/memberships/date?start=2000-01-01&end=2001-01-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getMembershipsByPaymentDateBetween_whenDatesAreInvalid_thenReturnBadRequest()
    {
        restTestClient.get()
                .uri("/api/v1/staff/users/memberships/date?start=start&end=end")
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
                .uri("/api/v1/staff/users/dni/{dni}/memberships", dni)
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
                .uri("/api/v1/staff/users/dni/{dni}/memberships", dni)
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
                .uri("/api/v1/staff/users/dni/{dni}/memberships/last", dni)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user_dni").isEqualTo("99999999")
                .jsonPath("$.payment_date").isEqualTo("2025-01-01");
    }

    @Test
    void getLastMembershipByDni_whenMembershipDoesNotExist_thenReturnNotFound()
    {
        String dni = "10101010";

        restTestClient.get()
                .uri("/api/v1/staff/users/dni/{dni}/memberships/last", dni)
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
                .uri("/api/v1/staff/users/memberships/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].status").isEqualTo("ACTIVE")
                .jsonPath("$[0].next_payment_date").isEqualTo("2099-01-01");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getActiveMemberships_whenActiveMembershipsDoNotExist_thenReturnEmptyList()
    {
        membershipRepository.deleteAllById(List.of(2, 3));

        restTestClient.get()
                .uri("/api/v1/staff/users/memberships/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateMembershipStatusById_whenMembershipExists_thenReturnMembershipUpdated()
    {
        int id = 3;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        restTestClient.patch()
                .uri("/api/v1/staff/users/memberships/{id}/status", id)
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
                .uri("/api/v1/staff/users/memberships/{id}/status", id)
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
                .uri("/api/v1/staff/users/memberships/{id}/status", id)
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
                .uri("/api/v1/staff/users/memberships/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("Membership status is required.");
    }
}
