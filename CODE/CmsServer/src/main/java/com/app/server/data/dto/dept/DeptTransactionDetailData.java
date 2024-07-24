package com.app.server.data.dto.dept;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeptTransactionDetailData {
    protected int ItemId;
    protected int AppItemId;
    protected String ItemCode = "";
    protected String ItemName = "";
    protected String Unit = "";
    protected int WarehouseIdApp;
    protected String WarehouseCode = "";
    protected int Quantity;
    protected long UnitPrice;
    protected long Amount;
    protected int IdApp_SO;
    protected String DocNoApp_SO = "";
}