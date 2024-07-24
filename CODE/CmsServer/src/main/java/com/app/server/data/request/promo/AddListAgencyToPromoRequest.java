package com.app.server.data.request.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddListAgencyToPromoRequest {
    private int promo_id;
    List<Integer> agency_ids = new ArrayList<>();
}