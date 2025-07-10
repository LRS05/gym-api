package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.data.UserTestDataFactory;
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
    void generateAccessTokenTest()
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
    void generateRefreshTokenTest()
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
    void isTokenValidTest()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        String result = jwtService.generateAccessToken(expectedUser);

        // Then
        assertTrue(jwtService.isTokenValid(result, expectedUser));
    }

    @Test
    void isTokenValidInvalidSubjectTest()
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
    void isTokenValidExpiredTokenTest()
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
    void extractDniTest()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userStaff();
        String dni = "12345678";

        // When
        String result = jwtService.generateAccessToken(expectedUser);

        // Then
        assertEquals(dni, jwtService.extractSubject(result));
    }

}
