package com.app.server.data.request.catalog;

import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditCatalogRequest extends CreateCatalogRequest {
    private int id;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}