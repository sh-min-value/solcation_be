package org.solcation.solcation_be.security;

import lombok.Getter;

import java.util.Collection;


public record JwtPrincipal (
        String userId,
        String userName,
        String email,
        String tel,
        Collection<String> roles
) { }
