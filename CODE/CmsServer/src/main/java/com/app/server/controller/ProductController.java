package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.ImportPath;
import com.app.server.constants.path.ProductPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.*;
import com.app.server.data.request.product.*;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.service.ProductPriceSettingTimerService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class ProductController extends BaseController {

    private ProductPriceSettingTimerService productPriceSettingTimerService;

    @Autowired
    public void setProductPriceSettingTimerService(ProductPriceSettingTimerService productPriceSettingTimerService) {
        this.productPriceSettingTimerService = productPriceSettingTimerService;
    }

    @RequestMapping(value = ProductPath.FILTER_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach danh mục", notes = "")
    public ClientResponse filterCategory(

            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterCategory(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CREATE_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo danh mục", notes = "")
    public ClientResponse createCategory(

            @RequestBody CreateCategoryRequest request) {
        return this.productService.createCategory(request);
    }

    @RequestMapping(value = ProductPath.EDIT_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa danh mục", notes = "")
    public ClientResponse editCategory(

            @RequestBody EditCategoryRequest request) {
        return this.productService.editCategory(request);
    }

    @RequestMapping(value = ProductPath.DELETE_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa danh mục", notes = "")
    public ClientResponse deleteCategory(

            @RequestBody BasicRequest request) {
        return this.productService.deleteCategory(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach sản phẩm", notes = "")
    public ClientResponse filterProduct(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProduct(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo sản phẩm", notes = "")
    public ClientResponse createProduct(

            @RequestBody CreateProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.createProduct(sessionData, request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa sản phẩm", notes = "")
    public ClientResponse editProduct(
            @RequestBody EditProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.editProduct(sessionData, request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thông tin sản phẩm", notes = "")
    public ClientResponse getProductInfo(

            BasicRequest request) {
        return this.productService.getProductInfo(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach nhóm sản phẩm", notes = "")
    public ClientResponse filterProductGroup(

            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProductGroup(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo nhóm sản phẩm", notes = "")
    public ClientResponse createProductGroup(

            @RequestBody CreateProductGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.createProductGroup(sessionData, request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa nhóm sản phẩm", notes = "")
    public ClientResponse editProductGroup(

            @RequestBody EditProductGroupRequest request) {
        return this.productService.editProductGroup(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_GROUP_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thông tin nhóm sản phẩm", notes = "")
    public ClientResponse getProductGroupInfo(

            BasicRequest request) {
        return this.productService.getProductGroupInfo(request);
    }

    @RequestMapping(value = ProductPath.FILTER_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach thương hiệu", notes = "")
    public ClientResponse filterBrand(

            @RequestBody FilterListRequest request) {
        return this.productService.filterBrand(request);
    }

    @RequestMapping(value = ProductPath.CREATE_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thương hiệu", notes = "")
    public ClientResponse createBrand(

            @RequestBody CreateBrandRequest request) {
        return this.productService.createBrand(request);
    }

    @RequestMapping(value = ProductPath.EDIT_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thương hiệu", notes = "")
    public ClientResponse editBrand(

            @RequestBody EditBrandRequest request) {
        return this.productService.editBrand(request);
    }

    @RequestMapping(value = ProductPath.DELETE_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa thương hiệu", notes = "")
    public ClientResponse deleteBrand(

            @RequestBody BasicRequest request) {
        return this.productService.deleteBrand(request);
    }

    @RequestMapping(value = ProductPath.ADD_PRODUCT_COLOR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm màu sắc", notes = "")
    public ClientResponse addProductColor(

            @RequestBody AddProductColorRequest request) {
        return this.productService.addProductColor(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thông tin sku", notes = "")
    public ClientResponse editProductInfo(

            @RequestBody EditProductInfoRequest request) {
        return this.productService.editProductInfo(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_TECHNICAL_DATA, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thông số kỹ thuật của sku", notes = "")
    public ClientResponse editProductTechnicalData(

            @RequestBody EditDataRequest request) {
        return this.productService.editProductTechnicalData(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_USER_MANUAL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa hướng dẫn sử dụng của sku", notes = "")
    public ClientResponse editProductUserManual(

            @RequestBody EditDataRequest request) {
        return this.productService.editProductUserManual(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_DESCRIPTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa mô tả của sku", notes = "")
    public ClientResponse editProductDescription(

            @RequestBody EditDataRequest request) {
        return this.productService.editProductDescription(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_WARRANTY_TIME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thời gian bảo hành của sku", notes = "")
    public ClientResponse editProductWarrantyTime(

            @RequestBody EditDataRequest request) {
        return this.productService.editProductWarrantyTime(request);
    }

    @RequestMapping(value = ProductPath.SORT_CATEGORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Sắp xếp thứ tự danh mục", notes = "")
    public ClientResponse sortCategory(
            @RequestBody SortBrandRequest request) {
        return this.productService.sortCategoryChild(request);
    }

    @RequestMapping(value = ProductPath.SORT_BRAND, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Sắp xếp hiển thị thương hiệu", notes = "")
    public ClientResponse sortBrand(
            @RequestBody SortBrandRequest request) {
        return this.productService.sortBrand(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_PRICE_BY_AGENCY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Giá sản phẩm theo đại lý", notes = "")
    public ClientResponse getProductPriceByAgency(

            GetProductPriceByAgencyRequest request) {
        return this.productService.getProductPriceByAgency(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_VISIBILITY_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach thiết lập ẩn hiện", notes = "")
    public ClientResponse filterProductVisibilitySetting(

            @RequestBody FilterListRequest request) {
        return this.productService.filterProductVisibilitySetting(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách danh mục/sản phẩm của chi tiết thiết lập ẩn hiện", notes = "")
    public ClientResponse filterProductStatusSettingDetail(
            @RequestBody FilterListByIdRequest request) {
        return this.productService.filterProductVisibilitySettingDetail(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thiết lập ẩn hiện", notes = "")
    public ClientResponse getProductVisibilitySettingDetail(
            BasicRequest request) {
        return this.productService.getProductVisibilitySettingDetail(request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập ẩn hiện", notes = "")
    public ClientResponse createProductVisibilitySetting(
            @RequestBody CreateProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.createProductVisibilitySetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.ACTIVATE_PRODUCT_VISIBILITY_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thiết lập ẩn hiện", notes = "")
    public ClientResponse activeProductVisibilitySetting(
            @RequestBody ActiveProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.activeProductVisibilitySetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.ACTIVATE_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thiết lập ẩn hiện", notes = "")
    public ClientResponse activeProductVisibilitySettingDetail(
            @RequestBody ActiveProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.activeProductVisibilitySettingDetail(sessionData, request);
    }

    @RequestMapping(value = ProductPath.DEACTIVATE_PRODUCT_VISIBILITY_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạm ngưng thiết lập ẩn hiện", notes = "")
    public ClientResponse deactivateProductVisibilitySetting(
            @RequestBody ActiveProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.deactivateProductVisibilitySetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.DEACTIVATE_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạm ngưng chi tiết thiết lập ẩn hiện", notes = "")
    public ClientResponse deactivateProductVisibilitySettingDetail(
            @RequestBody ActiveProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.deactivateProductVisibilitySettingDetail(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_VISIBILITY_SETTING_DETAIL_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách lịch sử danh mục/sản phẩm của chi tiết thiết lập ẩn hiện", notes = "")
    public ClientResponse filterProductStatusSettingDetailHistory(
            @RequestBody FilterListProductInvisibilityBySettingHistoryRequest request) {
        return this.productService.filterProductVisibilitySettingDetailHistory(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_VISIBILITY_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thiết lập ẩn hiện", notes = "")
    public ClientResponse editProductVisibilitySetting(
            @RequestBody EditProductVisibilitySettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.editProductVisibilitySetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_VISIBILITY_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách ẩn hiện sản phẩm theo đại lý", notes = "")
    public ClientResponse filterProductVisibilityByAgency(
            @RequestBody FilterListByIdRequest request) {
        return this.productService.filterProductVisibilityByAgency(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach thiết lập giá đối tượng", notes = "")
    public ClientResponse filterProductPriceSetting(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.filterProductPriceSetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách danh mục/sản phẩm của chi tiết thiết lập giá đối tượng", notes = "")
    public ClientResponse filterProductPriceSettingDetail(
            @RequestBody FilterListByIdRequest request) {
        return this.priceService.filterProductPriceSettingDetail(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_PRICE_SETTING_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thiết lập giá đối tượng", notes = "")
    public ClientResponse getProductPriceSettingDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.getProductPriceSettingDetail(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập giá đối tượng", notes = "")
    public ClientResponse createProductPriceSetting(
            @RequestBody CreateProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.createProductPriceSetting(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_HOT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách danh mục/sản phẩm bán chạy", notes = "")
    public ClientResponse filterListProductHot(
            @RequestBody FilterListRequest request) {
        return this.productService.filterListProductHot(request);
    }

    /**
     * Chỉnh sửa thiết lập giá đối tượng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.EDIT_PRODUCT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thiết lập giá đối tượng", notes = "")
    public ClientResponse editProductPriceSetting(
            @RequestBody EditProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.editProductPriceSetting(sessionData, request);
    }

    /**
     * Thiết lập sản phẩm bán chạy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.SETTING_PRODUCT_HOT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thiết lập sản phẩm bán chạy", notes = "")
    public ClientResponse settingProductHot(
            @RequestBody SettingProductHotRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.settingProductHot(sessionData, request);
    }

    /**
     * Kiểm tra thiết lập sản phẩm bán chạy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.CHECK_SETTING_PRODUCT_HOT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kiểm tra Thiết lập sản phẩm bán chạy", notes = "")
    public ClientResponse checkSettingProductHot(
            @RequestBody SettingProductHotRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.checkSettingProductHot(sessionData, request);
    }

    /**
     * Kích hoạt thiết lập giá đối tượng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.ACTIVATE_PRODUCT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thiết lập giá đối tượng", notes = "")
    public ClientResponse activeProductPriceSetting(
            @RequestBody ActiveProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.activeProductPriceSetting(
                sessionData,
                request);
    }

    /**
     * Kích hoạt thiết lập giá đối tượng từng sản phẩm
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.ACTIVATE_PRODUCT_PRICE_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thiết lập giá đối tượng", notes = "")
    public ClientResponse activeProductPriceSettingDetail(
            @RequestBody ActiveProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.activeProductPriceSettingDetail(
                sessionData,
                request);
    }

    /**
     * Tạm ngưng thiết lập giá đối tượng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.DEACTIVATE_PRODUCT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạm ngưng thiết lập giá đối tượng", notes = "")
    public ClientResponse deactivateProductPriceSetting(
            @RequestBody ActiveProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.deactivateProductPriceSetting(sessionData, request);
    }

    /**
     * Tạm ngưng từng dòng sản phẩm thiết lập giá đối tượng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ProductPath.DEACTIVATE_PRODUCT_PRICE_SETTING_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạm ngưng chi tiết thiết lập giá đối tượng", notes = "")
    public ClientResponse deactivateProductPriceSettingDetail(
            @RequestBody ActiveProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.deactivateProductPriceSettingDetail(
                sessionData,
                request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách giá sản phẩm theo đại lý", notes = "")
    public ClientResponse filterProductPriceByAgency(
            @RequestBody FilterListByIdRequest request) {
        return this.priceService.filterProductPriceByAgency(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_HOT_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách sản phẩm bán chạy theo đại lý", notes = "")
    public ClientResponse filterProductHotByAgency(
            @RequestBody FilterListByIdRequest request) {
        return this.productService.filterProductHotByAgency(request);
    }

    @RequestMapping(value = ProductPath.FILTER_SETTING_PRICE_BY_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách thiết lập theo sản phẩm", notes = "")
    public ClientResponse filterSettingPrice(
            @RequestBody FilterListByIdRequest request) {
        return this.priceService.filterSettingPrice(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_SETTING_DETAIL_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách lịch sử sản phẩm của chi tiết thiết giá đối tượng", notes = "")
    public ClientResponse filterProductPriceSettingDetailHistory(
            @RequestBody FilterListProductPriceBySettingHistoryRequest request) {
        return this.priceService.filterProductPriceSettingDetailHistory(request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_PRICE_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo hẹn giờ áp dụng giá bán chung", notes = "")
    public ClientResponse createProductPriceTimer(
            @RequestBody CreateProductPriceTimerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.createProductPriceTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_PRICE_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Edit hẹn giờ áp dụng giá bán chung", notes = "")
    public ClientResponse editProductPriceTimer(
            @RequestBody EditProductPriceTimerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.editProductPriceTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.APPROVE_PRODUCT_PRICE_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt hẹn giờ áp dụng giá bán chung", notes = "")
    public ClientResponse approveProductPriceTimer(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.approveProductPriceTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách hẹn giờ áp dụng giá bán chung", notes = "")
    public ClientResponse filterProductPriceTimer(
            @RequestBody FilterListRequest request) {
        return this.priceService.filterProductPriceTimer(request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_TIMER_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách sản phẩm hẹn giờ giá bán chung", notes = "")
    public ClientResponse filterProductPriceTimerDetail(
            @RequestBody FilterListByIdRequest request) {
        return this.priceService.filterProductPriceTimerDetail(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_PRICE_TIMER_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thiết lập hẹn giờ giá bán chung", notes = "")
    public ClientResponse getProductPriceTimerDetail(
            BasicRequest request) {
        return this.priceService.getProductPriceTimerDetail(request);
    }

    @RequestMapping(value = ProductPath.CANCEL_PRODUCT_PRICE_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy thiết lập hẹn giờ áp dụng giá bán chung", notes = "")
    public ClientResponse cancelProductPriceTimer(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.cancelProductPriceTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_HOT_TYPE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách nhãn của sản phẩm", notes = "")
    public ClientResponse filterProductHotType(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProductHotType(sessionData, request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_HOT_TYPE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa nhãn sản phẩm", notes = "")
    public ClientResponse editProductHotType(
            @RequestBody EditBasicDataRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.editProductHotType(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_HOT_TYPE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Thêm mới nhãn sản phẩm", notes = "")
    public ClientResponse createProductHotType(
            @RequestBody EditBasicDataRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.createProductHotType(sessionData, request);
    }

    @RequestMapping(value = ProductPath.SYNC_PRODUCT_PRICE_STANDARD_TO_BRAVO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đồng bộ bảng giá chung qua Bravo", notes = "")
    public ClientResponse syncProductPriceStandardToBravo(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.reSyncProductPriceStandToBravo(request);
    }

    @RequestMapping(value = ProductPath.SYNC_PRODUCT_PRICE_SETTING_TO_BRAVO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đồng bộ bảng giá riêng qua Bravo", notes = "")
    public ClientResponse syncProductPriceSettingToBravo(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.priceService.reSyncProductPriceSettingToBravo(request);
    }

    @RequestMapping(value = ProductPath.CREATE_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo hẹn giờ áp dụng giá bán đối tượng", notes = "")
    public ClientResponse createProductPriceSettingTimer(
            @RequestBody CreateProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.createProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết hẹn giờ áp dụng giá bán đối tượng", notes = "")
    public ClientResponse getProductPriceSettingTimer(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.getProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sách thiết lập hẹn giờ áp dụng giá đối tượng", notes = "")
    public ClientResponse filterProductPriceSettingTimer(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.filterProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_PRICE_SETTING_TIMER_DETAIL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách danh mục/sản phẩm của chi tiết thiết lập hẹn giờ giá đối tượng", notes = "")
    public ClientResponse filterProductPriceSettingTimerDetail(
            @RequestBody FilterListByIdRequest request) {
        return this.productPriceSettingTimerService.filterProductPriceSettingTimerDetail(request);
    }

    @RequestMapping(value = ProductPath.APPROVE_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt hẹn giờ áp dụng giá đối tượng", notes = "")
    public ClientResponse approveProductPriceSettingTimer(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.approveProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa hẹn giờ áp dụng giá bán đối tượng", notes = "")
    public ClientResponse editProductPriceSettingTimer(
            @RequestBody CreateProductPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.editProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.CANCEL_PRODUCT_PRICE_SETTING_TIMER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy hẹn giờ áp dụng giá bán đối tượng", notes = "")
    public ClientResponse cancelProductPriceSettingTimer(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productPriceSettingTimerService.cancelProductPriceSettingTimer(sessionData, request);
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lich su chinh sua san pham", notes = "")
    public ClientResponse filterProductHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.filterProductHistory(request);
    }

    @RequestMapping(value = ProductPath.GET_PRODUCT_HISTORY_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết chỉnh sửa sản phẩm", notes = "")
    public ClientResponse getProductHistoryDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.getProductHistoryDetail(request);
    }

    @RequestMapping(value = ProductPath.IMPORT_UPDATE_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import cập nhật sản phẩm", notes = "")
    public ClientResponse importUpdateProduct(
            @RequestBody ImportUpdateProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.importUpdateProduct(sessionData, request);
    }

    @RequestMapping(value = ProductPath.IMPORT_UPDATE_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import cập nhật nhóm hàng", notes = "")
    public ClientResponse importUpdateProductGroup(
            @RequestBody ImportEditProductGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.importEditProductGroup(sessionData, request);
    }

    @RequestMapping(value = ProductPath.IMPORT_CREATE_PRODUCT_GROUP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import tạo nhóm hàng", notes = "")
    public ClientResponse importCreateProductGroup(
            @RequestBody CreateProductGroupRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.productService.createProductGroup(sessionData, request);
    }
}