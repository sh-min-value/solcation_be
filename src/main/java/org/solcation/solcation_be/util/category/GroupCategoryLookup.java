package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.enums.GROUPCODE;
import org.solcation.solcation_be.repository.GroupCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class GroupCategoryLookup {
    private final GroupCategoryRepository repository;
    private final EnumMap<GROUPCODE, GroupCategory> cache = new EnumMap<>(GROUPCODE.class);

    @PostConstruct
    void load() {
        for (var c: GROUPCODE.values()) {
            cache.put(c, repository.findByGcCode(c.name()).orElseThrow(() -> new IllegalStateException("Missing " + c)));
        }
    }

    public GroupCategory get(GROUPCODE code) { return cache.get(code); }
}
