package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.TravelCategory;
import org.solcation.solcation_be.entity.enums.TRAVELCODE;
import org.solcation.solcation_be.repository.TravelCategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TravelCategoryLookup {
    private final TravelCategoryRepository repository;
    private volatile Map<TRAVELCODE, TravelCategory> cache = Map.of();
    private volatile Map<Long, TravelCategory>  pkCache = Map.of();

    @PostConstruct
    @Transactional(readOnly = true)
    void load() {
        List<TravelCategory> all = repository.findAll();

        EnumMap<TRAVELCODE, TravelCategory> byCode = new EnumMap<>(TRAVELCODE.class);
        Map<Long, TravelCategory> byPk = new HashMap<>(all.size() * 2);

        for(TravelCategory tpc : all) {
            TRAVELCODE code = TRAVELCODE.valueOf(tpc.getTpcCode());
            byCode.put(code, tpc);
            byPk.put(tpc.getTpcPk(), tpc);
        }

        for(TRAVELCODE c : TRAVELCODE.values()) {
            if(!byCode.containsKey(c)) {
                throw new IllegalStateException("TRAVEL CODE - Missing: " + c);
            }
        }

        cache = Collections.unmodifiableMap(byCode);
        pkCache = Collections.unmodifiableMap(byPk);
    }

    public TravelCategory get(TRAVELCODE code) { return cache.get(code); }
    public TravelCategory get(Long tpcPk) { return pkCache.get(tpcPk); }
}
