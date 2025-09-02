package com.project.gym.controller;

import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO requestDTO)
    {
        log.info("Attempting to register a new account with dni={}", requestDTO.dni());

        Map<String, ResponseCookie> cookies = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookies.get("access_token").toString())
                .header(HttpHeaders.SET_COOKIE, cookies.get("refresh_token").toString())
                .body("Register successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@Valid @RequestBody AuthRequestDTO requestDTO)
    {
        log.info("Attempting to authenticate with dni={}", requestDTO.dni());

        Map<String, ResponseCookie> cookies = authService.authenticate(requestDTO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.get("access_token").toString())
                .header(HttpHeaders.SET_COOKIE, cookies.get("refresh_token").toString())
                .body("Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request)
    {
        log.info("Attempting to refresh access token");

        ResponseCookie cookie = authService.refresh(request);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response)
    {
        log.info("Attempting to logout");
        authService.logout(response);
        return ResponseEntity.ok("Logout successful");
    }
}
