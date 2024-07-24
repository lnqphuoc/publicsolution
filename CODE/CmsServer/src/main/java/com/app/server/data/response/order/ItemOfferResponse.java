package com.app.server.data.response.order;

import lombok.Data;

@Data
public class ItemOfferResponse {
    private int id;
    private String name;
    private String code;
    private String images;
    private int quantity_select;
    private int offer_value;
    private int real_value;
    private Double price;
    private String offer_type;
    private int is_error = 0;
    private String note;
}