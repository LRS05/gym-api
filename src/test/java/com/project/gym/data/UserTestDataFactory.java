package com.project.gym.data;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
import com.project.gym.entity.enums.Role;

import java.time.LocalDate;
import java.util.List;

public class UserTestDataFactory
{
    public static UserEntity userAdmin()
    {
        return UserEntity
                .builder()
                .id(1)
                .role(Role.ADMIN)
                .dni("46622977")
                .password("$2a$10$HrS.OveuXvTWKcDhSgMuvubAFVYBKtFz/mRFL93rpVf5pfpdLSYA6")
                .gender(Gender.MALE)
                .firstName("Lorenzo")
                .lastName("Sarlo")
                .phoneNumber("542345511370")
                .creationDate(LocalDate.of(2025, 1, 1))
                .build();
    }

    public static UserEntity userStaff()
    {
        return UserEntity
                .builder()
                .id(2)
                .role(Role.STAFF)
                .dni("12345678")
                .password("$2a$10$HrS.OveuXvTWKcDhSgMuvubAFVYBKtFz/mRFL93rpVf5pfpdLSYA6")
                .gender(Gender.MALE)
                .firstName("Matias")
                .lastName("Freccero")
                .phoneNumber("542345511370")
                .creationDate(LocalDate.of(2025, 1, 1))
                .build();
    }

    public static UserEntity userUser()
    {
        return UserEntity
                .builder()
                .id(3)
                .role(Role.USER)
                .dni("87654321")
                .password("$2a$10$HrS.OveuXvTWKcDhSgMuvubAFVYBKtFz/mRFL93rpVf5pfpdLSYA6")
                .gender(Gender.MALE)
                .firstName("Franco")
                .lastName("Cataldi")
                .phoneNumber("542345511370")
                .creationDate(LocalDate.of(2025, 1, 1))
                .build();
    }

    public static List<UserEntity> userList()
    {
        return List.of(userAdmin(), userStaff(), userUser());
    }

    public static UserResponseDTO userStaffDTO()
    {
        return entityToDTO(userStaff());
    }

    public static UserResponseDTO userUserDTO()
    {
        return entityToDTO(userUser());
    }

    public static UserResponseDTO userUserRoleUpdatedDTO()
    {
        var user = userUser();
        user.setRole(Role.STAFF);
        return entityToDTO(user);
    }

    public static List<UserResponseDTO> userListDTO()
    {
        return userList().stream()
                .map(UserTestDataFactory::entityToDTO)
                .toList();
    }

    private static UserResponseDTO entityToDTO(UserEntity user)
    {
        return new UserResponseDTO(
                user.getId(),
                user.getRole(),
                user.getGender(),
                user.getDni(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreationDate()
        );
    }

}
