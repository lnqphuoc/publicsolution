package com.app.server.manager;

import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.dept.DeptAgencyInfo;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.database.AgencyDB;
import com.app.server.database.DeptDB;
import com.app.server.enums.AgencyStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgencyManager {
    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    private DeptDB deptDB;

    @Autowired
    public void setDeptDB(DeptDB deptDB) {
        this.deptDB = deptDB;
    }


    public void loadData() {
        LogUtil.printDebug("AGENCY-LOAD DATA");
        this.loadAllAgency();
        LogUtil.printDebug("AGENCY-LOAD DATA DONE");
    }

    public void loadAllAgency() {
    }

    public AgencyEntity getAgency(int id) {
        return this.agencyDB.getAgencyEntity(id);
    }

    public JSONObject getAgencyInfo(int id) {
        return this.agencyDB.getAgencyInfoById(id);
    }

    public AgencyBasicData getAgencyBasicData(int id) {
        AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(id);
        if (agencyEntity == null) {
            return null;
        }
        AgencyBasicData agencyBasicData = new AgencyBasicData();
        agencyBasicData.initInfo(agencyEntity);
        return agencyBasicData;
    }

    public DeptAgencyInfo getDeptInfo(Integer agency_id) {
        JSONObject jsonObject = this.deptDB.getDeptInfo(agency_id);
        if (jsonObject == null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), DeptAgencyInfo.class);
        }
        return null;
    }

    public void initDeptAgencyInfo() {
    }

    public void test() {
        List<JSONObject> commits = this.deptDB.getListCommitDoing();
        for (JSONObject commit : commits) {
            /**
             * cập nhật sai cam kết
             */
            int agency_id = ConvertUtils.toInt(commit.get("agency_id"));
            LogUtil.printDebug("runMissCommit: agency - " + agency_id);
            boolean rsMissCommit = this.deptDB.increaseMissCommit(agency_id);
            if (!rsMissCommit) {
                /**
                 * Thông báo qua tele
                 */
                String message = "runMissCommit: agency - " + agency_id + " FAILED";
                LogUtil.printDebug(message);
            }
        }
    }
}