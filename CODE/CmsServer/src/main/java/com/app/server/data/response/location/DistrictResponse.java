package com.app.server.data.response.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DistrictResponse extends LocationResponse{
    @JsonProperty("wards")
    private List<WardResponse> ltWard;
    public DistrictResponse(){
        ltWard = new ArrayList<>();
    }
}