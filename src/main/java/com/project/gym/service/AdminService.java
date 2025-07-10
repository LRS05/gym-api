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
import org.springframework.security.core.context.SecurityContextHolder;
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
        List<UserEntity> users = userRepository.findAll();
        return userMapper.toDTO(users);
    }

    public UserResponseDTO getUserByDni(String dni)
    {
        UserEntity user = findUserByDniOrThrow(dni);
        return userMapper.toDTO(user);
    }

    public UserResponseDTO updateUserRole(String dni, RoleRequestDTO requestDTO)
    {
        UserEntity user = findUserByDniOrThrow(dni);

        if (user.getRole() == Role.ADMIN)
        {
            throw new InvalidRoleUpdateException("You cannot update other admins.");
        }

        user.setRole(requestDTO.role());
        UserEntity savedUser = userRepository.save(user);

        log.info("Admin successfully updated user with DNI {} to {} role", dni, requestDTO.role());
        return userMapper.toDTO(savedUser);
    }

    public void deleteUserByDni(String dni)
    {
        UserEntity user = findUserByDniOrThrow(dni);

        if (user.getRole() == Role.ADMIN)
        {
            throw new InvalidDeleteException("You cannot delete other admins.");
        }

        userRepository.delete(user);
        customMetrics.decrementUsers();
        log.info("Admin deleted user with DNI {}", dni);
    }

    public void deleteUserById(int id)
    {
        UserEntity user = userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (user.getRole().equals(Role.ADMIN))
        {
            throw new InvalidDeleteException("You cannot delete other admins.");
        }

        userRepository.delete(user);
        customMetrics.decrementUsers();
        log.info("Admin deleted user with ID {}", id);
    }

    private UserEntity findUserByDniOrThrow(String dni)
    {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    private String getCurrentUser()
    {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
