package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.RoleRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.UserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
    void getUsers_whenUsersExist_thenReturnUserList()
    {
        // Given
        List<UserEntity> expectedUsers = UserFactory.userList();
        List<UserResponseDTO> expectedDTOs = UserFactory.userListDTO();

        // When
        when(userRepository.findAll()).thenReturn(expectedUsers);
        when(userMapper.entityToDTO(expectedUsers)).thenReturn(expectedDTOs);

        List<UserResponseDTO> result = adminService.getUsers();

        // Then
        assertEquals(expectedDTOs, result);
        assertEquals(3, result.size());

        verify(userRepository).findAll();
        verify(userMapper).entityToDTO(expectedUsers);
    }

    @Test
    void getUserByDni_whenUserExists_thenReturnUser()
    {
        // Given
        String dni = "12345678";
        UserEntity expectedUser = UserFactory.userStaff();
        UserResponseDTO expectedDTO = UserFactory.userStaffDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = adminService.getUserByDni(dni);

        // Then
        assertEquals(expectedDTO, result);

        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUser);
    }

    @Test
    void getUserByDni_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> adminService.getUserByDni(dni));

        verify(userRepository).findByDni(dni);
    }

    @Test
    void updateUserRoleByDni_whenUserExists_thenReturnUserUpdated()
    {
        // Given
        String dni = "87654321";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.STAFF);
        UserEntity expectedUser = UserFactory.userUser();

        UserEntity expectedUserUpdated = UserFactory.userUser();
        expectedUserUpdated.setRole(Role.STAFF);
        UserResponseDTO expectedUserUpdatedDTO = UserFactory.userUserRoleUpdatedDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUserUpdated);
        when(userMapper.entityToDTO(expectedUserUpdated)).thenReturn(expectedUserUpdatedDTO);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        UserResponseDTO result = adminService.updateUserRoleByDni(dni, requestDTO);

        // Then
        verify(userRepository).save(captor.capture());

        assertEquals(requestDTO.role(), captor.getValue().getRole());
        assertEquals(expectedUserUpdatedDTO, result);

        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUserUpdated);
    }

    @Test
    void updateUserRoleByDni_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.ADMIN);

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> adminService.updateUserRoleByDni(dni, requestDTO));

        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserRoleByDni_whenUserIsAdmin_thenThrowException()
    {
        // Given
        String dni = "46622977";
        UserEntity expectedUser = UserFactory.userAdmin();
        RoleRequestDTO requestDTO = new RoleRequestDTO(Role.USER);

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));

        // Then
        assertThrows(AccessDeniedException.class, () -> adminService.updateUserRoleByDni(dni, requestDTO));

        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserByDni_whenUserIsNotAdmin_thenDeleteUser()
    {
        // Given
        String dni = "87654321";
        UserEntity expectedUser = UserFactory.userUser();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        adminService.deleteUserByDni(dni);

        // Then
        assertEquals(dni, expectedUser.getDni());
        assertNotEquals(Role.ADMIN, expectedUser.getRole());

        verify(userRepository).findByDni(dni);
        verify(userRepository).delete(expectedUser);
    }

    @Test
    void deleteUserByDni_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> adminService.deleteUserByDni(dni));

        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserByDni_whenUserIsAdmin_thenThrowException()
    {
        // Given
        String dni = "46622977";
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));

        // Then
        assertThrows(AccessDeniedException.class, () -> adminService.deleteUserByDni(dni));

        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserById_whenUserExists_thenReturnUser()
    {
        // Given
        int id = 3;
        UserEntity expectedUser = UserFactory.userUser();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));
        adminService.deleteUserById(3);

        // Test
        assertEquals(id, expectedUser.getId());

        verify(userRepository).findById(id);
        verify(userRepository).delete(expectedUser);
    }

    @Test
    void deleteUserById_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        int id = 99999999;

        // When
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> adminService.deleteUserById(id));

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserById_whenUserIsAdmin_thenThrowException()
    {
        // Given
        int id = 1;
        UserEntity expectedUser = UserFactory.userAdmin();

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

        // Then
        assertThrows(AccessDeniedException.class, () -> adminService.deleteUserById(id));

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository);
    }
}
