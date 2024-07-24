package com.app.server.controller;

import com.app.server.constants.path.UtilityPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.SearchRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.dept.ApproveDeptSettingRequest;
import com.app.server.data.request.order.ApproveRequestOrderRequest;
import com.app.server.data.request.product.FilterProductByAgencyRequest;
import com.app.server.data.request.product.SearchProductDataRequest;
import com.app.server.data.request.promo.ApprovePromoRequest;
import com.app.server.response.ClientResponse;
import com.app.server.data.response.FilterContentResponse;
import com.app.server.service.UtilityService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class UtilityController extends BaseController {

    private UtilityService utilityService;

    @Autowired
    public void setUtilityService(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    @RequestMapping(value = UtilityPath.GET_FILTER_CONTENT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<FilterContentResponse> getFilterContent(

            String type) {
        return utilityService.getFilterContent(type);
    }

    // Get config
    @RequestMapping(value = UtilityPath.GET_CONFIG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getConfig() {
        return utilityService.getConfig();
    }

    @RequestMapping(value = UtilityPath.UPLOAD_FILE, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse uploadFile(
            MultipartFile[] files,
            String type) {
        return utilityService.uploadFile(files, type);
    }

    @RequestMapping(
            value = UtilityPath.UPLOAD_FILE_BASE64,
            method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse uploadFileBase64(
            String files,
            String type,
            String extension) {
        return utilityService.uploadFileBase64(files, type, extension);
    }

    @RequestMapping(value = UtilityPath.SEARCH_CATEGORY, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchCategory(

            @RequestBody FilterListRequest request) {
        return utilityService.searchCategory(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_BRAND, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchBrand(

            @RequestBody FilterListRequest request) {
        return utilityService.searchBrand(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_GROUP, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductGroup(

            @RequestBody FilterListRequest request) {
        return utilityService.searchProductGroup(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_SMALL_UNIT, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductSmallUnit(

            @RequestBody FilterListRequest request) {
        return utilityService.searchProductSmallUnit(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_BIG_UNIT, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductBigUnit(

            @RequestBody FilterListRequest request) {
        return utilityService.searchProductBigUnit(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_COLOR, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductColor(

            @RequestBody FilterListRequest request) {
        return utilityService.searchProductColor(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProduct(

            @RequestBody FilterProductByAgencyRequest request) {
        return utilityService.searchProduct(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_DATA, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductData(
            @RequestBody SearchProductDataRequest request) {
        return utilityService.searchProductData(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_BY_AGENCY, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductByAgency(

            @RequestBody FilterProductByAgencyRequest request) {
        return utilityService.searchProductByAgency(request);
    }

    @RequestMapping(value = UtilityPath.GET_ALL_CATEGORY, method = RequestMethod.GET)
    @ResponseBody
    public ClientResponse getAllCategory() {
        return utilityService.getAllCategory();
    }

    @RequestMapping(value = UtilityPath.SEARCH_AGENCY, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchAgency(

            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return utilityService.searchAgency(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_AGENCY_ADDRESS_DELIVERY, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchAgencyAddressDelivery(

            @RequestBody FilterListRequest request) {
        return utilityService.searchAgencyAddressDelivery(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_AGENCY_ADDRESS_EXPORT_BILLING, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchAgencyAddressExportBilling(

            @RequestBody FilterListRequest request) {
        return utilityService.searchAgencyAddressExportBilling(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_TREE_PRODUCT, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchTreeProduct(

            @RequestBody SearchRequest request) {
        SessionData sessionData = this.getSessionData();
        return utilityService.searchTreeProduct(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_WAREHOUSE, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchWarehouse(
            @RequestBody FilterListRequest request) {
        return utilityService.searchWarehouse(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_STAFF, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchStaff(
            @RequestBody FilterListRequest request) {
        return utilityService.searchStaff(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PROMOTION, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchPromotion(

            @RequestBody FilterListRequest request) {
        return utilityService.searchPromotion(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_BANNER, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchBanner(

            @RequestBody FilterListRequest request) {
        return utilityService.searchBanner(request);
    }

    @RequestMapping(value = UtilityPath.GET_ALL_MENU, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Tất cả menu", notes = "")
    public ClientResponse getAllMenuInfo() {
        return this.utilityService.getAllMenuInfo();
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_GROUP_BY_CATEGORY, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchProductGroupByCategory(

            @RequestBody FilterListByIdRequest request) {
        return utilityService.searchProductGroupByCategory(request);
    }

    @RequestMapping(value = UtilityPath.FILTER_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach danh mục", notes = "")
    public ClientResponse filterCategory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterCategory(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.FILTER_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach sản phẩm", notes = "")
    public ClientResponse filterProduct(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProduct(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.FILTER_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach nhóm hàng", notes = "")
    public ClientResponse filterProductGroup(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProductGroup(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.FILTER_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach thương hiệu", notes = "")
    public ClientResponse filterBrand(
            @RequestBody FilterListRequest request) {
        return this.productService.filterBrand(request);
    }

    @RequestMapping(value = UtilityPath.GET_LIST_ADDRESS_DELIVERY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sách đại chỉ giao hàng của đại lý", notes = "")
    public ClientResponse getListAddressDelivery(

            BasicRequest request) {
        return this.agencyService.getListAddressDelivery(request);
    }

    @RequestMapping(value = UtilityPath.GET_LIST_ADDRESS_EXPORT_BILLING, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sách địa chỉ xuất hóa đơn", notes = "")
    public ClientResponse getListAddressExportBilling(

            BasicRequest request) {
        return this.agencyService.getListAddressExportBilling(request);
    }

    @RequestMapping(value = UtilityPath.FILTER_GROUP_PERMISSION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterGroupPermission(
            @RequestBody FilterListRequest request) {
        return this.staffService.filterGroupPermission(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_PRODUCT_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tìm kiếm sản phẩm săn sale", notes = "")
    public ClientResponse filterProductHuntSale(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.promoService.searchProductOrder(sessionData, request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_COMBO, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchCombo(

            @RequestBody FilterListRequest request) {
        return utilityService.searchCombo(request);
    }

    @RequestMapping(value = UtilityPath.FILTER_PRODUCT_STOCK, method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse filterProductStock(
            @RequestBody ApproveDeptSettingRequest request) {
        return utilityService.filterProductStock(request);
    }

    @RequestMapping(value = UtilityPath.FILTER_PRODUCT_HOT_TYPE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterProductHotType(
            @RequestBody FilterListRequest request) {
        return this.productService.filterProductHotType(null, request);
    }

    @RequestMapping(value = UtilityPath.FILTER_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterPromo(
            @RequestBody FilterListRequest request) {
        return this.utilityService.filterPromo(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_MISSION_BXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse searchMissionBXH(
            @RequestBody FilterListRequest request) {
        return this.missionService.searchMissionBXH(request);
    }

    @RequestMapping(value = UtilityPath.SEARCH_MISSION_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse searchMissionSetting(
            @RequestBody FilterListRequest request) {
        return this.missionService.searchMissionSetting(request);
    }

    @RequestMapping(value = "/utility/search_supplier", method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchSupplier(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return utilityService.searchSupplier(sessionData, request);
    }

    @RequestMapping(value = "/utility/search_order", method = RequestMethod.POST)
    @ResponseBody
    public ClientResponse searchOrder(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return orderService.searchOrder(sessionData, request);
    }
}