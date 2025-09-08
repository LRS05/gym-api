package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.data.UserTestDataFactory;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.CookieNotFoundException;
import com.project.gym.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest
{
    private static final String SECRET_KEY_TEST = "Z29yZG9ib2xpdmlhbm9sYWNvbmNoYWRldHVtYWRyZXRldm95YXZpb2xhcmRlYWxvdmlvbGFiYWphYWphamRlYXJlbW9nb2xpY28";

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp()
    {
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                900000,
                3600000
        );
    }

    @Test
    void generateAccessToken_WhenUserIsValid_ThenReturnAccessToken()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        String result = jwtService.generateAccessToken(expectedUser);
        String extractedDni = jwtService.extractSubject(result);

        // Then
        assertNotNull(result);
        assertEquals(expectedUser.getDni(), extractedDni);
    }

    @Test
    void generateRefreshToken_WhenUserIsValid_ThenReturnRefreshToken()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        String result = jwtService.generateRefreshToken(expectedUser);
        String extractedDni = jwtService.extractSubject(result);

        // Then
        assertNotNull(result);
        assertEquals(expectedUser.getDni(), extractedDni);
    }

    @Test
    void generateTokenCookies_WhenTokensAreValid_ThenReturnCookies()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessToken(expectedUser);
        String refreshToken = jwtService.generateRefreshToken(expectedUser);

        // When
        Map<String, ResponseCookie> result = jwtService.generateTokenCookies(accessToken, refreshToken);

        // Then
        assertEquals(accessToken, result.get("access_token").getValue());
        assertEquals(refreshToken, result.get("refresh_token").getValue());
    }

    @Test
    void generateAccessTokenCookie_WhenTokenIsValid_ThenReturnCookie()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessToken(expectedUser);

        // When
        ResponseCookie result = jwtService.generateAccessTokenCookie(accessToken);

        // Then
        assertEquals(accessToken, result.getValue());
    }

    @Test
    void extractCookiesToken_WhenTokenTypeIsValid_ThenReturnToken()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessToken(expectedUser);

        Cookie[] cookies = {new Cookie("ACCESS_TOKEN", accessToken)};

        // When
        String result = jwtService.extractCookiesToken(cookies, TokenType.ACCESS_TOKEN);

        // Then
        assertEquals(accessToken, result);
    }

    @Test
    void extractCookiesToken_WhenCookiesAreNull_ThenThrowException()
    {
        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.extractCookiesToken(null, TokenType.ACCESS_TOKEN));
    }

    @Test
    void extractCookiesToken_WhenCookieNotFound_ThenThrowException()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessToken(expectedUser);

        Cookie[] cookies = {new Cookie("ACCESS_TOKEN", accessToken)};

        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.extractCookiesToken(cookies, TokenType.REFRESH_TOKEN));
    }

    @Test
    void isTokenValid_WhenTokenBelongsToSameUser_ThenReturnTrue()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        String result = jwtService.generateAccessToken(expectedUser);

        // Then
        assertTrue(jwtService.isTokenValid(result, expectedUser));
    }

    @Test
    void isTokenValid_WhenTokenBelongsToDifferentUser_ThenReturnFalse()
    {
        // Given
        UserEntity expectedTokenUser = UserTestDataFactory.userAdmin();
        UserEntity expectedUser = UserTestDataFactory.userUser();

        // When
        String result = jwtService.generateAccessToken(expectedTokenUser);

        // Then
        assertFalse(jwtService.isTokenValid(result, expectedUser));
    }

    @Test
    void isTokenValid_WhenTokenIsExpired_ThenReturnFalse()
    {
        // Given
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        String result = jwtService.generateAccessToken(expectedUser);

        // Then
        assertFalse(jwtService.isTokenValid(result, expectedUser));
    }

    @Test
    void extractSubject_WhenTokenIsValid_ThenReturnExpectedDni()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userStaff();
        String dni = "12345678";

        // When
        String result = jwtService.generateAccessToken(expectedUser);

        // Then
        assertEquals(dni, jwtService.extractSubject(result));
    }

    @Test
    void extractSubject_WhenTokenIsMalformed_ThenThrowException()
    {
        // Given
        String malformedToken = "malformedToken";

        // Then
        assertThrows(InvalidTokenException.class, () -> jwtService.extractSubject(malformedToken));
    }

    @Test
    void extractSubject_WhenTokenIsExpired_ThenThrowException()
    {
        // Given
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );
        UserEntity expectedUser = UserTestDataFactory.userUser();

        // When
        String expiredToken = jwtService.generateAccessToken(expectedUser);

        // Then
        assertThrows(InvalidTokenException.class, () -> jwtService.extractSubject(expiredToken));
    }

    @Test
    void emptyCookie_WhenCookieHasTooken_ThenReturnEmptyCookie()
    {
        // Given
        UserEntity user = UserTestDataFactory.userUser();
        ResponseCookie refreshTokenCookie = jwtService.generateRefreshTokenCookie(jwtService.generateRefreshToken(user));
        String cookieName = TokenType.REFRESH_TOKEN.name();
        ResponseCookie expectedEmptyCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        // When & Then
        ResponseCookie result = jwtService.emptyCookie(refreshTokenCookie.getName());

        assertEquals(expectedEmptyCookie, result);
    }
}
