package com.project.gym.mapper;

import com.project.gym.dto.UserResponseDTO;
import com.project.gym.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper
{
    UserResponseDTO entityToDTO(UserEntity user);

    List<UserResponseDTO> entityToDTO(List<UserEntity> users);
}
