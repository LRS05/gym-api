package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.data.UserFactory;
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
    void generateAccessTokenCookie_whenUserExists_thenReturnAccessTokenCookie()
    {
        // Given
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        ResponseCookie result = jwtService.generateAccessTokenCookie(expectedUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void generateRefreshTokenCookie_whenUserExists_thenReturnRefreshTokenCookie()
    {
        // Given
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        ResponseCookie result = jwtService.generateRefreshTokenCookie(expectedUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void getTokenFromCookies_whenTokenCookieExists_thenReturnToken()
    {
        // Given
        UserEntity user = UserFactory.userUser();
        String expectedAccessToken = jwtService.generateAccessTokenCookie(user).getValue();


        // When
        Cookie[] cookies = {new Cookie("access-token", expectedAccessToken)};
        String result = jwtService.getTokenFromCookies(cookies, TokenType.ACCESS);

        // Then
        assertEquals(expectedAccessToken, result);
    }

    @Test
    void getTokenFromCookies_whenCookiesAreNull_thenThrowException()
    {
        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.getTokenFromCookies(null, TokenType.ACCESS));
    }

    @Test
    void getTokenFromCookies_whenTokenCookieDoesNotExist_thenThrowException()
    {
        // Given
        UserEntity user = UserFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        Cookie[] cookies = {new Cookie("ACCESS_TOKEN", accessToken)};

        // When & Then
        assertThrows(CookieNotFoundException.class, () -> jwtService.getTokenFromCookies(cookies, TokenType.REFRESH));
    }

    @Test
    void isTokenValid_whenTokenBelongsToSameUser_thenReturnTrue()
    {
        // Given
        UserEntity user = UserFactory.userAdmin();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, user);

        // Then
        assertTrue(result);
    }

    @Test
    void isTokenValid_whenTokenBelongsToDifferentUser_thenReturnFalse()
    {
        // Given
        UserEntity user = UserFactory.userUser();
        UserEntity userAdmin = UserFactory.userAdmin();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, userAdmin);

        // Then
        assertFalse(result);
    }

    @Test
    void isTokenValid_whenTokenIsExpired_thenReturnFalse()
    {
        // Given
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );
        UserEntity user = UserFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        boolean result = jwtService.isTokenValid(accessToken, user);

        // Then
        assertFalse(result);
    }

    @Test
    void getSubject_whenTokenIsValid_thenReturnDni()
    {
        // Given
        UserEntity user = UserFactory.userUser();
        String expectedDni = "87654321";
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When
        String result = jwtService.getSubject(accessToken);

        // Then
        assertEquals(expectedDni, result);
    }

    @Test
    void getSubject_whenTokenIsInvalid_thenThrowException()
    {
        // Given
        String malformedToken = "malformedToken";

        // Then
        assertThrows(InvalidTokenException.class, () -> jwtService.getSubject(malformedToken));
    }

    @Test
    void getSubject_whenTokenIsExpired_thenThrowException()
    {
        // Given
        jwtService = new JwtService(
                SECRET_KEY_TEST,
                -1,
                -1
        );
        UserEntity user = UserFactory.userUser();
        String accessToken = jwtService.generateAccessTokenCookie(user).getValue();

        // When & Then
        assertThrows(InvalidTokenException.class, () -> jwtService.getSubject(accessToken));
    }

    @Test
    void emptyCookie_whenCookieExists_thenReturnEmptyCookie()
    {
        // Given
        String cookieName = "access_token";

        // When
        ResponseCookie result = jwtService.emptyCookie(cookieName);

        // Then
        assertEquals("", result.getValue());
    }
}
