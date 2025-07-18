package com.project.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private JwtService jwtService;


    @Test
    void registerTest() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "99999999",
                "Abc123!!",
                "Angelo",
                "Rochista"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerInvalidDTOTest() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                null,
                null,
                "Angelo",
                "Rochista"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUserAlreadyRegisteredTest() throws Exception
    {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "46622977",
                "Gordomono8!",
                "Lorenzo",
                "Sarlo"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void authenticateTest() throws Exception
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
    void authenticateIncorrectCredentialsTest() throws Exception
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
    void authenticateInvalidDTOTest() throws Exception
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
    void refreshTest() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321")
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String refreshToken = jwtService.generateRefreshToken(user);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isEmpty());
    }

    @Test
    void refreshMissingTokenTest() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshUserNotFoundTest() throws Exception
    {
        UserEntity user = userRepository.findByDni("87654321")
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String refreshToken = jwtService.generateRefreshToken(user);

        userRepository.delete(user);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isNotFound());
    }
}
