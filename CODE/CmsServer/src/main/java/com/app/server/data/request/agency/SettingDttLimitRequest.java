package com.app.server.data.request.agency;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SettingDttLimitRequest {
    private List<Long> data = new ArrayList<>();
}