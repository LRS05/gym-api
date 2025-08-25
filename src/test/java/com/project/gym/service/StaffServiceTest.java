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
    void getUsers_WhenUserExist_ThenReturnUsers()
    {
        // Given
        List<UserEntity> expectedUsers = UserTestDataFactory.userList();
        expectedUsers.forEach(u -> u.setRole(Role.USER));
        List<UserResponseDTO> expectedDTOs = expectedUsers.stream()
                .map(u -> new UserResponseDTO(
                        u.getId(),
                        u.getRole(),
                        u.getGender(),
                        u.getDni(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getCreationDate()
                ))
                .toList();

        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(expectedUsers);
        when(userMapper.entityToDTO(expectedUsers)).thenReturn(expectedDTOs);

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).entityToDTO(expectedUsers);

        assertEquals(expectedDTOs, result);
        assertTrue(expectedDTOs.stream().allMatch(u -> u.role() == Role.USER));
    }

    @Test
    void getUsers_WhenNoUsersExist_ThenReturnEmptyList()
    {

        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(new ArrayList<>());
        when(userMapper.entityToDTO(anyList())).thenReturn(new ArrayList<>());

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).entityToDTO(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserByDni_WhenUserExistsAndHasRoleUser_ThenReturnUser()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserTestDataFactory.userUser();
        UserResponseDTO expectedDTO = UserTestDataFactory.userUserDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = staffService.getUserByDni(dni);

        // Then
        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUser);

        assertEquals(Role.USER, result.role());
        assertEquals(expectedDTO, result);
    }

    @Test
    void getUserByDni_WhenUserDoesNotExist_ThenThrowException()
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
    void getUserByDni_WhenUserExistsAndHasInvalidRole_ThenThrowException()
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
    void getUserById_WhenUserExistsAndHasRoleUser_ThenReturnUser()
    {
        // Given
        int id = 3;
        UserEntity expectedUser = UserTestDataFactory.userUser();
        UserResponseDTO expectedDTO = UserTestDataFactory.userUserDTO();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = staffService.getUserById(id);

        // Then
        verify(userRepository).findById(id);
        verify(userMapper).entityToDTO(expectedUser);

        assertEquals(Role.USER, result.role());
        assertEquals(expectedDTO, result);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ThenThrowException()
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
    void getUserById_WhenUserExistsAndHasInvalidRole_ThenThrowException()
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
