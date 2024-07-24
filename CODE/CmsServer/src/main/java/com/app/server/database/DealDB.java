package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.enums.DealPriceStatus;
import com.app.server.enums.SourceOrderType;
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
public class DealDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }

    public JSONObject getDealPriceSetting() {
        return this.masterDB.getOne(
                "SELECT * FROM deal_apply_object WHERE id = 1"
        );
    }

    public boolean updateDealPriceSetting(
            String agency_include,
            String agency_ignore,
            String filters) {
        return this.masterDB.update(
                "UPDATE deal_apply_object SET " +
                        " agency_include = " + parseStringToSql(agency_include) + "," +
                        " agency_ignore = " + parseStringToSql(agency_ignore) + "," +
                        " filters = " + parseStringToSql(filters) + "," +
                        " modified_date = NOW()" +
                        " WHERE id = 1"
        );
    }

    public List<JSONObject> getListRound(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_deal_price_round" +
                        " WHERE agency_deal_price_id = " + id +
                        " ORDER BY round ASC"
        );
    }

    public JSONObject getDealPrice(int id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_deal_price" +
                        " WHERE id = " + id
        );
    }

    public boolean saveResponseDeal(
            int agency_deal_price_id,
            int round,
            long product_price,
            int product_total_quantity,
            int deposit_percent,
            int payment_duration,
            int complete_payment_duration,
            Date request_delivery_date,
            String note) {
        return this.masterDB.update(
                "UPDATE agency_deal_price_round SET" +
                        " product_price_cms = " + parseLongToSql(product_price) + "," +
                        " product_total_quantity_cms = " + parseIntegerToSql(product_total_quantity) + "," +
                        " product_price_cms = " + parseLongToSql(product_price) + "," +
                        " deposit_percent_cms = " + parseIntegerToSql(deposit_percent) + "," +
                        " payment_duration_cms = " + parseIntegerToSql(payment_duration) + "," +
                        " complete_payment_duration_cms = " + parseIntegerToSql(complete_payment_duration) + "," +
                        " request_delivery_date_cms = " + parseDateToSql(request_delivery_date) + "," +
                        " note_cms = " + parseStringToSql(note) + "," +
                        " updated_date_cms = NOW()" +
                        " WHERE agency_deal_price_id = " + agency_deal_price_id +
                        " AND round = " + round
        );
    }

    public boolean updateDealPrice(
            int id,
            int status,
            long product_price_end,
            int product_total_quantity,
            int deposit_percent,
            int payment_duration,
            int complete_payment_duration,
            long total_end_price,
            long deposit_money,
            long remain_payment_money,
            Date request_delivery_date,
            Date confirmed_date,
            Date payment_date,
            Date complete_payment_date,
            String note) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET" +
                        " status = " + parseIntegerToSql(status) + "," +
                        " product_price_end = " + parseLongToSql(product_price_end) + "," +
                        " product_total_quantity = " + parseIntegerToSql(product_total_quantity) + "," +
                        " deposit_percent = " + parseIntegerToSql(deposit_percent) + "," +
                        " payment_duration = " + parseIntegerToSql(payment_duration) + "," +
                        " complete_payment_duration = " + parseIntegerToSql(complete_payment_duration) + "," +
                        " total_end_price = " + parseLongToSql(total_end_price) + "," +
                        " deposit_money = " + parseLongToSql(deposit_money) + "," +
                        " remain_payment_money = " + parseLongToSql(remain_payment_money) + "," +
                        " request_delivery_date = " + parseDateToSql(request_delivery_date) + "," +
                        " confirmed_date = " + parseDateToSql(confirmed_date) + "," +
                        " payment_date = " + parseDateToSql(payment_date) + "," +
                        " complete_payment_date = " + parseDateToSql(complete_payment_date) + "," +
                        " update_status_date = NOW()" + "," +
                        " note = " + parseStringToSql(note) +
                        " WHERE id = " + id
        );
    }

    public JSONObject getDealPriceRoundDetail(int agency_deal_price_id, int round) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_deal_price_round" +
                        " WHERE agency_deal_price_id = " + agency_deal_price_id +
                        " AND round = " + round
        );
    }

    public boolean confirmDealPrice(
            int id,
            int status,
            long product_price_end,
            int product_total_quantity,
            int deposit_percent,
            int payment_duration,
            int complete_payment_duration,
            long total_end_price,
            long deposit_money,
            long remain_payment_money,
            Date request_delivery_date,
            Date confirmed_date,
            Date payment_date,
            Date complete_payment_date) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET" +
                        " status = " + parseIntegerToSql(status) + "," +
                        " product_price_end = " + parseLongToSql(product_price_end) + "," +
                        " product_total_quantity = " + parseIntegerToSql(product_total_quantity) + "," +
                        " deposit_percent = " + parseIntegerToSql(deposit_percent) + "," +
                        " payment_duration = " + parseIntegerToSql(payment_duration) + "," +
                        " complete_payment_duration = " + parseIntegerToSql(complete_payment_duration) + "," +
                        " total_end_price = " + parseLongToSql(total_end_price) + "," +
                        " deposit_money = " + parseLongToSql(deposit_money) + "," +
                        " remain_payment_money = " + parseLongToSql(remain_payment_money) + "," +
                        " request_delivery_date = " + parseDateToSql(request_delivery_date) + "," +
                        " confirmed_date = " + parseDateToSql(confirmed_date) + "," +
                        " payment_date = " + parseDateToSql(payment_date) + "," +
                        " complete_payment_date = " + parseDateToSql(complete_payment_date) + "," +
                        " confirmed_by = " + SourceOrderType.CMS.getValue() + "," +
                        " update_status_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean cancelDealPrice(int id, String note, int modifier_id) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET" +
                        " status = " + DealPriceStatus.CANCEL.getId() + "," +
                        " note_cancel = " + parseStringToSql(note) + "," +
                        " modifier_id = " + modifier_id + "," +
                        " update_status_date = NOW()" + "," +
                        " modified_date = NOW()" + "," +
                        " confirmed_by = " + SourceOrderType.CMS.getValue() +
                        " WHERE id = " + id
        );
    }

    public boolean hideDealPrice(int id) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET hide = 1" +
                        " WHERE id = " + id
        );
    }

    public boolean updatePromo(int id, String promo_product_info, String promo_product_info_ctkm) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE agency_deal_price SET " +
                    "promo_product_info" + " = " + "?" + "," +
                    "promo_product_info_ctkm" + " = " + "?" + "," +
                    "update_status_date" + " = " + "NOW()" +
                    " WHERE id = " + id;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, promo_product_info);
                stmt.setString(2, promo_product_info_ctkm);
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

    public boolean saveProductInfoToDealPrice(
            int id,
            long product_price,
            String product_full_name,
            String product_images,
            int product_small_unit_id,
            String product_code) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET " +
                        " product_price = " + parseLongToSql(product_price) + "," +
                        " product_full_name = " + parseStringToSql(product_full_name) + "," +
                        " product_images = " + parseStringToSql(product_images) + "," +
                        " product_small_unit_id = " + parseIntegerToSql(product_small_unit_id) + "," +
                        " product_code = " + parseStringToSql(product_code) +
                        " WHERE id = " + id
        );
    }

    public boolean saveDealProductNew(int id, String product_full_name, String product_description, String product_images) {
        return this.masterDB.update(
                "UPDATE agency_deal_price SET " +
                        " product_full_name = " + parseStringToSql(product_full_name) + "," +
                        " product_description = " + parseStringToSql(product_description) + "," +
                        " product_images = " + parseStringToSql(product_images) +
                        " WHERE id = " + id
        );
    }
}