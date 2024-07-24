package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.mission.TransactionInfo;
import com.app.server.data.entity.*;
import com.app.server.database.sql.OrderSQL;
import com.app.server.enums.*;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private OrderSQL orderSQL;

    @Autowired
    public void setOrderSQL(OrderSQL orderSQL) {
        this.orderSQL = orderSQL;
    }


    public List<JSONObject> filterPurchaseOrder(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalPurchaseOrder(String query) {
        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> getListProductInOrder(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_detail" +
                        " WHERE agency_order_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListProductInOrderConfirm(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_confirm_product" +
                        " WHERE agency_order_confirm_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListProductInPOM(int id) {
        return this.masterDB.find(
                "SELECT * FROM pom_product" +
                        " WHERE agency_order_confirm_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListGoodsInOrder(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_promo_detail" +
                        " WHERE agency_order_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public JSONObject getAgencyOrder(int id) {
        String sql = this.orderSQL.getAgencyOrder(id);
        return this.masterDB.getOne(sql);
    }

    public JSONObject getAgencyOrderConfirm(int id) {
        return this.masterDB.getOne("SELECT * FROM agency_order_confirm WHERE id = " + id);
    }

    public AgencyOrderEntity getAgencyOrderEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM agency_order WHERE id = " + id);
        if (rs != null) {
            return AgencyOrderEntity.from(rs);
        }

        return null;
    }

    public boolean updateAgencyOrderStatus(
            int agency_order_id,
            int agency_order_status,
            String note_internal,
            int modifier_id) {
        String sql = this.orderSQL.updateAgencyOrderStatus(agency_order_id, agency_order_status, note_internal, modifier_id);
        return this.masterDB.update(sql);
    }

    public boolean confirmShippingOrder(
            int id,
            int status,
            String note_internal,
            int modifier_id) {
        String sql = "UPDATE agency_order "
                + " SET status = " + status
                + " , update_status_date = NOW() "
                + " , confirm_shipping_date = NOW() "
                + " , modifier_id=" + modifier_id +
                " WHERE id=" + id;
        return this.masterDB.update(sql);
    }

    public boolean confirmShippingOC(
            int id,
            int status) {
        String sql = "UPDATE agency_order_confirm "
                + " SET status = " + status
                + " , update_status_date = NOW() " +
                " WHERE id=" + id;
        return this.masterDB.update(sql);
    }

    public boolean confirmPrepareOrder(
            int agency_order_id,
            String note_internal,
            int modifier_id) {
        return this.masterDB.update(
                "UPDATE agency_order "
                        + " SET status = " + OrderStatus.PREPARE.getKey()
                        + " , update_status_date = NOW() "
                        + " , confirm_prepare_date = NOW() "
                        + " , locked = 1 "
                        + " , modifier_id=" + modifier_id +
                        " WHERE id=" + agency_order_id
        );
    }

    public boolean cancelAgencyOrder(int agency_order_id, int agency_order_status, String note, int modifier_id) {
        String sql = this.orderSQL.cancelAgencyOrder(agency_order_id, agency_order_status, note, modifier_id);
        return this.masterDB.update(sql);
    }

    public int saveUpdateOrderStatusHistory(int agency_order_id, int agency_order_status, String note_internal, int modifier_id) {
        String sql = this.orderSQL.saveUpdateOrderStatusHistory(agency_order_id, agency_order_status, note_internal, modifier_id);
        return this.masterDB.insert(sql);
    }

    public int insertAgencyOrder(AgencyOrderEntity entity) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO agency_order(" +
                    "code," +
                    "agency_id," +
                    "agency_account_id," +
                    "membership_id," +
                    "address_delivery," +
                    "address_billing," +
                    "request_delivery_date," +
                    "note," +
                    "total_begin_price," +
                    "total_promotion_price," +
                    "total_end_price," +
                    "total_product_quantity," +
                    "source," +
                    "type," +
                    "status," +
                    "confirm_delivery_date," +
                    "created_date," +
                    "update_status_date," +
                    "creator_id," +
                    "modifier_id," +
                    "note_internal," +
                    "note_cancel," +
                    "agency_info," +
                    "total_promotion_product_price," +
                    "total_promotion_order_price," +
                    "total_promotion_order_price_ctkm," +
                    "total_sansale_promotion_price," +
                    "total_refund_price," +
                    "promo_order_info," +
                    "promo_order_info_ctkm," +
                    "promo_product_info," +
                    "promo_product_info_ctkm," +
                    "promo_good_offer_info," +
                    "promo_good_offer_info_ctkm," +
                    "promo_all_id_info," +
                    "stuck_type," +
                    "stuck_info," +
                    "increase_dept," +
                    "nqh_order," +
                    "hmkd_over_order," +
                    "total," +
                    "total_goods_quantity," +
                    "total_bonus_goods_quantity," +
                    "total_bonus_gift_quantity," +
                    "total_dm_price," +
                    "dm_product_info," +
                    "total_voucher_price" + "," +
                    "voucher_info)" +
                    " VALUES(" +
                    parseStringToSql(entity.getCode()) + "," +
                    parseIntegerToSql(entity.getAgency_id()) + "," +
                    parseIntegerToSql(entity.getAgency_account_id()) + "," +
                    parseIntegerToSql(entity.getMembership_id()) + "," +
                    parseStringToSql(entity.getAddress_delivery()) + "," +
                    parseStringToSql(entity.getAddress_billing()) + "," +
                    parseDateToSql(entity.getRequest_delivery_date()) + "," +
                    parseStringToSql(entity.getNote()) + "," +
                    parseDoubleToSql(entity.getTotal_begin_price()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_price()) + "," +
                    parseDoubleToSql(entity.getTotal_end_price()) + "," +
                    parseLongToSql(entity.getTotal_product_quantity()) + "," +
                    parseIntegerToSql(entity.getSource()) + "," +
                    parseIntegerToSql(entity.getType()) + "," +
                    parseIntegerToSql(entity.getStatus()) + "," +
                    parseDateToSql(entity.getConfirm_delivery_date()) + "," +
                    parseDateToSql(entity.getCreated_date()) + "," +
                    parseDateToSql(entity.getUpdate_status_date()) + "," +
                    parseIntegerToSql(entity.getCreator_id()) + "," +
                    parseIntegerToSql(entity.getModifier_id()) + "," +
                    parseStringToSql(entity.getNote_internal()) + "," +
                    parseStringToSql(entity.getNote_cancel()) + "," +
                    parseStringToSql(entity.getAgency_info()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_product_price()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_order_price()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_order_price_ctkm()) + "," +
                    parseDoubleToSql(entity.getTotal_sansale_promotion_price()) + "," +
                    parseDoubleToSql(entity.getTotal_refund_price()) + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    parseStringToSql(entity.getPromo_all_id_info()) + "," +
                    parseIntegerToSql(entity.getStuck_type()) + "," +
                    parseStringToSql(entity.getStuck_info()) + "," +
                    parseIntegerToSql(entity.getIncrease_dept()) + "," +
                    parseDoubleToSql(entity.getNqh_order()) + "," +
                    parseDoubleToSql(entity.getHmkd_over_order()) + "," +
                    parseIntegerToSql(entity.getTotal()) + "," +
                    parseIntegerToSql(entity.getTotal_goods_quantity()) + "," +
                    parseIntegerToSql(entity.getTotal_bonus_goods_quantity()) + "," +
                    parseIntegerToSql(entity.getTotal_bonus_gift_quantity()) + "," +
                    parseDoubleToSql(entity.getTotal_dm_price()) + "," +
                    "?" + "," +
                    parseDoubleToSql(entity.getTotal_voucher_price()) + "," +
                    parseStringToSql(entity.getVoucher_info()) +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getPromo_order_info());
                stmt.setString(2, entity.getPromo_order_info_ctkm());
                stmt.setString(3, entity.getPromo_product_info());
                stmt.setString(4, entity.getPromo_product_info_ctkm());
                stmt.setString(5, entity.getPromo_good_offer_info());
                stmt.setString(6, entity.getPromo_good_offer_info_ctkm());
                stmt.setString(7, entity.getDm_product_info());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
//        return this.masterDB.insert(
//                "INSERT INTO agency_order(" +
//                        "code," +
//                        "agency_id," +
//                        "agency_account_id," +
//                        "membership_id," +
//                        "address_delivery," +
//                        "address_billing," +
//                        "request_delivery_date," +
//                        "note," +
//                        "total_begin_price," +
//                        "total_promotion_price," +
//                        "total_end_price," +
//                        "total_product_quantity," +
//                        "source," +
//                        "type," +
//                        "status," +
//                        "confirm_delivery_date," +
//                        "created_date," +
//                        "update_status_date," +
//                        "creator_id," +
//                        "modifier_id," +
//                        "note_internal," +
//                        "note_cancel," +
//                        "agency_info," +
//                        "total_promotion_product_price," +
//                        "total_promotion_order_price," +
//                        "total_promotion_order_price_ctkm," +
//                        "total_refund_price," +
//                        "promo_order_info," +
//                        "promo_order_info_ctkm," +
//                        "promo_product_info," +
//                        "promo_product_info_ctkm," +
//                        "promo_good_offer_info," +
//                        "promo_good_offer_info_ctkm," +
//                        "promo_all_id_info," +
//                        "stuck_type," +
//                        "stuck_info," +
//                        "increase_dept," +
//                        "nqh_order," +
//                        "hmkd_over_order)" +
//                        " VALUES(" +
//                        parseStringToSql(entity.getCode()) + "," +
//                        parseIntegerToSql(entity.getAgency_id()) + "," +
//                        parseIntegerToSql(entity.getAgency_account_id()) + "," +
//                        parseIntegerToSql(entity.getMembership_id()) + "," +
//                        parseStringToSql(entity.getAddress_delivery()) + "," +
//                        parseStringToSql(entity.getAddress_billing()) + "," +
//                        parseDateToSql(entity.getRequest_delivery_date()) + "," +
//                        parseStringToSql(entity.getNote()) + "," +
//                        parseLongToSql(entity.getTotal_begin_price()) + "," +
//                        parseLongToSql(entity.getTotal_promotion_price()) + "," +
//                        parseLongToSql(entity.getTotal_end_price()) + "," +
//                        parseLongToSql(entity.getTotal_product_quantity()) + "," +
//                        parseIntegerToSql(entity.getSource()) + "," +
//                        parseIntegerToSql(entity.getType()) + "," +
//                        parseIntegerToSql(entity.getStatus()) + "," +
//                        parseDateToSql(entity.getConfirm_delivery_date()) + "," +
//                        parseDateToSql(entity.getCreated_date()) + "," +
//                        parseDateToSql(entity.getUpdate_status_date()) + "," +
//                        parseIntegerToSql(entity.getCreator_id()) + "," +
//                        parseIntegerToSql(entity.getModifier_id()) + "," +
//                        parseStringToSql(entity.getNote_internal()) + "," +
//                        parseStringToSql(entity.getNote_cancel()) + "," +
//                        parseStringToSql(entity.getAgency_info()) + "," +
//                        parseLongToSql(entity.getTotal_promotion_product_price()) + "," +
//                        parseLongToSql(entity.getTotal_promotion_order_price()) + "," +
//                        parseLongToSql(entity.getTotal_promotion_order_price_ctkm()) + "," +
//                        parseLongToSql(entity.getTotal_refund_price()) + "," +
//                        parseStringToSql(entity.getPromo_order_info()) + "," +
//                        parseStringToSql(entity.getPromo_order_info_ctkm()) + "," +
//                        parseStringToSql(entity.getPromo_product_info()) + "," +
//                        parseStringToSql(entity.getPromo_product_info_ctkm()) + "," +
//                        parseStringToSql(entity.getPromo_good_offer_info()) + "," +
//                        parseStringToSql(entity.getPromo_good_offer_info_ctkm()) + "," +
//                        parseStringToSql(entity.getPromo_all_id_info()) + "," +
//                        parseIntegerToSql(entity.getStuck_type()) + "," +
//                        parseStringToSql(entity.getStuck_info()) + "," +
//                        parseIntegerToSql(entity.getIncrease_dept()) + "," +
//                        parseLongToSql(entity.getNqh_order()) + "," +
//                        parseLongToSql(entity.getHmkd_over_order()) + "" +
//                        ")"
//        );
    }

    public int countOrderByDate(String startDate, String endDate, int agency_id) {
        String sql = this.orderSQL.countOrderByDate(startDate, endDate, agency_id);
        return this.masterDB.getTotal(sql);
    }

    public int createAgencyOrderDetail(AgencyOrderDetailEntity entity) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO agency_order_detail(" +
                            "agency_order_id," +
                            "product_id," +
                            "product_tag," +
                            "product_code," +
                            "product_full_name," +
                            "product_warranty_time," +
                            "product_images," +
                            "product_specification," +
                            "product_color_id," +
                            "product_color_name," +
                            "product_characteristic," +
                            "product_description," +
                            "product_user_manual," +
                            "product_technical_data," +
                            "product_price," +
                            "product_small_unit_id," +
                            "product_small_unit_name," +
                            "product_big_unit_id," +
                            "product_big_unit_name," +
                            "product_convert_small_unit_ratio," +
                            "product_minimum_purchase," +
                            "product_step," +
                            "product_item_type," +
                            "product_sort_data," +
                            "product_total_quantity," +
                            "product_total_begin_price," +
                            "product_total_promotion_price," +
                            "product_total_promotion_price_ctkm," +
                            "product_total_end_price," +
                            "created_date," +
                            "product_brand_id," +
                            "product_brand_name," +
                            "combo_id," +
                            "promo_id," +
                            "combo_quantity," +
                            "combo_data," +
                            "promo_description," +
                            "dm_id," +
                            "dm_percent," +
                            "product_total_dm_price" +
                            ") VALUES(" +
                            parseIntegerToSql(entity.getAgency_order_id()) + "," +//"agency_order_id," +
                            parseIntegerToSql(entity.getProduct_id()) + "," +//"product_id," +
                            parseStringToSql(entity.getProduct_tag()) + "," + //"product_tag," +
                            parseStringToSql(entity.getProduct_code()) + "," + //"product_code," +
                            parseStringToSql(entity.getProduct_full_name()) + "," + //"product_full_name," +
                            parseStringToSql(entity.getProduct_warranty_time()) + "," + //"product_warranty_time," +
                            parseStringToSql(entity.getProduct_images()) + "," + // "product_images," +
                            parseStringToSql(entity.getProduct_specification()) + "," + //"product_specification," +
                            parseIntegerToSql(entity.getProduct_color_id()) + "," + //"product_color_id," +
                            parseStringToSql(entity.getProduct_color_name()) + "," + //"product_color_name," +
                            parseStringToSql(entity.getProduct_characteristic()) + "," + //"product_characteristic," +
                            parseStringToSql(entity.getProduct_description()) + "," + //"product_description," +
                            parseStringToSql(entity.getProduct_user_manual()) + "," + //"product_user_manual," +
                            parseStringToSql(entity.getProduct_technical_data()) + "," + //"product_technical_data," +
                            parseDoubleToSql(entity.getProduct_price()) + "," + //"product_price," +
                            parseIntegerToSql(entity.getProduct_small_unit_id()) + "," + //"product_small_unit_id," +
                            parseStringToSql(entity.getProduct_small_unit_name()) + "," + //"product_small_unit_name," +
                            parseIntegerToSql(entity.getProduct_big_unit_id()) + "," + //"product_big_unit_id," +
                            parseStringToSql(entity.getProduct_big_unit_name()) + "," + //"product_big_unit_name," +
                            parseIntegerToSql(entity.getProduct_convert_small_unit_ratio()) + "," + //"product_convert_small_unit_ratio," +
                            parseIntegerToSql(entity.getProduct_minimum_purchase()) + "," + //"product_minimum_purchase," +
                            parseIntegerToSql(entity.getProduct_step()) + "," + //"product_step," +
                            parseIntegerToSql(entity.getProduct_item_type()) + "," + //"product_item_type," +
                            parseStringToSql(entity.getProduct_sort_data()) + "," + //"product_sort_data," +
                            parseIntegerToSql(entity.getProduct_total_quantity()) + "," + //"product_total_quantity," +
                            parseDoubleToSql(entity.getProduct_total_begin_price()) + "," + //"product_total_begin_price," +
                            parseDoubleToSql(entity.getProduct_total_promotion_price()) + "," + //"product_total_promotion_price," +
                            parseDoubleToSql(entity.getProduct_total_promotion_price_ctkm()) + "," + //"product_total_promotion_price_ctkm," +
                            parseDoubleToSql(entity.getProduct_total_end_price()) + "," + //"product_total_end_price," +
                            parseDateToSql(entity.getCreated_date()) + "," + //"created_date," +
                            parseIntegerToSql(entity.getProduct_brand_id()) + "," + //"product_brand_id," +
                            parseStringToSql(entity.getProduct_brand_name()) + "," + //"product_brand_name" +
                            parseIntegerToSql(entity.getCombo_id()) + "," +
                            parseIntegerToSql(entity.getPromo_id()) + "," +
                            parseIntegerToSql(entity.getCombo_quantity()) + "," +
                            "?" + "," +
                            "?" + "," +
                            parseIntegerToSql(entity.getDm_id()) + "," +
                            parseIntegerToSql(entity.getDm_percent()) + "," +
                            parseDoubleToSql(entity.getProduct_total_dm_price()) +
                            ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getCombo_data());
                stmt.setString(2, entity.getPromo_description());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public int createAgencyOrderPromoDetail(
            AgencyOrderPromoDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO agency_order_promo_detail(" +
                        "agency_order_id," +
                        "product_id," +
                        "product_tag," +
                        "product_code," +
                        "product_full_name," +
                        "product_warranty_time," +
                        "product_images," +
                        "product_specification," +
                        "product_color_id," +
                        "product_color_name," +
                        "product_characteristic," +
                        "product_description," +
                        "product_user_manual," +
                        "product_technical_data," +
                        "product_price," +
                        "product_small_unit_id," +
                        "product_small_unit_name," +
                        "product_big_unit_id," +
                        "product_big_unit_name," +
                        "product_convert_small_unit_ratio," +
                        "product_minimum_purchase," +
                        "product_step," +
                        "product_item_type," +
                        "product_sort_data," +
                        "product_total_quantity," +
                        "product_total_begin_price," +
                        "product_total_promotion_price," +
                        "product_total_end_price," +
                        "created_date," +
                        "product_brand_id," +
                        "product_brand_name," +
                        "type," +
                        "promo_id," +
                        "voucher_id" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getAgency_order_id()) + "," +//"agency_order_id," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +//"product_id," +
                        parseStringToSql(entity.getProduct_tag()) + "," + //"product_tag," +
                        parseStringToSql(entity.getProduct_code()) + "," + //"product_code," +
                        parseStringToSql(entity.getProduct_full_name()) + "," + //"product_full_name," +
                        parseStringToSql(entity.getProduct_warranty_time()) + "," + //"product_warranty_time," +
                        parseStringToSql(entity.getProduct_images()) + "," + // "product_images," +
                        parseStringToSql(entity.getProduct_specification()) + "," + //"product_specification," +
                        parseIntegerToSql(entity.getProduct_color_id()) + "," + //"product_color_id," +
                        parseStringToSql(entity.getProduct_color_name()) + "," + //"product_color_name," +
                        parseStringToSql(entity.getProduct_characteristic()) + "," + //"product_characteristic," +
                        parseStringToSql(entity.getProduct_description()) + "," + //"product_description," +
                        parseStringToSql(entity.getProduct_user_manual()) + "," + //"product_user_manual," +
                        parseStringToSql(entity.getProduct_technical_data()) + "," + //"product_technical_data," +
                        parseDoubleToSql(entity.getProduct_price()) + "," + //"product_price," +
                        parseIntegerToSql(entity.getProduct_small_unit_id()) + "," + //"product_small_unit_id," +
                        parseStringToSql(entity.getProduct_small_unit_name()) + "," + //"product_small_unit_name," +
                        parseIntegerToSql(entity.getProduct_big_unit_id()) + "," + //"product_big_unit_id," +
                        parseStringToSql(entity.getProduct_big_unit_name()) + "," + //"product_big_unit_name," +
                        parseIntegerToSql(entity.getProduct_convert_small_unit_ratio()) + "," + //"product_convert_small_unit_ratio," +
                        parseIntegerToSql(entity.getProduct_minimum_purchase()) + "," + //"product_minimum_purchase," +
                        parseIntegerToSql(entity.getProduct_step()) + "," + //"product_step," +
                        parseIntegerToSql(entity.getProduct_item_type()) + "," + //"product_item_type," +
                        parseStringToSql(entity.getProduct_sort_data()) + "," + //"product_sort_data," +
                        parseIntegerToSql(entity.getProduct_total_quantity()) + "," + //"product_total_quantity," +
                        parseDoubleToSql(entity.getProduct_total_begin_price()) + "," + //"product_total_begin_price," +
                        parseDoubleToSql(entity.getProduct_total_promotion_price()) + "," + //"product_total_promotion_price," +
                        parseDoubleToSql(entity.getProduct_total_end_price()) + "," + //"product_total_end_price," +
                        parseDateToSql(entity.getCreated_date()) + "," + //"created_date," +
                        parseIntegerToSql(entity.getProduct_brand_id()) + "," + //"product_brand_id," +
                        parseStringToSql(entity.getProduct_brand_name()) + "," + //"product_brand_name" +
                        parseIntegerToSql(entity.getType()) + "," + //"type" +
                        parseIntegerToSql(entity.getPromo_id()) + "," + //"promo_id" +
                        parseIntegerToSql(entity.getVoucher_id()) + "" + //"voucher_id" +
                        ")"
        );
    }

    public List<JSONObject> getListOrderStatusHistory(int id) {
        String sql = this.orderSQL.getListOrderStatusHistory(id);
        return this.masterDB.find(sql);
    }

    public int insertAgencyOrderHistory(AgencyOrderHistoryEntity agencyOrderHistoryEntity) {
        return 1;
    }

    public int insertAgencyOrderDetailHistory(AgencyOrderDetailHistoryEntity agencyOrderDetailHistoryEntity) {
        return 1;
    }

    public List<AgencyOrderDetailEntity> getListAgencyOrderDetail(int agency_order_id) {
        List<AgencyOrderDetailEntity> result = new ArrayList<>();
        List<JSONObject> records = this.masterDB.find(
                "SELECT * FROM agency_order_detail WHERE agency_order_id = " + agency_order_id
        );
        for (JSONObject record : records) {
            result.add(AgencyOrderDetailEntity.from(record));
        }
        return result;
    }

    public List<AgencyOrderPromoDetailEntity> getListAgencyOrderPromoDetail(int agency_order_id) {
        List<AgencyOrderPromoDetailEntity> result = new ArrayList<>();
        List<JSONObject> rs = this.masterDB.find(
                "SELECT * FROM agency_order_promo_detail WHERE agency_order_id = " + agency_order_id
        );
        for (JSONObject js : rs) {
            result.add(AgencyOrderPromoDetailEntity.from(js));
        }
        return result;
    }

    public boolean deleteAgencyOrderDetail(Integer id) {
        String sql = this.orderSQL.deleteAgencyOrderDetail(id);
        return this.masterDB.update(sql);
    }

    public boolean deleteAgencyOrderPromoDetail(Integer id) {
        return this.masterDB.update(
                "DELETE FROM agency_order_promo_detail WHERE agency_order_id = " + id);
    }

    public boolean updateAgencyOrder(AgencyOrderEntity entity) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE agency_order SET " +
                    "code" + " = " + parseStringToSql(entity.getCode()) + "," +
                    "agency_id" + " = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                    "agency_account_id" + " = " + parseIntegerToSql(entity.getAgency_account_id()) + "," +
                    "membership_id" + " = " + parseIntegerToSql(entity.getMembership_id()) + "," +
                    "address_delivery" + " = " + parseStringToSql(entity.getAddress_delivery()) + "," +
                    "address_billing" + " = " + parseStringToSql(entity.getAddress_billing()) + "," +
                    "request_delivery_date" + " = " + parseDateToSql(entity.getRequest_delivery_date()) + "," +
                    "note" + " = " + parseStringToSql(entity.getNote()) + "," +
                    "total_begin_price" + " = " + parseDoubleToSql(entity.getTotal_begin_price()) + "," +
                    "total_promotion_price" + " = " + parseDoubleToSql(entity.getTotal_promotion_price()) + "," +
                    "total_end_price" + " = " + parseDoubleToSql(entity.getTotal_end_price()) + "," +
                    "total_product_quantity" + " = " + parseLongToSql(entity.getTotal_product_quantity()) + "," +
                    "source" + " = " + parseIntegerToSql(entity.getSource()) + "," +
                    "type" + " = " + parseIntegerToSql(entity.getType()) + "," +
                    "status" + " = " + parseIntegerToSql(entity.getStatus()) + "," +
                    "confirm_delivery_date" + " = " + parseDateToSql(entity.getConfirm_delivery_date()) + "," +
                    "created_date" + " = " + parseDateToSql(entity.getCreated_date()) + "," +
                    "update_status_date" + " = " + parseDateToSql(entity.getUpdate_status_date()) + "," +
                    "creator_id" + " = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                    "modifier_id" + " = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                    "note_internal" + " = " + parseStringToSql(entity.getNote_internal()) + "," +
                    "note_cancel" + " = " + parseStringToSql(entity.getNote_cancel()) + "," +
                    "agency_info" + " = " + parseStringToSql(entity.getAgency_info()) + "," +
                    "total_promotion_product_price" + " = " + parseDoubleToSql(entity.getTotal_promotion_product_price()) + "," +
                    "total_promotion_order_price" + " = " + parseDoubleToSql(entity.getTotal_promotion_order_price()) + "," +
                    "total_promotion_order_price_ctkm" + " = " + parseDoubleToSql(entity.getTotal_promotion_order_price_ctkm()) + "," +
                    "total_sansale_promotion_price" + " = " + parseDoubleToSql(entity.getTotal_sansale_promotion_price()) + "," +
                    "total_refund_price" + " = " + parseDoubleToSql(entity.getTotal_refund_price()) + "," +
                    "total_voucher_price" + " = " + parseDoubleToSql(entity.getTotal_voucher_price()) + "," +
                    "promo_order_info" + " = " + "?" + "," +
                    "promo_order_info_ctkm" + " = " + "?" + "," +
                    "promo_product_info" + " = " + "?" + "," +
                    "promo_product_info_ctkm" + " = " + "?" + "," +
                    "promo_good_offer_info" + " = " + "?" + "," +
                    "promo_good_offer_info_ctkm" + " = " + "?" + "," +
                    "promo_all_id_info" + " = " + parseStringToSql(entity.getPromo_all_id_info()) + "," +
                    "stuck_type" + " = " + parseIntegerToSql(entity.getStuck_type()) + "," +
                    "stuck_info" + " = " + parseStringToSql(entity.getStuck_info()) + "," +
                    "increase_dept" + " = " + parseIntegerToSql(entity.getIncrease_dept()) + "," +
                    "nqh_order" + " = " + parseDoubleToSql(entity.getNqh_order()) + "," +
                    "hmkd_over_order" + " = " + parseDoubleToSql(entity.getHmkd_over_order()) + "," +
                    "confirm_prepare_date" + " = " + parseDateToSql(entity.getConfirm_prepare_date()) + "," +
                    "total" + " = " + parseIntegerToSql(entity.getTotal()) + "," +
                    "total_goods_quantity" + " = " + parseIntegerToSql(entity.getTotal_goods_quantity()) + "," +
                    "total_bonus_goods_quantity" + " = " + parseIntegerToSql(entity.getTotal_bonus_goods_quantity()) + "," +
                    "total_bonus_gift_quantity" + " = " + parseIntegerToSql(entity.getTotal_bonus_gift_quantity()) + "," +
                    "voucher_info = " + parseStringToSql(entity.getVoucher_info()) +
                    " WHERE id = " + entity.getId();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, entity.getPromo_order_info());
                stmt.setString(2, entity.getPromo_order_info_ctkm());
                stmt.setString(3, entity.getPromo_product_info());
                stmt.setString(4, entity.getPromo_product_info_ctkm());
                stmt.setString(5, entity.getPromo_good_offer_info());
                stmt.setString(6, entity.getPromo_good_offer_info_ctkm());
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
//        return this.masterDB.update(
//                "UPDATE agency_order SET " +
//                        "code" + " = " + parseStringToSql(entity.getCode()) + "," +
//                        "agency_id" + " = " + parseIntegerToSql(entity.getAgency_id()) + "," +
//                        "agency_account_id" + " = " + parseIntegerToSql(entity.getAgency_account_id()) + "," +
//                        "membership_id" + " = " + parseIntegerToSql(entity.getMembership_id()) + "," +
//                        "address_delivery" + " = " + parseStringToSql(entity.getAddress_delivery()) + "," +
//                        "address_billing" + " = " + parseStringToSql(entity.getAddress_billing()) + "," +
//                        "request_delivery_date" + " = " + parseDateToSql(entity.getRequest_delivery_date()) + "," +
//                        "note" + " = " + parseStringToSql(entity.getNote()) + "," +
//                        "total_begin_price" + " = " + parseLongToSql(entity.getTotal_begin_price()) + "," +
//                        "total_promotion_price" + " = " + parseLongToSql(entity.getTotal_promotion_price()) + "," +
//                        "total_end_price" + " = " + parseLongToSql(entity.getTotal_end_price()) + "," +
//                        "total_product_quantity" + " = " + parseLongToSql(entity.getTotal_product_quantity()) + "," +
//                        "source" + " = " + parseIntegerToSql(entity.getSource()) + "," +
//                        "type" + " = " + parseIntegerToSql(entity.getType()) + "," +
//                        "status" + " = " + parseIntegerToSql(entity.getStatus()) + "," +
//                        "confirm_delivery_date" + " = " + parseDateToSql(entity.getConfirm_delivery_date()) + "," +
//                        "created_date" + " = " + parseDateToSql(entity.getCreated_date()) + "," +
//                        "update_status_date" + " = " + parseDateToSql(entity.getUpdate_status_date()) + "," +
//                        "creator_id" + " = " + parseIntegerToSql(entity.getCreator_id()) + "," +
//                        "modifier_id" + " = " + parseIntegerToSql(entity.getModifier_id()) + "," +
//                        "note_internal" + " = " + parseStringToSql(entity.getNote_internal()) + "," +
//                        "note_cancel" + " = " + parseStringToSql(entity.getNote_cancel()) + "," +
//                        "agency_info" + " = " + parseStringToSql(entity.getAgency_info()) + "," +
//                        "total_promotion_product_price" + " = " + parseLongToSql(entity.getTotal_promotion_product_price()) + "," +
//                        "total_promotion_order_price" + " = " + parseLongToSql(entity.getTotal_promotion_order_price()) + "," +
//                        "total_promotion_order_price_ctkm" + " = " + parseLongToSql(entity.getTotal_promotion_order_price_ctkm()) + "," +
//                        "total_refund_price" + " = " + parseLongToSql(entity.getTotal_refund_price()) + "," +
//                        "promo_order_info" + " = " + parseStringToSql(entity.getPromo_order_info()) + "," +
//                        "promo_order_info_ctkm" + " = " + parseStringToSql(entity.getPromo_order_info_ctkm()) + "," +
//                        "promo_product_info" + " = " + parseStringToSql(entity.getPromo_product_info()) + "," +
//                        "promo_product_info_ctkm" + " = " + parseStringToSql(entity.getPromo_product_info_ctkm()) + "," +
//                        "promo_good_offer_info" + " = " + parseStringToSql(entity.getPromo_good_offer_info()) + "," +
//                        "promo_good_offer_info_ctkm" + " = " + parseStringToSql(entity.getPromo_good_offer_info_ctkm()) + "," +
//                        "promo_all_id_info" + " = " + parseStringToSql(entity.getPromo_all_id_info()) + "," +
//                        "stuck_type" + " = " + parseIntegerToSql(entity.getStuck_type()) + "," +
//                        "stuck_info" + " = " + parseStringToSql(entity.getStuck_info()) + "," +
//                        "increase_dept" + " = " + parseIntegerToSql(entity.getIncrease_dept()) + "," +
//                        "nqh_order" + " = " + parseLongToSql(entity.getNqh_order()) + "," +
//                        "hmkd_over_order" + " = " + parseLongToSql(entity.getHmkd_over_order()) +
//                        " WHERE id = " + entity.getId()
//        );
    }

    public boolean deliveryAgencyOrder(int agency_order_id, int agency_order_status, String note_internal, int modifier_id) {
        String sql = this.orderSQL.deliveryAgencyOrder(agency_order_id, agency_order_status, note_internal, modifier_id);
        return this.masterDB.update(sql);
    }

    public JSONObject getLatestCompletedOrder(int agencyId, String fromDate, String endDate) {
        String sql = "SELECT id,confirm_delivery_date" +
                " FROM agency_order" +
                " WHERE agency_id = "
                + agencyId + " AND status = " + OrderStatus.COMPLETE.getKey() +
                " ORDER BY confirm_delivery_date DESC LIMIT 0,1";
        if (StringUtils.isNotBlank(fromDate) && StringUtils.isNotBlank(endDate))
            sql = "SELECT id,confirm_delivery_date" +
                    " FROM agency_order" +
                    " WHERE agency_id=" + agencyId +
                    " AND status = " + OrderStatus.COMPLETE.getKey() +
                    " AND confirm_delivery_date>=" + fromDate +
                    " AND confirm_delivery_date<=" + endDate +
                    " ORDER BY confirm_delivery_date DESC LIMIT 0,1";

        return this.masterDB.getOne(sql);
    }

    public JSONObject getOrderByOrderCode(String order_code) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order WHERE code='" + order_code + "'"
        );
    }

    /**
     * Tổng mua hàng trong ngày
     *
     * @param agency_id
     * @return
     */
    public Long getTotalPriceOrderFinishToday(Integer agency_id) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT sum(total_end_price) as total" +
                        " FROM agency_order WHERE agency_id = " + agency_id +
                        " AND (status = " + OrderStatus.COMPLETE.getKey() +
                        " AND DATE(confirm_delivery_date) = DATE(NOW())" +
                        ")"
        );

        if (rsTotal != null) {
            return ConvertUtils.toLong(rsTotal.get("total"));
        }

        return 0L;
    }

    /**
     * Danh sách đơn chờ giao
     *
     * @param product_id
     * @return
     */
    public List<JSONObject> getListOrderWaitingShip(int product_id) {
        return this.masterDB.find(
                "\n" +
                        "select product_id,agency_order_id, sum(total) as total_final,code,`status`, agency_info, total_end_price,created_date\n" +
                        "FROM \n" +
                        "(SELECT t1.agency_order_id, t1.product_id, sum(product_total_quantity) as total, t2.code, t2.`status`, t2.agency_info, t2.total_end_price, t2.created_date\n" +
                        "FROM agency_order_detail t1\n" +
                        "LEFT JOIN agency_order t2 ON t2.id = t1.agency_order_id\n" +
                        "where t1.product_id = " + product_id + " and t2.status IN (" + OrderStatus.getStatusWaitingShip() + ")\n" +
                        "GROUP BY t1.product_id,  t1.agency_order_id\n" +
                        "UNION ALL\n" +
                        "SELECT s1.agency_order_id, s1.product_id, sum(product_total_quantity) as total, s2.code, s2.`status`, s2.agency_info, s2.total_end_price, s2.created_date\n" +
                        "FROM agency_order_promo_detail s1\n" +
                        "LEFT JOIN agency_order s2 ON s2.id = s1.agency_order_id\n" +
                        "where s1.product_id = " + product_id + "  and s2.status IN (" + OrderStatus.getStatusWaitingShip() + ")\n" +
                        "GROUP BY s1.product_id, s1.agency_order_id\n" +
                        ") as a\n" +
                        "GROUP BY agency_order_id,product_id,code,`status`, agency_info, total_end_price,created_date"
        );
    }

    /**
     * Danh sách tồn kho chờ duyệt
     *
     * @param product_id
     * @return
     */
    public List<JSONObject> getListOrderWaitingApprove(int product_id) {
        return this.masterDB.find(
                "\n" +
                        "select product_id,agency_order_id, sum(total) as total_final,code,`status`, agency_info, total_end_price,created_date\n" +
                        "FROM \n" +
                        "(SELECT t1.agency_order_id, t1.product_id, sum(product_total_quantity) as total, t2.code, t2.`status`, t2.agency_info, t2.total_end_price, t2.created_date\n" +
                        "FROM agency_order_detail t1\n" +
                        "LEFT JOIN agency_order t2 ON t2.id = t1.agency_order_id\n" +
                        "where t1.product_id = " + product_id + " and t2.status IN (" + OrderStatus.getStatusWaitingApprove() + ")\n" +
                        "GROUP BY t1.product_id,  t1.agency_order_id\n" +
                        "UNION ALL\n" +
                        "SELECT s1.agency_order_id, s1.product_id, sum(product_total_quantity) as total, s2.code, s2.`status`, s2.agency_info, s2.total_end_price, s2.created_date\n" +
                        "FROM agency_order_promo_detail s1\n" +
                        "LEFT JOIN agency_order s2 ON s2.id = s1.agency_order_id\n" +
                        "where s1.product_id = " + product_id + "  and s2.status IN (" + OrderStatus.getStatusWaitingApprove() + ")\n" +
                        "GROUP BY s1.product_id, s1.agency_order_id\n" +
                        ") as a\n" +
                        "GROUP BY agency_order_id,product_id,code,`status`, agency_info, total_end_price,created_date"
        );
    }

    public long getTotalPriceOrderDoing(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(total_end_price-total_money_dept) AS total" +
                        " FROM agency_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND status IN (" + OrderStatus.getStatusDoing() + ")" +
                        " AND increase_dept = 0" +
                        " AND type != " + OrderType.CONTRACT.getValue()
        );

        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }
        return 0;
    }

    public double getTotalPriceOrderDeptDoing(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(total_end_price-total_money_dept) AS total" +
                        " FROM agency_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND status IN (" + OrderStatus.getStatusDeptDoing() + ")" +
                        " AND increase_dept = 0" +
                        " AND type != " + OrderType.CONTRACT.getValue()
        );

        if (rs != null) {
            return ConvertUtils.toDouble(rs.get("total"));
        }
        return 0;
    }

    public boolean approveCommit(int id) {
        return this.masterDB.update(
                "UPDATE agency_order SET commit_approve_status = " + CommitApproveStatus.APPROVED.getId() + "," +
                        " status = " + OrderStatus.RETURN_AGENCY.getKey() + "," +
                        " update_status_date = NOW()" +
                        " WHERE id = " + id
        );
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

    public JSONObject getAgencyOrderCommit(int agency_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_commit" +
                        " WHERE agency_id = " + agency_id +
                        " AND status != " + CommitStatus.CANCEL.getId() +
                        " ORDER BY id DESC" +
                        " LIMIT 1"
        );
    }

    public JSONObject getAgencyOrderCommitByOrder(int agency_order_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_commit" +
                        " WHERE order_id = " + agency_order_id +
                        " AND status != " + CommitStatus.CANCEL.getId() +
                        " ORDER BY id DESC" +
                        " LIMIT 1"
        );
    }

    public JSONObject getAgencyOrderTemp(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_tmp WHERE id = " + id
        );
    }

    public boolean cancelCommit(int order_id) {
        return this.masterDB.update(
                "UPDATE agency_order_commit SET status = " + CommitStatus.CANCEL.getId() +
                        " WHERE order_id = " + order_id
        );
    }

    public int countFailedCommit(int id) {
        JSONObject jsonObject = this.masterDB.getOne(
                "SELECT COUNT(id) AS count" +
                        " FROM agency_order_commit" +
                        " WHERE agency_id=" + id + " AND status=1 AND DATE(NOW())>committed_date"
        );

        return ConvertUtils.toInt(jsonObject.get("count"));
    }

    public List<JSONObject> getListOrderWaitingConfirm(int time_over, int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM agency_order" +
                        " WHERE status = " + OrderStatus.WAITING_CONFIRM.getKey() +
                        " AND stuck_type != " + StuckType.NQH_CK.getId() +
                        " AND NOW() - update_status_date > " + time_over +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public List<JSONObject> getListOrderWaitingApprove(int time_over, int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM agency_order" +
                        " WHERE status = " + OrderStatus.WAITING_APPROVE.getKey() +
                        " AND NOW() - update_status_date > " + time_over +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public JSONObject getAgencyOrderByOrderCode(String order_code) {
        return this.masterDB.getOne("SELECT * FROM agency_order" +
                " WHERE code = '" + order_code + "'" +
                " LIMIT 1");
    }

    public boolean updateAgencyOrderStuckType(
            int id,
            int stuck_type,
            String stuck_info) {
        return this.masterDB.update(
                "UPDATE agency_order set stuck_type = " + stuck_type + "," +
                        " stuck_info = '" + stuck_info + "'" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListOrderHenGiao(int status) {
        return this.masterDB.find(
                "SELECT * FROM agency_order" +
                        " WHERE status = " + status +
                        " AND request_delivery_date IS NOT NULL" +
                        " AND increase_dept = 0"
        );
    }

    public boolean setOrderIncreaseDept(int agency_order_id, int value) {
        return this.masterDB.update(
                "UPDATE agency_order SET increase_dept = " + value +
                        " WHERE id = " + agency_order_id
        );
    }

    public boolean setOrderIncreaseAcoin(int agency_order_id, long value) {
        return this.masterDB.update(
                "UPDATE agency_order SET acoin = acoin + " + value +
                        " WHERE id = " + agency_order_id
        );
    }

    public boolean setOrderDecreaseAcoin(int agency_order_id, long value) {
        return this.masterDB.update(
                "UPDATE agency_order SET acoin = acoin - " + value +
                        " WHERE id = " + agency_order_id
        );
    }

    public boolean updateDeptCycleToAgencyOrder(int id, Integer dept_cycle) {
        return this.masterDB.update(
                "UPDATE agency_order SET dept_cycle = " + dept_cycle +
                        " WHERE id = " + id
        );
    }

    public boolean updateDeptCycleToAgencyOrderDept(int agency_order_id, Integer dept_cycle, int order_data_index) {
        return this.masterDB.update(
                "UPDATE agency_order_dept SET dept_cycle = " + dept_cycle +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND order_data_index = " + order_data_index
        );
    }

    public boolean setDeptOrderNQH(Integer id) {
        return this.masterDB.update(
                "UPDATE dept_order SET is_nqh = 1," +
                        " modified_date = NOW()," +
                        " nqh_date = NOW()" +
                        " WHERE id = " + id +
                        " AND is_nqh = 0"
        );
    }

    public boolean editRequestDeliveryDate(
            int order_id,
            String request_delivery_date,
            int status,
            String note_internal,
            int modifier_id) {
        return this.masterDB.update(
                "UPDATE agency_order "
                        + " SET status = " + status
                        + " , request_delivery_date = " + parseStringToSql(request_delivery_date) + ""
                        + " , update_status_date = NOW() "
                        + " , modifier_id = " + modifier_id +
                        " WHERE id = " + order_id
        );
    }

    public boolean editPlanDeliveryDate(
            int order_id,
            String plan_delivery_date,
            int status,
            String note_internal,
            int modifier_id) {
        return this.masterDB.update(
                "UPDATE agency_order_confirm "
                        + " SET status = " + status
                        + " , plan_delivery_date = " + parseStringToSql(plan_delivery_date) + ""
                        + " , update_status_date = NOW() "
                        + " , modifier_id = " + modifier_id +
                        " WHERE id = " + order_id
        );
    }

    public boolean lockTimeOrder(int id) {
        return this.masterDB.update(
                "UPDATE agency_order "
                        + " SET locked = 1" +
                        " , modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean setMissCommit(int id) {
        return this.masterDB.update(
                "UPDATE agency_order_commit " +
                        " SET miss_commit = 1" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getAgencyOrderDeptHuntSaleList(Integer agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id +
                        " AND type != 1"
        );
    }

    public boolean forwardToOrderAppointment(int id) {
        return this.masterDB.update(
                "UPDATE agency_order SET type = " + OrderType.APPOINTMENT.getValue() + " WHERE id = " + id
        );
    }

    public int insertAgencyOrderDelivery(AgencyOrderEntity entity,
                                         int agency_order_id,
                                         String agency_order_code,
                                         String code,
                                         double total_end_price) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO agency_order_delivery(" +
                    "code," +
                    "agency_id," +
                    "agency_account_id," +
                    "membership_id," +
                    "address_delivery," +
                    "address_billing," +
                    "request_delivery_date," +
                    "note," +
                    "total_begin_price," +
                    "total_promotion_price," +
                    "total_end_price," +
                    "total_product_quantity," +
                    "source," +
                    "type," +
                    "status," +
                    "confirm_delivery_date," +
                    "created_date," +
                    "update_status_date," +
                    "creator_id," +
                    "modifier_id," +
                    "note_internal," +
                    "note_cancel," +
                    "agency_info," +
                    "total_promotion_product_price," +
                    "total_promotion_order_price," +
                    "total_promotion_order_price_ctkm," +
                    "total_refund_price," +
                    "promo_order_info," +
                    "promo_order_info_ctkm," +
                    "promo_product_info," +
                    "promo_product_info_ctkm," +
                    "promo_good_offer_info," +
                    "promo_good_offer_info_ctkm," +
                    "promo_all_id_info," +
                    "stuck_type," +
                    "stuck_info," +
                    "increase_dept," +
                    "nqh_order," +
                    "hmkd_over_order," +
                    "agency_order_id," +
                    "agency_order_code)" +
                    " VALUES(" +
                    parseStringToSql(code) + "," +
                    parseIntegerToSql(entity.getAgency_id()) + "," +
                    parseIntegerToSql(entity.getAgency_account_id()) + "," +
                    parseIntegerToSql(entity.getMembership_id()) + "," +
                    parseStringToSql(entity.getAddress_delivery()) + "," +
                    parseStringToSql(entity.getAddress_billing()) + "," +
                    parseDateToSql(entity.getRequest_delivery_date()) + "," +
                    parseStringToSql(entity.getNote()) + "," +
                    parseDoubleToSql(total_end_price) + "," +
                    "0" + "," +
                    parseDoubleToSql(total_end_price) + "," +
                    parseLongToSql(entity.getTotal_product_quantity()) + "," +
                    parseIntegerToSql(entity.getSource()) + "," +
                    parseIntegerToSql(entity.getType()) + "," +
                    parseIntegerToSql(OrderStatus.COMPLETE.getKey()) + "," +
                    parseDateToSql(entity.getConfirm_delivery_date()) + "," +
                    parseDateToSql(entity.getCreated_date()) + "," +
                    parseDateToSql(entity.getUpdate_status_date()) + "," +
                    parseIntegerToSql(entity.getCreator_id()) + "," +
                    parseIntegerToSql(entity.getModifier_id()) + "," +
                    parseStringToSql(entity.getNote_internal()) + "," +
                    parseStringToSql(entity.getNote_cancel()) + "," +
                    parseStringToSql(entity.getAgency_info()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_product_price()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_order_price()) + "," +
                    parseDoubleToSql(entity.getTotal_promotion_order_price_ctkm()) + "," +
                    parseDoubleToSql(entity.getTotal_refund_price()) + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    "?" + "," +
                    parseStringToSql(entity.getPromo_all_id_info()) + "," +
                    parseIntegerToSql(entity.getStuck_type()) + "," +
                    parseStringToSql(entity.getStuck_info()) + "," +
                    parseIntegerToSql(entity.getIncrease_dept()) + "," +
                    parseDoubleToSql(entity.getNqh_order()) + "," +
                    parseDoubleToSql(entity.getHmkd_over_order()) + "," +
                    parseIntegerToSql(agency_order_id) + "," +
                    parseStringToSql(agency_order_code) + "" +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getPromo_order_info());
                stmt.setString(2, entity.getPromo_order_info_ctkm());
                stmt.setString(3, entity.getPromo_product_info());
                stmt.setString(4, entity.getPromo_product_info_ctkm());
                stmt.setString(5, entity.getPromo_good_offer_info());
                stmt.setString(6, entity.getPromo_good_offer_info_ctkm());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public int insertAgencyOrderDeliveryProduct(
            AgencyOrderDetailEntity entity,
            int agency_order_delivery_id,
            String agency_order_code,
            int agency_order_dept_id) {
        return this.masterDB.insert(
                "INSERT INTO agency_order_delivery_product(" +
                        "agency_order_id," +
                        "product_id," +
                        "product_tag," +
                        "product_code," +
                        "product_full_name," +
                        "product_warranty_time," +
                        "product_images," +
                        "product_specification," +
                        "product_color_id," +
                        "product_color_name," +
                        "product_characteristic," +
                        "product_description," +
                        "product_user_manual," +
                        "product_technical_data," +
                        "product_price," +
                        "product_small_unit_id," +
                        "product_small_unit_name," +
                        "product_big_unit_id," +
                        "product_big_unit_name," +
                        "product_convert_small_unit_ratio," +
                        "product_minimum_purchase," +
                        "product_step," +
                        "product_item_type," +
                        "product_sort_data," +
                        "product_total_quantity," +
                        "product_total_begin_price," +
                        "product_total_promotion_price," +
                        "product_total_promotion_price_ctkm," +
                        "product_total_end_price," +
                        "created_date," +
                        "product_brand_id," +
                        "product_brand_name," +
                        "agency_order_delivery_id," +
                        "agency_order_code," +
                        "agency_order_dept_id" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getAgency_order_id()) + "," +//"agency_order_id," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +//"product_id," +
                        parseStringToSql(entity.getProduct_tag()) + "," + //"product_tag," +
                        parseStringToSql(entity.getProduct_code()) + "," + //"product_code," +
                        parseStringToSql(entity.getProduct_full_name()) + "," + //"product_full_name," +
                        parseStringToSql(entity.getProduct_warranty_time()) + "," + //"product_warranty_time," +
                        parseStringToSql(entity.getProduct_images()) + "," + // "product_images," +
                        parseStringToSql(entity.getProduct_specification()) + "," + //"product_specification," +
                        parseIntegerToSql(entity.getProduct_color_id()) + "," + //"product_color_id," +
                        parseStringToSql(entity.getProduct_color_name()) + "," + //"product_color_name," +
                        parseStringToSql(entity.getProduct_characteristic()) + "," + //"product_characteristic," +
                        parseStringToSql(entity.getProduct_description()) + "," + //"product_description," +
                        parseStringToSql(entity.getProduct_user_manual()) + "," + //"product_user_manual," +
                        parseStringToSql(entity.getProduct_technical_data()) + "," + //"product_technical_data," +
                        parseDoubleToSql(entity.getProduct_price()) + "," + //"product_price," +
                        parseIntegerToSql(entity.getProduct_small_unit_id()) + "," + //"product_small_unit_id," +
                        parseStringToSql(entity.getProduct_small_unit_name()) + "," + //"product_small_unit_name," +
                        parseIntegerToSql(entity.getProduct_big_unit_id()) + "," + //"product_big_unit_id," +
                        parseStringToSql(entity.getProduct_big_unit_name()) + "," + //"product_big_unit_name," +
                        parseIntegerToSql(entity.getProduct_convert_small_unit_ratio()) + "," + //"product_convert_small_unit_ratio," +
                        parseIntegerToSql(entity.getProduct_minimum_purchase()) + "," + //"product_minimum_purchase," +
                        parseIntegerToSql(entity.getProduct_step()) + "," + //"product_step," +
                        parseIntegerToSql(entity.getProduct_item_type()) + "," + //"product_item_type," +
                        parseStringToSql(entity.getProduct_sort_data()) + "," + //"product_sort_data," +
                        parseIntegerToSql(entity.getProduct_total_quantity()) + "," + //"product_total_quantity," +
                        parseDoubleToSql(entity.getProduct_total_begin_price()) + "," + //"product_total_begin_price," +
                        parseDoubleToSql(entity.getProduct_total_promotion_price()) + "," + //"product_total_promotion_price," +
                        parseDoubleToSql(entity.getProduct_total_promotion_price_ctkm()) + "," + //"product_total_promotion_price_ctkm," +
                        parseDoubleToSql(entity.getProduct_total_end_price()) + "," + //"product_total_end_price," +
                        parseDateToSql(entity.getCreated_date()) + "," + //"created_date," +
                        parseIntegerToSql(entity.getProduct_brand_id()) + "," + //"product_brand_id," +
                        parseStringToSql(entity.getProduct_brand_name()) + "," + //"product_brand_name" +
                        parseIntegerToSql(agency_order_delivery_id) + "," +
                        parseStringToSql(agency_order_code) + "," +
                        parseIntegerToSql(agency_order_dept_id) +
                        ")"
        );
    }

    public int insertAgencyOrderDeliveryGift(
            AgencyOrderPromoDetailEntity entity,
            int agency_order_delivery_id,
            String agency_order_code,
            int agency_order_dept_id) {
        return this.masterDB.insert(
                "INSERT INTO agency_order_delivery_gift(" +
                        "agency_order_id," +
                        "product_id," +
                        "product_tag," +
                        "product_code," +
                        "product_full_name," +
                        "product_warranty_time," +
                        "product_images," +
                        "product_specification," +
                        "product_color_id," +
                        "product_color_name," +
                        "product_characteristic," +
                        "product_description," +
                        "product_user_manual," +
                        "product_technical_data," +
                        "product_price," +
                        "product_small_unit_id," +
                        "product_small_unit_name," +
                        "product_big_unit_id," +
                        "product_big_unit_name," +
                        "product_convert_small_unit_ratio," +
                        "product_minimum_purchase," +
                        "product_step," +
                        "product_item_type," +
                        "product_sort_data," +
                        "product_total_quantity," +
                        "product_total_begin_price," +
                        "product_total_promotion_price," +
                        "product_total_end_price," +
                        "created_date," +
                        "product_brand_id," +
                        "product_brand_name," +
                        "type," +
                        "agency_order_delivery_id," +
                        "agency_order_code," +
                        "agency_order_dept_id" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getAgency_order_id()) + "," +//"agency_order_id," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +//"product_id," +
                        parseStringToSql(entity.getProduct_tag()) + "," + //"product_tag," +
                        parseStringToSql(entity.getProduct_code()) + "," + //"product_code," +
                        parseStringToSql(entity.getProduct_full_name()) + "," + //"product_full_name," +
                        parseStringToSql(entity.getProduct_warranty_time()) + "," + //"product_warranty_time," +
                        parseStringToSql(entity.getProduct_images()) + "," + // "product_images," +
                        parseStringToSql(entity.getProduct_specification()) + "," + //"product_specification," +
                        parseIntegerToSql(entity.getProduct_color_id()) + "," + //"product_color_id," +
                        parseStringToSql(entity.getProduct_color_name()) + "," + //"product_color_name," +
                        parseStringToSql(entity.getProduct_characteristic()) + "," + //"product_characteristic," +
                        parseStringToSql(entity.getProduct_description()) + "," + //"product_description," +
                        parseStringToSql(entity.getProduct_user_manual()) + "," + //"product_user_manual," +
                        parseStringToSql(entity.getProduct_technical_data()) + "," + //"product_technical_data," +
                        parseDoubleToSql(entity.getProduct_price()) + "," + //"product_price," +
                        parseIntegerToSql(entity.getProduct_small_unit_id()) + "," + //"product_small_unit_id," +
                        parseStringToSql(entity.getProduct_small_unit_name()) + "," + //"product_small_unit_name," +
                        parseIntegerToSql(entity.getProduct_big_unit_id()) + "," + //"product_big_unit_id," +
                        parseStringToSql(entity.getProduct_big_unit_name()) + "," + //"product_big_unit_name," +
                        parseIntegerToSql(entity.getProduct_convert_small_unit_ratio()) + "," + //"product_convert_small_unit_ratio," +
                        parseIntegerToSql(entity.getProduct_minimum_purchase()) + "," + //"product_minimum_purchase," +
                        parseIntegerToSql(entity.getProduct_step()) + "," + //"product_step," +
                        parseIntegerToSql(entity.getProduct_item_type()) + "," + //"product_item_type," +
                        parseStringToSql(entity.getProduct_sort_data()) + "," + //"product_sort_data," +
                        parseIntegerToSql(entity.getProduct_total_quantity()) + "," + //"product_total_quantity," +
                        parseDoubleToSql(entity.getProduct_total_begin_price()) + "," + //"product_total_begin_price," +
                        parseDoubleToSql(entity.getProduct_total_promotion_price()) + "," + //"product_total_promotion_price," +
                        parseDoubleToSql(entity.getProduct_total_end_price()) + "," + //"product_total_end_price," +
                        parseDateToSql(entity.getCreated_date()) + "," + //"created_date," +
                        parseIntegerToSql(entity.getProduct_brand_id()) + "," + //"product_brand_id," +
                        parseStringToSql(entity.getProduct_brand_name()) + "," + //"product_brand_name" +
                        parseIntegerToSql(entity.getType()) + "," + //"type" +
                        parseIntegerToSql(agency_order_delivery_id) + "," +
                        parseStringToSql(agency_order_code) + "," +
                        parseIntegerToSql(agency_order_dept_id) +
                        ")"
        );
    }

    public List<JSONObject> getListProductInOrderDelivery(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_delivery_product" +
                        " WHERE agency_order_delivery_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListGoodsInOrderDelivery(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_delivery_gift" +
                        " WHERE agency_order_delivery_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListProductInOrderDeliveryByAgencyOrderId(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_delivery_product" +
                        " WHERE agency_order_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public List<JSONObject> getListGoodsInOrderDeliveryByAgencyOrderId(int id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_delivery_gift" +
                        " WHERE agency_order_id=" + id +
                        " AND product_total_quantity > 0"
        );
    }

    public JSONObject getAgencyOrderDelivery(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_delivery WHERE id = " + id
        );
    }

    public List<JSONObject> getListAgencyOrderDeliveryHasProduct(
            int agency_order_id) {
        return this.masterDB.find(
                "SELECT agency_order_delivery_id as id" +
                        " FROM agency_order_delivery_product" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " GROUP BY agency_order_delivery_id"
        );
    }

    public List<JSONObject> getListAgencyOrderDeliveryHasGift(
            int agency_order_id) {
        return this.masterDB.find(
                "SELECT agency_order_delivery_id as id" +
                        " FROM agency_order_delivery_gift" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " GROUP BY agency_order_delivery_id"
        );
    }

    public int getProductQuantityDelivery(int agency_order_id, int product_id) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT t.product_id, sum(t.product_total_quantity) as total" +
                        " FROM agency_order_delivery_product t" +
                        " WHERE t.agency_order_id = " + agency_order_id +
                        " AND t.product_id = " + product_id +
                        " GROUP BY t.product_id"
        );

        if (rsTotal != null) {
            return ConvertUtils.toInt(rsTotal.get("total"));
        }
        return 0;
    }

    public int getGiftQuantityDelivery(int agency_order_id, int product_id) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT t.product_id, sum(t.product_total_quantity) as total" +
                        " FROM agency_order_delivery_gift t" +
                        " WHERE t.agency_order_id = " + agency_order_id +
                        " AND t.product_id = " + product_id +
                        " GROUP BY t.product_id"
        );

        if (rsTotal != null) {
            return ConvertUtils.toInt(rsTotal.get("total"));
        }
        return 0;
    }

    public boolean syncOrderFailed(int agency_order_id,
                                   String message) {
        return this.masterDB.update(
                "UPDATE agency_order SET sync_status = 2," +
                        " sync_note = '" + "Đồng bộ thất bại: " + message + "'" +
                        " WHERE id = " + agency_order_id
        );
    }

    public boolean syncOrderDeptFailed(int agency_order_dept_id,
                                       String message) {
        return this.masterDB.update(
                "UPDATE agency_order_dept SET sync_status = 2," +
                        " sync_note = '" + "Đồng bộ thất bại: " + message + "'" +
                        " WHERE id = " + agency_order_dept_id
        );
    }

    public boolean syncOrderDeptSuccess(int agency_order_dept_id) {
        return this.masterDB.update(
                "UPDATE agency_order_dept SET sync_status = 1" +
                        " WHERE id = " + agency_order_dept_id
        );
    }

    public boolean syncOrderSuccess(int agency_order_id) {
        return this.masterDB.update(
                "UPDATE agency_order SET sync_status = 1" +
                        " WHERE id = " + agency_order_id
        );
    }

    public JSONObject getAgencyOrderDept(int agency_order_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id +
                        " LIMIT 1"
        );
    }


    public int insertAgencyOrderDept(
            Integer agency_order_id,
            int type,
            int promo_id,
            int dept_cycle,
            double total_begin_price,
            double total_promotion_price,
            double total_end_price,
            int order_data_index,
            String promo_info,
            double total_promotion_product_price,
            double total_promotion_order_price,
            double total_promotion_order_price_ctkm,
            double total_refund_price,
            double total_dm_price,
            double total_voucher_price) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO agency_order_dept(" +
                            "agency_order_id," +
                            "type," +
                            "promo_id," +
                            "dept_cycle," +
                            "total_begin_price," +
                            "total_promotion_price," +
                            "total_end_price," +
                            "order_data_index," +
                            "promo_info," +
                            "total_refund_price," +
                            "total_promotion_product_price," +
                            "total_promotion_order_price," +
                            "total_promotion_order_price_ctkm," +
                            "total_dm_price," +
                            "total_voucher_price" +
                            ") VALUES(" +
                            parseIntegerToSql(agency_order_id) + "," +
                            parseIntegerToSql(type) + "," +
                            parseIntegerToSql(promo_id) + "," +
                            parseIntegerToSql(dept_cycle) + "," +
                            parseDoubleToSql(total_begin_price) + "," +
                            parseDoubleToSql(total_promotion_price) + "," +
                            parseDoubleToSql(total_end_price) + "," +
                            parseIntegerToSql(order_data_index) + "," +
                            "?," +
                            parseDoubleToSql(total_refund_price) + "," +
                            parseDoubleToSql(total_promotion_product_price) + "," +
                            parseDoubleToSql(total_promotion_order_price) + "," +
                            parseDoubleToSql(total_promotion_order_price_ctkm) + "," +
                            parseDoubleToSql(total_dm_price) + "," +
                            parseDoubleToSql(total_voucher_price) +
                            ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, promo_info);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public boolean updateTotalOrder(Integer id, long total_order) {
        return this.masterDB.update(
                "UPDATE agency_order SET total = " + total_order +
                        " WHERE id = " + id
        );
    }

    public boolean updateAgencyOrderTotal(int agency_order_id, int total) {
        return this.masterDB.update(
                "UPDATE agency_order SET total = " + total +
                        " WHERE id = " + agency_order_id
        );
    }

    public List<JSONObject> getListAgencyOrderOfOrderDelivery(int agency_order_delivery_id) {
        return this.masterDB.find(
                "SELECT agency_order_id as id, agency_order_code as code" +
                        " FROM agency_order_delivery_product" +
                        " WHERE agency_order_delivery_id = " + agency_order_delivery_id +
                        " GROUP BY agency_order_id,agency_order_code"
        );
    }

    public List<JSONObject> getListAgencyOrderOfOrderDeliveryGift(int agency_order_delivery_id) {
        return this.masterDB.find(
                "SELECT agency_order_id as id, agency_order_code as code" +
                        " FROM agency_order_delivery_gift" +
                        " WHERE agency_order_delivery_id = " + agency_order_delivery_id +
                        " GROUP BY agency_order_id,agency_order_code"
        );
    }

    public List<JSONObject> getListAgencyOrderDept(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id
        );
    }

    public List<JSONObject> getListProductByAgencyOrderDept(
            int agency_order_id,
            int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND promo_id = " + promo_id
        );
    }

    public List<JSONObject> getListGiftByAgencyOrderDept(
            int agency_order_id,
            int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_promo_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND promo_id = " + promo_id
        );
    }

    public JSONObject getAgencyOrderDeptById(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_dept WHERE id = " + id
        );
    }

    public boolean completeAgencyOrderDept(int id) {
        return this.masterDB.update(
                "UPDATE agency_order_dept SET status = " + OrderStatus.COMPLETE.getKey() +
                        " WHERE id = " + id
        );
    }

    public int insertAgencyOrderHuntSaleDetail(AgencyOrderHuntSaleEntity agencyOrderHuntSaleEntity) {
        return this.masterDB.insert(
                "INSERT INTO agency_order_hunt_sale(" +
                        "agency_order_id," +
                        "promo_id," +
                        "product_id," +
                        "product_small_unit_id," +
                        "type," +
                        "product_total_quantity," +
                        "is_combo," +
                        "product_code," +
                        "product_images," +
                        "product_full_name," +
                        "product_description," +
                        "product_step," +
                        "product_item_type," +
                        "product_price," +
                        "product_total_begin_price," +
                        "product_total_promotion_price," +
                        "product_total_end_price" +
                        ") VALUES(" +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getAgency_order_id()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getPromo_id()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getProduct_id()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getProduct_small_unit_id()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getType()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getProduct_total_quantity()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getIs_combo()) + "," +
                        parseStringToSql(agencyOrderHuntSaleEntity.getProduct_code()) + "," +
                        parseStringToSql(agencyOrderHuntSaleEntity.getProduct_images()) + "," +
                        parseStringToSql(agencyOrderHuntSaleEntity.getProduct_full_name()) + "," +
                        parseStringToSql(agencyOrderHuntSaleEntity.getProduct_description()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getProduct_step()) + "," +
                        parseIntegerToSql(agencyOrderHuntSaleEntity.getProduct_item_type()) + "," +
                        parseLongToSql(agencyOrderHuntSaleEntity.getProduct_price()) + "," +
                        parseLongToSql(agencyOrderHuntSaleEntity.getProduct_total_begin_price()) + "," +
                        parseLongToSql(agencyOrderHuntSaleEntity.getProduct_total_promotion_price()) + "," +
                        parseLongToSql(agencyOrderHuntSaleEntity.getProduct_total_end_price()) +
                        ")"
        );
    }

    public List<JSONObject> getListAgencyOrderDeptHuntSale(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id +
                        " AND order_data_index != 0" +
                        " ORDER BY order_data_index ASC"
        );
    }

    public List<JSONObject> getListAgencyOrderHuntSale(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_hunt_sale WHERE agency_order_id = " + agency_order_id
        );
    }

    public boolean deleteAgencyOrderDept(Integer agency_order_id) {
        return this.masterDB.update(
                "DELETE FROM agency_order_dept WHERE agency_order_id = " + agency_order_id
        );
    }

    public JSONObject getOrderNormalDept(int agency_order_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id +
                        " AND order_data_index = 0"
        );
    }

    public List<JSONObject> getAgencyPromoHuntSaleByOrder(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_promo_hunt_sale WHERE agency_order_id = " + agency_order_id
        );
    }

    public boolean deleteAgencyPromoHuntSale(int agency_order_id) {
        return this.masterDB.update(
                "DELETE FROM agency_promo_hunt_sale WHERE agency_order_id = " + agency_order_id
        );
    }

    public List<JSONObject> getListProductHuntSaleInOrder(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_detail" +
                        " WHERE agency_order_id=" + agency_order_id +
                        " AND product_total_quantity > 0" +
                        " AND promo_id != 0"
        );
    }

    public int insertAgencyPromoHuntSale(int agency_order_id, int agency_id, int promo_id, int product_id, int product_quantity) {
        return this.masterDB.insert(
                "INSERT INTO agency_promo_hunt_sale(" +
                        " agency_order_id," +
                        " agency_id," +
                        " promo_id," +
                        " product_id," +
                        " product_quantity" +
                        ") VALUES(" +
                        agency_order_id + "," +
                        agency_id + "," +
                        promo_id + "," +
                        product_id + "," +
                        product_quantity + "" +
                        ")"
        );
    }

    public void saveAgencyOrderDeptPromo(int rsInsertAgencyOrderDept, String serialize) {
    }

    public List<JSONObject> getListAgencyOrderDeptNotFinish(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_dept" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND status != " + OrderStatus.COMPLETE.getKey()
        );
    }

    public List<JSONObject> getListAgencyOrderDeptNotGNCN(int agency_order_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_order_dept" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND increase_dept != 1"
        );
    }

    public boolean increaseDeptForAgencyOrderDept(int id) {
        return this.masterDB.update(
                "UPDATE agency_order_dept" +
                        " SET increase_dept = 1" +
                        " WHERE id = " + id
        );
    }

    public int getTotalProductQuantityBuyByAgencyOrderDept(
            int agency_order_id,
            int promo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND promo_id = " + promo_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getTotalGiftQuantityBuyByAgencyOrderDept(
            int agency_order_id,
            int promo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_promo_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND promo_id = " + promo_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getProductQuantityDeliveryByAgencyOrderDept(
            int agency_order_dept_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_delivery_product" +
                        " WHERE agency_order_dept_id = " + agency_order_dept_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getGiftQuantityDeliveryByAgencyOrderDept(
            int agency_order_dept_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_delivery_gift" +
                        " WHERE agency_order_dept_id = " + agency_order_dept_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public boolean increaseTotalMoneyDeptOfAgencyOrder(int agency_order_id, long total_end_price) {
        return this.masterDB.update(
                "UPDATE agency_order SET total_money_dept = total_money_dept + "
                        + total_end_price +
                        " WHERE id = " + agency_order_id
        );
    }

    public JSONObject getAgencyOrderDeptCompleteByAgencyOrderId(int agency_order_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_order_dept WHERE agency_order_id = " + agency_order_id +
                        " AND status = " + OrderStatus.COMPLETE.getKey() +
                        " LIMIT 1"
        );
    }

    public JSONObject getAgencyHBTL(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_hbtl WHERE id = " + id
        );
    }

    public List<JSONObject> getListHBTLDetail(int agency_hbtl_id) {
        return this.masterDB.find(
                "SELECT * FROM agency_hbtl_detail WHERE agency_hbtl_id = " + agency_hbtl_id
        );
    }

    public boolean saveDeptCode(int id, String dept_code) {
        return this.masterDB.update(
                "UPDATE agency_order_dept SET dept_code = '" + dept_code + "'" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getAgencyOrderBasicByAgencyOrderDeptId(int agency_order_dept_id) {
        return this.masterDB.getOne(
                "SELECT t1.id, t1.code, t1.status, " +
                        "t1.created_date, " +
                        "t1.update_status_date as confirm_date, " +
                        "t.total_end_price,t.dept_code" +
                        " FROM agency_order_dept t " +
                        " JOIN agency_order t1 ON t1.id = t.agency_order_id" +
                        " WHERE t.id = " + agency_order_dept_id
        );
    }

    public int getAgencyOrderDeptByDeptCode(String dept_code) {
        JSONObject js = this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_order_dept" +
                        " WHERE dept_code = '" + dept_code + "'"
        );
        return js == null ? 0 : ConvertUtils.toInt(js.get("id"));
    }

    public int getTotalProductQuantityBuy(
            int agency_order_id,
            int product_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND product_id = " + product_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getTotalGiftQuantityBuy(
            int agency_order_id,
            int product_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_quantity) as total" +
                        " FROM agency_order_promo_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND product_id = " + product_id
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public List<JSONObject> getlistAgencyOrderShipping(int type, int status) {
        return this.masterDB.find(
                "SELECT id" +
                        " FROM agency_order WHERE type = " + type + "" +
                        " AND status = " + status
        );
    }

    public int saveAgencyHBTL(
            String code,
            int agency_id,
            long total_begin_price,
            long total_end_price) {
        return this.masterDB.insert(
                "INSERT INTO agency_hbtl(" +
                        "doc_no," +
                        "agency_id," +
                        "total_begin_price," +
                        "total_end_price" +
                        ") VALUES(" +
                        "'" + code + "'," +
                        "'" + agency_id + "'," +
                        "'" + total_begin_price + "'," +
                        "'" + total_end_price + "'" +
                        ")"
        );
    }

    public boolean updateCodeAgencyHBTL(int id, String code) {
        return this.masterDB.update(
                "UPDATE agency_hbtl SET code = '" + code + "'" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListProductNormal(int agency_order_id) {
        return this.masterDB.find(
                "SELECT product_id, product_total_quantity as product_quantity" +
                        " FROM agency_order_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND promo_id = 0"
        );
    }

    public List<JSONObject> getProductListByHBTLForCSDM(int id) {
        return this.masterDB.find(
                "SELECT product_id, product_total_quantity as product_quantity" +
                        " FROM agency_hbtl_detail" +
                        " WHERE agency_hbtl_id = " + id);
    }

    public List<JSONObject> getListProductDelivery(int agency_order_id,
                                                   int agency_order_delivery_id) {
        return this.masterDB.find(
                "SELECT product_id, product_total_quantity as product_quantity" +
                        " FROM agency_order_delivery_product" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND agency_order_delivery_id = " + agency_order_delivery_id
        );
    }

    public long getUuDaiDamMeByPromo(int agency_order_id, int dm_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_dm_price) as total" +
                        " FROM agency_order_detail" +
                        " WHERE agency_order_id = " + agency_order_id +
                        " AND dm_id = " + dm_id
        );
        return ConvertUtils.toLong(rs.get("total"));
    }

    public boolean saveAgencyCSDMClaim(Integer agency_order_id,
                                       String agency_order_code,
                                       int agency_id) {
        return this.masterDB.update(
                "INSERT INTO agency_csdm_claim(" +
                        "promo_id," +
                        "product_id," +
                        "agency_id," +
                        "product_total_dm_price," +
                        "agency_order_id," +
                        "agency_order_code" +
                        ")" +
                        " SELECT dm_id," +
                        " product_id," +
                        "'" + agency_id + "'," +
                        "product_total_dm_price," +
                        "'" + agency_order_id + "'," +
                        "'" + agency_order_code + "'" +
                        " FROM agency_order_detail WHERE agency_order_id = " + agency_order_id +
                        " AND dm_id is not null" +
                        " AND product_total_dm_price > 0"
        );
    }

    public List<JSONObject> getListProductInOrderNormal(int id) {
        return this.masterDB.find(
                "SELECT product_id, product_total_quantity as product_quantity" +
                        " FROM agency_order_detail" +
                        " WHERE agency_order_id=" + id +
                        " AND promo_id = 0" +
                        " AND product_total_quantity > 0");
    }

    public boolean expireVoucher() {
        return this.masterDB.update("UPDATE voucher SET status = " + VoucherStatus.EXPIRED.getId()
                + " WHERE status = " + VoucherStatus.READY.getId() + " AND expired_date < NOW()");
    }

    public JSONObject getAgencyOrderInfo(int agency_order_id) {
        return this.masterDB.getOne("SELECT id, code, total_end_price FROM agency_order WHERE id = " + agency_order_id);
    }

    public boolean returnVoucher(int agency_order_id) {
        return this.masterDB.update(
                "UPDATE voucher SET status = " + VoucherStatus.READY.getId() + "," +
                        " used_date = NULL," +
                        " agency_order_id = NULL" +
                        " WHERE agency_order_id = " + agency_order_id
        );
    }

    public List<JSONObject> getListVoucherByAgency(int agency_id) {
        return this.masterDB.find("SELECT * FROM voucher WHERE agency_id = " + agency_id +
                " AND status = " + VoucherStatus.READY.getId());
    }

    public JSONObject getVoucher(Integer voucherId) {
        return this.masterDB.getOne("SELECT * FROM voucher WHERE id = " + voucherId);
    }

    public List<JSONObject> getListVoucherGift(int agency_order_id) {
        return this.masterDB.find("SELECT id,product_id,product_total_quantity,voucher_id FROM agency_order_promo_detail" +
                " WHERE agency_order_id = " + agency_order_id +
                " AND type = " + AgencyOrderPromoType.GIFT_VOUCHER.getId());
    }

    public List<JSONObject> getListVoucherMoneyByAgencyOrder(int agency_order_id) {
        return this.masterDB.find("SELECT * FROM voucher" +
                " WHERE agency_order_id = " + agency_order_id +
                " AND offer_type = " + parseStringToSql(VoucherOfferType.MONEY_DISCOUNT.getKey()));
    }

    public JSONObject getVRP(int id) {
        return this.masterDB.getOne("SELECT * FROM voucher_release_period" +
                " WHERE id = " + id);
    }

    public boolean saveVoucherUsed(Integer agency_order_id, Integer voucher_id) {
        return this.masterDB.update("UPDATE voucher SET" +
                " status = " + parseIntegerToSql(VoucherStatus.USED.getId()) + "," +
                " used_date = NOW()," +
                " agency_order_id = " + agency_order_id +
                " WHERE id = " + voucher_id);
    }

    public List<JSONObject> getListVoucherUseGift(int agency_order_id) {
        return this.masterDB.find("SELECT * FROM voucher" +
                " WHERE agency_order_id = " + agency_order_id +
                " AND offer_type = " + parseStringToSql(VoucherOfferType.GIFT_OFFER.getKey()));
    }

    public boolean ghiNhanTrangThaiTichLuyNhiemVuChoDonHang(int id) {
        return this.masterDB.update("UPDATE agency_order SET accumulate_mission_status=1, accumulate_mission_date = NOW() WHERE id = " + id);
    }

    public boolean ghiNhanTrangThaiTichLuyNhiemVuChoDonHangOver(int id) {
        return this.masterDB.update("UPDATE agency_order SET accumulate_mission_status=1, accumulate_mission_over_status=1, accumulate_mission_date = NOW() WHERE id = " + id);
    }

    public boolean huyTichLuyNhiemVuChoDonHangOver(int id) {
        return this.masterDB.update("UPDATE agency_order SET accumulate_mission_status=0 WHERE id = " + id);
    }

    public boolean ghiNhanDonHangOver(int id) {
        return this.masterDB.update("UPDATE agency_order SET accumulate_mission_over_status=1 WHERE id = " + id);
    }

    public int insertAgencyOrderConfirm(JSONObject jsAgencyOrder, String note) {
        return this.masterDB.insert(
                "INSERT INTO agency_order_confirm(" +
                        "agency_order_id, " +
                        "total_begin_price," +
                        "total_end_price," +
                        "agency_id," +
                        "code," +
                        "request_delivery_date," +
                        "plan_delivery_date," +
                        "note) VALUES(" +
                        parseIntegerToSql(ConvertUtils.toInt(jsAgencyOrder.get("id"))) + "," +
                        parseDoubleToSql(ConvertUtils.toDouble(jsAgencyOrder.get("total_begin_price"))) + "," +
                        parseDoubleToSql(ConvertUtils.toDouble(jsAgencyOrder.get("total_end_price"))) + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsAgencyOrder.get("agency_id"))) + "," +
                        parseStringToSql(ConvertUtils.toString(jsAgencyOrder.get("code"))) + "," +
                        parseDateToSql(jsAgencyOrder.get("request_delivery_date") == null ?
                                null :
                                (DateTimeUtils.getDateTime(
                                        jsAgencyOrder.get("request_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"))) + "," +
                        parseDateToSql(jsAgencyOrder.get("request_delivery_date") == null ?
                                null :
                                (DateTimeUtils.getDateTime(
                                        jsAgencyOrder.get("request_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"))) + "," +
                        parseStringToSql(note) +
                        ")"
        );
    }

    public int insertPOM(JSONObject jsAgencyOrder) {
        return this.masterDB.insert(
                "INSERT INTO pom(" +
                        "agency_order_id, total_begin_price, total_end_price, agency_id, business_department_id,code) VALUES(" +
                        parseIntegerToSql(ConvertUtils.toInt(jsAgencyOrder.get("id"))) + "," +
                        parseLongToSql(ConvertUtils.toLong(jsAgencyOrder.get("total_begin_price"))) + "," +
                        parseLongToSql(ConvertUtils.toLong(jsAgencyOrder.get("total_end_price"))) + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsAgencyOrder.get("agency_id"))) + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsAgencyOrder.get("business_department_id"))) + "," +
                        parseStringToSql(ConvertUtils.toString(jsAgencyOrder.get("code"))) + "" +
                        ")"
        );
    }

    public int insertAgencyOrderConfirmProduct(
            int agency_order_id,
            int agency_order_confirm_id,
            JSONObject jsProduct,
            String product_note,
            String product_info) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO agency_order_confirm_product(" +
                            "agency_order_id," +
                            "agency_order_confirm_id," +
                            "product_id," +
                            "product_total_quantity," +
                            "product_total_begin_price," +
                            "product_total_end_price," +
                            "product_price," +
                            "product_small_unit_id," +
                            "product_note," +
                            "product_info) VALUES(" +
                            agency_order_id + "," +
                            agency_order_confirm_id + "," +
                            parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_id"))) + "," +
                            parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_total_quantity"))) + "," +
                            parseDoubleToSql(ConvertUtils.toDouble(jsProduct.get("product_total_begin_price"))) + "," +
                            parseDoubleToSql(ConvertUtils.toDouble(jsProduct.get("product_total_end_price"))) + "," +
                            parseDoubleToSql(ConvertUtils.toDouble(jsProduct.get("product_price"))) + "," +
                            parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_small_unit_id"))) + "," +
                            parseStringToSql(product_note) + "," +
                            "?)";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, product_info);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public int insertPOMProduct(int agency_order_id, int agency_order_confirm_id, JSONObject jsProduct) {
        return this.masterDB.insert(
                "INSERT INTO pom_product(" +
                        "agency_order_id," +
                        "agency_order_confirm_id," +
                        "product_id," +
                        "product_total_quantity," +
                        "product_total_begin_price," +
                        "product_total_end_price," +
                        "product_price," +
                        "product_small_unit_id) VALUES(" +
                        agency_order_id + "," +
                        agency_order_confirm_id + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_id"))) + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_total_quantity"))) + "," +
                        parseLongToSql(ConvertUtils.toLong(jsProduct.get("product_total_begin_price"))) + "," +
                        parseLongToSql(ConvertUtils.toLong(jsProduct.get("product_total_end_price"))) + "," +
                        parseLongToSql(ConvertUtils.toLong(jsProduct.get("product_price"))) + "," +
                        parseIntegerToSql(ConvertUtils.toInt(jsProduct.get("product_small_unit_id"))) + ")"
        );
    }

    public JSONObject getAgencyOrderDetailByProduct(int agency_order_id, int product_id) {
        return this.masterDB.getOne("SELECT * FROM agency_order_detail WHERE agency_order_id=" + agency_order_id +
                " AND product_id=" + product_id);
    }

    public JSONObject getPOMInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM pom WHERE id = " + id);
    }

    public boolean editOCNo(int orderId, String docNo) {
        return this.masterDB.update("UPDATE agency_order_confirm SET doc_no=" + parseStringToSql(docNo) +
                " WHERE id = " + orderId);
    }

    public boolean editPONo(int orderId, String docNo) {
        return this.masterDB.update("UPDATE agency_order SET doc_no=" + parseStringToSql(docNo) +
                " WHERE id = " + orderId);
    }

    public boolean completeOC(int id, int status) {
        return this.masterDB.update("UPDATE agency_order_confirm SET status=" + status +
                " WHERE id = " + id);
    }

    public int countOCP(int product_id) {
        return this.masterDB.getTotal(
                "SELECT * FROM product_summary WHERE product_id=" + product_id);
    }

    public int insertProductSummary(int product_id, int product_total_quantity) {
        return this.masterDB.insert("INSERT INTO product_summary(product_id,product_total_quantity) VALUES(" +
                product_id + "," +
                product_total_quantity + ")");
    }
}