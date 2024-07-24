package com.app.server.data.dto.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoProductInfoData {
    private int product_id;
    private List<PromoOrderData> promo = new ArrayList<>();
}