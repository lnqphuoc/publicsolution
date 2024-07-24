package com.app.server.database;

import com.app.server.data.entity.NotifyHistoryEntity;
import com.app.server.data.entity.NotifySettingEntity;
import com.app.server.enums.NotifyDisplayType;
import com.app.server.enums.NotifyStatus;
import com.app.server.enums.NotifyWaitingPushStatus;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotifyDB extends BaseDB {
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

    public JSONObject getNotifySetting(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM notify_setting WHERE id = " + id
        );
    }

    public int insertNotifySetting(
            NotifySettingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO notify_setting(" +
                        "name" + "," +
                        "image" + "," +
                        "description" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "agency_ids" + "," +
                        "city_ids" + "," +
                        "region_ids" + "," +
                        "membership_ids" + "," +
                        "setting_type" + "," +
                        "setting_value" + "," +
                        "note" +
                        ") VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseStringToSql(entity.getImage()) + "," +
                        parseStringToSql(entity.getDescription()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseStringToSql(entity.getAgency_ids()) + "," +
                        parseStringToSql(entity.getCity_ids()) + "," +
                        parseStringToSql(entity.getRegion_ids()) + "," +
                        parseStringToSql(entity.getMembership_ids()) + "," +
                        parseStringToSql(entity.getSetting_type()) + "," +
                        parseStringToSql(entity.getSetting_value()) + "," +
                        parseStringToSql(entity.getNote()) +
                        ")"
        );
    }

    public boolean updateNotifySetting(
            NotifySettingEntity entity) {
        return this.masterDB.update(
                "UPDATE notify_setting SET" +
                        " name = " + parseStringToSql(entity.getName()) + "," +
                        " image = " + parseStringToSql(entity.getImage()) + "," +
                        " description = " + parseStringToSql(entity.getDescription()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " agency_ids = " + parseStringToSql(entity.getAgency_ids()) + "," +
                        " city_ids = " + parseStringToSql(entity.getCity_ids()) + "," +
                        " region_ids = " + parseStringToSql(entity.getRegion_ids()) + "," +
                        " membership_ids = " + parseStringToSql(entity.getMembership_ids()) + "," +
                        " setting_type = " + parseStringToSql(entity.getSetting_type()) + "," +
                        " setting_value = " + parseStringToSql(entity.getSetting_value()) + "," +
                        " note = " + parseStringToSql(entity.getNote()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public NotifySettingEntity getNotifySettingEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM notify_setting WHERE id = " + id
        );
        if (rs != null) {
            return NotifySettingEntity.from(rs);
        }
        return null;
    }

    public boolean activateNotifySetting(int notify_id, int staff_id) {
        return this.masterDB.update(
                "UPDATE notify_setting SET status = " + NotifyStatus.SENT.getId() +
                        ", modified_date= NOW()" +
                        ", modifier_id = " + staff_id +
                        " WHERE id = " + notify_id
        );
    }

    public boolean cancelNotify(int notify_id, int staff_id) {
        return this.masterDB.update(
                "UPDATE notify_setting SET status = " + NotifyStatus.CANCEL.getId() +
                        ", modified_date= NOW()" +
                        ", modifier_id = " + staff_id +
                        " WHERE id = " + notify_id
        );
    }

    public int insertNotifyHistory(
            NotifyHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO notify_history(" +
                        "name" + "," +
                        "image" + "," +
                        "description" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "agency_ids" + "," +
                        "city_ids" + "," +
                        "region_ids" + "," +
                        "membership_ids" + "," +
                        "setting_type" + "," +
                        "setting_value" +
                        ") VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseStringToSql(entity.getImage()) + "," +
                        parseStringToSql(entity.getDescription()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseStringToSql(entity.getAgency_ids()) + "," +
                        parseStringToSql(entity.getCity_ids()) + "," +
                        parseStringToSql(entity.getRegion_ids()) + "," +
                        parseStringToSql(entity.getMembership_ids()) + "," +
                        parseStringToSql(entity.getSetting_type()) + "," +
                        parseStringToSql(entity.getSetting_value()) +
                        ")"
        );
    }

    public boolean deleteNotifySetting(int id, int staff_id) {
        return this.masterDB.update(
                "UPDATE notify_setting SET status = -1 " +
                        ", modifier_id = " + staff_id +
                        ", modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public int insertNotifyWaitingPush(
            String firebase_token_data,
            String name,
            String description,
            String image,
            int status) {
        return this.masterDB.insert(
                "INSERT INTO notify_waiting_push(" +
                        "firebase_token_data," +
                        "name," +
                        "description," +
                        "image," +
                        "status)" +
                        " VALUES(" +
                        "'" + firebase_token_data + "'," +
                        "'" + name + "'," +
                        "'" + description + "'," +
                        "'" + image + "'," +
                        "'" + status + "'" +
                        ")"
        );
    }

    public int insertPopupWaitingPush(
            String firebase_token_data,
            String name,
            String description,
            String image,
            int status,
            String data) {
        return this.masterDB.insert(
                "INSERT INTO notify_waiting_push(" +
                        "firebase_token_data," +
                        "name," +
                        "description," +
                        "image," +
                        "status," +
                        "data," +
                        "type)" +
                        " VALUES(" +
                        "'" + firebase_token_data + "'," +
                        "'" + name + "'," +
                        "'" + description + "'," +
                        "'" + image + "'," +
                        "'" + status + "'," +
                        parseStringToSql(data) + "," +
                        NotifyDisplayType.POPUP.getId() +
                        ")"
        );
    }

    public boolean setNotifyWaitingPush(int id, int status) {
        return this.masterDB.update(
                "UPDATE notify_waiting_push SET status = " + status +
                        ", modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListNotifyWaitingPush(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM notify_waiting_push" +
                        " WHERE status = " + NotifyWaitingPushStatus.WAITING.getId() +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public List<JSONObject> getListNotifySettingNeetStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM notify_setting" +
                        " WHERE status = " + NotifyStatus.WAITING.getId() +
                        " AND start_date <= NOW()" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public boolean saveStartDate(int id) {
        return this.masterDB.update(
                "UPDATE notify_setting SET start_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListNotifyWaitingScheduleNeedStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM notify_waiting_schedule" +
                        " WHERE status = " + NotifyStatus.WAITING.getId() +
                        " AND start_date <= NOW()" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public boolean setNotifyWaitingSchedulePush(int id, int status) {
        return this.masterDB.update(
                "UPDATE notify_waiting_schedule SET status = " + status +
                        " WHERE id = " + id);
    }
}