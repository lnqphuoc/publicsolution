package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MissionTransactionInfo {
    private int id;
    private String code;
    private String type;
    private long totalValue;
    private long totalValueFinal;
    private Long createdTime;
    private TransactionInfo transaction_info;
    private int accumulate_type;
    private int status;
    private int orderId;
    private int orderDeptId;
    private boolean isAdd;
    private List<MissionTransactionInfo> childList = new ArrayList<>();
}