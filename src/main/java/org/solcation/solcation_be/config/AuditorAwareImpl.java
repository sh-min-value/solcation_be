package org.solcation.solcation_be.config;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        if(principal instanceof JwtPrincipal jwtPrincipal) {
            return Optional.of(jwtPrincipal.userPk());
        }


        return Optional.empty();
    }
}
