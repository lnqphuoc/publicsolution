package com.app.server.data.request.product;

import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SettingProductHotRequest {
    private int number_common = 0;
    private int number_agency = 0;
    private List<ProductHotRequest> records = new ArrayList<>();

    public ClientResponse validate() {
        for (int iHot = 0; iHot < records.size(); iHot++) {
            ClientResponse clientResponse = records.get(iHot).validate();
            if (clientResponse.failed()) {
                clientResponse.setMessage("[Thứ " + (iHot + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return ClientResponse.success(null);
    }
}