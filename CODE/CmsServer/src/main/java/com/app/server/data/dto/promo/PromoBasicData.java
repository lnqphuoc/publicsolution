package com.app.server.data.dto.promo;

import lombok.Data;

@Data
public class PromoBasicData {
    private int id;
    private String code;
    private String name;
    private String image;
    private String description;
    private String condition_type;
    private String promo_type;
}