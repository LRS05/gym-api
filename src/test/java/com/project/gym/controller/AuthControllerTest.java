package com.project.gym.controller;

import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerTest
{
    @LocalServerPort
    int port;

    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp()
    {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void register_whenUserIsNotRegistered_thenRegisterUserAndReturnCookies()
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "10101010",
                "Gatorade123!",
                Gender.MALE,
                "Enzo",
                "Viviani"
        );

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void register_whenDTOFieldsAreInvalid_thenReturnBadRequest()
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "123",
                "hello123",
                Gender.MALE,
                "Enzo123",
                "Hola!!!"
        );

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.dni").exists()
                .jsonPath("$.firstName").exists()
                .jsonPath("$.lastName").exists()
                .jsonPath("$.password").exists();
    }

    @Test
    void register_whenUserIsAlreadyRegistered_thenReturnConflict()
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "Gatorade123!",
                Gender.MALE,
                "Enzo",
                "Viviani"
        );

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.error").isEqualTo("USER_ALREADY_EXISTS")
                .jsonPath("$.message").isEqualTo("User is already registered.");
    }

    @Test
    void register_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void register_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {
        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("Dni is required.")
                .jsonPath("$.password").isEqualTo("Password is required.");
    }

    @Test
    void authenticate_whenCredentialsAreValid_thenReturnCookies()
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "Gordomono8!"
        );

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("access-token")
                .expectCookie().exists("refresh-token");
    }

    @Test
    void authenticate_whenCredentialsAreInvalid_thenReturnUnauthorized()
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "Gatorade123!"
        );

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("BAD_CREDENTIALS")
                .jsonPath("$.message").isEqualTo("Bad credentials");
    }

    @Test
    void authenticate_whenRequestBodyIsMalformed_thenReturnBadRequest()
    {
        String invalidDTO = "INVALID_REQUEST_BODY";

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("HTTP_MESSAGE_NOT_READABLE")
                .jsonPath("$.message").isEqualTo("Invalid request body.");
    }

    @Test
    void authenticate_whenRequestBodyIsEmpty_thenReturnBadRequest()
    {

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.dni").isEqualTo("Dni is required.")
                .jsonPath("$.password").isEqualTo("Password is required.");
    }

    @Test
    void refresh_whenRefreshTokenIsValid_thenReturnNewAccessTokenCookie()
    {
        UserEntity user = userRepository.findById(1)
                .orElse(null);

        ResponseCookie refreshTokenCookie = jwtService.generateRefreshTokenCookie(user);

        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .cookie("refresh-token", refreshTokenCookie.getValue())
                .exchange()
                .expectStatus().isNoContent()
                .expectCookie().exists("access-token");
    }

    @Test
    void refresh_whenRefreshTokenCookieIsEmpty_thenReturnUnauthorized()
    {
        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .cookie("refresh-token", "")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void refresh_whenTokenOwnerDoesNotExist_thenReturnNotFound()
    {
        UserEntity user = userRepository.findById(1)
                .orElse(null);

        ResponseCookie refreshTokenCookie = jwtService.generateRefreshTokenCookie(user);

        userRepository.delete(user);

        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .cookie("refresh-token", refreshTokenCookie.getValue())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void accessAuthUrls_whenUserIsAuthenticated_thenReturnForbidden()
    {
        UserEntity user = userRepository.findById(1)
                .orElse(null);

        ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(user);

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .cookie("access-token", accessTokenCookie.getValue())
                .exchange()
                .expectStatus().isForbidden();
    }
}
