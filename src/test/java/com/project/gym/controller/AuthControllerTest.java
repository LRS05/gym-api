package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest
{
    private static final String SECRET_KEY_TEST = "Z29yZG9ib2xpdmlhbm9sYWNvbmNoYWRldHVtYWRyZXRldm95YXZpb2xhcmRlYWxvdmlvbGFiYWphYWphamRlYXJlbW9nb2xpY28";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void register_whenUserIsNotRegistered_thenRegisterUserAndReturnTokens() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "99999999",
                "Gordomono8!",
                Gender.MALE,
                "Angelo",
                "Rochista"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(cookie().exists("access-token"))
                .andExpect(cookie().exists("refresh-token"))
                .andExpect(status().isCreated());
    }

    @Test
    void register_whenDTOIsInvalid_thenThrowBadRequest() throws Exception
    {
        String invalidDTO = "invalid-dto";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenUserIsAlreadyRegistered_thenThrowConflict() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "46622977",
                "Gordomono8!",
                Gender.MALE,
                "Lorenzo",
                "Sarlo"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void register_whenUserIsAuthenticated_thenThrowForbidden() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "12012012",
                "Gordomono8!",
                Gender.MALE,
                "Enzo",
                "Viviani"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticate_whenCredentialsAreValid_thenReturnAuthenticateAndReturnTokens() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "Gordomono8!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(cookie().exists("access-token"))
                .andExpect(cookie().exists("refresh-token"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_whenCredentialsAreInvalid_thenThrowUnauthorized() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "hola1234"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticate_whenMissingCredentials_thenReturnBadRequest() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "87654321")
    void authenticate_whenUserIsAuthenticated_thenThrowForbidden() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "87654321",
                "Gordomono8!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void refresh_whenRefreshTokenIsValid_thenReturnNewAccessToken() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321").orElse(null);

        String refreshToken = jwtService.generateRefreshTokenCookie(user).getValue();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh-token", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void refresh_whenCookiesDoNotExist_thenThrowUnauthorized() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_whenRefreshTokenCookieDoesNotExist_thenThrowUnauthorized() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("access-token", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_whenUserDoesNotExist_thenThrowNotFound() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321").orElse(null);
        String refreshToken = jwtService.generateRefreshTokenCookie(user).getValue();
        userRepository.deleteById(3);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh-token", refreshToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void refresh_whenTokenIsInvalid_thenThrowUnauthorized() throws Exception
    {
        String invalidRefreshToken = "invalid-refresh-token";

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh-token", invalidRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_whenTokenIsExpired_thenThrowUnauthorized() throws Exception
    {
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );

        UserEntity user =  userRepository.findByDni("87654321").orElse(null);
        String refreshToken = jwtService.generateRefreshTokenCookie(user).getValue();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh-token", refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_whenCookiesAreNotEmpty_thenEmptyCookies() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("access-token", "access-token-value"))
                        .cookie(new Cookie("refresh-token", "refresh-token-value")))
                .andExpect(status().isOk());
    }
}
