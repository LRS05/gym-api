package com.project.gym.entity.enums;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.project.gym.entity.enums.Permission.*;

@RequiredArgsConstructor
public enum Role
{
    ADMIN(
            Set.of(
                    ROLE_UPDATE,
                    ADMIN_READ,
                    STAFF_READ, STAFF_UPDATE, STAFF_DELETE,
                    USER_READ, USER_UPDATE, USER_DELETE,
                    MEMBERSHIP_CREATE, MEMBERSHIP_READ, MEMBERSHIP_UPDATE, MEMBERSHIP_DELETE
            )
    ),
    STAFF(
            Set.of(
                    USER_READ, USER_UPDATE, USER_DELETE,
                    MEMBERSHIP_CREATE, MEMBERSHIP_READ, MEMBERSHIP_UPDATE
            )
    ),
    USER(
            Set.of(
                    MEMBERSHIP_CREATE, MEMBERSHIP_READ
            )
    );

    private final Set<Permission> permissions;

    public Set<SimpleGrantedAuthority> getPermissions()
    {
        Set<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toSet());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
