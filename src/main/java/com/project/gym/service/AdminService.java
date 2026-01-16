package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.RoleRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService
{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomMetrics customMetrics;

    public List<UserResponseDTO> getUsers()
    {
        return userMapper.entityToDTO(
                userRepository.findAll()
        );
    }

    public UserResponseDTO getUserByDni(String dni)
    {
        return userMapper.entityToDTO(
                userRepository.findByDni(dni)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."))
        );
    }

    public UserResponseDTO updateUserRoleByDni(String dni, RoleRequestDTO requestDTO)
    {
        UserEntity user = findNonAdminUserByDniOrThrow(dni);
        user.setRole(requestDTO.role());

        UserEntity savedUser = userRepository.save(user);

        log.info("Updated user with dni={} and role={}, to {}", dni, user.getRole(), requestDTO.role());
        return userMapper.entityToDTO(savedUser);
    }

    public void deleteUserByDni(String dni)
    {
        UserEntity user = findNonAdminUserByDniOrThrow(dni);

        // Deletes the user, updates metrics, and logs the action.
        deleteUser(user);
    }

    public void deleteUserById(int id)
    {
        UserEntity user = userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN)
        {
            throw new AccessDeniedException("You cannot delete an ADMIN user.");
        }

        // Deletes the user, updates metrics, and logs the action.
        deleteUser(user);
    }

    private UserEntity findNonAdminUserByDniOrThrow(String dni)
    {
        UserEntity user = userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN)
        {
            throw new AccessDeniedException("You cannot perform this action on an ADMIN user.");
        }

        return user;
    }

    private void deleteUser(UserEntity user)
    {
        userRepository.delete(user);
        customMetrics.decrementUsers();

        log.info("Deleted user {} {}, with id={}, dni={} and role={}",
                user.getFirstName(),
                user.getLastName(),
                user.getId(),
                user.getDni(),
                user.getRole()
        );
    }
}
