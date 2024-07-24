package com.app.server.database;

import com.app.server.data.entity.*;
import com.app.server.database.repository.*;
import com.app.server.enums.WarehouseBillType;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WarehouseDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    public List<JSONObject> searchWarehouse(String query, int offset, int pageSize, int isLimit) {
        query += " LIMIT " + offset + "," + pageSize;
        return this.masterDB.find(query);
    }

    public List<JSONObject> filter(String query, int offset, int pageSize) {
        query += " LIMIT " + offset + "," + pageSize;
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }


    public int getQuantityImportByDate(int product_id, String from_date, String to_date) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(quantity_import_today) as total" +
                        " FROM warehouse_info_date" +
                        " WHERE product_id = " + product_id +
                        " AND DATE(created_date) >= DATE('" + from_date + "')" +
                        " AND DATE(created_date) <= DATE('" + to_date + "')"
        );

        return ConvertUtils.toInt(rs.get("total"));
    }

    public int getQuantityExportByDate(int product_id, String from_date, String to_date) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(quantity_export_today) as total" +
                        " FROM warehouse_info_date" +
                        " WHERE product_id = " + product_id +
                        " AND DATE(created_date) >= DATE('" + from_date + "')" +
                        " AND DATE(created_date) <= DATE('" + to_date + "')"
        );
        return ConvertUtils.toInt(rs.get("total"));
    }

    public int getQuantityStartToday(int product_id, String start_date) {
        return 0;
    }

    public boolean checkCodeWarehouseBill(String code) {
        JSONObject jsonObject = this.getWarehouseBillByCode(code);
        if (jsonObject != null) {
            return true;
        }

        return false;
    }

    private JSONObject getWarehouseBillByCode(String code) {
        return this.masterDB.getOne(
                "SELECT * FROM warehouse_bill WHERE code = '" + code + "' LIMIT 1"
        );
    }

    public int insertWarehouseBill(WarehouseBillEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO warehouse_bill(" +
                        "code" + "," +
                        "warehouse_id" + "," +
                        "warehouse_bill_type_id" + "," +
                        "warehouse_export_bill_type_id" + "," +
                        "data" + "," +
                        "note" + "," +
                        "reason" + "," +
                        "confirmed_date" + "," +
                        "order_code" + "," +
                        "agency_id" + "," +
                        "target_warehouse_id" + "," +
                        "confirmer_id" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "creator_id" + "," +
                        "modified_date" + "" +
                        ") VALUES(" +
                        parseStringToSql(entity.getCode()) + "," +
                        parseIntegerToSql(entity.getWarehouse_id()) + "," +
                        parseIntegerToSql(entity.getWarehouse_bill_type_id()) + "," +
                        parseIntegerToSql(entity.getWarehouse_export_bill_type_id()) + "," +
                        parseStringToSql(entity.getData()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseStringToSql(entity.getReason()) + "," +
                        parseDateToSql(entity.getConfirmed_date()) + "," +
                        parseStringToSql(entity.getOrder_code()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getTarget_warehouse_id()) + "," +
                        parseIntegerToSql(entity.getConfirmer_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "" +
                        ")"
        );
    }

    public int insertWarehouseBillDetail(WarehouseBillDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO warehouse_bill_detail(" +
                        "warehouse_bill_id" + "," +
                        "product_id" + "," +
                        "product_quantity" + "," +
                        "note" + "," +
                        "created_date" + "" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getWarehouse_bill_id()) + "," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getProduct_quantity()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "" +
                        ")"
        );
    }

    public JSONObject getWarehouseInfoByProduct(int product_id) {
        return this.masterDB.getOne(
                "SELECT * FROM warehouse_info" +
                        " WHERE product_id = " + product_id
        );
    }

    public JSONObject getWarehouseInfoDateByProduct(int product_id, String date) {
        return this.masterDB.getOne(
                "SELECT * FROM warehouse_info_date" +
                        " WHERE product_id = " + product_id +
                        " AND DATE(created_date) = DATE('" + date + "')"
        );
    }

    public int insertWarehouseInfo(WarehouseInfoEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO warehouse_info(" +
                        "product_id" + "," +
                        "quantity_start_today" + "," +
                        "quantity_import_today" + "," +
                        "quantity_export_today" + "," +
                        "quantity_waiting_approve_today" + "," +
                        "quantity_waiting_ship_today" + "," +
                        "quantity_end_today" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modified_date" + "" +
                        ")" +
                        " VALUES(" +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getQuantity_start_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_import_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_export_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_waiting_approve_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_waiting_ship_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_end_today()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) +
                        ")"
        );
    }

    public boolean updateWarehouseInfo(WarehouseInfoEntity entity) {
        return this.masterDB.update(
                "UPDATE warehouse_info SET " +
                        "product_id = " + parseIntegerToSql(entity.getProduct_id()) + "," +
                        "quantity_start_today = " + parseIntegerToSql(entity.getQuantity_start_today()) + "," +
                        "quantity_import_today = " + parseIntegerToSql(entity.getQuantity_import_today()) + "," +
                        "quantity_export_today = " + parseIntegerToSql(entity.getQuantity_export_today()) + "," +
                        "quantity_waiting_approve_today = " + parseIntegerToSql(entity.getQuantity_waiting_approve_today()) + "," +
                        "quantity_waiting_ship_today = " + parseIntegerToSql(entity.getQuantity_waiting_ship_today()) + "," +
                        "quantity_end_today = " + parseIntegerToSql(entity.getQuantity_end_today()) + "," +
                        "status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        "created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        "modified_date = " + parseDateToSql(entity.getModified_date()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertWarehouseInfoDate(WarehouseInfoDateEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO warehouse_info_date(" +
                        "product_id" + "," +
                        "quantity_start_today" + "," +
                        "quantity_import_today" + "," +
                        "quantity_export_today" + "," +
                        "quantity_waiting_approve_today" + "," +
                        "quantity_waiting_ship_today" + "," +
                        "quantity_end_today" + "," +
                        "status" + "," +
                        "created_date" + "" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getQuantity_start_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_import_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_export_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_waiting_approve_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_waiting_ship_today()) + "," +
                        parseIntegerToSql(entity.getQuantity_end_today()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "" +
                        ")"
        );
    }

    public WarehouseBillEntity getWarehouseBill(int id) {
        JSONObject jsonObject = this.masterDB.getOne(
                "SELECT * FROM warehouse_bill WHERE id = " + id
        );
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), WarehouseBillEntity.class);
        }
        return null;
    }

    public boolean updateWarehouseBill(WarehouseBillEntity entity) {
        return this.masterDB.update(
                "UPDATE warehouse_bill SET " +
                        "code" + "=" + parseStringToSql(entity.getCode()) + "," +
                        "warehouse_id" + "=" + parseIntegerToSql(entity.getWarehouse_id()) + "," +
                        "warehouse_bill_type_id" + "=" + parseIntegerToSql(entity.getWarehouse_bill_type_id()) + "," +
                        "warehouse_export_bill_type_id" + "=" + parseIntegerToSql(entity.getWarehouse_export_bill_type_id()) + "," +
                        "data" + "=" + parseStringToSql(entity.getData()) + "," +
                        "note" + "=" + parseStringToSql(entity.getNote()) + "," +
                        "reason" + "=" + parseStringToSql(entity.getReason()) + "," +
                        "confirmed_date" + "=" + parseDateToSql(entity.getConfirmed_date()) + "," +
                        "order_code" + "=" + parseStringToSql(entity.getOrder_code()) + "," +
                        "agency_id" + "=" + parseIntegerToSql(entity.getAgency_id()) + "," +
                        "target_warehouse_id" + "=" + parseIntegerToSql(entity.getTarget_warehouse_id()) + "," +
                        "confirmer_id" + "=" + parseIntegerToSql(entity.getConfirmer_id()) + "," +
                        "status" + "=" + parseIntegerToSql(entity.getStatus()) + "," +
                        "created_date" + "=" + parseDateToSql(entity.getCreated_date()) + "," +
                        "modifier_id" + "=" + parseIntegerToSql(entity.getModifier_id()) + "," +
                        "creator_id" + "=" + parseIntegerToSql(entity.getCreator_id()) + "," +
                        "modified_date" + "=" + parseDateToSql(entity.getModified_date()) + "" +
                        " WHERE id = " + entity.getId()
        );
    }

    public boolean clearWarehouseBillDetail(int id) {
        return this.masterDB.update(
                "DELETE FROM warehouse_bill_detail WHERE warehouse_bill_id = " + id
        );
    }

    public List<JSONObject> getListProductInBill(int warehouse_bill_id) {
        return this.masterDB.find(
                "SELECT * FROM warehouse_bill_detail WHERE warehouse_bill_id = " + warehouse_bill_id
        );
    }

    public boolean increaseQuantityImportToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_import_today = quantity_import_today + " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean increaseQuantityExportToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_export_today = quantity_export_today + " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean increaseQuantityWaitingShipToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_waiting_ship_today = quantity_waiting_ship_today + " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean decreaseQuantityWaitingApproveToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_waiting_approve_today = quantity_waiting_approve_today - " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean decreaseQuantityWaitingShipToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_waiting_ship_today = quantity_waiting_ship_today - " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean decreaseQuantityExportToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_export_today = quantity_export_today + " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public boolean increaseQuantityWaitingApproveToday1(int product_id, int product_quantity) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_waiting_approve_today = quantity_waiting_approve_today + " + product_quantity +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public int insertWarehouseStockHistory(WarehouseStockHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO warehouse_stock_history(" +
                        "product_id" + "," +
                        "before_value" + "," +
                        "change_value" + "," +
                        "after_value" + "," +
                        "type" + "," +
                        "note" + "," +
                        "status" + "," +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getBefore_value()) + "," +
                        parseIntegerToSql(entity.getChange_value()) + "," +
                        parseIntegerToSql(entity.getAfter_value()) + "," +
                        parseStringToSql(entity.getType()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "" +
                        ")"
        );
    }

    public int getTotalWarehouseBillImportToday() {
        return this.masterDB.getTotal(
                "SELECT * FROM warehouse_bill WHERE warehouse_bill_type_id = " + WarehouseBillType.IMPORT.getValue() +
                        " AND DATE(created_date) = DATE('" + DateTimeUtils.getNow("yyyy-MM-dd") + "')"
        );
    }

    public int getTotalWarehouseBillExportToday() {
        return this.masterDB.getTotal(
                "SELECT * FROM warehouse_bill WHERE warehouse_bill_type_id = " + WarehouseBillType.EXPORT.getValue() +
                        " AND DATE(created_date) = DATE('" + DateTimeUtils.getNow("yyyy-MM-dd") + "')"
        );
    }

    public boolean updateWarehouseStartToday(int product_id, int quantity_start_today) {
        return this.masterDB.update(
                "UPDATE warehouse_info" +
                        " SET quantity_start_today =  " + quantity_start_today +
                        ", modified_date = NOW()" +
                        " WHERE product_id = " + product_id
        );
    }

    public List<JSONObject> getStockByListProduct(String product_ids) {
        return this.masterDB.find(
                "SELECT product_id as id, " +
                        " quantity_start_today + quantity_import_today - quantity_export_today - quantity_waiting_ship_today - quantity_waiting_approve_today as stock" +
                        " FROM warehouse_info" +
                        " WHERE '" + product_ids + "' LIKE CONCAT('%\"',product_id,'\"%')"
        );
    }
}