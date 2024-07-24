package com.app.server.data.request.promo;

import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoItemIgnoreRequest {
    protected int item_id;
    protected String item_code = "";
    protected String item_name = "";
    protected double item_price = 0;
}