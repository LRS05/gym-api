package com.project.gym.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CustomMetrics
{
    private final AtomicInteger totalUsers = new AtomicInteger(0);
    private final AtomicInteger totalMemberships = new AtomicInteger(0);
    private final AtomicInteger activeMemberships = new AtomicInteger(0);

    public CustomMetrics(MeterRegistry meterRegistry)
    {
        meterRegistry.gauge("gym.total.users", totalUsers);
        meterRegistry.gauge("gym.total.memberships", totalMemberships);
        meterRegistry.gauge("gym.active.memberships", activeMemberships);
    }

    public void incrementUsers()
    {
        totalUsers.incrementAndGet();
    }

    public void decrementUsers()
    {
        totalUsers.decrementAndGet();
    }

    public void incrementActiveMemberships()
    {
        activeMemberships.incrementAndGet();
    }

    public void decrementActiveMemberships()
    {
        activeMemberships.decrementAndGet();
    }

    public void incrementMemberships()
    {
        totalMemberships.incrementAndGet();
    }

    public void decrementMemberships()
    {
        totalMemberships.decrementAndGet();
    }
}
