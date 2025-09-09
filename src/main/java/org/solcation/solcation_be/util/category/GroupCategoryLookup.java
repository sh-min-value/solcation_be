package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.enums.GROUPCODE;
import org.solcation.solcation_be.repository.GroupCategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GroupCategoryLookup {
    private final GroupCategoryRepository repository;
    private volatile Map<GROUPCODE, GroupCategory> cache = Map.of();
    private volatile Map<Long, GroupCategory>  pkCache = Map.of();

    @PostConstruct
    @Transactional(readOnly = true)
    void load() {
        List<GroupCategory> all = repository.findAll();

        EnumMap<GROUPCODE, GroupCategory> byCode = new EnumMap<>(GROUPCODE.class);
        Map<Long, GroupCategory> byPk = new HashMap<>(all.size() * 2);

        for(GroupCategory gc : all) {
            GROUPCODE code = GROUPCODE.valueOf(gc.getGcCode());
            byCode.put(code, gc);
            byPk.put(gc.getGcPk(), gc);
        }

        for(GROUPCODE c : GROUPCODE.values()) {
            if(!byCode.containsKey(c)) {
                throw new IllegalStateException("GROUP CODE - Missing: " + c);
            }
        }

        cache = Collections.unmodifiableMap(byCode);
        pkCache = Collections.unmodifiableMap(byPk);
    }

    public GroupCategory get(GROUPCODE code) { return cache.get(code); }
    public GroupCategory get(Long gcPk) { return pkCache.get(gcPk); }
}
