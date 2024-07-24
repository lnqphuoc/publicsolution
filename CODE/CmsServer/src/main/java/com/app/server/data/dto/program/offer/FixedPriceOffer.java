package com.app.server.data.dto.program.offer;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class FixedPriceOffer extends ProgramOffer {
    private Map<Integer, Double> mpProductPrice;

    public FixedPriceOffer() {
        mpProductPrice = new LinkedHashMap<>();
    }
}