package com.app.server.database.sql;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderSQL {
    public String getAgencyOrder(int id) {
        return "SELECT * FROM agency_order WHERE id=" + id;
    }

    public String getListProductInOrder(int id) {
        return "SELECT * FROM agency_order_detail WHERE agency_order_id=" + id;
    }

    public String updateAgencyOrderStatus(int id, int status, String note_internal, int modifier_id) {
        return "UPDATE agency_order "
                + " SET status = " + status
                + " , update_status_date = NOW() "
                + " , modifier_id=" + modifier_id +
                " WHERE id=" + id;
    }

    public String deliveryAgencyOrder(int id, int status, String note_internal, int modifier_id) {
        return "UPDATE agency_order "
                + " SET status = " + status
                + " , update_status_date = NOW() "
                + " , confirm_delivery_date = NOW() "
                + " , modifier_id=" + modifier_id +
                " WHERE id=" + id;
    }

    public String cancelAgencyOrder(int id, int status, String note, int modifier_id) {
        return "UPDATE agency_order "
                + " SET status = " + status
                + " , note_cancel='" + note + "'"
                + " , update_status_date = NOW() "
                + " , modifier_id=" + modifier_id +
                " WHERE id=" + id;
    }

    public String saveUpdateOrderStatusHistory(int agency_order_id, int agency_order_status, String note_internal, int creator_id) {
        return "INSERT INTO agency_order_status_history(" +
                "agency_order_id," +
                "creator_id," +
                "agency_order_status," +
                "note)" +
                " VALUES(" +
                "" + agency_order_id + "," +
                "" + creator_id + "," +
                "" + agency_order_status + "," +
                "'" + note_internal + "'" +
                ")";
    }

    public String countOrderByDate(String startDate, String endDate, int agency_id) {
        return "SELECT agency_order.id FROM agency_order WHERE created_date BETWEEN '" +
                startDate + "' AND '" + endDate + "' AND agency_id = " + agency_id;
    }

    public String getListOrderStatusHistory(int id) {
        return "SELECT * FROM agency_order_status_history WHERE agency_order_id=" + id;
    }

    public String getListAgencyOrderDetail(int agency_order_id) {
        return "SELECT * FROM agency_order_detail WHERE agency_order_id=" + agency_order_id;
    }

    public String deleteAgencyOrderDetail(Integer id) {
        return "DELETE FROM agency_order_detail WHERE agency_order_id = " + id;
    }
}