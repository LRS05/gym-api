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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerIntegrationTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void register_WhenValidRequest_ThenReturnCreated() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "99999999",
                "Abc123!!",
                Gender.MALE,
                "Angelo",
                "Rochista"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_WhenInvalidRequest_ThenReturnBadRequest() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                null,
                null,
                Gender.MALE,
                "Angelo",
                "Rochista"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenUserAlreadyExists_ThenReturnConflict() throws Exception
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
    void authenticate_WhenValidCredentials_ThenReturnOk() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "gordomono"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_WhenInvalidCredentials_ThenReturnUnauthorized() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "abc123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticate_WhenMissingCredentials_ThenReturnBadRequest() throws Exception
    {
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                null,
                null
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_WhenValidRefreshToken_ThenReturnNewAccessToken() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321")
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String refreshToken = jwtService.generateRefreshToken(user);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("ACCESS_TOKEN"));
    }

    @Test
    void refresh_WhenMissingCookies_ThenReturnUnauthorized() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WhenRefreshTokenCookieDoesNotExist_ThenReturnUnauthorized() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321")
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String accessToken = jwtService.generateAccessToken(user);

        userRepository.delete(user);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("ACCESS_TOKEN", accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WhenUserNotFound_ThenReturnNotFound() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321")
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String refreshToken = jwtService.generateRefreshToken(user);

        userRepository.delete(user);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", refreshToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void refresh_WhenTokenIsInvalidOrExpired_ThenReturnUnauthorized() throws Exception
    {
        String invalidRefreshToken = "invalid-refresh-token";

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", invalidRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "46622977")
    void logout_WhenUserIsAuthenticated_ThenReturnOk() throws Exception
    {
        UserEntity user = userRepository.findByDni("46622977")
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("ACCESS_TOKEN", accessToken))
                        .cookie(new Cookie("REFRESH_TOKEN", refreshToken)))
                .andExpect(status().isOk());
    }
}
