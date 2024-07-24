package com.app.server.data.dto.program;

import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.limit.StepLimit;
import com.app.server.data.dto.program.product.ProgramProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SalePolicy extends Program {
    private ProgramConditionType conditionType;
    private ProgramGoodsType goodsType;
    private boolean isEndLimit;
    private ProgramLimit ltStepLimit;
    private boolean allowShowTag;

    public SalePolicy() {
        super();
    }
}