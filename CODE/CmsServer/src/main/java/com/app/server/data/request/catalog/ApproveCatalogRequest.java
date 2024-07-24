package com.app.server.data.request.catalog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveCatalogRequest {
    private int id;
    private String note;
}