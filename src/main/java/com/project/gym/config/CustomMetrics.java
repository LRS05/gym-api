package com.project.gym.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CustomMetrics
{
    private final AtomicInteger gymTotalUsers = new AtomicInteger(0);
    private final AtomicInteger gymTotalMemberships = new AtomicInteger(0);
    private final AtomicInteger gymTotalActiveMemberships = new AtomicInteger(0);

    public CustomMetrics(MeterRegistry meterRegistry)
    {
        meterRegistry.gauge("gym.total.users", gymTotalUsers);
        meterRegistry.gauge("gym.total.memberships", gymTotalMemberships);
        meterRegistry.gauge("gym.active.memberships", gymTotalActiveMemberships);
    }

    public void incrementUsers()
    {
        gymTotalUsers.incrementAndGet();
    }

    public void decrementUsers()
    {
        gymTotalUsers.decrementAndGet();
    }

    public void incrementMemberships()
    {
        gymTotalMemberships.incrementAndGet();
    }

    public void decrementMemberships()
    {
        gymTotalMemberships.decrementAndGet();
    }

    public void incrementActiveMemberships()
    {
        gymTotalActiveMemberships.incrementAndGet();
    }

    public void decrementActiveMemberships()
    {
        gymTotalActiveMemberships.decrementAndGet();
    }
}
