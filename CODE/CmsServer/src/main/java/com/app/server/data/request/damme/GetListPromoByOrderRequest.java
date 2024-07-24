package com.app.server.data.request.damme;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class GetListPromoByOrderRequest {
    private String code;
    private List<ProductGiamThemRequest> products;
}