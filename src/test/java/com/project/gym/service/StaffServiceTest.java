package com.project.gym.service;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserFactory;
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
    void getUsers_whenUsersExist_thenReturnUserList()
    {
        // Given
        List<UserEntity> expectedUsers = List.of(UserFactory.userUser());
        List<UserResponseDTO> expectedDTOs = List.of(UserFactory.userUserDTO());

        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(expectedUsers);
        when(userMapper.entityToDTO(expectedUsers)).thenReturn(expectedDTOs);

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        assertEquals(expectedDTOs, result);
        assertTrue(expectedDTOs.stream().allMatch(u -> u.role() == Role.USER));

        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).entityToDTO(expectedUsers);
    }

    @Test
    void getUsers_whenUsersDoNotExist_thenReturnEmptyList()
    {
        // When
        when(userRepository.findAllByRole(Role.USER)).thenReturn(new ArrayList<>());
        when(userMapper.entityToDTO(anyList())).thenReturn(new ArrayList<>());

        List<UserResponseDTO> result = staffService.getUsers();

        // Then
        assertTrue(result.isEmpty());

        verify(userRepository).findAllByRole(Role.USER);
        verify(userMapper).entityToDTO(anyList());
    }

    @Test
    void getUserByDni_whenUserExistsAndIsNotAdminOrStaff_thenReturnUser()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserFactory.userUser();
        UserResponseDTO expectedDTO = UserFactory.userUserDTO();

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
    void getUserByDni_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> staffService.getUserByDni(dni));

        verify(userRepository).findByDni(dni);
    }

    @Test
    void getUserByDni_whenUserIsAdminOrStaff_thenThrowException()
    {
        // Given
        String dni = "46622977";
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));

        // Then
        assertThrows(AccessDeniedException.class, () -> staffService.getUserByDni(dni));
        assertNotEquals(Role.USER, expectedUser.getRole());

        verify(userRepository).findByDni(dni);
    }

    @Test
    void getUserById_whenUserExistsAndIsNotAdminOrStaff_thenReturnUser()
    {
        // Given
        int id = 3;
        UserEntity expectedUser = UserFactory.userUser();
        UserResponseDTO expectedDTO = UserFactory.userUserDTO();

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
    void getUserById_whenUserDoesNotExist_thenThrowException()
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
    void getUserById_whenUserIsAdminOrStaff_thenThrowException()
    {
        // Given
        int id = 1;
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));
        assertThrows(AccessDeniedException.class, () -> staffService.getUserById(id));

        // Then
        verify(userRepository).findById(id);

        assertNotEquals(Role.USER, expectedUser.getRole());
    }
}
