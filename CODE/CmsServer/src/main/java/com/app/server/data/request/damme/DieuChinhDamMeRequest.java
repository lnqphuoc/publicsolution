package com.app.server.data.request.damme;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DieuChinhDamMeRequest {
    private String code;
    private String option;
    private long giam_gia_tri_don_hang;
    private List<ProductGiamThemRequest> products = new ArrayList<>();
}