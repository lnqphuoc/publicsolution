package com.app.server.data.dto.order;

import com.app.server.data.dto.product.ProductData;
import com.app.server.data.dto.promo.PromoBasicData;
import com.app.server.data.response.product.ProductMoneyResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuntSaleOrderDetail extends ProductMoneyResponse {
    private int is_combo;
    private int is_hunt_sale;
    private List<ProductData> products = new ArrayList<>();
    protected String promo_description;

    public HuntSaleOrderDetail initStub(int id, int quantity, int is_combo) {
        this.setId(id);
        this.setQuantity(quantity);
        this.setIs_combo(is_combo);
        return this;
    }

    public void calculateTotalMoney() {
        this.setPrice(
                products.stream().reduce(0.0, (total, object) -> total + (object.getPrice() * object.getQuantity()), Double::sum)
        );
        this.setTotal_promo_price(
                products.stream().reduce(0.0, (total, object) -> total + (object.getTotal_promo_price() * getQuantity()), Double::sum)
        );
        this.setTotal_begin_price(
                this.getPrice() * this.getQuantity()
        );
        this.setTotal_end_price(
                this.getTotal_begin_price() -
                        this.getTotal_promo_price()
        );
    }

    public Integer sumProductQuantity() {
        if (is_combo == 0) {
            return getQuantity();
        } else {
            return products.stream().reduce(0, (total, object) -> total + object.getQuantity(), Integer::sum);
        }
    }

    public void setPriceContact() {
        this.total_begin_price = 0;
        this.total_promo_price = 0;
        this.total_end_price = 0;
    }
}