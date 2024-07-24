package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.agency.EditAddressDeliveryRequest;
import com.app.server.data.request.lock.CreateSettingLockAgencyRequest;
import com.app.server.data.request.lock.FilterLockListRequest;
import com.app.server.data.request.lock.SettingLockAgencyRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.LockService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@ApiOperation(value = "Hẹn khóa đại lý")
public class LockController extends BaseController {
    private LockService lockService;

    @Autowired
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    @RequestMapping(value = AgencyPath.CREATE_SETTING_LOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập khóa đại lý", notes = "")
    public ClientResponse createSettingLockAgency(
            @RequestBody CreateSettingLockAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.createSettingLockAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.APPROVE_SETTING_LOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt thiết lập khóa đại lý", notes = "")
    public ClientResponse approveSettingLockAgency(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.approveSettingLockAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.CANCEL_SETTING_LOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy thiết lập khóa đại lý", notes = "")
    public ClientResponse cancelSettingLockAgency(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.cancelSettingLockAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_SETTING_LOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu thiết lập khóa đại lý", notes = "")
    public ClientResponse filterSettingLockAgency(
            @RequestBody FilterLockListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.filterSettingLockAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.GET_SETTING_LOCK_AGENCY_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết phiếu thiết lập khóa đại lý", notes = "")
    public ClientResponse getSettingLockAgencyDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.getSettingLockAgencyDetail(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY_LOCK_DATA, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách cơ cấu khóa đại lý", notes = "")
    public ClientResponse filterAgencyLockData(
            @RequestBody FilterLockListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.filterAgencyLockData(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY_LOCK_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách cơ cấu khóa đại lý", notes = "")
    public ClientResponse filterAgencyLockHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.filterAgencyLockHistory(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.SET_DAY_NUMBER_PUSH_NOTIFY_LOCK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập thời hạn thông báo khóa", notes = "")
    public ClientResponse setDayNumberPushNotifyLock(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.setDayNumberPushNotifyLock(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.RUN_LOCK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập thời hạn thông báo khóa", notes = "")
    public ClientResponse runLock() {
        SessionData sessionData = this.getSessionData();
        return this.lockService.runLockSchedule();
    }

    @RequestMapping(value = AgencyPath.RUN_WARNING_LOCK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập thời hạn thông báo khóa", notes = "")
    public ClientResponse runWarningLock() {
        SessionData sessionData = this.getSessionData();
        return this.lockService.runPushNotifyWarningLock();
    }

    @RequestMapping(value = AgencyPath.STOP_AGENCY_LOCK_DATA, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Dừng lệnh khóa đại lý", notes = "")
    public ClientResponse stopAgencyLockData(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.lockService.stopAgencyLockData(sessionData, request);
    }
}