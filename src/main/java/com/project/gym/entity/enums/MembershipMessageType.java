package com.project.gym.entity.enums;

import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MembershipMessageType
{
    EXPIRING("Hello %s %s!\nYour membership (ID=%s, type=%s), created on %s, is about to expire tomorrow %s."),

    EXPIRED("Hello %s %s!\nYour membership (ID=%s, type=%s), created on %s, expired today %s."),

    CREATED("Hello %s %s!\nYour membership (ID=%s, type=%s) was successfully created on %s and expires on %s."),

    DELETED("Hello %s %s!\nYour membership (ID=%s, type=%s), created on %s and expiring on %s, was deleted by a staff member.");

    private final String template;

    public String format(UserEntity user, MembershipEntity membership)
    {
        return String.format(
                template,
                user.getFirstName(),
                user.getLastName(),
                membership.getId(),
                membership.getType(),
                membership.getPaymentDate(),
                membership.getNextPaymentDate()
        );
    }
}
