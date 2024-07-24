package com.app.server.data.request.promo;

import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class GetPromoByProductRequest {
    private int product_id;
    private int is_combo;
    private int agency_id;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}