package com.app.server.data.request.damme;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class ProductGiamThemRequest {
    private int id;
    private long giam_them;
    private int status;
    private String name;
    private String code;
    private long price;
    private int quantity;
}