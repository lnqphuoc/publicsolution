package com.app.server.data.request.damme;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetOrderInfoByCodeRequest {
    private String code;
}