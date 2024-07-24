package com.app.server.data.dto.cttl;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class CTTLGiftOffer {
    private int product_id;
    private int product_quantity;
    private int id;
    private String full_name;
    private String code;
    private String images;
    private int quantity;
}