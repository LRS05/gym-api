package com.project.gym.service;

import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.dto.TokenResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.exception.UserAlreadyRegisteredException;
import com.project.gym.exception.InvalidTokenException;
import com.project.gym.exception.MissingTokenException;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserTestDataFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTest()
    {
        // Given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "gordomono",
                "Franco",
                "Cataldi"
        );
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUser);
        when(jwtService.generateAccessToken(expectedUser)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(expectedUser)).thenReturn(refreshToken);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        TokenResponseDTO result = authService.register(requestDTO);

        // Then
        verify(userRepository).existsByDni(requestDTO.dni());
        verify(userRepository).save(captor.capture());
        verify(jwtService).generateAccessToken(expectedUser);
        verify(jwtService).generateRefreshToken(expectedUser);

        assertEquals(requestDTO.dni(), captor.getValue().getDni());
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
    }

    @Test
    void registerUserAlreadyRegisteredTest()
    {
        // Given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "gordomono",
                "Ulises",
                "Quiroz"
        );

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(true);

        assertThrows(UserAlreadyRegisteredException.class, () -> authService.register(requestDTO));

        // Then
        verify(userRepository).existsByDni(requestDTO.dni());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticateTest()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "gordomono"
        );
        UserEntity expectedUser = UserTestDataFactory.userAdmin();
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(
                new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password())
        );
        when(userRepository.findByDni(requestDTO.dni())).thenReturn(Optional.of(expectedUser));
        when(jwtService.generateAccessToken(expectedUser)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(expectedUser)).thenReturn(refreshToken);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        TokenResponseDTO result = authService.authenticate(requestDTO);

        // Then
        verify(authenticationManager).authenticate(captor.capture());
        verify(userRepository).findByDni(requestDTO.dni());
        verify(jwtService).generateAccessToken(expectedUser);
        verify(jwtService).generateRefreshToken(expectedUser);

        assertEquals(requestDTO.dni(), captor.getValue().getName());
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
    }

    @Test
    void authenticateInvalidCredentialsTest()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "HOLA123"
        );

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(BadCredentialsException.class);
        assertThrows(BadCredentialsException.class, () -> authService.authenticate(requestDTO));

        // Then
        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticateUserNotFoundTest()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "87654321",
                "gordomono"
        );

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(UsernameNotFoundException.class);

        assertThrows(UsernameNotFoundException.class, () -> authService.authenticate(requestDTO));

        // Then
        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshTest()
    {
        // Given
        String dni = "46622977";
        UserEntity expectedUser = UserTestDataFactory.userAdmin();
        String refreshToken = "refresh-token";
        String accessToken = "new-access-token";

        // When
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractSubject(refreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(jwtService.isTokenValid(refreshToken, expectedUser)).thenReturn(true);
        when(jwtService.generateAccessToken(expectedUser)).thenReturn(accessToken);

        TokenResponseDTO result = authService.refresh(httpServletRequest);

        // Then
        verify(httpServletRequest).getHeader("Authorization");
        verify(jwtService).extractSubject(refreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(refreshToken, expectedUser);
        verify(jwtService).generateAccessToken(expectedUser);

        assertEquals(accessToken, result.accessToken());
        assertNull(result.refreshToken());
    }

    @Test
    void refreshInvalidHeaderTest()
    {
        // When
        when(httpServletRequest.getHeader("Authorization")).thenReturn("invalid-header ");
        assertThrows(MissingTokenException.class, () -> authService.refresh(httpServletRequest));

        // Then
        verify(httpServletRequest).getHeader("Authorization");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshInvalidDniTest()
    {
        // Given
        String refreshToken = "refresh-token";

        // When
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractSubject(refreshToken)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> authService.refresh(httpServletRequest));

        // Then
        verify(httpServletRequest).getHeader("Authorization");
        verify(jwtService).extractSubject(refreshToken);
    }

    @Test
    void refreshInvalidOrExpiredTokenTest()
    {
        // Given
        String dni = "46622977";
        String refreshToken = "invalid-refresh-token";
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractSubject(refreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(jwtService.isTokenValid(refreshToken, expectedUser)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.refresh(httpServletRequest));

        // Then
        verify(httpServletRequest).getHeader("Authorization");
        verify(jwtService).extractSubject(refreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(refreshToken, expectedUser);
    }

}
