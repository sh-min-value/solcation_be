package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;
import org.solcation.solcation_be.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionCategoryLookup {
    private final TransactionCategoryRepository repository;
    private volatile Map<TRANSACTIONCODE, TransactionCategory> cache = Map.of();
    private volatile Map<Long, TransactionCategory>  pkCache = Map.of();

    @PostConstruct
    @Transactional(readOnly = true)
    void load() {
        List<TransactionCategory> all = repository.findAll();

        EnumMap<TRANSACTIONCODE, TransactionCategory> byCode = new EnumMap<>(TRANSACTIONCODE.class);
        Map<Long, TransactionCategory> byPk = new HashMap<>(all.size() * 2);

        for(TransactionCategory tc : all) {
            TRANSACTIONCODE code = TRANSACTIONCODE.valueOf(tc.getTcCode());
            byCode.put(code, tc);
            byPk.put(tc.getTcPk(), tc);
        }

        for(TRANSACTIONCODE c : TRANSACTIONCODE.values()) {
            if(!byCode.containsKey(c)) {
                throw new IllegalStateException("TRANSACTION CODE - Missing: " + c);
            }
        }

        cache = Collections.unmodifiableMap(byCode);
        pkCache = Collections.unmodifiableMap(byPk);
    }

    public TransactionCategory get(TRANSACTIONCODE code) { return cache.get(code); }
    public TransactionCategory get(Long tcPk) { return pkCache.get(tcPk); }
    public List<TransactionCategory> getList() { return new ArrayList<>(pkCache.values()); }
}
