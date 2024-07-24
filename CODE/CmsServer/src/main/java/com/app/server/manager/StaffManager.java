package com.app.server.manager;

import com.app.server.constants.DeptConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.staff.MenuData;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.dto.staff.StaffProfile;
import com.app.server.data.dto.warehouse.WarehouseBasicData;
import com.app.server.data.dto.warehouse.WarehouseTypeData;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.staff.StaffStatusData;
import com.app.server.database.MasterDB;
import com.app.server.database.StaffDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.util.*;

@Service
@Getter
public class StaffManager {
    private Map<Integer, Staff> mpStaff = new LinkedHashMap<>();

    private Map<Integer, MenuData> mpMenu = new HashMap<>();
    private Map<Integer, MenuData> mpSubMenu = new HashMap<>();
    private Map<Integer, MenuData> mpAction = new HashMap<>();

    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private SessionData staff_auto;


    public void loadData() {

        this.reload();

        staff_auto = new SessionData();

        Staff staff = this.getStaff(0);
        if (staff != null) {
            staff_auto.setId(staff.getId());
            staff_auto.setName(staff.getName());
        }
    }

    private void reload() {
        this.loadStaff();
        this.loadMenu();
    }

    private void loadMenu() {
        try {
            mpMenu.clear();
            mpSubMenu.clear();
            mpAction.clear();
            List<JSONObject> rsMenuList = this.masterDB.find(
                    "SELECT * FROM cms_menu WHERE status = 1"
            );
            List<JSONObject> rsSubMenuList = this.masterDB.find(
                    "SELECT * FROM cms_sub_menu WHERE status = 1 ORDER BY priority ASC,id ASC"
            );
            List<JSONObject> rsActionList = this.masterDB.find(
                    "SELECT * FROM cms_action WHERE status = 1 ORDER BY cms_sub_menu_id ASC,priority ASC,id ASC"
            );

            for (JSONObject jsMenu : rsMenuList) {
                MenuData menuData = new MenuData();
                menuData.setId(ConvertUtils.toInt(jsMenu.get("id")));
                menuData.setName(ConvertUtils.toString(jsMenu.get("name")));
                menuData.setCode(ConvertUtils.toString(jsMenu.get("code")));
                menuData.setLevel(1);
                mpMenu.put(menuData.getId(), menuData);
            }

            for (JSONObject jsSubMenu : rsSubMenuList) {
                MenuData subMenuData = new MenuData();
                subMenuData.setId(ConvertUtils.toInt(jsSubMenu.get("id")));
                subMenuData.setName(ConvertUtils.toString(jsSubMenu.get("name")));
                subMenuData.setCode(ConvertUtils.toString(jsSubMenu.get("code")));
                subMenuData.setLevel(2);
                subMenuData.setPriority(ConvertUtils.toInt(jsSubMenu.get("priority")));
                subMenuData.setParent_id(ConvertUtils.toInt(jsSubMenu.get("cms_menu_id")));

                mpSubMenu.put(subMenuData.getId(), subMenuData);
                mpMenu.get(subMenuData.getParent_id()).getChildren().put(subMenuData.getId(), subMenuData);
            }

            for (JSONObject jsAction : rsActionList) {
                MenuData action = new MenuData();
                action.setId(ConvertUtils.toInt(jsAction.get("id")));
                action.setName(ConvertUtils.toString(jsAction.get("name")));
                action.setCode(ConvertUtils.toString(jsAction.get("code")));
                action.setType(ConvertUtils.toString(jsAction.get("type")));
                action.setLevel(3);
                action.setParent_id(ConvertUtils.toInt(jsAction.get("cms_sub_menu_id")));
                action.setPriority(ConvertUtils.toInt(jsAction.get("priority")));
                MenuData subMenuData = mpSubMenu.get(action.getParent_id());
                subMenuData.getChildren().put(action.getId(), action);
                mpMenu.get(subMenuData.getParent_id()).getChildren().put(subMenuData.getId(), subMenuData);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void loadStaff() {
        this.mpStaff.clear();
        List<JSONObject> rs = this.masterDB.find("select * from staff");
        for (JSONObject js : rs) {
            Staff staff = JsonUtils.DeSerialize(JsonUtils.Serialize(js), Staff.class);
            staff.setName(staff.getFull_name());
            mpStaff.put(staff.getId(), staff);
        }
    }

    public Staff getStaff(int staff_id) {
        Staff staff = this.mpStaff.get(staff_id);
        if (staff == null) {
            JSONObject jsonObject = this.masterDB.getOne(
                    "select id,full_name,code, staff_group_permission_id,department_id from staff where id=" + staff_id);
            if (jsonObject != null) {
                staff = JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), Staff.class);
                staff.setName(staff.getFull_name());
                this.mpStaff.put(staff_id, staff);
            }
        }

        return staff;
    }

    public StaffProfile getStaffProfile(int staff_id) {
        Staff staff = this.mpStaff.get(staff_id);
        if (staff == null) {
            JSONObject jsonObject = this.masterDB.getOne(
                    "select id,full_name,code, staff_group_permission_id from staff where id=" + staff_id);
            if (jsonObject != null) {
                staff = JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), Staff.class);
                staff.setName(staff.getFull_name());
                this.mpStaff.put(staff_id, staff);
            }
        }

        if (staff != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(staff), StaffProfile.class);
        }

        return null;
    }

    public String getStaffFullName(int staff_id) {
        Staff staff = this.getStaff(staff_id);
        if (staff != null) {
            return staff.getName();
        }
        return "";
    }

    public SessionData getSessionStaffBot() {
        SessionData sessionData = new SessionData();
        return sessionData;
    }

    public FilterRequest getFilterOrderStatus(int id, String table) {
        JSONObject staff_manage_data = this.masterDB.getOne(
                "SELECT * FROM staff_manage_data WHERE id = " + id
        );
        if (staff_manage_data != null) {
            JSONObject order_data = JsonUtils.DeSerialize(
                    staff_manage_data.get("order_data").toString(), JSONObject.class);
            if (order_data != null) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setKey("CONCAT('%\"," + table + ".status,\"%)");
                filterRequest.setType(TypeFilter.LIKE);
                filterRequest.setValue(order_data.get("status").toString());
                return filterRequest;
            }
        }

        return null;
    }

    public JSONObject getStaffManageData(int staff_id) {
        return this.masterDB.getOne(
                "SELECT * FROM staff_manage_data WHERE staff_id = " + staff_id
        );
    }

    public List<FilterRequest> getFilterOrder(int staff_id) {
        List<FilterRequest> filterRequests = new ArrayList<>();

        JSONObject staff_manage_data = this.getStaffManageData(staff_id);

        if (staff_manage_data != null) {
            JSONObject order_data = JsonUtils.DeSerialize(
                    staff_manage_data.get("order_data").toString(), JSONObject.class);
            if (order_data != null) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setKey("CONCAT('%\"'," + "t" + ".status,'\"%')");
                filterRequest.setType(TypeFilter.LIKE);
                filterRequest.setValue(
                        JsonUtils.Serialize(order_data.get("status")));
                filterRequests.add(filterRequest);
            }
        }

        FilterRequest filterAgency = this.getFilterAgency(staff_manage_data);
        if (filterAgency != null) {
            filterRequests.add(filterAgency);
        }
        return filterRequests;
    }

    public FilterRequest getFilterAgency(JSONObject staff_manage_data) {
        try {
            if (staff_manage_data != null) {
                String query = "";
                JSONObject agency_data = JsonUtils.DeSerialize(
                        staff_manage_data.get("agency_data").toString(), JSONObject.class);
                if (agency_data != null) {
                    if (!"ALL".equals(ConvertUtils.toString(agency_data.get("type")))) {
                        String sql_ignore = "";
                        if (agency_data.get("agency_ignore_ids") != null &&
                                !agency_data.get("agency_ignore_ids").toString().equals("[]")) {
                            String agency_ignore_ids = JsonUtils.Serialize(agency_data.get("agency_ignore_ids"));
                            sql_ignore += "'" + agency_ignore_ids + "' NOT LIKE CONCAT('%\"'," + "t_agency" + ".id,'\"%')";
                        }

                        String sql_filter = "";
                        String agency_ids = "";
                        if (agency_data.get("agency_ids") == null) {
                            agency_ids = "[]";
                        } else {
                            agency_ids = JsonUtils.Serialize(agency_data.get("agency_ids"));
                        }
                        String city_ids = "";
                        if (agency_data.get("city_ids") == null) {
                            city_ids = "[]";
                        } else {
                            city_ids = JsonUtils.Serialize(agency_data.get("city_ids"));
                        }
                        String region_ids = "";
                        if (agency_data.get("region_ids") == null) {
                            region_ids = "[]";
                        } else {
                            region_ids = JsonUtils.Serialize(agency_data.get("region_ids"));
                        }
                        String business_department_ids = "";
                        if (agency_data.get("business_department_ids") == null) {
                            business_department_ids = "[]";
                        } else {
                            business_department_ids = JsonUtils.Serialize(agency_data.get("business_department_ids"));
                        }


                        if (!agency_ids.equals("[]") ||
                                !business_department_ids.equals("[]") ||
                                !region_ids.equals("[]") ||
                                !city_ids.equals("[]")) {

                            if (!agency_ids.equals("[]")) {
                                String agency_sql = "";
                                agency_sql = "'" + agency_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".id,'\"%')";
                                if (!business_department_ids.equals("[]") ||
                                        !region_ids.equals("[]") ||
                                        !city_ids.equals("[]")) {
                                    String sql_region = "";
                                    if (!business_department_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + business_department_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".business_department_id,'\"%')";
                                    }

                                    if (!region_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + region_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".region_id,'\"%')";
                                    }

                                    if (!city_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + city_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".city_id,'\"%')";
                                    }

                                    sql_filter += "( " + agency_sql + " OR (" + sql_region + " )" + ")";
                                } else {
                                    sql_filter = agency_sql;
                                }
                            } else {
                                if (!business_department_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "(" + "t_agency.business_department_id = 0 OR " +
                                            "'" + business_department_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".business_department_id,'\"%'))";
                                }

                                if (!region_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "'" + region_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".region_id,'\"%')";
                                }

                                if (!city_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "'" + city_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".city_id,'\"%')";
                                }

                                sql_filter = "(" + sql_filter + ")";
                            }
                        }

                        if (!sql_filter.isEmpty()) {
                            query += sql_filter;
                        }
                        if (!sql_ignore.isEmpty()) {
                            if (!query.isEmpty()) {
                                query += " AND ";
                            }
                            query += sql_ignore;
                        }
                    }

                    String agency_status = "";
                    if (agency_data.get("status") == null || agency_data.get("status").toString().toString() == "[]") {
                        agency_status = AgencyStatus.getAllToString();
                    } else {
                        StaffStatusData jsStatus = StaffStatusData.from(agency_data.get("status").toString());
                        if (jsStatus != null) {
                            if (jsStatus.getType().equals("ALL")) {
                                agency_status = AgencyStatus.getAllToString();
                            } else {
                                for (String strStatus : jsStatus.getStatus()) {
                                    if (!agency_status.isEmpty()) {
                                        agency_status += ",";
                                    }
                                    agency_status += ConvertUtils.toInt(strStatus);
                                }
                            }
                        }
                    }

                    if (!query.isEmpty()) {
                        query += " AND ";
                    }
                    query += " t_agency.status IN (" + (agency_status.isEmpty() ? "null" : agency_status) + ")";
                }
                if (!query.isEmpty()) {
                    FilterRequest filterRequest = new FilterRequest();
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setKey("");
                    filterRequest.setValue("(" + query + ")");
                    return filterRequest;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return null;
    }

    public FilterRequest getFilterAgencyForListAgency(JSONObject staff_manage_data) {
        try {
            if (staff_manage_data != null) {
                String query = "";
                JSONObject agency_data = JsonUtils.DeSerialize(
                        staff_manage_data.get("agency_data").toString(), JSONObject.class);
                if (agency_data != null) {
                    if (!"ALL".equals(ConvertUtils.toString(agency_data.get("type")))) {
                        String sql_ignore = "";
                        if (agency_data.get("agency_ignore_ids") != null &&
                                !agency_data.get("agency_ignore_ids").toString().equals("[]")) {
                            String agency_ignore_ids = JsonUtils.Serialize(agency_data.get("agency_ignore_ids"));
                            sql_ignore += "'" + agency_ignore_ids + "' NOT LIKE CONCAT('%\"'," + "t_agency" + ".id,'\"%')";
                        }

                        String sql_filter = "";
                        String agency_ids = "";
                        if (agency_data.get("agency_ids") == null) {
                            agency_ids = "[]";
                        } else {
                            agency_ids = JsonUtils.Serialize(agency_data.get("agency_ids"));
                        }
                        String city_ids = "";
                        if (agency_data.get("city_ids") == null) {
                            city_ids = "[]";
                        } else {
                            city_ids = JsonUtils.Serialize(agency_data.get("city_ids"));
                        }
                        String region_ids = "";
                        if (agency_data.get("region_ids") == null) {
                            region_ids = "[]";
                        } else {
                            region_ids = JsonUtils.Serialize(agency_data.get("region_ids"));
                        }
                        String business_department_ids = "";
                        if (agency_data.get("business_department_ids") == null) {
                            business_department_ids = "[]";
                        } else {
                            business_department_ids = JsonUtils.Serialize(agency_data.get("business_department_ids"));
                        }


                        if (!agency_ids.equals("[]") ||
                                !business_department_ids.equals("[]") ||
                                !region_ids.equals("[]") ||
                                !city_ids.equals("[]")) {

                            if (!agency_ids.equals("[]")) {
                                String agency_sql = "";
                                agency_sql = "'" + agency_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".id,'\"%')";
                                if (!business_department_ids.equals("[]") ||
                                        !region_ids.equals("[]") ||
                                        !city_ids.equals("[]")) {
                                    String sql_region = "";
                                    if (!business_department_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + business_department_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".business_department_id,'\"%')";
                                    }

                                    if (!region_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + region_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".region_id,'\"%')";
                                    }

                                    if (!city_ids.equals("[]")) {
                                        if (!sql_region.isEmpty()) {
                                            sql_region += " AND ";
                                        }
                                        sql_region += "'" + city_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".city_id,'\"%')";
                                    }

                                    sql_filter += "( " + agency_sql + " OR (" + sql_region + " )" + ")";
                                } else {
                                    sql_filter = agency_sql;
                                }
                            } else {
                                if (!business_department_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "(t_agency.business_department_id = 0 OR " +
                                            "'" + business_department_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".business_department_id,'\"%'))";
                                }

                                if (!region_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "'" + region_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".region_id,'\"%')";
                                }

                                if (!city_ids.equals("[]")) {
                                    if (!sql_filter.isEmpty()) {
                                        sql_filter += " AND ";
                                    }
                                    sql_filter += "'" + city_ids + "' LIKE CONCAT('%\"'," + "t_agency" + ".city_id,'\"%')";
                                }

                                sql_filter = "(" + sql_filter + ")";
                            }
                        }

                        if (!sql_filter.isEmpty()) {
                            query += sql_filter;
                        }
                        if (!sql_ignore.isEmpty()) {
                            if (!query.isEmpty()) {
                                query += " AND ";
                            }
                            query += sql_ignore;
                        }
                    }

                    String agency_status = "";
                    if (agency_data.get("status") == null || agency_data.get("status").toString().toString() == "[]") {
                        agency_status = AgencyStatus.getAllToString();
                    } else {
                        StaffStatusData jsStatus = StaffStatusData.from(agency_data.get("status").toString());
                        if (jsStatus != null) {
                            if (jsStatus.getType().equals("ALL")) {
                                agency_status = AgencyStatus.getAllToString();
                            } else {
                                for (String strStatus : jsStatus.getStatus()) {
                                    if (!agency_status.isEmpty()) {
                                        agency_status += ",";
                                    }
                                    agency_status += ConvertUtils.toInt(strStatus);
                                }
                            }
                        }
                    }

                    if (query.isEmpty()) {
                        query += "t_agency.status IN (" + (agency_status.isEmpty() ? "null" : agency_status) + ")";
                    } else {
                        query += " AND t_agency.status IN (" + (agency_status.isEmpty() ? "null" : agency_status) + ")";
                    }
                }
                if (!query.isEmpty()) {
                    FilterRequest filterRequest = new FilterRequest();
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue("(" + query + ")");
                    return filterRequest;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return null;
    }

    public boolean checkStaffManageAgency(int staff_id, JSONObject agency) {
        return this.checkStaffManageAgency(
                this.getStaffInfo(staff_id),
                agency
        );
    }

    public boolean checkStaffManageAgency(
            JSONObject staff,
            JSONObject agency) {
        if (staff == null) {
            return false;
        }

        if (ConvertUtils.toInt(staff.get("status")) != StaffStatus.ACTIVATED.getValue()) {
            return false;
        }

        JSONObject staff_manage_data = this.getStaffManageData(
                ConvertUtils.toInt(staff.get("id"))
        );
        if (staff_manage_data == null) {
            return false;
        }

        JSONObject agency_data = JsonUtils.DeSerialize(
                staff_manage_data.get("agency_data").toString(), JSONObject.class);
        if (agency_data == null) {
            return false;
        }

        boolean agency_status = false;
        if (agency_data.get("status") != null &&
                !agency_data.get("status").toString().isEmpty() &&
                !agency_data.get("status").toString().equals("[]")) {
            StaffStatusData staffStatusData = JsonUtils.DeSerialize(
                    agency_data.get("status").toString(),
                    StaffStatusData.class);
            if (staffStatusData.getType().equals("ALL")) {
                agency_status = true;
            } else if (staffStatusData.getType().equals("LIST")) {
                if (!staffStatusData.getStatus().contains(agency.get("status").toString())) {
                    return false;
                }
            } else {
                agency_status = false;
                return false;
            }
        }

        if ("ALL".equals(ConvertUtils.toString(agency_data.get("type")))) {
            return true;
        }

        if (agency_data.get("agency_ignore_ids") != null &&
                !agency_data.get("agency_ignore_ids").toString().equals("[]")) {
            String agency_ignore_ids = JsonUtils.Serialize(agency_data.get("agency_ignore_ids"));
            if (agency_ignore_ids.contains("\"" + ConvertUtils.toInt(agency.get("id")) + "\"")) {
                return false;
            }
        }

        if (agency_data.get("agency_ids") != null &&
                !agency_data.get("agency_ids").toString().equals("[]")) {
            String agency_ids = JsonUtils.Serialize(agency_data.get("agency_ids"));
            if (agency_ids.contains("\"" + ConvertUtils.toInt(agency.get("id")) + "\"")) {
                return true;
            }
        }

        if ((agency_data.get("business_department_ids") != null &&
                !agency_data.get("business_department_ids").toString().equals("[]")) ||
                (agency_data.get("region_ids") != null &&
                        !agency_data.get("region_ids").toString().equals("[]")) ||
                (agency_data.get("city_ids") != null &&
                        !agency_data.get("city_ids").toString().equals("[]"))) {
            if (agency_data.get("business_department_ids") != null &&
                    !agency_data.get("business_department_ids").toString().equals("[]")) {
                String region_ids = JsonUtils.Serialize(agency_data.get("business_department_ids"));
                if (ConvertUtils.toInt(agency.get("business_department_id")) != 0 &&
                        !region_ids.contains("\"" + ConvertUtils.toInt(agency.get("business_department_id")) + "\"")) {
                    return false;
                }
            }

            if (agency_data.get("region_ids") != null &&
                    !agency_data.get("region_ids").toString().equals("[]")) {
                String region_ids = JsonUtils.Serialize(agency_data.get("region_ids"));
                if (!region_ids.contains("\"" + ConvertUtils.toInt(agency.get("region_id")) + "\"")) {
                    return false;
                }
            }

            if (agency_data.get("city_ids") != null &&
                    !agency_data.get("city_ids").toString().equals("[]")) {
                String city_ids = JsonUtils.Serialize(agency_data.get("city_ids"));
                if (!city_ids.contains("\"" + ConvertUtils.toInt(agency.get("city_id")) + "\"")) {
                    return false;
                }
            }
            return true;
        }


        return false;
    }

    public boolean checkManageOrder(int staff_id, JSONObject agency, int order_status) {
        JSONObject staff = this.getStaffInfo(staff_id);
        if (staff == null) {
            return false;
        }
        if (checkGroupFullPermission(
                ConvertUtils.toInt(staff.get("staff_group_permission_id")))) {
            return true;
        }
        JSONObject staff_manage_data = this.getStaffManageData(staff_id);
        if (staff_manage_data == null) {
            return false;
        }

        if (!checkStaffManageAgency(
                staff, agency
        )) {
            return false;
        }

        JSONObject order_data = JsonUtils.DeSerialize(
                staff_manage_data.get("order_data").toString(), JSONObject.class);
        if (order_data != null &&
                order_data.get("status") != null &&
                !JsonUtils.Serialize(order_data.get("status")).contains("\"" + order_status + "\"")) {
            return false;
        }
        return true;
    }

    private JSONObject getStaffInfo(int staff_id) {
        return this.masterDB.getOne("select * from staff where id = " + staff_id);
    }

    public int getStaffSystemId() {
        return 0;
    }

    public boolean checkGroupFullPermission(int staff_group_permission_id) {
        JSONObject staff_group_permission = this.masterDB.getOne(
                "SELECT * FROM staff_group_permission WHERE id = " + staff_group_permission_id
        );
        if (staff_group_permission != null &&
                ConvertUtils.toInt(staff_group_permission.get("full_permission")) == YesNoStatus.YES.getValue()) {
            return true;
        }
        return false;
    }

    public Integer getBussinessDepartment(int id) {
        JSONObject staff = this.getStaffInfo(id);
        if (staff != null) {
            return ConvertUtils.toInt(staff.get("department_id"));
        }
        return null;
    }
}