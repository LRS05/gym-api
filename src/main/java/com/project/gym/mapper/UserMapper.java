package com.project.gym.mapper;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper
{
    UserResponseDTO toDTO(UserEntity user);

    List<UserResponseDTO> toDTO(List<UserEntity> users);
}
