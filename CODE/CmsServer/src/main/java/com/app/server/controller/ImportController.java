package com.app.server.controller;

import com.app.server.constants.path.ImportPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.order.ImportOrderConfirmationRequest;
import com.app.server.data.request.promo.AddAgencyToListPromoRequest;
import com.app.server.data.request.promo.AddListAgencyToPromoRequest;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@ApiOperation(value = "Chi tiết thông báo")
public class ImportController extends BaseController {
    @RequestMapping(value = ImportPath.IMPORT_DEPT_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách giao dịch", notes = "")
    public ClientResponse importDeptTransaction(
            MultipartFile file
    ) {
        return this.deptService.importDeptTransaction(file);
    }

    @RequestMapping(value = ImportPath.IMPORT_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import giá đối tượng", notes = "")
    public ClientResponse importPriceSetting() {
        return this.priceService.importPriceSetting();
    }

    @RequestMapping(value = ImportPath.IMPORT_PROMO_AGENCY_IGNORE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import đại lý loại trừ của chính sách", notes = "")
    public ClientResponse importPromoAgencyIgnore() {

        return this.promoService.importPromoAgencyIgnore();
    }

    @RequestMapping(value = ImportPath.IMPORT_DEPT_DTT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh doanh thu thuần", notes = "")
    public ClientResponse importDeptDTT(MultipartFile file) {
        return this.deptService.importDeptDTT(file);
    }

    @RequestMapping(value = ImportPath.ADD_LIST_AGENCY_IGNORE_PROMO,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add đại lý loại trừ vào chính sách", notes = "")
    public ClientResponse addAgencyIgnoreToPromo(
            @RequestBody AddListAgencyToPromoRequest request) {
        return this.promoService.addListAgencyIgnoreToPromo(request);
    }

    @RequestMapping(value = ImportPath.ADD_AGENCY_IGNORE_LIST_PROMO,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add đại lý loại trừ vào danh sách chính sách", notes = "")
    public ClientResponse addAgencyIgnoreToListPromo(
            @RequestBody AddAgencyToListPromoRequest request) {
        return this.promoService.addAgencyIgnoreToListPromo(request);
    }

    @RequestMapping(value = ImportPath.IMPORT_WAREHOUSE_START_TODAY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Import tồn kho đầu ngày", notes = "")
    public ClientResponse importQuantityStartToday(
            MultipartFile file
    ) {
        return this.warehouseService.importQuantityStartToday(file);
    }

    @RequestMapping(value = "/import/remove_agency_ignore_promo",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Gỡ đại lý loại trừ khỏi chính sách", notes = "")
    public ClientResponse removeAgencyIgnoreToPromo(String request) {
        return this.promoService.removeAgencyIgnoreToListPromo(request);
    }

    @RequestMapping(value = "/import/import_order_confirmation",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "import_order_confirmation", notes = "")
    public ClientResponse importOrderConfirmation(@RequestBody ImportOrderConfirmationRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.importOrderConfirmation(sessionData,
                request);
    }

    @RequestMapping(value = "/import/import_new_soc_and_po",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "import_order_confirmation", notes = "")
    public ClientResponse importOrderConfirmationAndCreatePO(@RequestBody ImportOrderConfirmationRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.importNewSOCAndCreatePO(sessionData,
                request);
    }
}