package com.app.server.data.request;

import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class EditBasicDataRequest {
    private int id;
    private String code = "";
    private String name = "";
    private String image = "";

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}