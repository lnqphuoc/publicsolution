package com.app.server.data.dto.location;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class City extends Location {
    private Region region;
    private String code;
    private List<District> ltDistrict;

    public City() {
        ltDistrict = new ArrayList<>();
    }
}