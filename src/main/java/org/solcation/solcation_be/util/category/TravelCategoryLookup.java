package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.TravelCategory;
import org.solcation.solcation_be.entity.enums.TRAVELCODE;
import org.solcation.solcation_be.repository.TravelCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class TravelCategoryLookup {
    private final TravelCategoryRepository repository;
    private final EnumMap<TRAVELCODE, TravelCategory> cache = new EnumMap<>(TRAVELCODE.class);

    @PostConstruct
    void load() {
        for (var c: TRAVELCODE.values()) {
            cache.put(c, repository.findByTpcCode(c.name()).orElseThrow(() -> new IllegalStateException("Missing " + c)));
        }
    }

    public TravelCategory get(TRAVELCODE code) { return cache.get(code); }
}
