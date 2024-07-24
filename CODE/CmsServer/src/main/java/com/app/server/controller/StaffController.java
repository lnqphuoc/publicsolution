package com.app.server.controller;

import com.app.server.constants.path.StaffPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.staff.*;
import com.app.server.response.ClientResponse;
import com.app.server.service.StaffService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class StaffController extends BaseController {
    private StaffService staffService;

    @Autowired
    public void setStaffService(StaffService staffService) {
        this.staffService = staffService;
    }

    @RequestMapping(value = StaffPath.GET_LIST_STAFF, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getListStaff(
            @RequestBody FilterListRequest request) {
        return this.staffService.getListStaff(request);
    }

    @RequestMapping(value = StaffPath.FILTER_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterGroupPermission(
            @RequestBody FilterListRequest request) {
        return this.staffService.filterGroupPermission(request);
    }

    @RequestMapping(value = StaffPath.ACTIVATE_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse activateGroupPermission(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.activateGroupPermission(sessionData, request);
    }

    @RequestMapping(value = StaffPath.DEACTIVATE_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deactivateGroupPermission(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.deactivateGroupPermission(sessionData, request);
    }

    @RequestMapping(value = StaffPath.GET_MENU_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Thông tin menu", notes = "")
    public ClientResponse getAllMenuInfo() {
        SessionData sessionData = this.getSessionData();
        return this.staffService.getAllMenuInfo(sessionData);
    }

    @RequestMapping(value = StaffPath.CREATE_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Tạo nhóm phân quyền", notes = "")
    public ClientResponse createGroupPermission(
            @RequestBody CreateGroupPermissionRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.createGroupPermission(sessionData, request);
    }

    @RequestMapping(value = StaffPath.EDIT_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chỉnh sửa nhóm phân quyền", notes = "")
    public ClientResponse createGroupPermission(
            @RequestBody EditGroupPermissionRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.editGroupPermission(sessionData, request);
    }

    @RequestMapping(value = StaffPath.GET_GROUP_PERMISSION_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chi tiết nhóm phân quyền", notes = "")
    public ClientResponse getGroupPermissionDetail(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.getGroupPermissionDetail(sessionData, request);
    }

    @RequestMapping(value = StaffPath.CREATE_STAFF, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Tạo nhân viên", notes = "")
    public ClientResponse createStaff(
            @RequestBody CreateStaffRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.createStaff(sessionData, request);
    }

    @RequestMapping(value = StaffPath.EDIT_STAFF, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chỉnh sửa nhân viên", notes = "")
    public ClientResponse editStaff(
            @RequestBody EditStaffRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.editStaff(sessionData, request);
    }

    @RequestMapping(value = StaffPath.GET_STAFF_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Thông tin nhân viên", notes = "")
    public ClientResponse getStaffInfo(
            BasicRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.getStaffInfo(request);
    }

    @RequestMapping(value = StaffPath.ACTIVATE_STAFF, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse activateStaff(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.activateStaff(sessionData, request);
    }

    @RequestMapping(value = StaffPath.DEACTIVATE_STAFF, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deactivateStaff(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.deactivateStaff(sessionData, request);
    }

    @RequestMapping(value = StaffPath.GET_PROFILE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Thông tin cá nhân", notes = "")
    public ClientResponse getStaffProfile(
    ) {
        SessionData sessionData = this.getSessionData();
        return this.staffService.getStaffProfile(sessionData);
    }

    @RequestMapping(value = StaffPath.FILTER_NOTIFY_CMS_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterNotifyCMSHistory(
            @RequestBody FilterListRequest request) {
        return this.staffService.filterNotifyCMSHistory(request);
    }
}