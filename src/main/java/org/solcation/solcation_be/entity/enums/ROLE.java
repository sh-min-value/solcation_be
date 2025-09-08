package org.solcation.solcation_be.entity.enums;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum ROLE implements LegacyCommonType {
    ADMIN(0, "ADMIN"),
    USER(1, "USER");

    private final int code;
    private final String label;

    ROLE(int code, String label) {
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
