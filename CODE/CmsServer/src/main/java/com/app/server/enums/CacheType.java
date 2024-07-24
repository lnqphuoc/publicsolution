package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CacheType {
    CATEGORY("category"),
    COLOR("color"),
    BRAND("brand"),
    PRODUCT("product"),
    PRODUCT_GROUP("product_group"),
    PROMO("program"),
    VISIBILITY("visibility"),
    PRODUCT_HOT("product_hot"),
    PRODUCT_PRICE("product_price"),
    BANNER("banner"),
    COMBO("combo"),
    PRODUCT_HOT_TYPE("product_hot_type"),
    DEAL_PRICE("deal_price"),
    PRODUCT_SMALL_UNIT("product_small_unit"),
    PRODUCT_NEW("product_new"),
    CATALOG("catalog"),
    CONFIG("config"),
    DAM_ME("dam_me"),
    BXH("bxh"),
    ;

    private String value;

    CacheType(String value) {
        this.value = value;
    }
}