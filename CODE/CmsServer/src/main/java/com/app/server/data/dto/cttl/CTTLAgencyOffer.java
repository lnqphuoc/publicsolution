package com.app.server.data.dto.cttl;

import com.app.server.data.dto.product.ProductCache;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CTTLAgencyOffer {
    private int id;
    private int level;
    private long money;
    private long discount;
    private List<CTTLGiftOffer> gifts = new ArrayList<>();
    private List<JSONObject> ltGift = new ArrayList<>();

    public Long sumGift() {
        return gifts.stream().reduce(
                0L,
                (t, object) -> t + object.getProduct_quantity(),
                Long::sum);
    }

    public void convertGiftInfo(ProductCache gift_info) {
        if (gifts == null) {
            gifts = new ArrayList<>();
        }

        for (CTTLGiftOffer cttlGiftOffer : gifts) {
            cttlGiftOffer.setId(
                    gift_info.getId()
            );
            cttlGiftOffer.setCode(
                    gift_info.getCode()
            );
            cttlGiftOffer.setFull_name(
                    gift_info.getFull_name()
            );
            cttlGiftOffer.setImages(
                    gift_info.getImages()
            );
            cttlGiftOffer.setQuantity(
                    cttlGiftOffer.getProduct_quantity()
            );
        }
    }

    public void parseGiftInfo() {
        gifts.clear();
        for (JSONObject jsGift : ltGift) {
            CTTLGiftOffer cttlGiftOffer = new CTTLGiftOffer();
            cttlGiftOffer.setProduct_id(
                    ConvertUtils.toInt(jsGift.get("productId"))
            );
            cttlGiftOffer.setProduct_quantity(
                    ConvertUtils.toInt(jsGift.get("productQuantity"))
            );
            gifts.add(cttlGiftOffer);
        }
    }
}