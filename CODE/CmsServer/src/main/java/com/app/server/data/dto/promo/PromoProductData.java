package com.app.server.data.dto.promo;

import lombok.Data;

@Data
public class PromoProductData {
    private int id;
    private String code;
    private String name;
    private String image;
    private String description;
}