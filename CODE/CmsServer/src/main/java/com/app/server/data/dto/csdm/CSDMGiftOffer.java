package com.app.server.data.dto.csdm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CSDMGiftOffer {
    private int product_id;
    private int product_quantity;
    private int id;
    private String full_name;
    private String code;
    private String images;
    private int quantity;
}