package com.app.server.database.sql;

import com.app.server.enums.ProcessStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PromoSQL {
    public String getTotalPromoByPromoType(String key) {
        return "SELECT id FROM promo WHERE promo_type='" + key + "'";
    }

    public String getLastPromoHistory(int promo_id) {
        return "SELECT * FROM promo_history WHERE promo_id=" + promo_id + "" +
                " ORDER BY id DESC";
    }

    public String getPromoRunningById(int promo_id) {
        return "SELECT * FROM promo_running WHERE promo_id=" + promo_id + "";
    }

    public String getPromo(int id) {
        return "SELECT id FROM promo WHERE id='" + id + "'";
    }

    public String removePromoRunning(int promo_id) {
        return "DELETE FROM promo_running WHERE promo_id=" + promo_id;
    }

    public String getPromoScheduleWaiting(int scheduleRunningLimit) {
        return "SELECT * FROM promo_schedule" +
                " WHERE status = " + ProcessStatus.WAITING.getValue() +
                " AND schedule_time <= NOW()" +
                " LIMIT " + scheduleRunningLimit;
    }

    public String getPromoScheduleWaitingStartByPromo(int promo_id) {
        return "SELECT * FROM promo_schedule" +
                " WHERE promo_id = " + promo_id + " AND status = 0 and schedule_type = 'START'";
    }

    public String getPromoScheduleWaitingStopByPromo(int promo_id) {
        return "SELECT * FROM promo_schedule" +
                " WHERE promo_id = " + promo_id + " AND status = 0 AND schedule_type = 'STOP'";
    }

    public String getPromoInfo(int promo_id) {
        return "SELECT * FROM promo WHERE id=" + promo_id;
    }

    public String getPromoItemGroupList(int promo_id) {
        return "SELECT * FROM promo_item_group WHERE promo_id=" + promo_id;
    }

    public String getPromoItemGroupDetailList(int promo_item_group_id) {
        return "SELECT * FROM promo_item_group_detail WHERE promo_item_group_id=" + promo_item_group_id;
    }

    public String getPromoLimitList(int promo_id) {
        return "SELECT * FROM promo_limit WHERE promo_id=" + promo_id;
    }

    public String getPromoLimitGroupList(int promo_limit_id) {
        return "SELECT * FROM promo_limit_group WHERE promo_limit_id=" + promo_limit_id;
    }

    public String getPromoOffer(int promo_limit_group_id) {
        return "SELECT * FROM promo_offer WHERE promo_limit_group_id=" + promo_limit_group_id;
    }

    public String getPromoOfferProductList(int promo_offer_id) {
        return "SELECT * FROM promo_offer_product WHERE promo_offer_id=" + promo_offer_id;
    }

    public String getPromoOfferBonusList(int promo_offer_id) {
        return "SELECT * FROM promo_offer_bonus WHERE promo_offer_id=" + promo_offer_id;
    }
}