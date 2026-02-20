package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.*;
import com.project.gym.repository.UserRepository;
import com.project.gym.factory.UserFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
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

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenUserIsNotRegistered_thenReturnTokenCookies()
    {
        // Given
        RegisterRequestDTO requestDTO =  new RegisterRequestDTO(
                "87654321",
                "Holatodobien8!",
                Gender.MALE,
                "Franco",
                "Cataldi"
        );
        UserEntity expectedUser =  UserFactory.userUser();
        ResponseCookie expectedAccessTokenCookie = ResponseCookie.from("access-token", "access-token-value").build();
        ResponseCookie expectedRefreshTokenCookie = ResponseCookie.from("refresh-token", "refresh-token-value").build();

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUser);
        when(jwtService.generateAccessTokenCookie(any(UserEntity.class))).thenReturn(expectedAccessTokenCookie);
        when(jwtService.generateRefreshTokenCookie(any(UserEntity.class))).thenReturn(expectedRefreshTokenCookie);

        ArgumentCaptor<UserEntity> captor =  ArgumentCaptor.forClass(UserEntity.class);
        Map<String, ResponseCookie> result = authService.register(requestDTO);

        // Then
        verify(userRepository).save(captor.capture());

        assertEquals(expectedAccessTokenCookie, result.get("access-token"));
        assertEquals(expectedRefreshTokenCookie, result.get("refresh-token"));
        assertEquals(expectedUser.getDni(), captor.getValue().getDni());

        verify(userRepository).existsByDni(requestDTO.dni());
        verify(jwtService).generateAccessTokenCookie(captor.capture());
        verify(jwtService).generateRefreshTokenCookie(captor.capture());
    }

    @Test
    void register_whenUserIsAlreadyRegistered_thenThrowException()
    {
        // Given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "Gordomono8!",
                Gender.MALE,
                "Ulises",
                "Quiroz"
        );

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(true);

        // Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(requestDTO));

        verify(userRepository).existsByDni(requestDTO.dni());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_whenCredentialsAreValid_thenReturnTokenCookies()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "87654321",
                "Gordomono8!"
        );
        UserEntity expectedUser = UserFactory.userUser();
        ResponseCookie expectedAccessTokenCookie = ResponseCookie.from("access-token", "access-token-value").build();
        ResponseCookie expectedRefreshTokenCookie = ResponseCookie.from("access-token", "access-token-value").build();

        // When
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password()));
        when(userRepository.findByDni(requestDTO.dni())).thenReturn(Optional.of(expectedUser));
        when(jwtService.generateAccessTokenCookie(expectedUser)).thenReturn(expectedAccessTokenCookie);
        when(jwtService.generateRefreshTokenCookie(expectedUser)).thenReturn(expectedRefreshTokenCookie);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        Map<String, ResponseCookie> result = authService.authenticate(requestDTO);

        // Then
        verify(authenticationManager).authenticate(captor.capture());

        assertEquals(requestDTO.dni(), captor.getValue().getName());
        assertEquals(expectedAccessTokenCookie, result.get("access-token"));
        assertEquals(expectedRefreshTokenCookie, result.get("refresh-token"));

        verify(userRepository).findByDni(requestDTO.dni());
        verify(jwtService).generateAccessTokenCookie(expectedUser);
        verify(jwtService).generateRefreshTokenCookie(expectedUser);
    }

    @Test
    void authenticate_whenCredentialsAreInvalid_thenThrowException()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "HOLA123"
        );

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> authService.authenticate(requestDTO));

        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "87654321",
                "gordomono"
        );

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(UsernameNotFoundException.class);

        // Then
        assertThrows(UsernameNotFoundException.class, () -> authService.authenticate(requestDTO));

        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void refresh_whenRefreshTokenCookieExists_thenReturnAccessTokenCookie()
    {
        // Given
        String dni = "87654321";
        UserEntity user = UserFactory.userUser();
        String refreshToken = "refresh-token";
        ResponseCookie expectedAccessTokenCookie = ResponseCookie.from("access-token", "access-token-value").build();

        // When
        when(jwtService.getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(jwtService.getSubject(refreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.getTokenType(refreshToken)).thenReturn("refresh");
        when(jwtService.generateAccessTokenCookie(user)).thenReturn(expectedAccessTokenCookie);

        ResponseCookie result = authService.refresh(httpServletRequest.getCookies());

        // Then
        assertEquals(expectedAccessTokenCookie, result);
        assertEquals(expectedAccessTokenCookie.getValue(), result.getValue());

        verify(jwtService).getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH);
        verify(jwtService).getSubject(refreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(refreshToken, user);
        verify(jwtService).getTokenType(refreshToken);
        verify(jwtService).generateAccessTokenCookie(user);
    }

    @Test
    void refresh_whenCookiesAreNull_thenThrowException()
    {
        // When
        when(jwtService.getTokenFromCookies(null, TokenType.REFRESH))
                .thenThrow(CookieNotFoundException.class);

        // Then
        assertThrows(CookieNotFoundException.class, () -> authService.refresh(httpServletRequest.getCookies()));

        verify(jwtService).getTokenFromCookies(null, TokenType.REFRESH);
    }

    @Test
    void refresh_whenRefreshTokenCookieNotFound_thenThrowException()
    {
        // When
        when(jwtService.getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH))
                .thenThrow(CookieNotFoundException.class);

        // Then
        assertThrows(CookieNotFoundException.class, () -> authService.refresh(httpServletRequest.getCookies()));

        verify(jwtService).getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH);
    }

    @Test
    void refresh_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String refreshToken = "refresh-token";
        String dni = "99999999";

        // When
        when(jwtService.getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH))
                .thenReturn(refreshToken);
        when(jwtService.getSubject(refreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> authService.refresh(httpServletRequest.getCookies()));

        verify(jwtService).getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH);
        verify(jwtService).getSubject(refreshToken);
        verify(userRepository).findByDni(dni);
    }

    @Test
    void refresh_whenTokenIsInvalidOrExpired_thenThrowException()
    {
        // Given
        String dni = "87654321";
        UserEntity user = UserFactory.userAdmin();
        String refreshToken = "refresh-token";

        // When
        when(jwtService.getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(jwtService.getSubject(refreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(user));
        when(jwtService.getTokenType(refreshToken)).thenReturn("refresh");
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        // Then
        assertThrows(InvalidTokenException.class, () -> authService.refresh(httpServletRequest.getCookies()));

        verify(jwtService).getTokenFromCookies(httpServletRequest.getCookies(), TokenType.REFRESH);
        verify(jwtService).getSubject(refreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(refreshToken, user);
    }

    @Test
    void logout_whenUserHasTokenCookies_thenEmptyCookies()
    {
        // Given
        ResponseCookie expectedEmptyAccessTokenCookie = ResponseCookie.from("access-token", "").build();
        ResponseCookie expectedEmptyRefreshTokenCookie = ResponseCookie.from("refresh-token", "").build();

        // When
        when(jwtService.emptyCookie("access-token")).thenReturn(expectedEmptyAccessTokenCookie);
        when(jwtService.emptyCookie("refresh-token")).thenReturn(expectedEmptyRefreshTokenCookie);

        authService.emptyCookies();

        // Then
        verify(jwtService).emptyCookie("access-token");
        verify(jwtService).emptyCookie("refresh-token");
    }
}
