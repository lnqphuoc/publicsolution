package com.app.server.data.dto.csdm;

import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Set;

@Getter
@Setter
public class CSDMAgencyReward {
    private int id;
    private long totalDTT;
    private long totalOrderPrice;
    private long moneyDiscountPrice;
    private long percentDiscountPrice;
    private long rewardValue;

    private int totalGiftQuantity;

    private long totalValue; //Tổng giá trị tích lũy

    private int limitValue;
    private int percentValue;

    public void parseReward(JSONObject js) {
        totalDTT = ConvertUtils.toLong(js.get("totalDTT"));
        totalOrderPrice = ConvertUtils.toLong(js.get("totalOrderPrice"));
        moneyDiscountPrice = ConvertUtils.toLong(js.get("moneyDiscountPrice"));
        percentDiscountPrice = ConvertUtils.toLong(js.get("percentDiscountPrice"));
        if (js.get("mpGiftProduct") != null) {
            JSONObject mpGiftProduct =
                    JsonUtils.DeSerialize(js.get("mpGiftProduct").toString(), JSONObject.class);
            Set<String> names = mpGiftProduct.keySet();
            if (moneyDiscountPrice > 0) {
                rewardValue = moneyDiscountPrice;
            } else if (percentDiscountPrice > 0) {
                rewardValue = percentDiscountPrice;
            } else if (names.size() > 0) {
                for (String key : names) {
                    rewardValue += ConvertUtils.toInt(mpGiftProduct.get(key));
                }
            }
        }

        limitValue = ConvertUtils.toInt(js.get("limitValue"));
        percentValue = ConvertUtils.toInt(js.get("percentValue"));
        totalValue = ConvertUtils.toInt(js.get("totalValue"));
    }

    public boolean isDat() {
        return limitValue > 0;
    }
}