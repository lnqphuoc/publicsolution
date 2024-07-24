package com.app.server.data.dto.cttl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CTTLProduct {
    private double productNetPrice;
    private long productDtt;
    private int productId;
    private String createdDate;
    private long productPrice;
    private int productQuantity;
}