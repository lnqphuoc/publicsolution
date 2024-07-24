package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.AuthPath;
import com.app.server.data.request.LoginRequest;
import com.app.server.data.request.config.SettingConfigDataRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class DataMasterController extends BaseController {
    @RequestMapping(value = "/data/reload_product_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadProductManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getProductManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reload_promo_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadPromoManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getProgramManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reload_config_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadConfigManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getConfigManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reload_warehouse_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadWarehouseManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getWarehouseManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reload_banner_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadBannerManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getBannerManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reload_staff_manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadStaffManager(@RequestBody LoginRequest request) {
        this.agencyService.dataManager.getStaffManager().loadData();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reset_warehouse_info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse updateWarehouseInfo() {
        this.agencyService.dataManager.getWarehouseManager().resetTonKho();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/set_dept_order_nqh", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse setDeptOrderNQH() {
        this.deptService.runDeptOrderNQH();
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/reset_end_year", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse resetEndYear(int year) {
        return resetService.resetEndYear(year);
    }

    @RequestMapping(value = "/data/reset_end_year_by_agency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse resetEndYear(int year, int agency_id) {
        return resetService.resetEndYearOne(
                year,
                agency_id
        );
    }

    @RequestMapping(value = "/data/push_notify_reset_membership", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse pushNotifyResetMembership(int year) {
        return resetService.pushNotifyResetMembership(
                year
        );
    }

    @RequestMapping(value = "/data/push_notify_reset_acoin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse pushNotifyResetAcoin(int year) {
        return resetService.pushNotifyResetAcoin(
                year
        );
    }

    @RequestMapping(value = "/data/send_change_membership_to_bravo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse sendChangeMembershipToBravo(int year) {
        return resetService.sendChangeMembershipToBravo(
                year
        );
    }

    @RequestMapping(value = "/data/push_notify_reset_cno", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse pushNotifyResetCNO(int year) {
        return resetService.pushNotifyResetCNO(
                year
        );
    }

    @RequestMapping(value = "/data/reload_product_visibility", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadProductVisibility(int id) {
        this.productService.dataManager.getProductManager().reloadProductVisibilitySetting(id);
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/data/update_config_data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse updateConfigData(@RequestBody SettingConfigDataRequest request) {
        return this.masterService.updateConfigData(
                request);
    }
}