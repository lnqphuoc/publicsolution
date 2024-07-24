package com.app.server.data.dto.program;

public enum ProgramType {
    SALE_POLICY(1),
    PROMOTION(2),
    CTSS(3),
    CTTL(4),
    DAMME(5),
    BXH(6),
    NHIEM_VU_BXH(7);
    private final int value;

    private ProgramType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}