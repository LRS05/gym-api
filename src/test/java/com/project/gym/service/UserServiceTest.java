package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.PasswordRequestDTO;
import com.project.gym.dto.PhoneNumberRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.CountryCode;
import com.project.gym.exception.InvalidPasswordException;
import com.project.gym.exception.PhoneNumberAlreadyExistsException;
import com.project.gym.mapper.UserMapper;
import com.project.gym.repository.UserRepository;
import com.project.gym.factory.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setSecurityContext()
    {
        String dni = "87654321";

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dni, null, new ArrayList<>())
        );
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMe_whenUserExists_thenReturnOwnInfo()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity expectedUser = UserFactory.userUser();
        UserResponseDTO expectedDTO = UserFactory.userUserDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userMapper.entityToDTO(expectedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = userService.getMe();

        // Then
        assertEquals(expectedDTO, result);

        verify(userRepository).findByDni(dni);
        verify(userMapper).entityToDTO(expectedUser);
    }

    @Test
    void getMe_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getMe());

        verify(userRepository).findByDni(dni);
    }

    @Test
    void deleteMe_whenUserExistsAndPasswordIsCorrect_thenDeleteUser()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");

        UserEntity expectedUser = UserFactory.userUser();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(passwordEncoder.matches(requestDTO.password(), expectedUser.getPassword())).thenReturn(true);

        userService.deleteMe(requestDTO);

        // Then
        assertEquals(dni, expectedUser.getDni());

        verify(userRepository).findByDni(dni);
        verify(passwordEncoder).matches(requestDTO.password(), expectedUser.getPassword());
        verify(userRepository).delete(expectedUser);
    }

    @Test
    void deleteMe_whenUserDoesNotExist_thenThrowException()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();
        PasswordRequestDTO requestDTO = new PasswordRequestDTO("gordomono");

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(UsernameNotFoundException.class, () -> userService.deleteMe(requestDTO));

        verify(userRepository).findByDni(dni);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteMe_whenPasswordIsIncorrect_thenThrowException()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();

        PasswordRequestDTO requestDTO = new PasswordRequestDTO("monogordo");
        UserEntity expectedUser = UserFactory.userUser();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(passwordEncoder.matches(requestDTO.password(), expectedUser.getPassword())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.deleteMe(requestDTO));

        // Then
        verify(userRepository).findByDni(dni);
        verify(passwordEncoder).matches(requestDTO.password(), expectedUser.getPassword());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addPhoneNumber_whenPhoneNumberIsValidAndNotRegistered_thenAddPhoneNumberAndReturnMessage()
    {
        // Given
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity expectedUser = UserFactory.userUser();
        UserEntity expectedUserUpdated = UserFactory.userUserWithPhoneNumber();

        PhoneNumberRequestDTO requestDTO = new PhoneNumberRequestDTO(
                CountryCode.ARGENTINA,
                "2345511370"
        );
        String phoneNumber = "542345511370";
        String expectedMessage = "The phone number " + phoneNumber + " has been successfully added to your account!";

        // When
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUserUpdated);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        String result = userService.addPhoneNumber(requestDTO);

        // Then
        verify(userRepository).save(captor.capture());

        assertEquals(phoneNumber, captor.getValue().getPhoneNumber());
        assertEquals(expectedMessage, result);
    }

    @Test
    void addPhoneNumber_whenPhoneNumberIsAlreadyRegistered_thenThrowException()
    {
        // Given
        PhoneNumberRequestDTO requestDTO = new PhoneNumberRequestDTO(
                CountryCode.ARGENTINA,
                "2345511370"
        );
        String phoneNumber = "542345511370";

        // When
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // Then
        assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.addPhoneNumber(requestDTO));

        verify(userRepository).existsByPhoneNumber(phoneNumber);
    }
}
