package com.project.gym.controller;

import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.entity.UserEntity;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest
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
        UserEntity user = userRepository.findById(3)
                .orElse(null);

        ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(user);

        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultCookie("access-token", accessTokenCookie.getValue())
                .build();
    }

    @Test
    void getMe_whenUserIsAuthenticated_thenReturnUserInfo()
    {
        restTestClient.get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("87654321")
                .jsonPath("$.role").isEqualTo("USER");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteMe_whenPasswordIsCorrect_thenDeleteUser()
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO(
                "Gordomono8!"
        );

        restTestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Account deleted successfully.");
    }

    @Test
    void deleteMe_whenPasswordIsIncorrect_thenReturnBadRequest()
    {
        PasswordRequestDTO requestDTO = new PasswordRequestDTO(
                "Hello123"
        );

        restTestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INVALID_PASSWORD")
                .jsonPath("$.message").isEqualTo("Incorrect password.");
    }

    @Test
    void deleteMe_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void deleteMe_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {
        restTestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.password").isEqualTo("Password is required.");
    }

    @Test
    void createMembership_whenDTOIsValid_thenReturnMembership()
    {
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                "87654321",
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );

        restTestClient.post()
                .uri("/api/v1/user/memberships")
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
    void createMembership_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.post()
                .uri("/api/v1/user/memberships")
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
                .uri("/api/v1/user/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.type").isEqualTo("Membership type is required.")
                .jsonPath("$.paymentMethod").isEqualTo("Payment method is required.");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getMemberships_whenMembershipsExist_thenReturnMembershipsList()
    {
        restTestClient.get()
                .uri("/api/v1/user/memberships")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getMemberships_whenMembershipsDoNotExist_thenReturnNotFound()
    {
        membershipRepository.deleteAllById(List.of( 1, 2));

        restTestClient.get()
                .uri("/api/v1/user/memberships")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MEMBERSHIP_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Memberships not found.");
    }

    @Test
    void getLastMembership_whenMembershipExists_thenReturnMembership()
    {
        restTestClient.get()
                .uri("/api/v1/user/memberships/last")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user_dni").isEqualTo("87654321")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.next_payment_date").isEqualTo("2099-01-01");
    }
}
