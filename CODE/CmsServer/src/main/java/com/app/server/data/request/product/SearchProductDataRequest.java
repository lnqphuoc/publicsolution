package com.app.server.data.request.product;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchProductDataRequest {
    private List<ProductDataRequest> products = new ArrayList<>();
}