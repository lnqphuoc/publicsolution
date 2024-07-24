package com.app.server.manager;

import com.app.server.data.dto.warehouse.WarehouseBasicData;
import com.app.server.data.dto.warehouse.WarehouseTypeData;
import com.app.server.data.entity.WarehouseInfoEntity;
import com.app.server.database.MasterDB;
import com.app.server.database.OrderDB;
import com.app.server.database.WarehouseDB;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Getter
public class WarehouseManager {
    private Map<Integer, WarehouseBasicData> mpWarehouse = new LinkedHashMap<>();
    private Map<Integer, WarehouseTypeData> mpWarehouseType = new LinkedHashMap<>();

    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private WarehouseDB warehouseDB;

    @Autowired
    public void setWarehouseDB(WarehouseDB warehouseDB) {
        this.warehouseDB = warehouseDB;
    }

    private OrderDB orderDB;

    @Autowired
    public void setOrderDB(OrderDB orderDB) {
        this.orderDB = orderDB;
    }

    public void loadData() {
        this.reload();
    }

    private void reload() {
        this.loadWarehouse();
        this.loadWarehouseType();
    }

    private void loadWarehouseType() {
        this.mpWarehouseType.clear();
        List<JSONObject> rs = this.masterDB.find("select * from warehouse_type");
        for (JSONObject js : rs) {
            WarehouseTypeData warehouseBasicData = JsonUtils.DeSerialize(JsonUtils.Serialize(js), WarehouseTypeData.class);
            mpWarehouseType.put(warehouseBasicData.getId(), warehouseBasicData);
        }
    }

    private void loadWarehouse() {
        this.mpWarehouse.clear();
        List<JSONObject> rs = this.masterDB.find("select * from warehouse");
        for (JSONObject js : rs) {
            WarehouseBasicData warehouseBasicData = JsonUtils.DeSerialize(JsonUtils.Serialize(js), WarehouseBasicData.class);
            mpWarehouse.put(warehouseBasicData.getId(), warehouseBasicData);
        }
    }

    public WarehouseBasicData getWarehouse(int warehouse_id) {
        return this.mpWarehouse.get(warehouse_id);
    }

    public WarehouseInfoEntity initWarehouseStartDate(int product_id) {
        WarehouseInfoEntity warehouseInfoEntity;
        JSONObject jsWarehouseInfo = this.warehouseDB.getWarehouseInfoByProduct(product_id);
        if (jsWarehouseInfo != null) {
            warehouseInfoEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(jsWarehouseInfo), WarehouseInfoEntity.class);

            int total = warehouseInfoEntity.getQuantity_start_today()
                    + warehouseInfoEntity.getQuantity_import_today()
                    - warehouseInfoEntity.getQuantity_export_today();


            warehouseInfoEntity.setQuantity_start_today(total);
            warehouseInfoEntity.setQuantity_import_today(0);
            warehouseInfoEntity.setQuantity_export_today(0);
            boolean rsUpdate = this.warehouseDB.updateWarehouseInfo(warehouseInfoEntity);
            if (!rsUpdate) {
                return null;
            }
        } else {
            warehouseInfoEntity = new WarehouseInfoEntity();
            warehouseInfoEntity.setProduct_id(product_id);
            warehouseInfoEntity.setCreated_date(DateTimeUtils.getNow());
            warehouseInfoEntity.setModified_date(DateTimeUtils.getNow());
            int rsInsert = this.warehouseDB.insertWarehouseInfo(warehouseInfoEntity);
            if (rsInsert <= 0) {
                return null;
            }
            warehouseInfoEntity.setId(rsInsert);
        }

        return warehouseInfoEntity;
    }

    public JSONObject getWarehouseInfoByProduct(int product_id) {
        return this.warehouseDB.getWarehouseInfoByProduct(product_id);
    }

    public WarehouseBasicData getWarehouseSell() {
        for (WarehouseBasicData warehouseBasicData : mpWarehouse.values()) {
            if (warehouseBasicData.getAllow_sell() == 1) {
                return warehouseBasicData;
            }
        }
        return null;
    }

    public boolean checkAllowSell(int allow_sell) {
        if (allow_sell == 1) {
            return true;
        }
        return false;
    }

    public void resetTonKho() {
        List<JSONObject> products = this.masterDB.find("SELECT * FROM warehouse_info");
        for (JSONObject jsonObject : products) {
            int product_id = ConvertUtils.toInt(jsonObject.get("product_id"));
            LogUtil.printDebug("product_id: " + product_id);
            List<JSONObject> orderWaitingApproveList = this.orderDB.getListOrderWaitingApprove(product_id);
            int quantity_waiting_approve_today = 0;
            for (JSONObject orderWaitingApprove : orderWaitingApproveList) {
                quantity_waiting_approve_today +=
                        ConvertUtils.toInt(orderWaitingApprove.get("total_final"));
            }
            this.masterDB.update(
                    "UPDATE warehouse_info SET quantity_waiting_approve_today = " + quantity_waiting_approve_today +
                            " WHERE product_id = " + product_id
            );
            LogUtil.printDebug("quantity_waiting_approve_today: " + quantity_waiting_approve_today);
            List<JSONObject> orderWaitingShipList = this.orderDB.getListOrderWaitingShip(product_id);
            int quantity_waiting_ship_today = 0;
            for (JSONObject orderWaitingShip : orderWaitingShipList) {
                quantity_waiting_ship_today +=
                        ConvertUtils.toInt(orderWaitingShip.get("total_final"));
            }
            this.masterDB.update(
                    "UPDATE warehouse_info SET quantity_waiting_ship_today = " + quantity_waiting_ship_today +
                            " WHERE product_id = " + product_id
            );
        }
    }
}