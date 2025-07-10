package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.dto.TokenResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.exception.UserAlreadyRegisteredException;
import com.project.gym.exception.InvalidTokenException;
import com.project.gym.exception.MissingTokenException;
import com.project.gym.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

    public TokenResponseDTO register(RegisterRequestDTO requestDTO)
    {
        if (userRepository.existsByDni(requestDTO.dni()))
        {
            throw new UserAlreadyRegisteredException("User is already registered.");
        }

        UserEntity user = UserEntity
                .builder()
                .role(Role.USER)
                .dni(requestDTO.dni())
                .password(passwordEncoder.encode(requestDTO.password()))
                .firstName(requestDTO.firstName())
                .lastName(requestDTO.lastName())
                .creationDate(LocalDate.now())
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("Successfully registered a new account with DNI {}", requestDTO.dni());
        customMetrics.incrementUsers();

        return new TokenResponseDTO(
                jwtService.generateAccessToken(savedUser),
                jwtService.generateRefreshToken(savedUser)
        );
    }

    public TokenResponseDTO authenticate(AuthRequestDTO requestDTO)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password())
        );

        UserEntity user = findUserByDniOrThrow(requestDTO.dni());
        log.info("Successfully authenticated");

        return new TokenResponseDTO(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public TokenResponseDTO refresh(HttpServletRequest request)
    {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new MissingTokenException("Invalid authentication header.");
        }

        String refreshToken = authHeader.substring(7);
        String dni = jwtService.extractSubject(refreshToken);

        UserEntity user = findUserByDniOrThrow(dni);

        if (!jwtService.isTokenValid(refreshToken, user))
        {
            throw new InvalidTokenException("Invalid or expired refresh token.");
        }

        log.info("Access token refreshed successfully");
        return new TokenResponseDTO(
                jwtService.generateAccessToken(user),
                null
        );
    }

    private UserEntity findUserByDniOrThrow(String dni)
    {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
