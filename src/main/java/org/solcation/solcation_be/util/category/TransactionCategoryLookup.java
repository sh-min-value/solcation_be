package org.solcation.solcation_be.util.category;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;
import org.solcation.solcation_be.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class TransactionCategoryLookup {
    private final TransactionCategoryRepository repository;
    private final EnumMap<TRANSACTIONCODE, TransactionCategory> cache = new EnumMap<>(TRANSACTIONCODE.class);

    @PostConstruct
    void load() {
        for (var c: TRANSACTIONCODE.values()) {
            cache.put(c, repository.findByTcCode(c.name()).orElseThrow(() -> new IllegalStateException("Missing " + c)));
        }
    }

    public TransactionCategory get(TRANSACTIONCODE code) { return cache.get(code); }
}
