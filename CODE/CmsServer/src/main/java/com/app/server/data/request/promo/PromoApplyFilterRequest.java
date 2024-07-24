package com.app.server.data.request.promo;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoApplyFilterRequest {
    private int data_index;
    private List<PromoApplyFilterDetailRequest> filter_types = new ArrayList<>();
}