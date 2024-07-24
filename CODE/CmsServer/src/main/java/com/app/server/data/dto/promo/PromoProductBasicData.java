
package com.app.server.data.dto.promo;

import lombok.Data;

@Data
public class PromoProductBasicData {
    private int id;
    private int product_id;
    private String code;
    private String name;
    private String image;
    private String description;
}