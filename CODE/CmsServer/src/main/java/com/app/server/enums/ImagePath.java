package com.app.server.enums;

import com.app.server.config.ConfigInfo;

public enum ImagePath {
    PRODUCT("product"),
    CATEGORY("category"),
    BANNER("banner"),
    PROMO("promo"),
    AGENCY("agency"),
    AVATAR("avatar"),
    BRAND("brand"),
    NOTIFY("notify"),
    COMBO("combo"),
    PRODUCT_HOT("product_hot"),
    DEAL_PRODUCT("deal_product"),
    CATALOG("catalog"),
    MEMBERSHIP("membership"),
    FILE("file"),
    VOUCHER("voucher");

    private String value;

    ImagePath(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getImagePath() {
        return ConfigInfo.IMAGE_FOLDER_PATH + "/" + value + "/";
    }

    public String getImageUrl() {
        return ConfigInfo.IMAGE_URL + "/" + value + "/";
    }

}