package com.app.server.data.response.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CityResponse extends LocationResponse {
    @JsonProperty("districts")
    private List<DistrictResponse> ltDistrict;
    private int region_id;

    public CityResponse() {
        ltDistrict = new ArrayList<>();
    }
}