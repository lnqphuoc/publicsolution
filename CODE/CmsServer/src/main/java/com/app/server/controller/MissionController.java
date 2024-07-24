package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.AgencyPath;
import com.app.server.constants.path.CTXHPath;
import com.app.server.constants.path.MissionPath;
import com.app.server.data.SessionData;
import com.app.server.data.dto.mission.UpdateMissionConfigRequest;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.agency.AddNewAgencyRequest;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetResultCTTLOfAgencyRequest;
import com.app.server.data.request.cttl.GetResultMissionBXHInfoRequest;
import com.app.server.data.request.damme.GetOrderInfoByCodeRequest;
import com.app.server.data.request.mission.*;
import com.app.server.data.request.promo.StopPromoRequest;
import com.app.server.enums.MissionPeriodType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.service.BaseService;
import com.app.server.service.MissionService;
import com.ygame.framework.common.LogUtil;
import io.swagger.annotations.ApiOperation;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MissionController extends BaseController {

    @RequestMapping(value = MissionPath.FILTER_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse filterMissionGroup(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterMissionGroup(request);
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_BY_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse filterMissionByGroup(
            @RequestBody FilterListByIdRequest request) {
        return this.missionService.filterMissionByGroup(request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_GROUP_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse getMissionGroupInfo(
            BasicRequest request) {
        return this.missionService.getMissionGroupInfo(request);
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse filterMissionBXH(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterMissionBXH(request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_BXH_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse getMissionBXHInfo(
            BasicRequest request) {
        return this.missionService.getMissionBXHInfo(request);
    }

    @RequestMapping(value = MissionPath.CREATE_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse createMissionBXH(
            @RequestBody CreateMissionBXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.createMissionBXH(sessionData, request);
    }

    @RequestMapping(value = MissionPath.EDIT_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse editMissionBXH(
            @RequestBody EditMissionBXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.editMissionBXH(sessionData, request);
    }

    @RequestMapping(value = MissionPath.ACTIVE_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse activeMissionBXH(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.activeMissionBXH(sessionData, request);
    }

    @RequestMapping(value = MissionPath.DOUBLE_CHECK_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kiểm tra trùng bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse doubleCheckMissionBXH(
            @RequestBody CreateMissionBXHRequest request) {
        return this.missionService.doubleCheckMissionBXH(request);
    }

    @RequestMapping(value = MissionPath.CANCEL_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse cancelMissionBXH(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.cancelMissionBXH(sessionData, request);
    }

    /**
     * Thống kê chi tiết của đại lý
     *
     * @param request - btt_id
     *                - agency_id
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_TRANSACTION_MISSION_BXH_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê chi tiết chương trình xếp hạng của Đại lý")
    public ClientResponse filterTransactionMissionBXHByAgency(@RequestBody FilterTransactionMissionBXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterTransactionMissionBXHByAgency(sessionData, request);
    }

    @RequestMapping(value = MissionPath.CREATE_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse createMissionGroup(
            @RequestBody CreateMissionGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.createMissionGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_BXH_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sửa chỉnh sửa bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse filterMissionBXHHistory(
            @RequestBody FilterListByIdRequest request) {
        return this.missionService.filterMissionBXHHistory(request);
    }

    @RequestMapping(value = MissionPath.DELETE_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse deleteMissionBXH(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.deleteMissionBXH(sessionData, request);
    }

    @RequestMapping(value = MissionPath.CANCEL_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse cancelMission(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.cancelMission(sessionData, request);
    }

    @RequestMapping(value = MissionPath.EDIT_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse editMissionGroup(
            @RequestBody EditMissionGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.editMissionGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.CREATE_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse createMissionSetting(
            @RequestBody CreateMissionSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.createMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.EDIT_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse editMissionSetting(
            @RequestBody EditMissionSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.editMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_SETTING_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thiết lập nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse getMissionSettingInfo(
            BasicRequest request) {
        return this.missionService.getMissionSettingInfo(request);
    }

    @RequestMapping(value = MissionPath.ACTIVE_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse activeMissionSetting(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.activeMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.STOP_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kết thúc thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse stopMissionSetting(
            @RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.stopMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.CANCEL_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse cancelMissionSetting(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.cancelMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.DELETE_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa thiết lập giao nhiệm vụ cho đại lý", notes = "")
    @ResponseBody
    public ClientResponse deleteMissionSetting(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.deleteMissionSetting(sessionData, request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_BXH_HISTORY_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết lịch sử bảng thành tích", notes = "")
    @ResponseBody
    public ClientResponse getMissionBXHHistoryInfo(
            BasicRequest request) {
        return this.missionService.getMissionBXHHistoryInfo(request);
    }

    @RequestMapping(value = MissionPath.SEARCH_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tìm kiếm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse searchMission(
            @RequestBody FilterListRequest request) {
        return this.missionService.searchMission(request);
    }

    @RequestMapping(value = MissionPath.FILTER_TK_MISSION_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thống kê danh sách đại lý", notes = "")
    @ResponseBody
    public ClientResponse filterTKMissionAgency(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterTKMissionAgency(request);
    }

    @RequestMapping(value = MissionPath.SEARCH_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tìm kiếm nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse searchMissionGroup(
            @RequestBody FilterListRequest request) {
        return this.missionService.searchMissionGroup(request);
    }

    @RequestMapping(value = MissionPath.FILTER_TK_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thống kê danh sách nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse filterTKMission(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterTKMission(request);
    }

    @RequestMapping(value = MissionPath.FILTER_TK_MISSION_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thống kê chi tiết thực hiện nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse filterTKMissionTransaction(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterTKMissionTransaction(request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_AGENCY_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết nhiệm vụ của đại lý", notes = "")
    @ResponseBody
    public ClientResponse getMissionAgencyInfo(
            BasicRequest request) {
        return this.missionService.getMissionAgencyInfo(request);
    }

    @RequestMapping(value = MissionPath.ADD_MISSION_TO_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm nhiệm vụ vào nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse addMissionToGroup(
            @RequestBody AddMissionToGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.addMissionToGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.EDIT_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse editMission(
            @RequestBody AddMissionToGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.editMission(sessionData, request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_CONFIG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập các giá trị cho nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse getMissionConfig() {
        return this.missionService.getMissionConfig();
    }

    @RequestMapping(value = MissionPath.UPDATE_MISSION_CONFIG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh thiết lập giá trị cho nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse editMission(
            @RequestBody UpdateMissionConfigRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.updateMissionConfig(sessionData, request);
    }

    @RequestMapping(value = MissionPath.DOUBLE_CHECK_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kiểm tra trùng bảng thiết lập giao nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse doubleCheckMissionSetting(
            @RequestBody CreateMissionSettingRequest request) {
        return this.missionService.doubleCheckMissionSetting(request);
    }

    /**
     * Danh sách nhiệm vụ theo tuần
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_MISSION_TUAN_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách nhiệm vụ theo tuần của Đại lý")
    public ClientResponse filterMissionTuanByAgency(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionTuanByAgency(sessionData, request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse getMissionInfo(
            BasicRequest request) {
        return this.missionService.getMissionInfo(request);
    }

    /**
     * Danh sách nhiệm vụ theo tháng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_MISSION_THANG_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách nhiệm vụ theo tháng của Đại lý")
    public ClientResponse filterMissionThangByAgency(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionThangByAgency(sessionData, request);
    }

    /**
     * Danh sách nhiệm vụ theo quý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_MISSION_QUY_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách nhiệm vụ theo quý của Đại lý")
    public ClientResponse filterMissionQuyByAgency(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionQuyByAgency(sessionData, request);
    }

    /**
     * Danh sách nhiệm vụ loại trừ/đã đổi
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_MISSION_REPLACE_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách nhiệm vụ loại trừ/đã đổi của đại lý")
    public ClientResponse filterMissionReplaceByAgency(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionReplaceByAgency(sessionData, request);
    }

    /**
     * Tăng/giảm số lượng huy hiệu của đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.MODIFY_AGENCY_MISSION_POINT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tăng giảm số lượng huy hiệu của đại lý")
    public ClientResponse modifyMissionPoint(@RequestBody ModifyAgencyMissionPointRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.modifyAgencyMissionPoint(sessionData, request);
    }

    /**
     * Thống kê bảng thành tích - Danh sách đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_TK_MISSION_BXH_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê bảng thành tích - Danh sách đại lý")
    public ClientResponse filterTKMissionBXHAgency(@RequestBody FilterTKAgencyMissionBXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterTKMissionBXHAgency(sessionData, request);
    }

    /**
     * Kết quả tổng bảng thành tích
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.GET_RESULT_MISSION_BXH_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Kết quả tổng bảng thành tích: tổng tham gia, tổng tích lũy")
    public ClientResponse getTKMissionBXHInfo(GetResultMissionBXHInfoRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.getResultMissionBXHInfo(sessionData, request);
    }

    /**
     * test
     *
     * @return
     */
    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("test")
    public ClientResponse test() {
        return missionService.runTest();
    }

    @RequestMapping(value = MissionPath.GET_MISSION_LIMIT_OFFER_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết nhiệm vụ của đại lý", notes = "")
    @ResponseBody
    public ClientResponse getMissionLimitOfferInfo(
            BasicRequest request) {
        return this.missionService.getMissionLimitOfferInfo(request);
    }

    /**
     * Lịch sử nhiệm vụ
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_AGENCY_MISSION_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê bảng thành tích - Danh sách đại lý")
    public ClientResponse filterAgencyMissionHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterAgencyMissionHistory(sessionData, request);
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách thiết lập giao nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse filterMissionSetting(
            @RequestBody FilterListRequest request) {
        return this.missionService.filterMissionSetting(request);
    }

    @RequestMapping(value = MissionPath.GENERATE_MISSION_MIN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Giao nhiệm vụ cho kỳ ngắn", notes = "")
    @ResponseBody
    public ClientResponse giaoNhiemVuKyNgan() {
        return this.missionService.giaoNhiemVuKyNgan();
    }

    @RequestMapping(value = MissionPath.GENERATE_MISSION_MAX, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Giao nhiệm vụ cho kỳ dài", notes = "")
    @ResponseBody
    public ClientResponse giaoNhiemVuKyDai() {
        return this.missionService.giaoNhiemVuKyDai();
    }

    @RequestMapping(value = MissionPath.RESET_MISSION_POINT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "RESET_MISSION_POINT", notes = "")
    @ResponseBody
    public ClientResponse resetMissionPoint() {
        return this.missionService.callResetMissionPoint();
    }

    @RequestMapping(value = MissionPath.REWARD_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Trả thưởng", notes = "")
    @ResponseBody
    public ClientResponse rewardMission(int id) {
        return this.missionService.callRewardMission(Arrays.asList(id));
    }

    @RequestMapping(value = MissionPath.PUSH_NOTIFY_CLAIM, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thông báo nhận huy hiệu", notes = "")
    @ResponseBody
    public ClientResponse pushNotifyClaim(int id) {
        return this.missionService.thongBaoNhanHuyHieu(
                MissionPeriodType.from(id)
        );
    }

    @RequestMapping(value = MissionPath.GENERATE_MISSION_BY_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Giao nhiệm vụ theo thiết lập", notes = "")
    @ResponseBody
    public ClientResponse giaoNhiemVuTheoThietLap(int id) {
        return this.missionService.giaoNhiemVuTheoThietLap(
                id
        );
    }

    @RequestMapping(value = MissionPath.ACTIVE_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse activeMissionGroup(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.activeMissionGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.STOP_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Dừng nhóm nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse stopMissionGroup(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.stopMissionGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.DOUBLE_CHECK_MISSION_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kiểm tra nhóm nhiệm vụ trước khi dừng", notes = "")
    @ResponseBody
    public ClientResponse doubleCheckMissionGroup(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.doubleCheckMissionGroup(sessionData, request);
    }

    @RequestMapping(value = MissionPath.GET_MISSION_END_TIME, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thời gian kết thúc nhiệm vụ", notes = "")
    @ResponseBody
    public ClientResponse getMissionEndTime(BasicRequest request) {
        return this.missionService.getMissionEndTime(request);
    }

    @RequestMapping(value = MissionPath.FILTER_AGENCY_MISSION_HISTORY_OF_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử nhiệm vụ của đại lý")
    public ClientResponse filterAgencyMissionHistoryOfAgency(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterAgencyMissionHistoryOfAgency(sessionData, request);
    }

    /**
     * Kết quả bảng thành tích của đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.GET_RESULT_MISSION_BXH_INFO_OF_AGENCY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Kết quả bảng thành tích của đại lý")
    public ClientResponse getResultMissionBXHInfoOfAgency(GetResultMissionBXHOfAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.getResultMissionBXHInfoOfAgency(sessionData, request);
    }

    @RequestMapping(value = MissionPath.RESET_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "RESET_MISSION_BXH", notes = "")
    @ResponseBody
    public ClientResponse resetMissionBXH() {
        return this.missionService.runResetMissionBXH();
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_BXH_REWARD_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử trả thưởng bảng thành tích")
    public ClientResponse filterMissionBXHRewardHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionBXHRewardHistory(sessionData, request);
    }

    /**
     * Lịch sử huy hiệu
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_AGENCY_MISSION_POINT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử huy hiệu")
    public ClientResponse filterAgencyMissionPointHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterAgencyMissionPointHistory(sessionData, request);
    }

    /**
     * Lịch sử huy hiệu
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_AGENCY_MISSION_ACHIEVEMENT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử nhận mức ưu đãi")
    public ClientResponse filterAgencyMissionAchievementHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterAgencyMissionAchievementHistory(sessionData, request);
    }

    /**
     * Lịch sử huy hiệu
     *
     * @param request
     * @return
     */
    @RequestMapping(value = MissionPath.FILTER_AGENCY_MISSION_ACHIEVEMENT_HISTORY_OF_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử nhận mức ưu đãi của đại lý")
    public ClientResponse filterAgencyMissionAchievementHistoryOfAgency(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterAgencyMissionAchievementHistoryOfAgency(sessionData, request);
    }

    @RequestMapping(value = MissionPath.RESET_MISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Reset nhiệm vụ tháng")
    public ClientResponse resetMission(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.missionService.resetMission();
    }

    @RequestMapping(value = MissionPath.GET_ORDER_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thông tin tích lũy nhiệm vụ của đơn hàng")
    public ClientResponse getOrderInfo(GetOrderInfoByCodeRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.getOrderInfo(sessionData, request);
    }

    @RequestMapping(value = MissionPath.FILTER_MISSION_BXH_REWARD_HISTORY_OF_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử trả thưởng bảng thành tích của đại lý")
    public ClientResponse filterMissionBXHRewardHistoryOfAgency(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.filterMissionBXHRewardHistoryOfAgency(sessionData, request);
    }

    @RequestMapping(value = MissionPath.REJECT_ACCUMULATE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Gỡ tích lũy đơn hàng")
    public ClientResponse rejectAccumulateOrder(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.rejectAccumulateOrder(sessionData, request);
    }

    @RequestMapping(value = MissionPath.ACCEPT_ACCUMULATE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Bổ sung tích lũy đơn hàng")
    public ClientResponse acceptAccumulateOrder(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.acceptAccumulateOrder(sessionData, request);
    }

    @RequestMapping(value = MissionPath.ACCEPT_ACCUMULATE_CKS, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Bổ sung tích lũy đơn chiết khấu sau")
    public ClientResponse acceptAccumulateCKS(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return deptService.acceptAccumulateCKS(sessionData, request);
    }

    @RequestMapping(value = MissionPath.ON_OFF_REPEAT_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Dừng lặp lại")
    public ClientResponse onOffMissionBXH(
            @RequestBody OnOffMissionBXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return missionService.onOffMissionBXH(sessionData, request);
    }
}