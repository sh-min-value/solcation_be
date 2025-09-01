package org.solcation.solcation_be.entity;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum GENDER implements LegacyCommonType {
    M(0, "M"), F(1, "F");

    private final int code;
    private final String label;

    GENDER(int code, String label) {
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
