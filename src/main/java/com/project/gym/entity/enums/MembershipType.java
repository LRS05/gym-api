package com.project.gym.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MembershipType
{
    ANNUALLY(12),
    SIX_MONTHS(6),
    THREE_MONTHS(3),
    MONTHLY(1);

    private final int duration;
}
