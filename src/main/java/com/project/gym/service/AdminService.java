package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.RoleRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import com.project.gym.exception.InvalidDeleteException;
import com.project.gym.exception.InvalidRoleUpdateException;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        UserEntity user = findUserByDniOrThrow(dni);
        return userMapper.entityToDTO(user);
    }

    public UserResponseDTO updateUserRoleByDni(String dni, RoleRequestDTO requestDTO)
    {
        UserEntity user = findUserByDniOrThrow(dni);
        if (user.getRole() == Role.ADMIN)
        {
            throw new InvalidRoleUpdateException("You cannot update other admins.");
        }

        user.setRole(requestDTO.role());
        UserEntity savedUser = userRepository.save(user);

        log.info("Updated user with dni={} to {}", dni, requestDTO.role());
        return userMapper.entityToDTO(savedUser);
    }

    public void deleteUserByDni(String dni)
    {
        UserEntity user = findUserByDniOrThrow(dni);

        validateUserNotAdminOrThrow(user);

        userRepository.delete(user);
        customMetrics.decrementUsers();

        log.info("Deleted user with dni={}", dni);
    }

    public void deleteUserById(int id)
    {
        UserEntity user = userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        validateUserNotAdminOrThrow(user);

        userRepository.delete(user);
        customMetrics.decrementUsers();

        log.info("Deleted user with id={}", id);
    }

    private UserEntity findUserByDniOrThrow(String dni)
    {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    private void validateUserNotAdminOrThrow(UserEntity user)
    {
        if (user.getRole() == Role.ADMIN)
        {
            throw new InvalidDeleteException("You cannot delete an ADMIN.");
        }
    }
}
