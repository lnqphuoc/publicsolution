package com.app.server.data.dto.program;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDataSetting {
    private int productId;
    private int productMinimumPurchase;
    private double productPrice;
}