package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.enums.ALARMCODE;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.repository.AlarmCategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AlarmCategoryLookup {
    private final AlarmCategoryRepository repository;
    private volatile Map<ALARMCODE, AlarmCategory> cache = Map.of();
    private volatile Map<Long, AlarmCategory> pkCache = Map.of();

    @PostConstruct
    @Transactional(readOnly = true)
    void load() {
        List<AlarmCategory> all = repository.findAll();

        EnumMap<ALARMCODE, AlarmCategory> byCode = new EnumMap<>(ALARMCODE.class);
        Map<Long, AlarmCategory> byPk = new HashMap<>(all.size() * 2);

        for(AlarmCategory ac : all) {
            ALARMCODE code = ALARMCODE.valueOf(ac.getAcCode());
            byCode.put(code, ac);
            byPk.put(ac.getAcPk(), ac);
        }

        for(ALARMCODE c : ALARMCODE.values()) {
            if(!byCode.containsKey(c)) {
                throw new IllegalStateException("ALARM CODE - Missing: " + c);
            }
        }

        cache = Collections.unmodifiableMap(byCode);
        pkCache = Collections.unmodifiableMap(byPk);
    }

    public AlarmCategory get(ALARMCODE code) { return cache.get(code); }
    public AlarmCategory get(Long acPk) { return pkCache.get(acPk); }
}
