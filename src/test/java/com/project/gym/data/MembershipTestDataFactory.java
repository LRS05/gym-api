package com.project.gym.data;

import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

public class MembershipTestDataFactory
{
    public static MembershipEntity userRegisteredMembership()
    {
        return MembershipEntity
                .builder()
                .id(1)
                .user(UserTestDataFactory.userUser())
                .dni("87654321")
                .status(MembershipStatus.ACTIVE)
                .type(MembershipType.ANNUALLY)
                .paymentMethod(PaymentMethod.CARD)
                .paymentDate(LocalDate.of(2025, 1, 1))
                .nextPaymentDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    public static MembershipEntity userNotRegisteredMembership()
    {
        return MembershipEntity
                .builder()
                .id(2)
                .user(null)
                .dni("99999999")
                .status(MembershipStatus.ACTIVE)
                .type(MembershipType.MONTHLY)
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.of(2025, 6, 12))
                .nextPaymentDate(LocalDate.of(2025, 7, 12))
                .build();
    }

    public static MembershipEntity expiredMembership()
    {
        return MembershipEntity
                .builder()
                .id(3)
                .user(UserTestDataFactory.userUser())
                .dni("87654321")
                .status(MembershipStatus.ACTIVE)
                .type(MembershipType.MONTHLY)
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.of(2025, 4, 12))
                .nextPaymentDate(LocalDate.of(2025, 5, 12))
                .build();
    }

    public static List<MembershipEntity> membershipListOf2025()
    {
        return List.of(userRegisteredMembership(), userNotRegisteredMembership(), expiredMembership());
    }

    public static List<MembershipResponseDTO> membershipListOf2025DTO()
    {
        return membershipListOf2025().stream()
                .map(MembershipTestDataFactory::entityToDTO)
                .toList();
    }

    public static List<MembershipEntity> activeMembershipsList()
    {
        return List.of(userRegisteredMembership(), userNotRegisteredMembership());
    }

    public static List<MembershipResponseDTO> activeMembershipsListDTO()
    {
        return activeMembershipsList().stream()
                .map(MembershipTestDataFactory::entityToDTO)
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

    public static MembershipResponseDTO expiredMembershipDTO()
    {
        return entityToDTO(expiredMembership());
    }

    public static List<MembershipEntity> defaultUserMemberships()
    {
        return List.of(userRegisteredMembership(), expiredMembership());
    }

    public static List<MembershipResponseDTO> defaultUserMembershipsDTO()
    {
        return defaultUserMemberships().stream()
                .map(MembershipTestDataFactory::entityToDTO)
                .toList();
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
                membership.getNextPaymentDate()
        );
    }
}
