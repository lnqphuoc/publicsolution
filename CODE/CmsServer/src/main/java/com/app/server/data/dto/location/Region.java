package com.app.server.data.dto.location;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Region extends Location {
    private String code;
    private List<City> ltCity;

    public Region() {
        this.ltCity = new ArrayList<>();
    }
}