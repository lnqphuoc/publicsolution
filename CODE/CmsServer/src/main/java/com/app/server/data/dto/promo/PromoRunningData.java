package com.app.server.data.dto.promo;

import com.app.server.data.request.promo.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoRunningData {
    protected PromoInfoData promo_info;
    protected PromoApplyObjectRequest promo_apply_object;
    protected List<PromoItemGroupData> promo_item_groups = new ArrayList<>();
    protected List<PromoItemIgnoreData> promo_item_ignores = new ArrayList<>();
    protected List<PromoLimitData> promo_limits = new ArrayList<>();
}