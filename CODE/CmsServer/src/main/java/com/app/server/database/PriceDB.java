package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.entity.*;
import com.app.server.enums.ProductPriceTimerStatus;
import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PriceDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    public int insertProductPriceSettingDetail(
            ProductPriceSettingDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_setting_detail(" +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "modified_date" + "," +
                        "product_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "product_price_setting_id" + "," +
                        "price_setting_type" + "," +
                        "price_data_type" + "," +
                        "price_setting_value" + "," +
                        "is_auto" + "," +
                        "minimum_purchase" + "," +
                        "price_original" + "," +
                        "price_new" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseIntegerToSql(entity.getProduct_price_setting_id()) + "," +
                        parseStringToSql(entity.getPrice_setting_type()) + "," +
                        parseStringToSql(entity.getPrice_data_type()) + "," +
                        parseDoubleToSql(entity.getPrice_setting_value()) + "," +
                        parseIntegerToSql(entity.getIs_auto()) + "," +
                        parseIntegerToSql(entity.getMinimum_purchase()) + "," +
                        parseLongToSql(entity.getPrice_original()) + "," +
                        parseLongToSql(entity.getPrice_new()) +
                        ")"
        );
    }

    public boolean updateProductPriceSettingDetail(
            ProductPriceSettingDetailEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_setting_detail SET" +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " product_id = " + parseIntegerToSql(entity.getProduct_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " product_price_setting_id = " + parseIntegerToSql(entity.getProduct_price_setting_id()) + "," +
                        " price_setting_type = " + parseStringToSql(entity.getPrice_setting_type()) + "," +
                        " price_data_type = " + parseStringToSql(entity.getPrice_data_type()) + "," +
                        " price_setting_value = " + parseDoubleToSql(entity.getPrice_setting_value()) + "," +
                        " is_auto = " + parseIntegerToSql(entity.getIs_auto()) + "," +
                        " minimum_purchase = " + parseIntegerToSql(entity.getMinimum_purchase()) + "," +
                        " price_original = " + parseLongToSql(entity.getPrice_original()) + "," +
                        " price_new = " + parseLongToSql(entity.getPrice_new()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertProductPriceSettingDetailHistory(
            ProductPriceSettingDetailHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_setting_detail_history(" +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "modified_date" + "," +
                        "product_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "product_price_setting_id" + "," +
                        "price_setting_type" + "," +
                        "price_data_type" + "," +
                        "price_setting_value" + "," +
                        "is_auto" + "," +
                        "minimum_purchase" + "," +
                        "price_original" + "," +
                        "price_new" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseIntegerToSql(entity.getProduct_price_setting_id()) + "," +
                        parseStringToSql(entity.getPrice_setting_type()) + "," +
                        parseStringToSql(entity.getPrice_data_type()) + "," +
                        parseDoubleToSql(entity.getPrice_setting_value()) + "," +
                        parseIntegerToSql(entity.getIs_auto()) + "," +
                        parseIntegerToSql(entity.getMinimum_purchase()) + "," +
                        parseLongToSql(entity.getPrice_original()) + "," +
                        parseLongToSql(entity.getPrice_new()) +
                        ")"
        );
    }

    public int insertProductPriceSetting(
            ProductPriceSettingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_setting(" +
                        "name" + "," +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "agency_id" + "," +
                        "city_id" + "," +
                        "region_id" + "," +
                        "membership_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "price_object_type" + "," +
                        "price_object_id" +
                        ") VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getCity_id()) + "," +
                        parseIntegerToSql(entity.getRegion_id()) + "," +
                        parseIntegerToSql(entity.getMembership_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseStringToSql(entity.getPrice_object_type()) + "," +
                        parseIntegerToSql(entity.getPrice_object_id()) +
                        ")"
        );
    }

    public boolean updateProductPriceSetting(
            ProductPriceSettingEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_setting SET" +
                        " name = " + parseStringToSql(entity.getName()) + "," +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        " city_id = " + parseIntegerToSql(entity.getCity_id()) + "," +
                        " region_id = " + parseIntegerToSql(entity.getRegion_id()) + "," +
                        " membership_id = " + parseIntegerToSql(entity.getMembership_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " price_object_type = " + parseStringToSql(entity.getPrice_object_type()) + "," +
                        " price_object_id = " + parseIntegerToSql(entity.getPrice_object_id()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public ProductPriceSettingEntity getProductPriceSettingEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_setting WHERE id = " + id
        );
        if (rs != null) {
            return ProductPriceSettingEntity.from(rs);
        }
        return null;
    }

    public ProductPriceSettingDetailEntity getProductPriceSettingDetailEntity(Integer id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_setting_detail WHERE id = " + id
        );
        if (rs != null) {
            return ProductPriceSettingDetailEntity.from(rs);
        }
        return null;
    }

    public JSONObject getProductPriceSettingDetail(Integer id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting_detail WHERE id = " + id
        );
    }

    public boolean updateStartDateProductPriceSetting(int id) {
        return this.masterDB.update(
                "UPDATE product_price_setting_detail" +
                        " SET start_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListProductPriceSettingDetailNotPending(
            Integer id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM product_price_setting_detail" +
                        " WHERE product_price_setting_id = " + id +
                        " AND status != " + SettingStatus.PENDING.getId()
        );
    }

    public JSONObject getProductPriceByObjectAndProduct(
            String price_object_type,
            int price_object_id,
            int product_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM product_price_setting_detail t" +
                        " LEFT JOIN product_price_setting t1 ON t1.id = t.product_price_setting_id" +
                        " WHERE t.product_id = " + product_id +
                        " AND t1.price_object_type = '" + price_object_type + "'" +
                        " AND t1.price_object_id = " + price_object_id +
                        " AND t1.status = " + SettingStatus.RUNNING.getId() + " AND t1.start_date <= NOW() AND (t1.end_date is NULL OR t1.end_date >= NOW())" +
                        " AND t.status = " + SettingStatus.RUNNING.getId() + " AND (t1.start_date is NULL OR t1.start_date <= NOW()) AND (t1.end_date is NULL OR t1.end_date >= NOW())" +
                        " LIMIT 1"
        );
    }

    public JSONObject getProductPriceSettingDetailByProduct(
            int product_id,
            int setting_id
    ) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting_detail" +
                        " WHERE product_id = " + product_id +
                        " AND product_price_setting_id = " + setting_id
        );
    }

    public List<JSONObject> getListProductPriceSettingNeedStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting" +
                        " WHERE status = " + SettingStatus.ACTIVE.getId() +
                        " AND NOW() >= start_date" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public List<JSONObject> getListProductPriceSettingNeedStop(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting" +
                        " WHERE status = " + SettingStatus.RUNNING.getId() +
                        " AND end_date is not null AND NOW() >= end_date" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public JSONObject getProductPriceSettingInfoByAgencyId(int agency_id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting WHERE agency_id = " + agency_id
        );
    }

    public ProductPriceTimerEntity getProductPriceTimerEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_timer WHERE id = " + id
        );
        if (rs != null) {
            return ProductPriceTimerEntity.from(rs);
        }
        return null;
    }

    public JSONObject getProductPriceTimerInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_timer WHERE id = " + id
        );
    }

    public int insertProductPriceTimer(ProductPriceTimerEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_timer(" +
                        "name" + "," +
                        "note" + "," +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "status" + "," +
                        "start_date" + "" +
                        ")" +
                        " VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "" +
                        ")"
        );
    }

    public boolean updateProductPriceTimer(ProductPriceTimerEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_timer SET " +
                        "name = " + parseStringToSql(entity.getName()) + "," +
                        "note = " + parseStringToSql(entity.getNote()) + "," +
                        "creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        "created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        "modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        "modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        "status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        "start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        "confirmer_id = " + parseIntegerToSql(entity.getConfirmer_id()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public boolean updateProductPriceTimerDetail(ProductPriceTimerDetailEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_timer_detail SET " +
                        "product_id = " + parseIntegerToSql(entity.getProduct_id()) + "," +
                        "price = " + parseLongToSql(entity.getPrice()) + "," +
                        "product_price_timer_id = " + parseIntegerToSql(entity.getProduct_price_timer_id()) + "," +
                        "note = " + parseStringToSql(entity.getNote()) + "" +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertProductPriceTimerDetail(ProductPriceTimerDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_timer_detail(" +
                        "product_id" + "," +
                        "price" + "," +
                        "product_price_timer_id" + "," +
                        "note" + "" +
                        ")" +
                        " VALUES(" +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseLongToSql(entity.getPrice()) + "," +
                        parseIntegerToSql(entity.getProduct_price_timer_id()) + "," +
                        parseStringToSql(entity.getNote()) + "" +
                        ")"
        );
    }

    public boolean deleteProductPriceTimerDetail(int id) {
        return this.masterDB.update(
                "DELETE FROM product_price_timer_detail WHERE product_price_timer_id = " + id
        );
    }

    public boolean cancelProductPriceTimer(int id,
                                           int staff_id) {
        return this.masterDB.update(
                "UPDATE product_price_timer SET " +
                        "status = " + ProductPriceTimerStatus.CANCEL.getId() + "," +
                        "modifier_id = " + staff_id + "," +
                        "modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public ProductPriceTimerEntity getOneProductPriceTimerWaiting() {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_timer" +
                        " WHERE start_date <= NOW()" +
                        " AND status = " + ProductPriceTimerStatus.WAITING.getId() +
                        " ORDER BY id ASC" +
                        " LIMIT 1"
        );
        if (rs != null) {
            return ProductPriceTimerEntity.from(rs);
        }
        return null;
    }

    public List<JSONObject> getListProductPriceTimerDetail(Integer id) {
        return this.masterDB.find(
                "SELECT * FROM product_price_timer_detail WHERE product_price_timer_id = " + id
        );
    }

    public boolean activeProductPriceTimer(Integer id) {
        return this.masterDB.update(
                "UPDATE product_price_timer SET " +
                        "status = " + ProductPriceTimerStatus.RUNNING.getId() + "," +
                        "modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }


    public boolean syncPriceSettingFailed(int id,
                                          String message) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE product_price_setting SET sync_status = 2," +
                    " sync_note = ?" +
                    " WHERE id = " + id;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, "Đồng bộ thất bại: " + message);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public boolean syncPriceSettingSuccess(int id) {
        return this.masterDB.update(
                "UPDATE product_price_setting SET sync_status = 1, sync_note = ''" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListSettingDetailRunning(int product_price_setting_id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM product_price_setting_detail" +
                        " WHERE product_price_setting_id = " + product_price_setting_id +
                        " AND status = " + SettingStatus.RUNNING.getId() +
                        " AND end_date is not null AND NOW() > end_date"
        );
    }

    public JSONObject getProductPriceSetting(Integer id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting WHERE id = " + id);
    }

    public List<JSONObject> getListPriceSettingDetailOverEndDate() {
        return this.masterDB.find(
                "SELECT product_price_setting_id as id" +
                        " FROM product_price_setting_detail" +
                        " WHERE status = " + SettingStatus.RUNNING.getId() +
                        " AND end_date is not null AND NOW() >= end_date" +
                        " GROUP BY product_price_setting_id"
        );
    }

    public boolean stopPriceSettingDetailOverEndDate() {
        return this.masterDB.update(
                "UPDATE product_price_setting_detail SET status = " + SettingStatus.PENDING.getId() +
                        " WHERE status = " + SettingStatus.RUNNING.getId() +
                        " AND end_date is not null AND NOW() >= end_date"
        );
    }

    public List<JSONObject> getListProductPriceSettingDetailNeedStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting_detail" +
                        " WHERE status = " + SettingStatus.ACTIVE.getId() +
                        " AND NOW() >= start_date" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public boolean startPriceSettingDetail(int id) {
        return this.masterDB.update(
                "UPDATE product_price_setting_detail SET status = " + SettingStatus.RUNNING.getId() +
                        " WHERE id = " + id
        );
    }

    public JSONObject getProductPriceSettingRunning(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting " +
                        " WHERE id = " + id
        );
    }

    public boolean syncPriceTimerFailed(int id,
                                        String message) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE product_price_timer SET sync_status = 2," +
                    " sync_note = ?" +
                    " WHERE id = " + id;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, "Đồng bộ thất bại: " + message);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public boolean syncPriceTimerSuccess(int id) {
        return this.masterDB.update(
                "UPDATE product_price_timer SET sync_status = 1, sync_note = ''" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getAllSettingDetail(int product_price_setting_id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM product_price_setting_detail" +
                        " WHERE product_price_setting_id = " + product_price_setting_id
        );
    }

    public List<JSONObject> getListProductPriceSettingByProductId(String strProductIdList) {
        return this.masterDB.find(
                "SELECT product_price_setting_detail.product_price_setting_id as id" +
                        " FROM product_price_setting_detail" +
                        " WHERE '" + strProductIdList + "' LIKE CONCAT('%\"',product_price_setting_detail.product_id,'\"%')" +
                        " AND status = " + SettingStatus.RUNNING.getId() +
                        " AND (price_setting_type = 'INCREASE' OR price_setting_type = 'DECREASE')" +
                        " GROUP BY product_price_setting_detail.product_price_setting_id"
        );
    }

    public int insertProductPriceSettingTimerDetail(
            int product_id,
            int product_price_setting_timer_id,
            String data,
            Date start_date,
            Date end_date,
            int status,
            String note) {
        return this.masterDB.insert(
                "INSERT INTO product_price_setting_timer_detail(" +
                        "product_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "product_price_setting_timer_id" + "," +
                        "data" + "," +
                        "note" +
                        ") VALUES(" +
                        parseIntegerToSql(product_id) + "," +
                        parseIntegerToSql(status) + "," +
                        parseDateToSql(start_date) + "," +
                        parseDateToSql(end_date) + "," +
                        parseIntegerToSql(product_price_setting_timer_id) + "," +
                        parseStringToSql(data) + "," +
                        parseStringToSql(note) +
                        ")"
        );
    }

    public JSONObject getProductPriceSettingTimer(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting_timer WHERE id = " + id
        );
    }

    public int insertProductPriceSettingTimer(
            ProductPriceSettingTimerEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_price_setting_timer(" +
                        "name" + "," +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "agency_id" + "," +
                        "city_id" + "," +
                        "region_id" + "," +
                        "membership_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "price_object_type" + "," +
                        "price_object_id" + "," +
                        "note" +
                        ") VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getCity_id()) + "," +
                        parseIntegerToSql(entity.getRegion_id()) + "," +
                        parseIntegerToSql(entity.getMembership_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseStringToSql(entity.getPrice_object_type()) + "," +
                        parseIntegerToSql(entity.getPrice_object_id()) + "," +
                        parseStringToSql(entity.getNote()) +
                        ")"
        );
    }

    public boolean updateProductPriceSettingTimer(
            ProductPriceSettingTimerEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_setting_timer SET" +
                        " name = " + parseStringToSql(entity.getName()) + "," +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        " city_id = " + parseIntegerToSql(entity.getCity_id()) + "," +
                        " region_id = " + parseIntegerToSql(entity.getRegion_id()) + "," +
                        " membership_id = " + parseIntegerToSql(entity.getMembership_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " price_object_type = " + parseStringToSql(entity.getPrice_object_type()) + "," +
                        " price_object_id = " + parseIntegerToSql(entity.getPrice_object_id()) + "," +
                        " confirmer_id = " + parseIntegerToSql(entity.getConfirmer_id()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public List<JSONObject> getListProductPriceSettingTimerDetail(Integer product_price_setting_timer_id) {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting_timer_detail" +
                        " WHERE product_price_setting_timer_id = " + product_price_setting_timer_id
        );
    }

    public ProductPriceSettingDetailEntity getProductPriceSettingDetailEntityByProductIdAndSettingId(Integer product_id, int product_price_setting_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_setting_detail" +
                        " WHERE product_price_setting_id = " + product_price_setting_id +
                        " AND product_id = " + product_id
        );
        if (rs != null) {
            return ProductPriceSettingDetailEntity.from(rs);
        }
        return null;
    }

    public ProductPriceSettingEntity getProductPriceSettingEntityByAgency(Integer agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_price_setting" +
                        " WHERE agency_id = " + agency_id
        );
        if (rs != null) {
            return ProductPriceSettingEntity.from(rs);
        }
        return null;
    }

    public boolean clearProductPriceSettingTimerDetail(int product_price_setting_timer_id) {
        return this.masterDB.update(
                "DELETE FROM product_price_setting_timer_detail" +
                        " WHERE product_price_setting_timer_id = " + product_price_setting_timer_id
        );
    }

    public List<JSONObject> getListProductPriceSettingTimerNeedStart() {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting_timer" +
                        " WHERE status = " + ProductPriceTimerStatus.WAITING.getId() +
                        " AND NOW() >= start_date"
        );
    }

    public boolean cancelProductPriceSettingTimer(int id,
                                                  int staff_id) {
        return this.masterDB.update(
                "UPDATE product_price_setting_timer SET " +
                        "status = " + ProductPriceTimerStatus.CANCEL.getId() + "," +
                        "modifier_id = " + staff_id + "," +
                        "modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getAllProductPriceSettingDetail(
            int product_price_setting_id) {
        return this.masterDB.find(
                "SELECT * FROM product_price_setting_detail" +
                        " WHERE product_price_setting_id = " + product_price_setting_id
        );
    }

    public boolean updateProductPriceSettingTimerDetail(
            ProductPriceSettingTimerDetailEntity entity) {
        return this.masterDB.update(
                "UPDATE product_price_setting_timer_detail SET" +
                        " product_id = " + parseIntegerToSql(entity.getProduct_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " product_price_setting_timer_id = " + parseIntegerToSql(entity.getProduct_price_setting_timer_id()) + "," +
                        " data = " + parseStringToSql(entity.getData()) + "," +
                        " note = " + parseStringToSql(entity.getNote()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public boolean deactiveProductPriceSettingDetail(int id) {
        return this.masterDB.update(
                "UPDATE product_price_setting_detail SET" +
                        " status = " + SettingStatus.PENDING.getId() + "," +
                        " end_date = NOW()" +
                        " WHERE id = " + id
        );
    }
}