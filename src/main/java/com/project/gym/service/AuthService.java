package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.*;
import com.project.gym.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            throw new UserAlreadyExistsException("User is already registered.");
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
        log.debug("Registered a new account with dni={}", requestDTO.dni());
        customMetrics.incrementUsers();
        return generateTokenCookies(savedUser);
    }

    public Map<String, ResponseCookie> authenticate(AuthRequestDTO requestDTO)
    {
        // Can throw BadCredentialsException or UsernameNotFoundException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password())
        );

        UserEntity user = findUserByDniOrThrow(requestDTO.dni());

        log.debug("Authenticated with dni={}", requestDTO.dni());
        return generateTokenCookies(user);
    }

    public ResponseCookie refresh(Cookie[] cookies)
    {
        // Throws exception if cookies == null or token cookie not found.
        String refreshToken = jwtService.getTokenFromCookies(cookies, TokenType.REFRESH);

        UserEntity user = findUserByDniOrThrow(jwtService.getSubject(refreshToken));
        if (!jwtService.getTokenType(refreshToken).equals("refresh") || !jwtService.isTokenValid(refreshToken, user))
        {
            throw new InvalidTokenException("Invalid or expired refresh token.");
        }
        log.debug("Refreshed access token");
        return jwtService.generateAccessTokenCookie(user);
    }

    public Map<String, ResponseCookie> emptyCookies()
    {
        return Map.of(
                "access-token", jwtService.emptyCookie("access-token"),
                "refresh-token", jwtService.emptyCookie("refresh-token")
        );
    }

    private Map<String, ResponseCookie> generateTokenCookies(UserEntity user)
    {
        return Map.of(
                "access-token", jwtService.generateAccessTokenCookie(user),
                "refresh-token", jwtService.generateRefreshTokenCookie(user)
        );
    }

    private UserEntity findUserByDniOrThrow(String dni)
    {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
