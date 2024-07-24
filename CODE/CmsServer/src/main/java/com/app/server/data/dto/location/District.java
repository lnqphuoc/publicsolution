package com.app.server.data.dto.location;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class District extends Location {
    private City city;
    private List<Ward> ltWard;

    public District() {
        this.ltWard = new ArrayList<>();
    }
}