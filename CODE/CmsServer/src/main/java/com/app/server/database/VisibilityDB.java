package com.app.server.database;

import com.app.server.enums.SettingStatus;
import com.app.server.enums.SettingType;
import com.app.server.enums.VisibilityDataType;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VisibilityDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    public int insertVisibity(int product_id, int agency_id, int visibility) {
        return this.masterDB.insert(
                "INSERT INTO product_visibility_agency(" +
                        "product_id" + "," +
                        "agency_id" + "," +
                        "visibility" +
                        ") VALUES(" +
                        "'" + product_id + "'," +
                        "'" + agency_id + "'," +
                        "'" + visibility + "'" +
                        ")"
        );
    }

    public JSONObject getVisibilityBySettingId(int id, String visibility_data_type, int visibility_data_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM product_visibility_setting_detail t" +
                        " WHERE t.product_visibility_setting_id = " + id +
                        " AND t.visibility_data_type = '" + visibility_data_type + "'" +
                        " AND t.visibility_data_id = " + visibility_data_id +
                        " AND t.status = " + SettingStatus.RUNNING.getId() + " AND (t.start_date is NULL OR t.start_date <= NOW()) AND (t.end_date is NULL OR t.end_date >= NOW())" +
                        " LIMIT 1"
        );
    }

    public JSONObject getVisibilityByObject(String visibility_object_type, int visibility_object_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM product_visibility_setting t1" +
                        " WHERE t1.visibility_object_type = '" + visibility_object_type + "'" +
                        " AND t1.visibility_object_id = " + visibility_object_id +
                        " AND t1.status = " + SettingStatus.RUNNING.getId() + " AND t1.start_date <= NOW() AND (t1.end_date is NULL OR t1.end_date >= NOW())" +
                        " LIMIT 1"
        );
    }
}