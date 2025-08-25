package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.data.UserTestDataFactory;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest
{
    private static final String SECRET_KEY_TEST = "Z29yZG9ib2xpdmlhbm9sYWNvbmNoYWRldHVtYWRyZXRldm95YXZpb2xhcmRlYWxvdmlvbGFiYWphYWphamRlYXJlbW9nb2xpY28";
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
    void isRefreshToken_WhenTokenIsRefresh_ThenReturnTrue()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String refreshToken = jwtService.generateRefreshToken(expectedUser);

        // When
        boolean result = jwtService.isRefreshToken(refreshToken);

        // Then
        assertTrue(result);
    }

    @Test
    void isRefreshToken_WhenTokenIsAccess_ThenReturnFalse()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String accessToken = jwtService.generateAccessToken(expectedUser);

        // When
        boolean result = jwtService.isRefreshToken(accessToken);

        // Then
        assertFalse(result);
    }

    @Test
    void isRefreshToken_WhenTokenIsInvalidOrExpired_ThenReturnFalse()
    {
        // Given
        String invalidToken = "TOKEN";

        // When
        boolean result = jwtService.isRefreshToken(invalidToken);

        // Then
        assertFalse(result);
    }

    @Test
    void extractSubject_WhenAccessTokenIsValid_ThenReturnExpectedDni()
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
        assertThrows(MalformedJwtException.class, () -> jwtService.extractSubject(malformedToken));
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
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractSubject(expiredToken));
    }
}
