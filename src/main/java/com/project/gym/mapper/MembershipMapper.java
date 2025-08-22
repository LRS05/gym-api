package com.project.gym.mapper;

import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.entity.MembershipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MembershipMapper
{
    @Mapping(source = "user.id", target = "userId")
    MembershipResponseDTO entityToDTO(MembershipEntity membership);

    @Mapping(source = "user.id", target = "userId")
    List<MembershipResponseDTO> entityToDTO(List<MembershipEntity> membership);
}
