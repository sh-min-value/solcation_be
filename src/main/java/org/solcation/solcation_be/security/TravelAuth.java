package org.solcation.solcation_be.security;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Component;

@Component("travelAuth")
@RequiredArgsConstructor
public class TravelAuth {
    private final TravelRepository travelRepository;

    public boolean canAccessTravel(Long groupPk, Long travelPk) {
        return travelRepository.existsByGroup_GroupPkAndTpPk(groupPk, travelPk);
    }
}
