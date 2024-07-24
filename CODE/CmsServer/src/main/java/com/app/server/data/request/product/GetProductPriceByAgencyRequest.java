package com.app.server.data.request.product;

import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetProductPriceByAgencyRequest {
    private int product_id;
    private int agency_id;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}