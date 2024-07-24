package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.constants.path.WarehousePath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.warehouse.*;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class WarehouseController extends BaseController {
    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouse(
            @RequestBody FilterListRequest request) {
        return this.warehouseService.filterWarehouse(request);
    }

    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouseBillImport(
            @RequestBody FilterListRequest request) {
        return this.warehouseService.filterWarehouseBillImport(request);
    }

    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouseBillExport(
            @RequestBody FilterListRequest request) {
        return this.warehouseService.filterWarehouseBillExport(request);
    }

    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE_IMPORT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sử nhập kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouseImportHistory(
            @RequestBody FilterListRequest request) {
        return this.warehouseService.filterWarehouseImportHistory(request);
    }

    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE_EXPORT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sử xuất kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouseExportHistory(
            @RequestBody FilterListRequest request) {
        return this.warehouseService.filterWarehouseExportHistory(request);
    }

    @RequestMapping(value = WarehousePath.FILTER_WAREHOUSE_REPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Báo cáo tồn kho", notes = "")
    @ResponseBody
    public ClientResponse filterWarehouseReport(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.filterWarehouseReport(request);
    }

    @RequestMapping(value = WarehousePath.CREATE_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse createWarehouseBillImport(
            @RequestBody CreateWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.createWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.CANCEL_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse cancelWarehouseBillImport(
            @RequestBody CancelWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.cancelWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.EDIT_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse editWarehouseBillImport(
            @RequestBody EditWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.editWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.APPROVE_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse approveWarehouseBillImport(
            @RequestBody ApproveWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.approveWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.REJECT_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse rejectWarehouseBillImport(
            @RequestBody RejectWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.rejectWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.CREATE_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse createWarehouseBillExport(
            @RequestBody CreateWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.createWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.CANCEL_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse cancelWarehouseBillExport(
            @RequestBody CancelWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.cancelWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.EDIT_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse editWarehouseBillExport(
            @RequestBody EditWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.editWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.APPROVE_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse approveWarehouseBillExport(
            @RequestBody ApproveWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.approveWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.REJECT_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tu chối phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse rejectWarehouseBillExport(
            @RequestBody RejectWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.rejectWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.GET_LIST_ORDER_WAITING_SHIP, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đơn hàng đang chờ giao", notes = "")
    @ResponseBody
    public ClientResponse getListOrderWaitingShip(
            GetListOrderWaitingShipRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.getListOrderWaitingShip(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.REQUIRE_APPROVE_WAREHOUSE_BILL_IMPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Yêu cầu duyệt phiếu nhập kho", notes = "")
    @ResponseBody
    public ClientResponse requireApproveWarehouseBillImport(
            @RequestBody RequireApproveWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.requireApproveWarehouseBillImport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.REQUIRE_APPROVE_WAREHOUSE_BILL_EXPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Yêu cầu duyệt phiếu xuất kho", notes = "")
    @ResponseBody
    public ClientResponse requireApproveWarehouseBillExport(
            @RequestBody RequireApproveWarehouseBillRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.requireApproveWarehouseBillExport(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.GET_LIST_ORDER_WAITING_APPROVE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đơn hàng đang chờ xác nhận", notes = "")
    @ResponseBody
    public ClientResponse getListOrderWaitingApprove(
            GetListOrderWaitingShipRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.getListOrderWaitingApprove(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.GET_WAREHOUSE_BILL_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết phiếu nhập", notes = "")
    @ResponseBody
    public ClientResponse getWarehouseBillInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.getWarehouseBillInfo(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.GET_WAREHOUSE_BILL_IMPORT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết phiếu nhập", notes = "")
    @ResponseBody
    public ClientResponse getWarehouseBillImportInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.getWarehouseBillInfo(sessionData, request);
    }

    @RequestMapping(value = WarehousePath.GET_WAREHOUSE_BILL_EXPORT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết phiếu xuất", notes = "")
    @ResponseBody
    public ClientResponse getWarehouseBillExportInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.warehouseService.getWarehouseBillInfo(sessionData, request);
    }
}