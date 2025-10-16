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
    void generateAccessTokenCookie_WhenUserIsValid_ThenReturnAccessTokenCookie()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        ResponseCookie result = jwtService.generateAccessTokenCookie(expectedUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void generateRefreshTokenCookie_WhenUserIsValid_ThenReturnRefreshTokenCookie()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        ResponseCookie result = jwtService.generateRefreshTokenCookie(expectedUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void getTokenFromCookies_WhenTokenTypeIsValid_ThenReturnToken()
    {
        // Given
        UserEntity user = UserTestDataFactory.userUser();
        String expectedAccessToken = jwtService.generateAccessTokenCookie(user).getValue();


        // When
        Cookie[] cookies = {new Cookie("access-token", expectedAccessToken)};
        String result = jwtService.getTokenFromCookies(cookies, TokenType.ACCESS);

        // Then
        assertEquals(expectedAccessToken, result);
    }

    @Test
    void getTokenFromCookies_WhenCookiesAreNull_ThenThrowException()
    {
        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.getTokenFromCookies(null, TokenType.ACCESS));
    }

    @Test
    void getTokenFromCookies_WhenCookieNotFound_ThenThrowException()
    {
        // Given
        UserEntity user = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        Cookie[] cookies = {new Cookie("ACCESS_TOKEN", accessToken)};

        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.getTokenFromCookies(cookies, TokenType.REFRESH));
    }

    @Test
    void isTokenValid_WhenTokenBelongsToSameUser_ThenReturnTrue()
    {
        // Given
        UserEntity user = UserTestDataFactory.userAdmin();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, user);

        // Then
        assertTrue(result);
    }

    @Test
    void isTokenValid_WhenTokenBelongsToDifferentUser_ThenReturnFalse()
    {
        // Given
        UserEntity user = UserTestDataFactory.userUser();
        UserEntity userAdmin = UserTestDataFactory.userAdmin();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, userAdmin);

        // Then
        assertFalse(result);
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
        UserEntity user = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, user);

        // Then
        assertFalse(result);
    }

    @Test
    void getSubject_WhenTokenIsValid_ThenReturnExpectedDni()
    {
        // Given
        UserEntity user = UserTestDataFactory.userUser();
        String expectedDni = "87654321";
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        String result = jwtService.getSubject(accessToken);

        // Then
        assertEquals(expectedDni, result);
    }

    @Test
    void getSubject_WhenTokenIsMalformed_ThenThrowException()
    {
        // Given
        String malformedToken = "malformedToken";

        // Then
        assertThrows(InvalidTokenException.class, () -> jwtService.getSubject(malformedToken));
    }

    @Test
    void getSubject_WhenTokenIsExpired_ThenThrowException()
    {
        // Given
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );
        UserEntity user = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When & Then
        assertThrows(InvalidTokenException.class, () -> jwtService.getSubject(accessToken));
    }

    @Test
    void emptyCookie_WhenNameIsProvided_ThenReturnEmptyCookie()
    {
        // Given
        String cookieName = "access_token";

        // When
        ResponseCookie result = jwtService.emptyCookie(cookieName);

        // Then
        assertEquals("", result.getValue());
    }
}
