package com.project.gym.controller;

import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.dto.TokenResponseDTO;
import com.project.gym.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO)
    {
        log.info("Attempting to register a new account with dni={}", requestDTO.dni());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(requestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO requestDTO)
    {
        log.info("Attempting to authenticate with dni={}", requestDTO.dni());
        return ResponseEntity.ok(authService.authenticate(requestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(HttpServletRequest request)
    {
        log.info("Attempting to refresh access token");
        return ResponseEntity.ok(authService.refresh(request));
    }
}
