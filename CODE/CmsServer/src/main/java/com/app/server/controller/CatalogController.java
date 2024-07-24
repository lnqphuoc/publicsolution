package com.app.server.controller;

import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.catalog.*;
import com.app.server.data.request.promo.ApprovePromoRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.CatalogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class CatalogController extends BaseController {

    private CatalogService catalogService;

    @Autowired
    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Thiết lập Catalog
     * 1. Danh sách catalog
     * 2. Chi tiết
     * 3. Tạo mới
     * 4. Chỉnh sửa
     * 5. Xóa MH/PLSP
     * 6. Bổ sung MH/PLSP
     */

    /**
     * Danh sách catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/filter_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách catalog", notes = "")
    public ClientResponse filterCatalog(@RequestBody FilterListRequest request) {
        return catalogService.filterCatalog(request);
    }

    /**
     * Chi tiết catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/get_catalog_detail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết catalog", notes = "")
    public ClientResponse getCatalogDetail(BasicRequest request) {
        return catalogService.getCatalogDetail(request);
    }

    /**
     * Tạo catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/create_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo catalog", notes = "")
    public ClientResponse createCatalog(@RequestBody CreateCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.createCatalog(sessionData, request);
    }

    /**
     * Chỉnh sửa catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/edit_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa catalog", notes = "")
    public ClientResponse editCatalog(@RequestBody EditCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.editCatalog(sessionData, request);
    }

    /**
     * Xóa MH/PLSP trong catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/delete_item_in_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa MH/PLSP trong catalog", notes = "")
    public ClientResponse deleteItemInCatalog(@RequestBody AddCategoryRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.deleteCategory(
                sessionData,
                request
        );
    }

    /**
     * Bổ sung MH/PLSP vào catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/add_item_into_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Bổ sung MH/PLSP vào catalog", notes = "")
    public ClientResponse addItemIntoCatalog(@RequestBody AddCategoryRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.addCategory(
                sessionData,
                request
        );
    }

    /**
     * Quản lý catalog của tất cả đại lý
     * 1. Danh sách lần đăng ký của tất cả đại lý
     * 2. Chi tiết lần đăng ký
     * 3. Duyệt từng catalog của lần đăng ký(kiểm tra đại lý đã duyệt)
     * 4. Từ chối từng catalog của lần đăng ký(kiểm tra đại lý đã duyệt)
     * 5. Tạo lần yêu cầu mở catalog
     * 6. Điều chỉnh n Catalog
     */

    /**
     * Danh sách lần yêu cầu mở catalog
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/catalog/filter_agency_request_open_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách lần yêu cầu mở catalog", notes = "")
    public ClientResponse filterAgencyRequestOpenCatalog(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.filterAgencyRequestOpenCatalog(sessionData, request);
    }

    /**
     * Chi tiết lần yêu cầu mở catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/get_agency_request_catalog_detail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết lần yêu cầu mở catalog", notes = "")
    public ClientResponse getAgencyRequestOpenCatalogDetail(BasicRequest request) {
        return ClientResponse.success(request);
    }

    /**
     * Tạo lần yêu cầu mở catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/create_agency_request_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo lần yêu cầu mở catalog", notes = "")
    public ClientResponse createAgencyRequestOpenCatalog(@RequestBody CreateAgencyCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.createAgencyRequestOpenCatalog(
                sessionData, request
        );
    }

    /**
     * Duyệt catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/approve_agency_request_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt catalog", notes = "")
    public ClientResponse approveAgencyRequestOpenCatalog(@RequestBody ApproveCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.approveAgencyRequestOpenCatalog(sessionData, request);
    }

    /**
     * Từ chối catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/reject_agency_request_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối catalog", notes = "")
    public ClientResponse rejectAgencyRequestOpenCatalog(@RequestBody ApproveCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.rejectAgencyRequestOpenCatalog(sessionData, request);
    }

    /**
     * Điều chỉnh n Catalog được chọn khi đăng ký mới
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/edit_n_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh n Catalog được chọn khi đăng ký mới", notes = "")
    public ClientResponse editNCatalog(@RequestBody UpdateNCategoryRequest request) {
        return catalogService.editNCatalog(request);
    }

    /**
     * Danh sách lần yêu cầu mở catalog
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/catalog/filter_catalog_by_agency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách catalog ở chi tiết đại lý", notes = "")
    public ClientResponse filterCatalogByAgency(@RequestBody FilterListRequest request) {
        return catalogService.filterCatalogByAgency(request);
    }

    /**
     * Điều chỉnh n Catalog được chọn khi đăng ký mới
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/sort_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sắp xếp thứ tự Catalog", notes = "")
    public ClientResponse sortCatalog(@RequestBody SortCatalogRequest request) {
        return catalogService.sortCatalog(request);
    }

    /**
     * Danh sách category
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/catalog/filter_category", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách category", notes = "")
    public ClientResponse filterCategory(@RequestBody FilterListRequest request) {
        return catalogService.filterCategory(request);
    }

    /**
     * Danh sách lịch sử yêu cầu mở catalog
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/catalog/filter_agency_catalog_history", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách lịch sử yêu cầu mở catalog", notes = "")
    public ClientResponse filterAgencyCatalogHistory(@RequestBody FilterListRequest request) {
        return catalogService.filterAgencyCatalogHistory(request);
    }

    /**
     * Duyệt catalog
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/catalog/reset_agency_catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reset catalog", notes = "")
    public ClientResponse resetAgencyCatalog(@RequestBody ApproveCatalogRequest request) {
        SessionData sessionData = this.getSessionData();
        return catalogService.resetAgencyCatalog(sessionData, request);
    }
}