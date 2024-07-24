package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.staff.MenuData;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.StaffEntity;
import com.app.server.data.entity.StaffGroupPermissionDetailEntity;
import com.app.server.data.entity.StaffGroupPermissionEntity;
import com.app.server.data.entity.StaffManageDataEntity;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.staff.*;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.constants.ResponseMessage;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StaffService extends BaseService {
    /**
     * Staff login
     *
     * @param username
     * @param password
     * @return
     */
    public ClientResponse login(String username, String password) {
        try {
            Staff staff = this.staffDB.getStaffByUsernameAndPassword(username, password);
            if (staff == null) { // username or password incorrect
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USERNAME_PASS_INCORRECT);
            }
            if (staff.getStatus() != StaffStatus.ACTIVATED.getValue()) { // account inactive
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ACCOUNT_STOP);
            }

            String token = this.appUtils.genLoginToken(
                    staff.getId(), staff.getFull_name());

            JSONObject group = this.staffDB.getGroupPermissionInfo(staff.getStaff_group_permission_id());
            if (group == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int full_permission = ConvertUtils.toInt(group.get("full_permission"));
            int group_status = ConvertUtils.toInt(group.get("status"));

            List<JSONObject> detailList = this.staffDB.getAllActionByGroup(
                    staff.getStaff_group_permission_id());
            Map<Integer, Integer> mpAction = new HashMap<>();
            for (JSONObject detail : detailList) {
                mpAction.put(
                        ConvertUtils.toInt(detail.get("cms_action_id")),
                        group_status != PermissionStatus.ACTIVATED.getId() ?
                                0 : ConvertUtils.toInt(detail.get("allow")));
            }

            List<JSONObject> menuList = new ArrayList<>();
            for (MenuData menuData : this.dataManager.getStaffManager().getMpMenu().values()) {
                JSONObject menu = new JSONObject();
                menu.put("id", menuData.getId());
                menu.put("name", menuData.getName());
                menu.put("code", menuData.getCode());
                menu.put("level", menuData.getLevel());
                menu.put("parent_id", menuData.getParent_id());
                menu.put("allow", 0);
                List<JSONObject> subMenuList = new ArrayList<>();
                for (MenuData subMenuData : menuData.getChildren().values()) {
                    JSONObject subMenu = new JSONObject();
                    subMenu.put("id", subMenuData.getId());
                    subMenu.put("name", subMenuData.getName());
                    subMenu.put("code", subMenuData.getCode());
                    subMenu.put("level", subMenuData.getLevel());
                    subMenu.put("parent_id", subMenuData.getParent_id());
                    subMenu.put("allow", 0);
                    List<JSONObject> actionList = new ArrayList<>();
                    for (MenuData actionData : subMenuData.getChildren().values()) {
                        JSONObject action = new JSONObject();
                        action.put("id", actionData.getId());
                        action.put("name", actionData.getName());
                        action.put("code", actionData.getCode());
                        action.put("level", actionData.getLevel());
                        action.put("parent_id", actionData.getParent_id());
                        action.put("type", actionData.getType());
                        action.put("allow",
                                full_permission == YesNoStatus.YES.getValue() ? 1 :
                                        mpAction.get(actionData.getId()) == null ? 0 :
                                                mpAction.get(actionData.getId()));
                        action.put("priority", actionData.getPriority());
                        actionList.add(action);
                    }
                    subMenu.put("children", actionList);
                    subMenuList.add(subMenu);
                }

                menu.put("children", subMenuList);
                menuList.add(menu);
            }

            boolean rsForceUpdateStatus = this.staffDB.forceUpdateStatus(staff.getId(), 0);

            JSONObject data = new JSONObject();
            data.put("token", token);
            data.put("menus", menuList);
            data.put("full_permission", full_permission);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAllMenuInfo(SessionData sessionData) {
        try {
            List<JSONObject> menuList = this.staffDB.getAllMenu();

            JSONObject data = new JSONObject();
            data.put("records", menuList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createGroupPermission(
            SessionData sessionData,
            CreateGroupPermissionRequest request) {
        try {
            StaffGroupPermissionEntity staffGroupPermissionEntity = new StaffGroupPermissionEntity();
            staffGroupPermissionEntity.setName(request.getName());
            staffGroupPermissionEntity.setCreator_id(sessionData.getId());
            staffGroupPermissionEntity.setCreated_date(DateTimeUtils.getNow());
            staffGroupPermissionEntity.setStatus(PermissionStatus.WAITING.getId());
            int rsInsert = this.staffDB.insertStaffGroupPermission(staffGroupPermissionEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            staffGroupPermissionEntity.setId(rsInsert);

            for (MenuRequest menuRequest : request.getMenus()) {
                for (MenuRequest subMenuRequest : menuRequest.getChildren()) {
                    for (MenuRequest actionRequest : subMenuRequest.getChildren()) {
                        StaffGroupPermissionDetailEntity staffGroupPermissionDetailEntity =
                                new StaffGroupPermissionDetailEntity();
                        staffGroupPermissionDetailEntity.setStaff_group_permission_id(staffGroupPermissionEntity.getId());
                        staffGroupPermissionDetailEntity.setCms_action_id(
                                actionRequest.getId());
                        staffGroupPermissionDetailEntity.setAllow(actionRequest.getAllow());

                        int rsInsertDetail = this.staffDB.insertStaffGroupPermissionDetail(staffGroupPermissionDetailEntity);
                        if (rsInsertDetail <= 0) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        staffGroupPermissionDetailEntity.setId(rsInsertDetail);
                    }
                }
            }
            return ClientResponse.success(staffGroupPermissionEntity.getId());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editGroupPermission(
            SessionData sessionData,
            EditGroupPermissionRequest request) {
        try {
            StaffGroupPermissionEntity oldGroup = this.staffDB.getGroupPermissionEntity(request.getId());
            if (oldGroup == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
            }

            if (oldGroup.getFull_permission() == YesNoStatus.YES.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_IS_FULL);
            }

            StaffGroupPermissionEntity staffGroupPermissionEntity = new StaffGroupPermissionEntity();
            staffGroupPermissionEntity.setId(oldGroup.getId());
            staffGroupPermissionEntity.setName(request.getName());
            staffGroupPermissionEntity.setCreator_id(oldGroup.getCreator_id());
            staffGroupPermissionEntity.setCreated_date(oldGroup.getCreated_date());
            staffGroupPermissionEntity.setModifier_id(sessionData.getId());
            staffGroupPermissionEntity.setModified_date(DateTimeUtils.getNow());
            staffGroupPermissionEntity.setStatus(oldGroup.getStatus());
            boolean rsUpdate = this.staffDB.updateStaffGroupPermission(staffGroupPermissionEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (MenuRequest menuRequest : request.getMenus()) {
                for (MenuRequest subMenuRequest : menuRequest.getChildren()) {
                    for (MenuRequest actionRequest : subMenuRequest.getChildren()) {
                        JSONObject oldAction =
                                this.staffDB.getActionDetailByActionIdAndGroupId(
                                        actionRequest.getId(),
                                        request.getId()
                                );
                        StaffGroupPermissionDetailEntity
                                staffGroupPermissionDetailEntity =
                                new StaffGroupPermissionDetailEntity();
                        if (oldAction != null) {
                            staffGroupPermissionDetailEntity.setId(
                                    ConvertUtils.toInt(oldAction.get("id")));
                            staffGroupPermissionDetailEntity.setStaff_group_permission_id(
                                    staffGroupPermissionEntity.getId());
                            staffGroupPermissionDetailEntity.setCms_action_id(
                                    actionRequest.getId());
                            staffGroupPermissionDetailEntity.setAllow(actionRequest.getAllow());
                            boolean rsUpdateDetail = this.staffDB.updateStaffGroupPermissionDetail(
                                    staffGroupPermissionDetailEntity);
                            if (!rsUpdateDetail) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        } else {
                            staffGroupPermissionDetailEntity.setStaff_group_permission_id(
                                    staffGroupPermissionEntity.getId());
                            staffGroupPermissionDetailEntity.setCms_action_id(
                                    actionRequest.getId());
                            staffGroupPermissionDetailEntity.setAllow(actionRequest.getAllow());
                            int rsInsertDetail = this.staffDB.insertStaffGroupPermissionDetail(staffGroupPermissionDetailEntity);
                            if (rsInsertDetail <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                    }
                }
            }
            return ClientResponse.success(staffGroupPermissionEntity.getId());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getGroupPermissionDetail(
            SessionData sessionData,
            BasicRequest request) {
        try {

            JSONObject group = this.staffDB.getGroupPermissionInfo(request.getId());
            if (group == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
            }

            int full_permission = ConvertUtils.toInt(group.get("full_permission"));

            List<JSONObject> detailList = this.staffDB.getAllActionByGroup(request.getId());
            Map<Integer, Integer> mpAction = new HashMap<>();
            for (JSONObject detail : detailList) {
                mpAction.put(
                        ConvertUtils.toInt(detail.get("cms_action_id")),
                        ConvertUtils.toInt(detail.get("allow")));
            }

            List<JSONObject> menuList = new ArrayList<>();
            for (MenuData menuData : this.dataManager.getStaffManager().getMpMenu().values()) {
                JSONObject menu = new JSONObject();
                menu.put("id", menuData.getId());
                menu.put("name", menuData.getName());
                menu.put("code", menuData.getCode());
                menu.put("level", menuData.getLevel());
                menu.put("parent_id", menuData.getParent_id());
                menu.put("allow", 0);
                List<JSONObject> subMenuList = new ArrayList<>();
                for (MenuData subMenuData : menuData.getChildren().values()) {
                    JSONObject subMenu = new JSONObject();
                    subMenu.put("id", subMenuData.getId());
                    subMenu.put("name", subMenuData.getName());
                    subMenu.put("code", subMenuData.getCode());
                    subMenu.put("level", subMenuData.getLevel());
                    subMenu.put("parent_id", subMenuData.getParent_id());
                    subMenu.put("allow", 0);
                    List<JSONObject> actionList = new ArrayList<>();
                    for (MenuData actionData : subMenuData.getChildren().values()) {
                        JSONObject action = new JSONObject();
                        action.put("id", actionData.getId());
                        action.put("name", actionData.getName());
                        action.put("code", actionData.getCode());
                        action.put("level", actionData.getLevel());
                        action.put("parent_id", actionData.getParent_id());
                        action.put("priority", actionData.getPriority());
                        action.put("allow",
                                full_permission == YesNoStatus.YES.getValue() ? 1 :
                                        mpAction.get(actionData.getId()) == null ? 0 :
                                                mpAction.get(actionData.getId()));
                        actionList.add(action);
                    }
                    subMenu.put("children", actionList);
                    subMenuList.add(subMenu);
                }

                menu.put("children", subMenuList);
                menuList.add(menu);
            }

            JSONObject data = new JSONObject();
            group.put("menu_list", menuList);
            data.put("record", group);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListStaff(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_STAFF,
                    request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.staffDB.filter(
                    query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());

            for (JSONObject record : records) {
                record.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(record.get("creator_id"))
                ));

                record.put("permission_info", this.staffDB.getGroupPermissionInfo(
                        ConvertUtils.toInt(record.get("staff_group_permission_id"))
                ));
            }

            int total = this.agencyDB.getTotalAgency(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createStaff(SessionData sessionData, CreateStaffRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (this.staffDB.getStaffByUsername(request.getUsername()) != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USERNAME_NOT_AVAILABLE);
            }

            StaffEntity staffEntity = this.createStaffEntity(request);
            staffEntity.setCreator_id(sessionData.getId());
            staffEntity.setCreated_date(DateTimeUtils.getNow());
            staffEntity.setCode(this.generateStaffCode());
            staffEntity.setPassword(DigestUtils.md5Hex(request.getPassword()));
            int rsInsert = this.staffDB.insertStaff(staffEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            staffEntity.setId(rsInsert);
            /**
             * Lưu phân quyền dữ liệu
             */
            StaffManageDataEntity staffManageDataEntity = new StaffManageDataEntity();
            staffManageDataEntity.setStaff_id(staffEntity.getId());
            staffManageDataEntity.setAgency_data(JsonUtils.Serialize(request.getAgencyData()));
            staffManageDataEntity.setOrder_data(JsonUtils.Serialize(request.getOrderData()));
            int rsInsertData = this.staffDB.insertStaffManageData(staffManageDataEntity);
            if (rsInsertData <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            JSONObject data = new JSONObject();
            data.put("id", staffEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editStaff(SessionData sessionData, EditStaffRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            StaffEntity oldStaff = this.staffDB.getStaffEntity(request.getId());
            if (oldStaff == null || oldStaff.getIs_account_system() == 1) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
            }

            StaffEntity staffEntity = this.createStaffEntity(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(request), CreateStaffRequest.class));
            staffEntity.setId(oldStaff.getId());
            staffEntity.setCode(oldStaff.getCode());
            staffEntity.setCreator_id(oldStaff.getCreator_id());
            staffEntity.setCreated_date(oldStaff.getCreated_date());
            staffEntity.setModified_date(DateTimeUtils.getNow());
            staffEntity.setModifier_id(sessionData.getId());
            staffEntity.setStatus(oldStaff.getStatus());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staffEntity.setPassword(DigestUtils.md5Hex(request.getPassword()));
            } else {
                staffEntity.setPassword(oldStaff.getPassword());
            }
            boolean rsUpdate = this.staffDB.updateStaff(staffEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            /**
             * Lưu phân quyền dữ liệu
             */
            StaffManageDataEntity staffManageDataEntity = new StaffManageDataEntity();
            JSONObject oldStaffManageData = this.staffDB.getStaffManageData(request.getId());
            if (oldStaffManageData != null) {
                staffManageDataEntity.setId(ConvertUtils.toInt(oldStaffManageData.get("id")));
                staffManageDataEntity.setStaff_id(staffEntity.getId());
                staffManageDataEntity.setAgency_data(JsonUtils.Serialize(request.getAgencyData()));
                staffManageDataEntity.setOrder_data(JsonUtils.Serialize(request.getOrderData()));
                boolean rsUpdateData = this.staffDB.updateStaffManageData(staffManageDataEntity);
                if (!rsUpdateData) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            } else {
                staffManageDataEntity = new StaffManageDataEntity();
                staffManageDataEntity.setStaff_id(staffEntity.getId());
                staffManageDataEntity.setAgency_data(JsonUtils.Serialize(request.getAgencyData()));
                staffManageDataEntity.setOrder_data(JsonUtils.Serialize(request.getOrderData()));
                int rsInsert = this.staffDB.insertStaffManageData(staffManageDataEntity);
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }


            this.staffDB.forceUpdateStatus(request.getId(), 1);

            JSONObject data = new JSONObject();
            data.put("id", staffEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private StaffEntity createStaffEntity(CreateStaffRequest request) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(request), StaffEntity.class);
    }

    private String generateStaffCode() {
        int count = this.staffDB.countStaff();
        return "NV" + String.format("%04d", count++);
    }

    public ClientResponse filterGroupPermission(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILER_GROUP_PERMISSION,
                    request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.staffDB.filter(
                    query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject record : records) {
                record.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(record.get("creator_id"))
                ));
            }
            int total = this.agencyDB.getTotalAgency(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activateGroupPermission(SessionData sessionData, ActivateBannerRequest request) {
        try {
            for (int iGroup = 0; iGroup < request.getIds().size(); iGroup++) {
                int id = request.getIds().get(iGroup);
                JSONObject group = this.staffDB.getGroupPermissionInfo(id);
                if (group == null) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                int status = ConvertUtils.toInt(group.get("status"));
                if (PermissionStatus.ACTIVATED.getId() == status) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                if (ConvertUtils.toInt(group.get("full_permission")) == YesNoStatus.YES.getValue()) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                boolean rsActivate = this.staffDB.activateGroupPermission(id,
                        sessionData.getId());
                if (!rsActivate) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateGroupPermission(SessionData sessionData, ActivateBannerRequest request) {
        try {
            for (int iGroup = 0; iGroup < request.getIds().size(); iGroup++) {
                int id = request.getIds().get(iGroup);
                JSONObject group = this.staffDB.getGroupPermissionInfo(id);
                if (group == null) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                int status = ConvertUtils.toInt(group.get("status"));
                if (PermissionStatus.PENDING.getId() == status) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                if (ConvertUtils.toInt(group.get("full_permission")) == YesNoStatus.YES.getValue()) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }

                boolean rsDeactivate = this.staffDB.deactivateGroupPermission(
                        id,
                        sessionData.getId());
                if (!rsDeactivate) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    clientResponse.setMessage("[Thứ " + (iGroup + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getStaffInfo(BasicRequest request) {
        try {
            JSONObject staff = this.staffDB.getStaffInfo(request.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
            }
            staff.put("password", null);
            JSONObject staff_manage_data = this.staffDB.getStaffManageData(request.getId());
            JSONObject agencyData = this.convertAgencyData(staff_manage_data);
            staff.put("agency_data", agencyData);
            JSONObject orderData = this.convertOrderData(staff_manage_data);
            staff.put("orderData", orderData);
            JSONObject data = new JSONObject();
            data.put("record", staff);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject convertOrderData(JSONObject staff_manage_data) {
        JSONObject order_data = new JSONObject();
        if (staff_manage_data == null || staff_manage_data.get("order_data") == null) {
            return order_data;
        }

        JSONObject jsOrderData = JsonUtils.DeSerialize(
                staff_manage_data.get("order_data").toString(), JSONObject.class
        );

        String status = ConvertUtils.toString(jsOrderData.get("status"));
        List<String> statusList = this.appUtils.convertStringToArray(status);
        order_data.put("status_list", statusList);

        return order_data;
    }

    private JSONObject convertAgencyData(JSONObject staff_manage_data) {
        JSONObject agency_data = new JSONObject();
        if (staff_manage_data == null || staff_manage_data.get("agency_data") == null) {
            return agency_data;
        }

        JSONObject jsAgencyData = JsonUtils.DeSerialize(staff_manage_data.get("agency_data").toString(), JSONObject.class
        );
        int can_action_agency_lock = ConvertUtils.toInt(jsAgencyData.get("can_action_agency_lock"));
        agency_data.put("can_action_agency_lock", can_action_agency_lock);
        String type = ConvertUtils.toString(jsAgencyData.get("type"));
        agency_data.put("type", type);
        if (!type.equals("ALL")) {
            String agency_ids = ConvertUtils.toString(jsAgencyData.get("agency_ids"));
            List<String> agencyIdList = this.appUtils.convertStringToArray(agency_ids);
            List<AgencyBasicData> agencyList = new ArrayList<>();
            for (String agencyId : agencyIdList) {
                agencyList.add(
                        this.dataManager.getAgencyManager()
                                .getAgencyBasicData(
                                        ConvertUtils.toInt(agencyId)
                                ));
            }
            agency_data.put("agency_list", agencyList);

            String str_city_ids = ConvertUtils.toString(jsAgencyData.get("city_ids"));
            List<String> cityIdList = this.appUtils.convertStringToArray(str_city_ids);
            agency_data.put("city_list", cityIdList);

            String region_ids = ConvertUtils.toString(jsAgencyData.get("region_ids"));
            List<String> regionIdList = this.appUtils.convertStringToArray(region_ids);
            agency_data.put("region_list", regionIdList);

            String membership_ids = ConvertUtils.toString(jsAgencyData.get("membership_ids"));
            List<String> membershipIdList = this.appUtils.convertStringToArray(membership_ids);
            agency_data.put("membership_list", membershipIdList);

            String business_department_ids = ConvertUtils.toString(jsAgencyData.get("business_department_ids"));
            List<String> businessDepartmentIdList = this.appUtils.convertStringToArray(business_department_ids);
            agency_data.put("business_department_list", businessDepartmentIdList);

            String agency_ignore_ids = ConvertUtils.toString(jsAgencyData.get("agency_ignore_ids"));
            List<String> agencyIgnoreIdList = this.appUtils.convertStringToArray(agency_ignore_ids);
            List<AgencyBasicData> agencyIgnoreList = new ArrayList<>();
            for (String agencyId : agencyIgnoreIdList) {
                agencyIgnoreList.add(
                        this.dataManager.getAgencyManager()
                                .getAgencyBasicData(
                                        ConvertUtils.toInt(agencyId)
                                ));
            }
            agency_data.put("agency_ignore_list", agencyIgnoreList);
        }
        agency_data.put("status", this.convertAgencyStatus(jsAgencyData.get("status").toString()));
        return agency_data;
    }

    private StaffStatusData convertAgencyStatus(String status_data) {
        try {
            if (status_data == null || status_data.equals("[]")) {
                StaffStatusData staffStatusData = new StaffStatusData();
                staffStatusData.setType("ALL");
                return staffStatusData;
            }

            return JsonUtils.DeSerialize(status_data, StaffStatusData.class);
        } catch (Exception e) {
            LogUtil.printDebug(Module.STAFF.name(), e);
        }
        return null;
    }

    public ClientResponse activateStaff(SessionData sessionData, ActivateBannerRequest request) {
        try {
            for (int iStaff = 0; iStaff < request.getIds().size(); iStaff++) {
                ClientResponse clientResponse = this.activateStaffOne(
                        request.getIds().get(iStaff),
                        sessionData.getId());
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[Thứ " + (iStaff + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activateStaffOne(int staff_id, int modifier_id) {
        try {
            if (staff_id == 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
            }

            StaffEntity staffEntity = this.staffDB.getStaffEntity(staff_id);
            if (staffEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
            }

            if (staffEntity.getStatus() == PermissionStatus.ACTIVATED.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsActive = this.staffDB.activateStaff(staff_id, modifier_id);
            if (!rsActive) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateStaff(SessionData sessionData, ActivateBannerRequest request) {
        try {
            for (int iStaff = 0; iStaff < request.getIds().size(); iStaff++) {
                ClientResponse clientResponse = this.deactivateStaffOne(
                        request.getIds().get(iStaff),
                        sessionData.getId());
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[Thứ " + (iStaff + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse deactivateStaffOne(int staff_id, int modifier_id) {
        try {
            if (staff_id == 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
            }

            StaffEntity staffEntity = this.staffDB.getStaffEntity(staff_id);
            if (staffEntity == null || staffEntity.getIs_account_system() == 1) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
            }

            if (staffEntity.getStatus() == PermissionStatus.PENDING.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsDeactivate = this.staffDB.deactivateStaff(staff_id, modifier_id);
            if (!rsDeactivate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getStaffProfile(SessionData sessionData) {
        try {
            StaffEntity staff = this.staffDB.getStaffEntity(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
            }

            StaffGroupPermissionEntity groupPermissionEntity = this.staffDB.getGroupPermissionEntity(
                    staff.getStaff_group_permission_id()
            );

            JSONObject profile = new JSONObject();
            profile.put("full_name", staff.getFull_name());
            profile.put("group_permission", groupPermissionEntity == null ? "" : groupPermissionEntity.getName());
            JSONObject data = new JSONObject();
            data.put("record", profile);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterNotifyCMSHistory(FilterListRequest request) {
        try {
            int total = 1;
            List<JSONObject> records = new ArrayList<>();
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}