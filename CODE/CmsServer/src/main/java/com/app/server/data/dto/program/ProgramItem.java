package com.app.server.data.dto.program;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramItem {
    private ProgramItemType itemType;
    private int itemId;
    private int categoryLevel;

    public ProgramItem() {
        this.itemType = ProgramItemType.PRODUCT;
    }
}