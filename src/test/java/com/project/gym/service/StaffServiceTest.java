package com.project.gym.service;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaffServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StaffService staffService;

    @Test
    void getUsersTest()
    {
        // Given
        List<UserEntity> expectedUsers = UserTestDataFactory.userList();
        expectedUsers.forEach(u -> u.setRole(Role.USER));
        List<UserResponseDTO> expectedDTOs = expectedUsers.stream()
                .map(u -> new UserResponseDTO(
                        u.getId(),
                        u.getRole(),
                        u.getDni(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getCreationDate()
                ))
                .toList();

        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(expectedUsers);
        when(userMapper.toDTO(expectedUsers)).thenReturn(expectedDTOs);

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).toDTO(expectedUsers);

        assertEquals(expectedDTOs, result);
    }

    @Test
    void getUsersEmptyListTest()
    {

        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(new ArrayList<>());
        when(userMapper.toDTO(anyList())).thenReturn(new ArrayList<>());

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).toDTO(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserByDniTest()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserTestDataFactory.userUser();
        UserResponseDTO expectedDTO = UserTestDataFactory.userUserDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.toDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = staffService.getUserByDni(dni);

        // Then
        verify(userRepository).findByDni(dni);
        verify(userMapper).toDTO(expectedUser);

        assertEquals(Role.USER, result.role());
        assertEquals(expectedDTO, result);
    }

    @Test
    void getUserByDniNotFoundTest()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> staffService.getUserByDni(dni));

        // Then
        verify(userRepository).findByDni(dni);
    }

    @Test
    void getUserByDniAccessDeniedTest()
    {
        // Given
        String dni = "46622977";
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        assertThrows(AccessDeniedException.class, () -> staffService.getUserByDni(dni));

        // Then
        verify(userRepository).findByDni(dni);

        assertNotEquals(Role.USER, expectedUser.getRole());
    }

    @Test
    void getUserByIdTest()
    {
        // Given
        int id = 3;
        UserEntity expectedUser = UserTestDataFactory.userUser();
        UserResponseDTO expectedDTO = UserTestDataFactory.userUserDTO();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));
        when(userMapper.toDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = staffService.getUserById(id);

        // Then
        verify(userRepository).findById(id);
        verify(userMapper).toDTO(expectedUser);

        assertEquals(Role.USER, result.role());
        assertEquals(expectedDTO, result);
    }

    @Test
    void getUserByIdNotFoundTest()
    {
        // Given
        int id = 99999999;

        // When
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> staffService.getUserById(id));

        // Then
        verify(userRepository).findById(id);
    }

    @Test
    void getUserByIdAccessDeniedTest()
    {
        // Given
        int id = 1;
        UserEntity expectedUser = UserTestDataFactory.userAdmin();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));
        assertThrows(AccessDeniedException.class, () -> staffService.getUserById(id));

        // Then
        verify(userRepository).findById(id);

        assertNotEquals(Role.USER, expectedUser.getRole());
    }
}
