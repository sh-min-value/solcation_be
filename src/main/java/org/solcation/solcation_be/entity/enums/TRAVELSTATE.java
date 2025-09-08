package org.solcation.solcation_be.entity.enums;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum TRAVELSTATE implements LegacyCommonType {
    BEFORE(0, "BEFORE"),
    ONGOING(1, "ONGOING"),
    FINISH(2, "FINISH");

    private final int code;
    private final String label;

    TRAVELSTATE(int code, String label) {
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
