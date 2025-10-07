package org.solcation.solcation_be.entity.enums;

import org.solcation.solcation_be.util.entity.LegacyCommonType;

public enum DEPOSITDAY implements LegacyCommonType {
    MON(0, "MON"),
    TUE(1, "TUE"),
    WED(2, "WED"),
    THU(3, "THU"),
    FRI(4, "FRI"),
    SAT(5, "SAT"),
    SUN(6, "SUN");

    private final int code;
    private final String label;

    DEPOSITDAY(int code, String label) {
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
