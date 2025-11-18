package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.exception.InvalidPasswordException;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMe_whenUserExists_thenReturnOwnInfo()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserFactory.userUser();
        UserResponseDTO expectedDTO = UserFactory.userUserDTO();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = userService.getMe();

        // Then
        assertEquals(expectedDTO, result);

        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUser);
    }

    @Test
    void getMe_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getMe());

        verify(userRepository).findByDni(dni);
    }

    @Test
    void deleteMe_whenUserExistsAndPasswordIsCorrect_thenDeleteUser()
    {
        // Given
        String dni = "87654321";
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");
        UserEntity expectedUser = UserFactory.userUser();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(passwordEncoder.matches(requestDTO.password(), expectedUser.getPassword())).thenReturn(true);

        userService.deleteMe(requestDTO);

        // Then
        assertEquals(dni, expectedUser.getDni());

        verify(userRepository).findByDni(dni);
        verify(passwordEncoder).matches(requestDTO.password(), expectedUser.getPassword());
        verify(userRepository).delete(expectedUser);
    }

    @Test
    void deleteMe_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.deleteMe(requestDTO));

        // Then
        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteMe_whenPasswordIsIncorrect_thenThrowException()
    {
        // Given
        String dni = "87654321";
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("monogordo");
        UserEntity expectedUser = UserFactory.userUser();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(passwordEncoder.matches(requestDTO.password(), expectedUser.getPassword())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.deleteMe(requestDTO));

        // Then
        verify(userRepository).findByDni(dni);
        verify(passwordEncoder).matches(requestDTO.password(), expectedUser.getPassword());
        verifyNoMoreInteractions(userRepository);
    }
}
