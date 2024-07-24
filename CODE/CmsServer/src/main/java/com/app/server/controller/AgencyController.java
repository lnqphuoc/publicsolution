package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.constants.path.DeptPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.*;
import com.app.server.data.request.agency.*;
import com.app.server.data.request.order.ApproveRequestOrderRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.MissionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@CrossOrigin(origins = "*")
public class AgencyController extends BaseController {

    private MissionService missionService;

    @Autowired
    public void setMissionService(MissionService missionService) {
        this.missionService = missionService;
    }

    @RequestMapping(value = AgencyPath.ADD_NEW_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm đại lý mới", notes = "")
    @ResponseBody
    public ClientResponse addNewAgency(
            @RequestBody AddNewAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.addNewAgency(sessionData, request);
    }

    @GetMapping(value = AgencyPath.GET_AGENCY_INFO)
    @ApiOperation(value = "Lấy thông tin của đại lý", notes = "")
    public ClientResponse getAgencyInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.getAgencyInfo(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.EDIT_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chỉnh sửa đại lý", notes = "")
    public ClientResponse editAgency(

            @RequestBody EditAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.editAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.APPROVE_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Duyệt đại lý", notes = "")
    public ClientResponse approveAgency(

            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        ClientResponse clientResponse = this.agencyService.approveAgency(sessionData, request);
        if (clientResponse.success()) {
            this.missionService.giaoNhiemVu(request.getId());
            this.missionService.addAgencyToMissionBXH(request.getId());
        }
        return clientResponse;
    }

    @RequestMapping(value = AgencyPath.UPDATE_STATUS_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Cập nhật trạng thái đại lý", notes = "")
    public ClientResponse updateStatusAgency(

            @RequestBody UpdateStatusRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.updateStatusAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.LOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Khóa đại lý", notes = "")
    public ClientResponse lockAgency(
            @RequestBody UpdateStatusRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.lockAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.UNLOCK_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Mở khóa đại lý", notes = "")
    public ClientResponse unlockAgency(

            @RequestBody UpdateStatusRequest request) {
        ClientResponse clientResponse = this.agencyService.unlockAgency(request);
        if (clientResponse.success()) {
            this.missionService.giaoNhiemVu(request.getId());
            this.missionService.addAgencyToMissionBXH(request.getId());
        }
        return clientResponse;
    }

    @RequestMapping(value = AgencyPath.DEACTIVATE_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Ngưng hoạt động đại lý", notes = "")
    public ClientResponse deactivateAgency(

            @RequestBody UpdateStatusRequest request) {
        return this.agencyService.deactivateAgency(request);
    }

    @RequestMapping(value = AgencyPath.ACTIVE_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Kích hoạt đại lý ngưng hoạt động", notes = "")
    public ClientResponse activeAgency(

            @RequestBody UpdateStatusRequest request) {
        ClientResponse clientResponse = this.agencyService.activeAgency(request);
        if (clientResponse.success()) {
            this.missionService.giaoNhiemVu(request.getId());
            this.missionService.addAgencyToMissionBXH(request.getId());
        }
        return clientResponse;
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách đại lý", notes = "")
    @ResponseBody
    public ClientResponse getListAgency(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.filterAgency(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.GET_LIST_ADDRESS_DELIVERY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sách đại chỉ giao hàng của đại lý", notes = "")
    public ClientResponse getListAddressDelivery(

            BasicRequest request) {
        return this.agencyService.getListAddressDelivery(request);
    }

    @RequestMapping(value = AgencyPath.ADD_ADDRESS_DELIVERY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm địa chỉ giao hàng cho đại lý", notes = "")
    public ClientResponse addAddressDelivery(

            @RequestBody AddAddressDeliveryRequest request) {
        return this.agencyService.addAddressDelivery(request);
    }

    @RequestMapping(value = AgencyPath.GET_ADDRESS_DELIVERY_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy thông tin địa chỉ giao hàng", notes = "")
    public ClientResponse getAddressDeliveryDetail(

            BasicRequest request) {
        return this.agencyService.getAddressDeliveryDetail(request);
    }

    @RequestMapping(value = AgencyPath.EDIT_ADDRESS_DELIVERY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa đỉa chỉ giao hàng", notes = "")
    public ClientResponse editAddressDelivery(

            @RequestBody EditAddressDeliveryRequest request) {
        return this.agencyService.editAddressDelivery(request);
    }

    @RequestMapping(value = AgencyPath.DELETE_ADDRESS_DELIVERY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa đỉa chỉ giao hàng", notes = "")
    public ClientResponse deleteAddressDelivery(

            @RequestBody BasicRequest request) {
        return this.agencyService.deleteAddressDelivery(request);
    }

    @RequestMapping(value = AgencyPath.SET_ADDRESS_DELIVERY_DEFAULT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đặt làm địa chỉ giao hàng mặc định", notes = "")
    public ClientResponse setAddressDeliveryDefault(

            @RequestBody BasicRequest request) {
        return this.agencyService.setAddressDeliveryDefault(request);
    }


    @RequestMapping(value = AgencyPath.GET_LIST_ADDRESS_EXPORT_BILLING, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sách địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse getListAddressExportBilling(

            BasicRequest request) {
        return this.agencyService.getListAddressExportBilling(request);
    }

    @RequestMapping(value = AgencyPath.ADD_ADDRESS_EXPORT_BILLING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse addAddressExportBilling(

            @RequestBody AddAddressExportBillingRequest request) {
        return this.agencyService.addAddressExportBilling(request);
    }

    @RequestMapping(value = AgencyPath.GET_ADDRESS_EXPORT_BILLING_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse getAddressExportBillingDetail(

            BasicRequest request) {
        return this.agencyService.getAddressExportBillingDetail(request);
    }

    @RequestMapping(value = AgencyPath.EDIT_ADDRESS_EXPORT_BILLING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse editAddressExportBilling(

            @RequestBody EditAddressExportBillingRequest request) {
        return this.agencyService.editAddressExportBilling(request);
    }

    @RequestMapping(value = AgencyPath.DELETE_ADDRESS_EXPORT_BILLING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse deleteAddressExportBilling(

            @RequestBody BasicRequest request) {
        return this.agencyService.deleteAddressExportBilling(request);
    }

    @RequestMapping(value = AgencyPath.SET_ADDRESS_EXPORT_BILLING_DEFAULT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đặt làm địa chỉ mặc định", notes = "")
    public ClientResponse setAddressExportBillingDefault(

            @RequestBody BasicRequest request) {
        return this.agencyService.setAddressExportBillingDefault(request);
    }

    @PostMapping(value = AgencyPath.ADD_NEW_AGENCY_ACCOUNT)
    @ApiOperation(value = "Thêm tài khoản phụ cho đại lý", notes = "Returns a product as per the id")
    public ClientResponse addNewAgencyAccount(

            @RequestBody AddNewAgencyAccountRequest request) {
        return this.agencyService.addNewAgencyAccount(request);
    }

    @PostMapping(value = AgencyPath.EDIT_AGENCY_ACCOUNT)
    @ApiOperation(value = "Chỉnh sửa tài khoản phụ cho đại lý", notes = "Returns a product as per the id")
    public ClientResponse editAgencyAccount(

            @RequestBody EditAgencyAccountRequest request) {
        return this.agencyService.editAgencyAccount(request);
    }

    @RequestMapping(value = AgencyPath.GET_LIST_AGENCY_ACCOUNT, method = RequestMethod.GET)
    @ApiOperation(value = "Lấy danh sách tài khoản đăng nhập của đại lý", notes = "")
    public ClientResponse getListAgencyAccount(

            BasicRequest request) {
        return this.agencyService.getListAgencyAccount(request);
    }

    @RequestMapping(value = AgencyPath.GET_AGENCY_ACCOUNT_DETAIL, method = RequestMethod.GET)
    @ApiOperation(value = "Chi tiết tài khoản đăng nhập của đại lý", notes = "")
    public ClientResponse getAgencyAccountDetail(

            BasicRequest request) {
        return this.agencyService.getAgencyAccountDetail(request);
    }

    @PostMapping(value = AgencyPath.SET_MAIN_AGENCY_ACCOUNT)
    @ApiOperation(value = "Chọn làm tài khoản chính", notes = "")
    public ClientResponse setAgencyAccountMain(

            @RequestBody BasicRequest request) {
        return this.agencyService.setAgencyAccountMain(request);
    }

    @PostMapping(value = AgencyPath.UPDATE_STATUS_AGENCY_ACCOUNT)
    @ApiOperation(value = "Cập nhật trạng thái tài khoản đăng nhập", notes = "")
    public ClientResponse updateStatusAgencyAccount(

            @RequestBody UpdateStatusRequest request) {
        return this.agencyService.updateStatusAgencyAccount(request);
    }

    @RequestMapping(value = AgencyPath.UPDATE_AGENCY_CONTRACT_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Cập nhật thông tin hợp đồng của đại lý", notes = "")
    public ClientResponse updateAgencyContractInfo(

            @RequestBody UpdateAgencyContractInfoRequest request) {
        return this.agencyService.updateAgencyContractInfo(request);
    }

    @RequestMapping(value = AgencyPath.GET_AGENCY_CONTRACT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Lấy thông tin hợp đồng của đại lý", notes = "")
    public ClientResponse getAgencyContractInfo(

            @RequestBody BasicRequest body) {
        return this.agencyService.getAgencyContractInfo(body);
    }

    @GetMapping(value = AgencyPath.GET_LIST_STAFF_MANAGE_AGENCY)
    @ApiOperation(value = "Danh sách nhân viên quản lý đại lý", notes = "")
    public ClientResponse getListStaffManageAgency(
            BasicRequest request) {
        return this.agencyService.getListStaffManageAgency(request);
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY_MEMBERSHIP_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy sử thay đổi cấp bậc của đại lý", notes = "")
    public ClientResponse filterAgencyMemberShipHistory(
            @RequestBody FilterListRequest request) {
        return this.agencyService.filterAgencyMemberShipHistory(request);
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY_ACOIN_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy sử thay đổi acoin của đại lý", notes = "")
    public ClientResponse filterAgencyAcoinHistory(
            @RequestBody FilterListRequest request) {
        return this.agencyService.filterAgencyAcoinHistory(request);
    }

    @RequestMapping(value = "/agency/approve_all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Duyệt đại lý", notes = "")
    public ClientResponse approveAgencyAll() {
        return this.agencyService.approveAgencyAll();
    }

    @RequestMapping(value = "/agency/block_csbh", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn CSBH", notes = "")
    public ClientResponse blockCSBH(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCSBH(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_ctkm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn CTKM", notes = "")
    public ClientResponse blockCTKM(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCTKM(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_ctsn", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn CTSN", notes = "")
    public ClientResponse blockCTSN(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCTSN(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_price", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn giá", notes = "")
    public ClientResponse blockPrice(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockPrice(sessionData, request);
    }

    @RequestMapping(value = "/agency/setting_dtt_limit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Thiết lập hạn mức doanh thu thuần cho đại lý", notes = "")
    public ClientResponse settingDttLimit(
            @RequestBody SettingDttLimitRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.settingDttLimit(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.SYNC_AGENCY_TO_BRAVO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Đồng bộ bravo cho đại lý", notes = "")
    public ClientResponse syncAgencyToBravo(
            @RequestBody BasicRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.syncAgencyToBravo(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_cttl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn Chương trình tích lũy", notes = "")
    public ClientResponse blockCTTL(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCTTL(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_LIST_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Lọc danh sách đại lý", notes = "")
    public ClientResponse filterListAgency(
            @RequestBody FilterListAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.filterListAgency(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_ctss", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn Chương trình săn sale", notes = "")
    public ClientResponse blockCTSS(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCTSS(sessionData, request);
    }

    @RequestMapping(value = "/agency/block_csdm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Chặn Chương trình săn sale", notes = "")
    public ClientResponse blockCSDM(
            @RequestBody BlockAgencyRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.blockCSDM(sessionData, request);
    }

    @RequestMapping(value = "/agency/reject_agency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Từ chối đại lý", notes = "")
    public ClientResponse rejectAgency(
            @RequestBody ApproveRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.rejectAgency(sessionData, request);
    }

    @RequestMapping(value = "/agency/set_staff_manage_agency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chọn nhân viên phụ trách", notes = "")
    public ClientResponse setStaffManageAgency(
            @RequestBody SetStaffManageAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.agencyService.setStaffManageAgency(sessionData, request);
    }
}