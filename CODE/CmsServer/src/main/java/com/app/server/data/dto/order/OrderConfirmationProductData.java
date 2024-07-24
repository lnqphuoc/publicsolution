package com.app.server.data.dto.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderConfirmationProductData {
    private String po_code;
    private String item_code;
    private int item_quantity;
    private double item_price;
    private double item_amount;
    private String oc_code;
    private String item_description;
    private String note;
    private String delivery_date;
}