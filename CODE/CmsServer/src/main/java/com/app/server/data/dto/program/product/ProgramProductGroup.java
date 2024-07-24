package com.app.server.data.dto.program.product;

import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.offer.ProgramOffer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProgramProductGroup {
    private int id;
    private String name;
    private String code;
    private String image;
    private List<ProgramProduct> ltProgramProduct;
    private List<String> ltImage;
    private long fromValue;
    private long endValue;
    private ProgramOffer offer;
    private ProgramLimit programLimit;
    private int dataIndex;
    private boolean isCombo;
    private int maxOfferPerPromo;
    private int maxOfferPerAgency;
    private int comboId;
    private String note;
    private double offerValue;

    public ProgramProductGroup() {
        this.code = "";
        this.name = "";
        this.ltImage = new ArrayList<>();
        this.ltProgramProduct = new ArrayList<>();
        this.note = "";
    }

    // check contain program's product
    public boolean containProgramProduct(int productId) {
        Product product = new Product();
        product.setId(productId);
        ProgramProduct programProduct = new ProgramProduct();
        programProduct.setProduct(product);
        return ltProgramProduct.contains(programProduct);
    }

    public long getUnderEndValue() {
        return (endValue - 1);
    }
}