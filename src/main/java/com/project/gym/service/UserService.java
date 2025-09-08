package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.dto.PhoneNumberRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.exception.InvalidPasswordException;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomMetrics customMetrics;

    public UserResponseDTO getMe()
    {
        return userMapper.entityToDTO(
                findCurrentUserOrThrow()
        );
    }

    public void deleteMe(PasswordRequestDTO requestDTO)
    {
        UserEntity user = findCurrentUserOrThrow();

        if (!passwordEncoder.matches(requestDTO.password(), user.getPassword()))
        {
            throw new InvalidPasswordException("Incorrect password.");
        }
        userRepository.delete(user);
        customMetrics.decrementUsers();
        log.info("Deleted own account with dni={}", user.getDni());
    }

    public String addPhoneNumber(PhoneNumberRequestDTO requestDTO)
    {
        UserEntity user = findCurrentUserOrThrow();

        // The "54" is the country code for Argentina.
        user.setPhoneNumber("54" + requestDTO.phoneNumber());
        userRepository.save(user);

        return "The phone number " + requestDTO.phoneNumber() + " has been successfully added to your account!";
    }

    private UserEntity findCurrentUserOrThrow()
    {
        return userRepository.findByDni(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

}
