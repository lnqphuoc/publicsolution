package com.app.server.data.request.product;

import com.app.server.data.dto.product.ProductNewData;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EditProductNewRequest {
    private List<ProductNewData> products = new ArrayList<>();
}