package com.app.server.controller;

import com.app.server.constants.path.DeptPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.dept.*;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class DeptController extends BaseController {
    @RequestMapping(value = DeptPath.FILTER_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách giao dịch", notes = "")
    public ClientResponse filterDeptTransaction(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.filterDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.FILTER_DEPT_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách công nợ theo đơn hàng", notes = "")
    public ClientResponse filterDeptOrder(
            @RequestBody FilterListRequest request) {
        return this.deptService.filterDeptOrder(request);
    }

    @RequestMapping(value = DeptPath.CREATE_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo giao dịch tăng giảm", notes = "")
    public ClientResponse createDeptTransaction(
            @RequestBody CreateDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.createDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.CREATE_DEPT_TRANSACTION_PAYMENT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo giao dịch thanh toán", notes = "")
    public ClientResponse createDeptTransactionPayment(
            @RequestBody CreateDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.createDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.CREATE_DEPT_TRANSACTION_FEE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo giao dịch phí phát sinh", notes = "")
    public ClientResponse createDeptTransactionFee(
            @RequestBody CreateDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.createDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa giao dịch ", notes = "")
    public ClientResponse editDeptTransaction(
            @RequestBody EditDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_TRANSACTION_PAYMENT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa giao dịch thanh toán", notes = "")
    public ClientResponse editDeptTransactionPayment(
            @RequestBody EditDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_TRANSACTION_FEE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa giao dịch phí phát sinh", notes = "")
    public ClientResponse editDeptTransactionFee(
            @RequestBody EditDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.APPROVE_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt giao dịch tăng giảm", notes = "")
    public ClientResponse approveDeptTransaction(
            @RequestBody ApproveDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.approveDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.REJECT_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối giao dịch tăng giảm", notes = "")
    public ClientResponse rejectDeptTransaction(
            @RequestBody RejectDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.rejectDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.APPROVE_DEPT_TRANSACTION_FEE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt giao dịch phí phát sinh", notes = "")
    public ClientResponse approveDeptTransactionFee(
            @RequestBody ApproveDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.approveDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.REJECT_DEPT_TRANSACTION_FEE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối giao dịch phí phát sinh", notes = "")
    public ClientResponse rejectDeptTransactionFee(
            @RequestBody RejectDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.rejectDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.APPROVE_DEPT_TRANSACTION_PAYMENT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt giao dịch thanh toán", notes = "")
    public ClientResponse approveDeptTransactionPayment(
            @RequestBody ApproveDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.approveDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.REJECT_DEPT_TRANSACTION_PAYMENT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối giao dịch thanh toán", notes = "")
    public ClientResponse rejectDeptTransactionPayment(
            @RequestBody RejectDeptTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.rejectDeptTransaction(sessionData, request);
    }

    @RequestMapping(value = DeptPath.GET_DEPT_TRANSACTION_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết giao dịch thanh toán", notes = "")
    public ClientResponse getDeptTransactionInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.getDeptTransactionInfo(sessionData, request);
    }

    @RequestMapping(value = DeptPath.FILTER_DEPT_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập công nợ đại lý", notes = "")
    public ClientResponse filterDeptSetting(
            @RequestBody FilterListRequest request) {
        return this.deptService.filterDeptSetting(request);
    }

    @RequestMapping(value = DeptPath.CREATE_DEPT_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập công nợ cho đại lý", notes = "")
    public ClientResponse createDeptSetting(
            @RequestBody CreateDeptSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.createDeptSetting(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa giao dịch thanh toán", notes = "")
    public ClientResponse editDeptSetting(
            @RequestBody EditDeptSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptSetting(sessionData, request);
    }

    @RequestMapping(value = DeptPath.APPROVE_DEPT_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt thiet lap cong no", notes = "")
    public ClientResponse approveDeptSetting(
            @RequestBody ApproveDeptSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.approveDeptSetting(sessionData, request);
    }

    @RequestMapping(value = DeptPath.REJECT_DEPT_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối giao dịch thanh toán", notes = "")
    public ClientResponse rejectDeptSetting(
            @RequestBody RejectDeptSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.rejectDeptSetting(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_CYCLE_OF_DEPT_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh kỳ hạn nợ của công nợ đơn hàng", notes = "")
    public ClientResponse editDeptCycleOfDeptOrder(
            @RequestBody EditDeptCycleOfDeptOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptCycleOfDeptOrder(sessionData, request);
    }

    @RequestMapping(value = DeptPath.GET_DEPT_AGENCY_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thông tin công nợ của đại lý", notes = "")
    public ClientResponse getDeptAgencyInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.getDeptAgencyInfo(sessionData, request);
    }

    @RequestMapping(value = DeptPath.FILTER_DEPT_AGENCY_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sử thay đổi công nợ của đại lý", notes = "")
    public ClientResponse filterDeptAgencyHistory(
            @RequestBody FilterListRequest request) {
        return this.deptService.filterDeptAgencyHistory(request);
    }

    @RequestMapping(value = DeptPath.FILTER_DEPT_ORDER_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách công nợ theo đơn hàng của đại lý", notes = "")
    public ClientResponse getListDeptOrderByAgency(
            @RequestBody FilterListByIdRequest request) {
        return this.deptService.getListDeptOrderByAgency(request);
    }

    @RequestMapping(value = DeptPath.FILTER_DEPT_DTT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sử thay đổi doanh thu thuần", notes = "")
    public ClientResponse filterDeptDttHistory(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.filterDeptDttHistory(sessionData, request);
    }

    @RequestMapping(value = DeptPath.EDIT_DEPT_DTT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh doanh thu thuần", notes = "")
    public ClientResponse editDeptDtt(
            @RequestBody EditDeptDttRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.editDeptDtt(sessionData, request);
    }

    @RequestMapping(value = DeptPath.ESTIMATE_DEPT_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tính lại công nợ", notes = "")
    public ClientResponse estimateDeptInfo() {
        SessionData sessionData = this.getSessionData();
        return this.deptService.estimateDeptInfo(sessionData);
    }

    @RequestMapping(value = DeptPath.SAVE_DEPT_END_DATE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lưu công nợ cuối ngày", notes = "")
    public ClientResponse saveDeptEndDate() {
        SessionData sessionData = this.getSessionData();
        return this.deptService.runDeptAgencyDateByEndDate();
    }

    @RequestMapping(value = DeptPath.RESET_DEPT_START_DATE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reset công nợ đầu ngày", notes = "")
    public ClientResponse resetDeptStartEnd() {
        SessionData sessionData = this.getSessionData();
        return this.deptService.runDeptAgencyDateByStartDate();
    }

    @RequestMapping(value = DeptPath.GHI_NHAN_CONG_NO_DON_HEN_GIAO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "GHI_NHAN_CONG_NO_DON_HEN_GIAO", notes = "")
    public ClientResponse ghiNhanCongNoDonHenGiao(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.deptService.ghiNhanCongNoDonHenGiao(request, sessionData);
    }

    @RequestMapping(value = DeptPath.NQH_OVER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "NQH OVER", notes = "")
    public ClientResponse runNQHOver() {
        SessionData sessionData = this.getSessionData();
        return this.deptService.runNQHOver();
    }
}