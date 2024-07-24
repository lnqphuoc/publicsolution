package com.app.server.data.dto.catalog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Catalog {
    private int id;
    private String name;
    private String image;
    private int is_show;
}