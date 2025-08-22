package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.RoleRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getUsersTest()
    {
        // Given
        List<UserEntity> expectedUsers = UserTestDataFactory.userList();
        List<UserResponseDTO> expectedDTOs = UserTestDataFactory.userListDTO();

        // When
        when(userRepository.findAll()).thenReturn(expectedUsers);
        when(userMapper.entityToDTO(expectedUsers)).thenReturn(expectedDTOs);

        List<UserResponseDTO> result = adminService.getUsers();

        // Then
        verify(userRepository).findAll();
        verify(userMapper).entityToDTO(expectedUsers);
        assertEquals(expectedDTOs, result);
    }

    @Test
    void getUsersEmptyListTest()
    {
        // When
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userMapper.entityToDTO(anyList())).thenReturn(new ArrayList<>());

        List<UserResponseDTO> result = adminService.getUsers();

        // Then
        verify(userRepository).findAll();
        verify(userMapper).entityToDTO(anyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserByDniTest()
    {
        // Given
        String dni = "12345678";
        UserEntity expectedUser = UserTestDataFactory.userStaff();
        UserResponseDTO expectedDTO = UserTestDataFactory.userStaffDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = adminService.getUserByDni(dni);

        // Then
        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUser);

        assertEquals(expectedDTO, result);
    }

    @Test
    void getUserByDniNotFoundTest()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adminService.getUserByDni(dni));

        // Then
        verify(userRepository).findByDni(dni);
    }

    @Test
    void updateUserRoleTest()
    {
        // Given
        String dni = "87654321";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.STAFF);
        UserEntity expectedUser = UserTestDataFactory.userUser();

        UserEntity expectedUserUpdated = UserTestDataFactory.userUser();
        expectedUserUpdated.setRole(Role.STAFF);
        UserResponseDTO expectedUserUpdatedDTO = UserTestDataFactory.userUserRoleUpdatedDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUserUpdated);
        when(userMapper.entityToDTO(expectedUserUpdated)).thenReturn(expectedUserUpdatedDTO);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        UserResponseDTO result = adminService.updateUserRoleByDni(dni, requestDTO);

        // Then
        verify(userRepository).findByDni(dni);
        verify(userRepository).save(captor.capture());
        verify(userMapper).entityToDTO(captor.capture());

        assertEquals(expectedUserUpdated, captor.getValue());
        assertEquals(expectedUserUpdatedDTO, result);
    }

    @Test
    void updateUserRoleNotFoundTest()
    {
        // Given
        String dni = "99999999";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.ADMIN);

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adminService.updateUserRoleByDni(dni, requestDTO));

        // Then
        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserByDniTest()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserTestDataFactory.userUser();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));

        adminService.deleteUserByDni(dni);

        // Test
        verify(userRepository).findByDni(dni);
        verify(userRepository).delete(expectedUser);

        assertEquals(dni, expectedUser.getDni());
    }

    @Test
    void deleteUserByDniNotFoundTest()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adminService.deleteUserByDni(dni));

        // Then
        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserByIdTest()
    {
        // Given
        int id = 3;
        UserEntity expectedUser = UserTestDataFactory.userUser();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

        adminService.deleteUserById(3);

        // Test
        verify(userRepository).findById(id);
        verify(userRepository).delete(expectedUser);

        assertEquals(id, expectedUser.getId());
    }

    @Test
    void deleteUserByIdNotFoundTest()
    {
        // Given
        int id = 99999999;

        // When
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adminService.deleteUserById(id));

        // Then
        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository);
    }
}
