package com.app.server.enums;

import lombok.Getter;

@Getter
public enum TransactionEffectType {
    CNO(1, "CNO", "Công nợ"),
    DTT(2, "DTT", "Doanh thu thuần"),
    TT(3, "TT", "Tiền thu"),
    A_COIN(4, "A_COIN", "A-COIN"),
    ;

    private int id;
    private String code;
    private String label;

    TransactionEffectType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}