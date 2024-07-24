package com.app.server.data.dto.program.offer;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class PercentDiscountOffer extends ProgramOffer {
    private int percent;
    private Map<Integer, Integer> mpProductPercent;

    public PercentDiscountOffer() {
        mpProductPercent = new LinkedHashMap<>();
    }
}