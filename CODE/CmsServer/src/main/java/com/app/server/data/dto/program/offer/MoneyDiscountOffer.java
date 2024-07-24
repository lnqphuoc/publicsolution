package com.app.server.data.dto.program.offer;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class MoneyDiscountOffer extends ProgramOffer {
    private long money;
    private Map<Integer, Double> mpProductMoney;

    public MoneyDiscountOffer() {
        mpProductMoney = new LinkedHashMap<>();
    }
}