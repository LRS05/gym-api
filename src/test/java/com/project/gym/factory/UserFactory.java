package com.project.gym.factory;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Gender;
import com.project.gym.entity.enums.Role;

import java.time.LocalDate;
import java.util.List;

public class UserFactory
{
    public static UserEntity userAdmin()
    {
        return userBuilder(1, Role.ADMIN, "46622977", "Lorenzo", "Sarlo", null);
    }

    public static UserEntity userStaff()
    {
        return userBuilder(2, Role.STAFF, "12345678", "Matias", "Freccero", null);
    }

    public static UserEntity userUser()
    {
        return userBuilder(3, Role.USER, "87654321", "Franco", "Cataldi", "542345511370");
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
                .map(UserFactory::entityToDTO)
                .toList();
    }

    private static UserEntity userBuilder(int id, Role role, String dni, String firstName, String lastName, String phoneNumber)
    {
        return UserEntity.builder()
                .id(id)
                .role(role)
                .dni(dni)
                .password("$2a$10$HrS.OveuXvTWKcDhSgMuvubAFVYBKtFz/mRFL93rpVf5pfpdLSYA6")
                .gender(Gender.MALE)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .creationDate(LocalDate.of(2025, 1, 1))
                .build();
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
                user.getPhoneNumber(),
                user.getCreationDate()
        );
    }

}
