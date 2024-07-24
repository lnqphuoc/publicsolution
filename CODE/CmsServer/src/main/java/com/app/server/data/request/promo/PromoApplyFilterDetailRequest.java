package com.app.server.data.request.promo;


import lombok.Data;

import java.util.List;

@Data
public class PromoApplyFilterDetailRequest {
    private String filter_type;
    private String filter_data;
}