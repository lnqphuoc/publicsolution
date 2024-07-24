package com.app.server.utils;

import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.AgencyOrderEntity;
import com.app.server.data.entity.ProductEntity;
import com.app.server.data.response.product.ProductMoneyResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductUtils {
    public ProductMoneyResponse getProductMoney(
            AgencyEntity agencyEntity,
            ProductEntity productEntity,
            int quantity,
            double price,
            int minimum_purchase) {
        ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
        if (productEntity != null) {
            productMoneyResponse.setId(productEntity.getId());
            productMoneyResponse.setCode(productEntity.getCode());
            productMoneyResponse.setFull_name(productEntity.getFull_name());
            productMoneyResponse.setProduct_small_unit_id(productEntity.getProduct_small_unit_id());
            productMoneyResponse.setQuantity(quantity);
            productMoneyResponse.setImages(productEntity.getImages());
            productMoneyResponse.setPrice(price * 1L);
            productMoneyResponse.setStep(productEntity.getStep());
            productMoneyResponse.setMinimum_purchase(minimum_purchase);
            productMoneyResponse.setItem_type(productEntity.getItem_type());

            if (price > 0) {
                productMoneyResponse.setTotal_begin_price(1.0 * price * quantity);
                productMoneyResponse.setTotal_promo_price(0L);
                productMoneyResponse.setTotal_end_price(
                        productMoneyResponse.getTotal_begin_price() - productMoneyResponse.getTotal_promo_price());
            }
        }
        return productMoneyResponse;
    }
}