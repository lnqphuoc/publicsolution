package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Product {
    private int id;
    private String code;
    private String shortName;
    private String fullName;
    private String warrantyTime;
    private String images;
    private String specification;
    private int colorId;
    private String characteristic;
    private String description;
    private String userManual;
    private String technicalData;
    private int status;
    private double price;
    private int smallUnitId;
    private int bigUnitId;
    private int convertSmallUnitRatio;
    private int minimumPurchase;
    private int step;
    private long totalSellQuantity;
    private long totalSellTurn;
    private int hotPriority;
    private int warehouseQuantity;
    private Date createdDate;
    private int itemType;
    private String sortData;
    private String tag;
    private List<String> ltImage;
    private ProductGroup productGroup;
    private Brand brand;

    public Product() {
        code = "";
        tag = "";
        ltImage = new ArrayList<>();
    }

    // set data
    public void setData(Product product) {
        this.code = product.getCode();
        this.shortName = product.getShortName();
        this.fullName = product.getFullName();
        this.warrantyTime = product.getWarrantyTime();
        this.images = product.getImages();
        this.specification = product.getSpecification();
        this.colorId = product.getColorId();
        this.characteristic = product.getCharacteristic();
        this.description = product.getDescription();
        this.userManual = product.getUserManual();
        this.technicalData = product.getTechnicalData();
        this.status = product.getStatus();
        this.price = product.getPrice();
        this.smallUnitId = product.getSmallUnitId();
        this.bigUnitId = product.getBigUnitId();
        this.convertSmallUnitRatio = product.getConvertSmallUnitRatio();
        this.minimumPurchase = product.getMinimumPurchase();
        this.step = product.getStep();
        this.totalSellQuantity = product.getTotalSellQuantity();
        this.totalSellTurn = product.getTotalSellTurn();
        this.hotPriority = product.getHotPriority();
        this.warehouseQuantity = product.getWarehouseQuantity();
        this.createdDate = product.getCreatedDate();
        this.itemType = product.getItemType();
        this.sortData = product.getSortData();
        this.tag = product.getTag();
        this.ltImage = product.getLtImage();
    }

    // get preview image
    public String getPreviewImage() {
        if (!ltImage.isEmpty())
            return ltImage.get(0);
        return "";
    }

    // check is contact price
    public boolean isContactPrice() {
        return (price <= 0);
    }

    // get price
    public double getPrice() {
        if (price < 0)
            price = 0;
        return price;
    }

    // get category's parent priority
    public int getCategoryParentPriority() {
        return productGroup.getCategory().getParentPriority();
    }

    // get category's priority
    public int getCategoryPriority() {
        return productGroup.getCategory().getPriority();
    }

    // get product group's sort data
    public String getProductGroupSortData() {
        return productGroup.getSortData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}