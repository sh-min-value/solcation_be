package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.enums.ALARMCODE;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.repository.AlarmCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class AlarmCategoryLookup {
    private final AlarmCategoryRepository repository;
    private final EnumMap<ALARMCODE, AlarmCategory> cache = new EnumMap<>(ALARMCODE.class);

    @PostConstruct
    void load() {
        for (var c: ALARMCODE.values()) {
            cache.put(c, repository.findByAcCode(c.name()).orElseThrow(() -> new IllegalStateException("Missing " + c)));
        }
    }

    public AlarmCategory get(ALARMCODE code) { return cache.get(code); }
}
