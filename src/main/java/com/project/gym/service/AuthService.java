package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.*;
import com.project.gym.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService
{
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomMetrics customMetrics;

    public Map<String, ResponseCookie> register(RegisterRequestDTO requestDTO)
    {
        if (userRepository.existsByDni(requestDTO.dni()))
        {
            throw new UserAlreadyRegisteredException("User is already registered.");
        }

        UserEntity user = UserEntity
                .builder()
                .role(Role.USER)
                .gender(requestDTO.gender())
                .dni(requestDTO.dni())
                .password(passwordEncoder.encode(requestDTO.password()))
                .firstName(requestDTO.firstName())
                .lastName(requestDTO.lastName())
                .creationDate(LocalDate.now())
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("Successfully registered a new account with dni={}", requestDTO.dni());
        customMetrics.incrementUsers();

        return jwtService.generateTokenCookies(
                jwtService.generateAccessToken(savedUser),
                jwtService.generateRefreshToken(savedUser)
        );
    }

    public Map<String, ResponseCookie> authenticate(AuthRequestDTO requestDTO)
    {
        // Can throws BadCredentialsException or UsernameNotFoundException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password())
        );

        UserEntity user = findUserByDniOrThrow(requestDTO.dni());
        log.info("Successfully authenticated with dni={}", requestDTO.dni());

        return jwtService.generateTokenCookies(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public ResponseCookie refresh(HttpServletRequest request)
    {
        // Throws exception if cookies == null or token cookie not found.
        String refreshToken = jwtService.extractCookiesToken(request.getCookies(), TokenType.REFRESH_TOKEN);

        UserEntity user = findUserByDniOrThrow(jwtService.extractSubject(refreshToken));
        if (!jwtService.isTokenValid(refreshToken, user))
        {
            throw new InvalidTokenException("Invalid or expired refresh token.");
        }

        log.info("Successfully refreshed access token");
        return jwtService.generateAccessTokenCookie(jwtService.generateAccessToken(user));
    }

    public void logout(HttpServletResponse response)
    {
        ResponseCookie refreshCookie = jwtService.emptyCookie(TokenType.REFRESH_TOKEN.name());
        ResponseCookie accessCookie = jwtService.emptyCookie(TokenType.ACCESS_TOKEN.name());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    private UserEntity findUserByDniOrThrow(String dni)
    {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
