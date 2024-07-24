package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicResponse {
    private int id;
    private String label;
    private String key;
    private String code;
    private String image;
    private int region_id;

    public BasicResponse(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public BasicResponse(String key, String label) {
        this.key = key;
        this.label = label;
        this.code = key;
    }

    public BasicResponse(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.code = key;
    }

    public BasicResponse(int id, String key, String label, String image) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.code = key;
        this.image = image;
    }
}