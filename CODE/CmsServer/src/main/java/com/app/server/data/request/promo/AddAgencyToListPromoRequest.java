package com.app.server.data.request.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddAgencyToListPromoRequest {
    private int agency_id;
    List<Integer> promo_ids = new ArrayList<>();
}