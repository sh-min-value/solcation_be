package org.solcation.solcation_be.entity.enums;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum DEPOSITCYCLE implements LegacyCommonType {
    WEEK(0, "WEEK"), MONTH(1, "MONTH");

    private final int code;
    private final String label;

    DEPOSITCYCLE(int code, String label) {
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
