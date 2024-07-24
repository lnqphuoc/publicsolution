package com.app.server.data.dto.program;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TLProductReward {
    private int productId;
    private Program program;
    private int offerPercent;
    private long offerValue;

    public int getProgramId() {
        return program.getId();
    }
}