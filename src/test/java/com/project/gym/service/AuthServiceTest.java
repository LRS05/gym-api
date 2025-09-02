package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.AuthRequestDTO;
import com.project.gym.dto.RegisterRequestDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.*;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserTestDataFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private HttpServletResponse httpServletResponse;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WheUserNotRegistered_ThenCreateUserAndReturnTokens()
    {
        // Given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "gordomono",
                Gender.MALE,
                "Franco",
                "Cataldi"
        );
        UserEntity expectedUser = UserTestDataFactory.userUser();
        ResponseCookie expectedAccessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", "access-token").httpOnly(true).build();
        ResponseCookie expectedRefreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", "refresh-token").httpOnly(true).build();

        Map<String, ResponseCookie> expectedCookies = Map.of(
                "access_token", expectedAccessTokenCookie,
                "refresh_token", expectedRefreshTokenCookie
        );

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUser);
        when(jwtService.generateAccessToken(expectedUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(expectedUser)).thenReturn("refresh-token");
        when(jwtService.generateTokenCookies("access-token", "refresh-token")).thenReturn(expectedCookies);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        Map<String, ResponseCookie> result = authService.register(requestDTO);

        // Then
        verify(userRepository).existsByDni(requestDTO.dni());
        verify(userRepository).save(captor.capture());
        verify(jwtService).generateAccessToken(expectedUser);
        verify(jwtService).generateRefreshToken(expectedUser);
        verify(jwtService).generateTokenCookies("access-token", "refresh-token");

        assertEquals(requestDTO.dni(), captor.getValue().getDni());
        assertEquals(expectedCookies, result);
    }

    @Test
    void register_WhenUserAlreadyRegistered_ThenThrowException()
    {
        // Given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "87654321",
                "gordomono",
                Gender.MALE,
                "Ulises",
                "Quiroz"
        );

        // When
        when(userRepository.existsByDni(requestDTO.dni())).thenReturn(true);

        // Then
        assertThrows(UserAlreadyRegisteredException.class, () -> authService.register(requestDTO));

        verify(userRepository).existsByDni(requestDTO.dni());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_WhenCredentialsAreValid_ThenReturnTokens()
    {
        // Given
        AuthRequestDTO requestDTO = new AuthRequestDTO(
                "46622977",
                "gordomono"
        );
        UserEntity expectedUser = UserTestDataFactory.userAdmin();
        ResponseCookie expectedAccessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", "access-token").httpOnly(true).build();
        ResponseCookie expectedRefreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", "refresh-token").httpOnly(true).build();

        Map<String, ResponseCookie> expectedCookies = Map.of(
                "access_token", expectedAccessTokenCookie,
                "refresh_token", expectedRefreshTokenCookie
        );

        // When
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(
                new UsernamePasswordAuthenticationToken(requestDTO.dni(), requestDTO.password())
        );
        when(userRepository.findByDni(requestDTO.dni())).thenReturn(Optional.of(expectedUser));
        when(jwtService.generateAccessToken(expectedUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(expectedUser)).thenReturn("refresh-token");
        when(jwtService.generateTokenCookies("access-token", "refresh-token")).thenReturn(expectedCookies);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        Map<String, ResponseCookie> result = authService.authenticate(requestDTO);

        // Then
        verify(authenticationManager).authenticate(captor.capture());
        verify(userRepository).findByDni(requestDTO.dni());
        verify(jwtService).generateAccessToken(expectedUser);
        verify(jwtService).generateRefreshToken(expectedUser);
        verify(jwtService).generateTokenCookies("access-token", "refresh-token");

        assertEquals(requestDTO.dni(), captor.getValue().getName());
        assertEquals(expectedCookies, result);
    }

    @Test
    void authenticate_WhenCredentialsAreInvalid_ThenThrowException()
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
    void authenticate_WhenUserDoesNotExist_ThenThrowException()
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
    void refresh_WhenCookiesAndTokenAreValid_ThenReturnNewAccessToken()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String expectedRefreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        ResponseCookie newAccessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", "new-access-token").httpOnly(true).build();

        // When
        when(jwtService.extractCookiesToken(httpServletRequest.getCookies(), TokenType.REFRESH_TOKEN)).thenReturn(expectedRefreshToken);
        when(jwtService.extractSubject(expectedRefreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(jwtService.isTokenValid(expectedRefreshToken, expectedUser)).thenReturn(true);
        when(jwtService.generateAccessToken(expectedUser)).thenReturn(newAccessToken);
        when(jwtService.generateAccessTokenCookie(newAccessToken)).thenReturn(newAccessTokenCookie);

        ResponseCookie result = authService.refresh(httpServletRequest);

        // Then
        verify(jwtService).extractCookiesToken(httpServletRequest.getCookies(), TokenType.REFRESH_TOKEN);
        verify(jwtService).extractSubject(expectedRefreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(expectedRefreshToken, expectedUser);
        verify(jwtService).generateAccessToken(expectedUser);
        verify(jwtService).generateAccessTokenCookie(newAccessToken);

        assertEquals(newAccessTokenCookie, result);
        assertEquals(newAccessToken, result.getValue());
    }


    @Test
    void refresh_WhenCookiesAreNull_ThenThrowException()
    {
        // When
        when(httpServletRequest.getCookies()).thenReturn(null);
        when(jwtService.extractCookiesToken(null, TokenType.REFRESH_TOKEN)).thenThrow(CookieNotFoundException.class);

        // Then
        assertThrows(CookieNotFoundException.class, () -> authService.refresh(httpServletRequest));

        verify(httpServletRequest).getCookies();
        verify(jwtService).extractCookiesToken(null, TokenType.REFRESH_TOKEN);
    }

    @Test
    void refresh_WhenRefreshTokenCookieNotFound_ThenThrowException()
    {
        // Given
        Cookie[] cookies = {new Cookie("EXPECTED-ACCESS-TOKEN", "expected-access-token")};

        // When
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(jwtService.extractCookiesToken(cookies, TokenType.REFRESH_TOKEN)).thenThrow(CookieNotFoundException.class);

        // Then
        assertThrows(CookieNotFoundException.class, () -> authService.refresh(httpServletRequest));

        verify(httpServletRequest).getCookies();
    }

    @Test
    void refresh_WhenUserDoesNotExist_ThenThrowException()
    {
        // Given
        String dni = "99999999";
        String expectedRefreshToken = "expected-refresh-token";
        Cookie[] cookies = {new Cookie("EXPECTED-REFRESH-TOKEN", expectedRefreshToken)};

        // When
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(jwtService.extractCookiesToken(cookies, TokenType.REFRESH_TOKEN)).thenReturn(expectedRefreshToken);
        when(jwtService.extractSubject(expectedRefreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> authService.refresh(httpServletRequest));

        verify(httpServletRequest).getCookies();
        verify(jwtService).extractCookiesToken(cookies, TokenType.REFRESH_TOKEN);
        verify(jwtService).extractSubject(expectedRefreshToken);
        verify(userRepository).findByDni(dni);
    }

    @Test
    void refresh_WhenTokenIsInvalidOrExpired_ThenThrowException()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String expectedRefreshToken = "expected-refresh-token";
        Cookie[] cookies = {new Cookie("EXPECTED-REFRESH-TOKEN", expectedRefreshToken)};

        // When
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(jwtService.extractCookiesToken(cookies, TokenType.REFRESH_TOKEN)).thenReturn(expectedRefreshToken);
        when(jwtService.extractSubject(expectedRefreshToken)).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(jwtService.isTokenValid(expectedRefreshToken, expectedUser)).thenReturn(false);

        // Then
        assertThrows(InvalidTokenException.class, () -> authService.refresh(httpServletRequest));

        verify(httpServletRequest).getCookies();
        verify(jwtService).extractCookiesToken(cookies, TokenType.REFRESH_TOKEN);
        verify(jwtService).extractSubject(expectedRefreshToken);
        verify(userRepository).findByDni(dni);
        verify(jwtService).isTokenValid(expectedRefreshToken, expectedUser);
    }

    @Test
    void logout_WhenUserHasValidTokens_ThenEmptyTokenCookies()
    {
        // Given
        ResponseCookie expectedAccessTokenEmptyCookie = ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie expectedRefreshTokenEmptyCookie = ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        // When
        when(jwtService.emptyCookie(TokenType.REFRESH_TOKEN.name())).thenReturn(expectedRefreshTokenEmptyCookie);
        when(jwtService.emptyCookie(TokenType.ACCESS_TOKEN.name())).thenReturn(expectedAccessTokenEmptyCookie);

        authService.logout(httpServletResponse);

        // Then
        verify(jwtService).emptyCookie(TokenType.REFRESH_TOKEN.name());
        verify(jwtService).emptyCookie(TokenType.ACCESS_TOKEN.name());
    }

}
