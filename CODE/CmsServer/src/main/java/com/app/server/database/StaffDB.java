package com.app.server.database;

import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.StaffEntity;
import com.app.server.data.entity.StaffGroupPermissionDetailEntity;
import com.app.server.data.entity.StaffGroupPermissionEntity;
import com.app.server.data.entity.StaffManageDataEntity;
import com.app.server.enums.BannerStatus;
import com.app.server.enums.PermissionStatus;
import com.app.server.enums.StaffStatus;
import com.app.server.enums.task.TaskStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StaffDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    /**
     * Login with username and password
     *
     * @param username
     * @param password
     * @return
     */
    public Staff getStaffByUsernameAndPassword(String username, String password) {
        try {
            String sql = "SELECT * FROM staff" +
                    " WHERE username='" + username + "'" +
                    " AND password='" + password + "'" +
                    " AND is_account_system = 0";
            JSONObject rs = this.masterDB.getOne(sql);
            if (rs != null) {
                return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), Staff.class);
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public List<JSONObject> getAllMenu() {
        return this.masterDB.find(
                "SELECT * FROM cms_menu"
        );
    }

    public List<JSONObject> getAllSubMenu() {
        return this.masterDB.find(
                "SELECT * FROM cms_sub_menu"
        );
    }

    public List<JSONObject> getAllAction() {
        return this.masterDB.find(
                "SELECT * FROM cms_action"
        );
    }

    /**
     * Tạo mới nhóm phân quyền
     *
     * @param entity
     * @return
     */
    public int insertStaffGroupPermission(
            StaffGroupPermissionEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO staff_group_permission(" +
                        "name," +
                        "status," +
                        "created_date," +
                        "creator_id," +
                        "modified_date," +
                        "modifier_id," +
                        "full_permission" +
                        ") VALUES(" +
                        "'" + entity.getName() + "'," +
                        "'" + entity.getStatus() + "'," +
                        (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        "'" + entity.getCreator_id() + "'," +
                        (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        "'" + entity.getModifier_id() + "'," +
                        "'" + entity.getFull_permission() + "'" +
                        ");"
        );
    }

    public boolean updateStaffGroupPermission(StaffGroupPermissionEntity entity) {
        return this.masterDB.update(
                "UPDATE staff_group_permission SET " +
                        " name = '" + entity.getName() + "'," +
                        " status = '" + entity.getStatus() + "'," +
                        " created_date = " + (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        " creator_id = '" + entity.getCreator_id() + "'," +
                        " modified_date = " + (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        " modifier_id = '" + entity.getModifier_id() + "'," +
                        " full_permission = '" + entity.getFull_permission() + "'" +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertStaffGroupPermissionDetail(
            StaffGroupPermissionDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO staff_group_permission_detail(" +
                        "status," +
                        "staff_group_permission_id," +
                        "cms_action_id," +
                        "allow" +
                        ")" +
                        " VALUES(" +
                        "'" + entity.getStatus() + "'," +
                        "'" + entity.getStaff_group_permission_id() + "'," +
                        "'" + entity.getCms_action_id() + "'," +
                        "'" + entity.getAllow() + "'" +
                        ")"
        );
    }

    public JSONObject getGroupPermissionInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM staff_group_permission WHERE id = " + id
        );
    }

    public List<JSONObject> getAllActionByGroup(int id) {
        return this.masterDB.find(
                "SELECT * FROM staff_group_permission_detail" +
                        " WHERE staff_group_permission_id = " + id
        );
    }

    public JSONObject getStaffByUsername(String username) {
        return this.masterDB.getOne(
                "SELECT * FROM staff WHERE username = '" + username + "'"
        );
    }

    public int countStaff() {
        return this.masterDB.getTotal(
                "SELECT * FROM staff"
        );
    }

    public boolean activateGroupPermission(int id, int staff_id) {
        return this.masterDB.update(
                "UPDATE staff_group_permission SET" +
                        " status = " + PermissionStatus.ACTIVATED.getId() +
                        ", modifier_id = " + staff_id +
                        ", modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean deactivateGroupPermission(int id, int staff_id) {
        return this.masterDB.update(
                "UPDATE staff_group_permission SET" +
                        " status = " + PermissionStatus.PENDING.getId() +
                        ", modifier_id = " + staff_id +
                        ", modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getStaffInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM staff WHERE id = " + id +
                        " AND is_account_system = 0"
        );
    }

    public JSONObject getStaffManageData(int staff_id) {
        return this.masterDB.getOne(
                "SELECT * FROM staff_manage_data WHERE staff_id = " + staff_id
        );
    }

    public StaffGroupPermissionEntity getGroupPermissionEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM staff_group_permission WHERE id = " + id
        );
        if (rs != null) {
            return JsonUtils.DeSerialize(
                    JsonUtils.Serialize(rs), StaffGroupPermissionEntity.class
            );
        }

        return null;
    }

    public JSONObject getActionDetailByActionIdAndGroupId(
            int cms_action_id,
            int group_id) {
        return this.masterDB.getOne(
                "SELECT t.id,t.allow,t1.type FROM staff_group_permission_detail t" +
                        " LEFT JOIN cms_action t1 ON t1.id = t.cms_action_id" +
                        " WHERE t.staff_group_permission_id = " + group_id +
                        " AND t.cms_action_id = " + cms_action_id
        );
    }

    public int insertStaffManageData(
            StaffManageDataEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO staff_manage_data(" +
                        "status," +
                        "staff_id," +
                        "agency_data," +
                        "order_data" +
                        ")" +
                        " VALUES(" +
                        "'" + entity.getStatus() + "'," +
                        "'" + entity.getStaff_id() + "'," +
                        "'" + entity.getAgency_data() + "'," +
                        "'" + entity.getOrder_data() + "'" +
                        ")"
        );
    }

    public boolean updateStaffManageData(
            StaffManageDataEntity entity) {
        return this.masterDB.update(
                "UPDATE staff_manage_data SET" +
                        " status = '" + entity.getStatus() + "'," +
                        " staff_id = '" + entity.getStaff_id() + "'," +
                        " agency_data = '" + entity.getAgency_data() + "'," +
                        " order_data = '" + entity.getOrder_data() + "'," +
                        " modified_date = NOW()" +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertStaff(StaffEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO staff(" +
                        "code," +
                        "phone," +
                        "full_name," +
                        "username," +
                        "email," +
                        "password," +
                        "address," +
                        "status," +
                        "created_date," +
                        "creator_id," +
                        "modified_date," +
                        "modifier_id," +
                        "staff_group_permission_id," +
                        "is_account_system" +
                        ")" +
                        " VALUES(" +
                        "'" + entity.getCode() + "'," +
                        "'" + entity.getPhone() + "'," +
                        "'" + entity.getFull_name() + "'," +
                        "'" + entity.getUsername() + "'," +
                        "'" + entity.getEmail() + "'," +
                        "'" + entity.getPassword() + "'," +
                        "'" + entity.getAddress() + "'," +
                        "'" + entity.getStatus() + "'," +
                        (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        "'" + entity.getCreator_id() + "'," +
                        (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        "'" + entity.getModifier_id() + "'," +
                        "'" + entity.getStaff_group_permission_id() + "'," +
                        "'" + entity.getIs_account_system() + "'" +
                        ")"
        );
    }

    public boolean updateStaff(StaffEntity entity) {
        return this.masterDB.update(
                "UPDATE staff SET" +
                        " code = '" + entity.getCode() + "'," +
                        " phone = '" + entity.getPhone() + "'," +
                        " full_name = '" + entity.getFull_name() + "'," +
                        " username = '" + entity.getUsername() + "'," +
                        " email = '" + entity.getEmail() + "'," +
                        " password = '" + entity.getPassword() + "'," +
                        " address = '" + entity.getAddress() + "'," +
                        " status = '" + entity.getStatus() + "'," +
                        " created_date = " + (entity.getCreated_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getCreated_date()) + "'")) + "," +
                        " creator_id = '" + entity.getCreator_id() + "'," +
                        " modified_date = " + (entity.getModified_date() == null ? "NULL" : ("'" + DateTimeUtils.toString(entity.getModified_date()) + "'")) + "," +
                        " modifier_id = '" + entity.getModifier_id() + "'," +
                        " staff_group_permission_id = '" + entity.getStaff_group_permission_id() + "'," +
                        " is_account_system = '" + entity.getIs_account_system() + "'" +
                        " WHERE id = " + entity.getId()
        );
    }

    public StaffEntity getStaffEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM staff WHERE id = " + id
        );

        if (rs != null) {
            return StaffEntity.from(rs);
        }
        return null;
    }

    public JSONObject getCmsApi(String path) {
        return this.masterDB.getOne(
                "SELECT * FROM cms_api WHERE path = '" + path + "' AND status = 1"
        );
    }

    public boolean activateStaff(int staff_id, int modifier_id) {
        return this.masterDB.update(
                "UPDATE staff SET status = " + PermissionStatus.ACTIVATED.getId() +
                        ", modifier_id = " + modifier_id +
                        ", modified_date = NOW()" +
                        ", force_update_status = 1" +
                        " WHERE id = " + staff_id
        );
    }

    public boolean deactivateStaff(int staff_id, int modifier_id) {
        return this.masterDB.update(
                "UPDATE staff SET status = " + PermissionStatus.PENDING.getId() +
                        ", modifier_id = " + modifier_id +
                        ", modified_date = NOW()" +
                        ", force_update_status = 1" +
                        " WHERE id = " + staff_id
        );
    }

    public boolean forceUpdateStatus(int id, int force_update_status) {
        return this.masterDB.update(
                "UPDATE staff SET force_update_status = " + force_update_status +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getStaffManageAgency(int id) {
        return this.masterDB.find(
                "SELECT id, full_name" +
                        " FROM staff"
        );
    }

    public boolean updateStaffGroupPermissionDetail(
            StaffGroupPermissionDetailEntity entity) {
        return this.masterDB.update(
                "UPDATE staff_group_permission_detail SET" +
                        " status = '" + entity.getStatus() + "'," +
                        " staff_group_permission_id = '" + entity.getStaff_group_permission_id() + "'," +
                        " cms_action_id = '" + entity.getCms_action_id() + "'," +
                        " allow = '" + entity.getAllow() + "'," +
                        " modified_date = NOW()" +
                        " WHERE id = " + entity.getId()
        );
    }

    public List<JSONObject> getAllStaffActive() {
        return this.masterDB.find(
                "SELECT * FROM staff WHERE status = " + StaffStatus.ACTIVATED.getValue()
        );
    }

    public JSONObject getTaskInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM task WHERE id = " + id);
    }

    public boolean finishTask(int id) {
        return this.masterDB.update("UPDATE task SET status = " + TaskStatus.DONE.getId() + " WHERE id = " + id);
    }
}