
package com.app.server.data.dto.csdm;

import com.app.server.data.dto.cttl.CTTLGiftOffer;
import com.app.server.data.dto.product.ProductCache;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CSDMAgencyOffer {
    private int id;
    private int level;
    private long money;
    private long discount;
    private List<CSDMGiftOffer> gifts = new ArrayList<>();
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

        for (CSDMGiftOffer giftOffer : gifts) {
            giftOffer.setId(
                    gift_info.getId()
            );
            giftOffer.setCode(
                    gift_info.getCode()
            );
            giftOffer.setFull_name(
                    gift_info.getFull_name()
            );
            giftOffer.setImages(
                    gift_info.getImages()
            );
            giftOffer.setQuantity(
                    giftOffer.getProduct_quantity()
            );
        }
    }

    public void parseGiftInfo() {
        gifts.clear();
        for (JSONObject jsGift : ltGift) {
            CSDMGiftOffer giftOffer = new CSDMGiftOffer();
            giftOffer.setProduct_id(
                    ConvertUtils.toInt(jsGift.get("productId"))
            );
            giftOffer.setProduct_quantity(
                    ConvertUtils.toInt(jsGift.get("productQuantity"))
            );
            gifts.add(giftOffer);
        }
    }
}