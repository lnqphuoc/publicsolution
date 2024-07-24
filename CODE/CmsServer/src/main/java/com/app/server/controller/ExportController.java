package com.app.server.controller;

import com.app.server.data.SessionData;
import com.app.server.data.request.export.ExportRequest;
import com.app.server.enums.ExportType;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
@ApiOperation(value = "Xuất file")
public class ExportController extends BaseController {
    @RequestMapping(
            value = "/export/export_report_inventory",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất báo cáo tồn kho", notes = "")
    public ClientResponse exportReportInventory(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_INVENTORY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/download",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tải về", notes = "")
    public ResponseEntity<?> download(
            String data,
            HttpServletResponse response
    ) {
        return this.exportService.download(data, response);
    }

    @RequestMapping(
            value = "/export/export_report_agency",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất báo cáo đại lý", notes = "")
    public ClientResponse exportReportAgency(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_AGENCY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_product_price_standard",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất danh sách sản phẩm của bản cập nhật giá chung", notes = "")
    public ClientResponse exportProductPriceStandardOfTimer(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_PRODUCT_PRICE_TIMER.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_order",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất danh sách đơn hàng", notes = "")
    public ClientResponse exportOrder(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.EXPORT_ORDER.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_order_temp",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất danh sách đơn hàng tạm", notes = "")
    public ClientResponse exportOrderTemp(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_ORDER_TEMP.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_tk_cttl",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất danh sách đơn hàng tạm", notes = "")
    public ClientResponse exportTKCTTL(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_TK_CTTL.getValue());
        request.setSessionData(sessionData);
        int id = promoService.saveStatistic(request.getId());
        request.setId(id);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_agency_access_app",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất số lượng truy cập App", notes = "")
    public ClientResponse exportAgencyAccessApp(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_AGENCY_ACCESS_APP.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_warehouse_export_history",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất lịch sử xuất kho", notes = "")
    public ClientResponse exportWarehouseExportHistory(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_WAREHOUSE_EXPORT_HISTORY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_warehouse_import_history",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất lịch sử nhập kho", notes = "")
    public ClientResponse exportWarehouseImportHistory(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_WAREHOUSE_IMPORT_HISTORY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_product_visibility_by_agency",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất ẩn hiện sản phẩm theo đại lý", notes = "")
    public ClientResponse exportProductVisibilityByAgency(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_PRODUCT_VISIBILITY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_product",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất sản phẩm", notes = "")
    public ClientResponse exportProduct(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_PRODUCT.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_product_group",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất nhóm hàng", notes = "")
    public ClientResponse exportProductGroup(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_PRODUCT_GROUP.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_category",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất danh mục", notes = "")
    public ClientResponse exportCategory(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_CATEGORY.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_catalog",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xuất catalog", notes = "")
    public ClientResponse exportCatalog(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.REPORT_CATALOG.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }

    @RequestMapping(
            value = "/export/export_order_confirm_product",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "exportOrderConfirmProduct", notes = "")
    public ClientResponse exportOrderConfirmProduct(
            @RequestBody ExportRequest request
    ) {
        SessionData sessionData = this.getSessionData();
        request.setType(ExportType.EXPORT_ORDER_CONFIRM_PRODUCT.getValue());
        request.setSessionData(sessionData);
        return this.exportService.getLink(sessionData, request);
    }
}