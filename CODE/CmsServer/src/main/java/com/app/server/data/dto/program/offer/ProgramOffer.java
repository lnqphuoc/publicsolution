package com.app.server.data.dto.program.offer;

import com.app.server.data.dto.program.ProgramGoodsType;
import com.app.server.data.dto.program.ProgramOfferType;
import com.app.server.data.dto.program.product.ProgramProductGroup;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ProgramOffer {
    private ProgramOfferType offerType;
    private ProgramGoodsType goodsType;
    private double conversionRatio;
    private double offer_value;
    private ProgramProductGroup programProductGroup;
}