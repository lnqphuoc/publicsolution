package com.app.server.data.response.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EstimateCostOrderResponse extends OrderSummaryInfoResponse {
    private List<ItemOfferResponse> goodsOffers = new ArrayList<>();
}