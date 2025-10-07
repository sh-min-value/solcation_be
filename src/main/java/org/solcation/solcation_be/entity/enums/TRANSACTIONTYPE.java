package org.solcation.solcation_be.entity.enums;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum TRANSACTIONTYPE implements LegacyCommonType {
    DEPOSIT(0, "DEPOSIT"), WITHDRAW(1, "WITHDRAW"), CARD(2, "CARD");

    private final int code;
    private final String label;

    TRANSACTIONTYPE(int code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
