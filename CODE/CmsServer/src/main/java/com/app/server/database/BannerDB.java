package com.app.server.database;

import com.app.server.data.entity.BannerEntity;
import com.app.server.enums.BannerStatus;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BannerDB extends BaseDB {
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

    public JSONObject getBanner(Integer id) {
        return this.masterDB.getOne(
                "SELECT * FROM banner WHERE id=" + id
        );
    }

    public boolean deleteBanner(int id, String note) {
        return this.masterDB.update(
                "UPDATE banner SET status  = -1 WHERE id = " + id
        );
    }

    public int insertBanner(BannerEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO banner(" +
                        "name," +
                        "image," +
                        "description," +
                        "status," +
                        "created_date," +
                        "creator_id," +
                        "modified_date," +
                        "modifier_id," +
                        "start_date," +
                        "end_date," +
                        "agency_ids," +
                        "city_ids," +
                        "region_ids," +
                        "membership_ids," +
                        "setting_type," +
                        "setting_value," +
                        "priority," +
                        "type" +
                        ")" +
                        " VALUES(" +
                        "'" + entity.getName() + "'," +
                        "'" + entity.getImage() + "'," +
                        parseStringToSql(entity.getDescription()) + "," +
                        "'" + entity.getStatus() + "'," +
                        (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        "'" + entity.getCreator_id() + "'," +
                        (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        "'" + entity.getModifier_id() + "'," +
                        (entity.getStart_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getStart_date()) + "'")) + "," +
                        (entity.getEnd_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getEnd_date()) + "'")) + "," +
                        "'" + entity.getAgency_ids() + "'," +
                        "'" + entity.getCity_ids() + "'," +
                        "'" + entity.getRegion_ids() + "'," +
                        "'" + entity.getMembership_ids() + "'," +
                        "'" + entity.getSetting_type() + "'," +
                        "'" + entity.getSetting_value() + "'," +
                        "'" + entity.getPriority() + "'," +
                        "'" + entity.getType() + "'" +
                        ");"
        );
    }

    public boolean updateBanner(BannerEntity entity) {
        return this.masterDB.update(
                "UPDATE banner SET" +
                        " name = " + "'" + entity.getName() + "'," +
                        " image = '" + entity.getImage() + "'," +
                        " description = '" + entity.getDescription() + "'," +
                        " status = '" + entity.getStatus() + "'," +
                        " created_date = " + (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        " creator_id = '" + entity.getCreator_id() + "'," +
                        " modified_date = " + (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        " modifier_id = " + (entity.getModifier_id() == null ? "NULL" : ("'" + entity.getModifier_id() + "'")) + "," +
                        " start_date = " + (entity.getStart_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getStart_date()) + "'")) + "," +
                        " end_date = " + (entity.getEnd_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getEnd_date()) + "'")) + "," +
                        " agency_ids = '" + entity.getAgency_ids() + "'," +
                        " city_ids = '" + entity.getCity_ids() + "'," +
                        " region_ids = '" + entity.getRegion_ids() + "'," +
                        " membership_ids = '" + entity.getMembership_ids() + "'," +
                        " setting_type = '" + entity.getSetting_type() + "'," +
                        " setting_value = '" + entity.getSetting_value() + "'," +
                        " priority = '" + entity.getPriority() + "'," +
                        " type = '" + entity.getType() + "'" +
                        " WHERE id = " + entity.getId()
        );
    }

    public BannerEntity getBannerEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM banner WHERE id = " + id
        );
        if (rs != null) {
            return BannerEntity.from(rs);
        }

        return null;
    }

    public List<JSONObject> getListBannerPriority() {
        return this.masterDB.find(
                "SELECT id FROM banner WHERE priority != 0 ORDER BY priority ASC"
        );
    }

    public boolean removePriority(int id) {
        return this.masterDB.update(
                "UPDATE banner SET priority = 0" +
                        ", end_date = NOW()" +
                        ", status = " + BannerStatus.PENDING.getId() +
                        " WHERE id = " + id
        );
    }

    public boolean setPriority(int id, int priority) {
        return this.masterDB.update(
                "UPDATE banner SET priority = " + priority + " WHERE id = " + id
        );
    }

    public boolean activateBanner(int banner_id, int priority) {
        return this.masterDB.update(
                "UPDATE banner SET priority = " + priority +
                        ", status = " + BannerStatus.ACTIVATED.getId() +
                        ", start_date = NOW() " +
                        ", modified_date = NOW() " +
                        " WHERE id = " + banner_id
        );
    }

    public List<JSONObject> getListBannerNeedStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM banner" +
                        " WHERE status = " + BannerStatus.WAITING.getId() +
                        " AND priority != 0" +
                        " AND start_date <= NOW()" +
                        " AND (end_date IS NULL OR end_date > NOW())" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public List<JSONObject> getListBannerNeedStop(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM banner" +
                        " WHERE status = " + BannerStatus.ACTIVATED.getId() +
                        " AND end_date IS NOT NULL" +
                        " AND end_date <= NOW()" +
                        " LIMIT " + scheduleRunningLimit
        );
    }
}