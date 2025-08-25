package com.project.gym.service;

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
public class StaffService
{
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDTO> getUsers()
    {
        return userMapper.entityToDTO(
                userRepository.findAllByRole(Role.USER)
        );
    }

    public UserResponseDTO getUserByDni(String dni)
    {
        UserEntity user = userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // If the user is ADMIN or STAFF, throw an exception.
        validateStaffHasAccess(user);

        return userMapper.entityToDTO(user);
    }

    public UserResponseDTO getUserById(int id)
    {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // If the user is ADMIN or STAFF, throw an exception.
        validateStaffHasAccess(user);

        return userMapper.entityToDTO(user);
    }

    private void validateStaffHasAccess(UserEntity user)
    {
        if (user.getRole() != Role.USER)
        {
            log.warn("Attempted to read an ADMIN or STAFF user");
            throw new AccessDeniedException("You are not allowed to see this user.");
        }
    }

}
