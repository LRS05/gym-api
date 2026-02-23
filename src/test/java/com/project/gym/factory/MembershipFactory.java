package com.project.gym.factory;

import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

public class MembershipFactory
{
    public static MembershipEntity userRegisteredMembership()
    {
        return membershipBuilder(
                1,
                UserFactory.userUser(),
                "87654321",
                MembershipType.ANNUALLY,
                PaymentMethod.CARD,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1),
                "87654321"
        );
    }

    public static MembershipEntity userNotRegisteredMembership()
    {
        return membershipBuilder(
                2,
                null,
                "99999999",
                MembershipType.MONTHLY,
                PaymentMethod.CASH,
                LocalDate.of(2025, 6, 12),
                LocalDate.of(2025, 7, 12),
                "12345678"
        );
    }

    public static MembershipEntity expiredMembership()
    {
        return membershipBuilder(
                3,
                UserFactory.userUser(),
                "87654321",
                MembershipType.MONTHLY,
                PaymentMethod.CASH,
                LocalDate.of(2025, 4, 12),
                LocalDate.of(2025, 5, 12),
                "87654321"
        );
    }

    public static List<MembershipEntity> membershipListOf2025()
    {
        return List.of(userRegisteredMembership(), userNotRegisteredMembership(), expiredMembership());
    }

    public static List<MembershipResponseDTO> membershipListOf2025DTO()
    {
        return membershipListOf2025().stream()
                .map(MembershipFactory::entityToDTO)
                .toList();
    }

    public static List<MembershipEntity> activeMembershipsList()
    {
        return List.of(userRegisteredMembership(), userNotRegisteredMembership());
    }

    public static List<MembershipResponseDTO> activeMembershipsListDTO()
    {
        return activeMembershipsList().stream()
                .map(MembershipFactory::entityToDTO)
                .toList();
    }

    public static MembershipResponseDTO userRegisteredMembershipDTO()
    {
        return entityToDTO(userRegisteredMembership());
    }

    public static MembershipResponseDTO userNotRegisteredMembershipDTO()
    {
        return entityToDTO(userNotRegisteredMembership());
    }

    public static List<MembershipEntity> defaultUserMemberships()
    {
        return List.of(userRegisteredMembership(), expiredMembership());
    }

    public static List<MembershipResponseDTO> defaultUserMembershipsDTO()
    {
        return defaultUserMemberships().stream()
                .map(MembershipFactory::entityToDTO)
                .toList();
    }

    private static MembershipEntity membershipBuilder(
            int id,
            UserEntity user,
            String userDni,
            MembershipType type,
            PaymentMethod paymentMethod,
            LocalDate paymentDate,
            LocalDate nextPaymentDate,
            String createdBy
    )
    {
        return MembershipEntity.builder()
                .id(id)
                .user(user)
                .dni(userDni)
                .status(MembershipStatus.ACTIVE)
                .type(type)
                .paymentMethod(paymentMethod)
                .paymentDate(paymentDate)
                .nextPaymentDate(nextPaymentDate)
                .createdBy(createdBy)
                .lastModifiedBy(null)
                .build();
    }

    private static MembershipResponseDTO entityToDTO(MembershipEntity membership)
    {
        return new MembershipResponseDTO(
                membership.getId(),
                membership.getUser() == null ? -1 : membership.getUser().getId(),
                membership.getDni(),
                membership.getStatus(),
                membership.getType(),
                membership.getPaymentMethod(),
                membership.getPaymentDate(),
                membership.getNextPaymentDate(),
                membership.getCreatedBy(),
                membership.getLastModifiedBy()
        );
    }
}
