package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionChangedTransactionData {
    private String transactionCode;
    private long changedValue;
}