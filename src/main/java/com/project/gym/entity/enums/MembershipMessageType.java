package com.project.gym.entity.enums;

import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MembershipMessageType
{
    EXPIRING("Hello! %s %s,\nYour %s membership purchased on %s will expire tomorrow."),
    EXPIRED("Hello! %s %s,\nYour %s membership purchased on %s has expired."),
    CREATED("Hello! %s %s,\nYour %s membership was successfully created on %s.");

    private final String template;

    public String format(UserEntity user, MembershipEntity membership)
    {
        return String.format(
                template,
                user.getFirstName(),
                user.getLastName(),
                membership.getType().name().toLowerCase(),
                membership.getPaymentDate()
        );
    }
}
