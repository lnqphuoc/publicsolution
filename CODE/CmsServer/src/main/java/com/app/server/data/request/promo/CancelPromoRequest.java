package com.app.server.data.request.promo;

import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class CancelPromoRequest {
    private int id;
    private String note;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}