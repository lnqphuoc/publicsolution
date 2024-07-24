package com.app.server.data.request.catalog;

import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateCatalogRequest {
    private String name;
    private String image;
    private int is_show;
    private List<Integer> categories = new ArrayList<>();

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}