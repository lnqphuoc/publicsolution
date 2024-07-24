package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.request.order.ConfirmDeliveryOrderRequest;
import com.app.server.data.request.warehouse.CreateWarehouseBillRequest;
import com.app.server.enums.CacheType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class BravoController extends BaseController {
    @RequestMapping(value = "/bravo/estimate_dept", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reload(String type, int id) {
        switch (type) {
            case "DEPT_SETTING":
                return this.deptService.updateDeptAgencySetting(id);
            case "DEPT_INFO":
                return this.deptService.updateDeptAgencyInfo(id);
        }
        return ClientResponse.success(null);
    }

    @RequestMapping(value = "/bravo/create_dept_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse createDeptOrder(String type, int id) {
        return this.orderService.createDeptOrderByAgencyOrderDept(id);
    }

    @RequestMapping(value = "/bravo/complete_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse completeOrder(String type, int id) {
        return this.orderService.completeAgencyOrderDept(id);
    }

    @RequestMapping(value = "/bravo/cancel_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelOrder(String type, int id) {
        return this.orderService.cancelOrderByBravo(id);
    }

    @RequestMapping(value = "/bravo/close_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse closeOrder(String type, int id) {
        return this.orderService.closeOrder(id);
    }

    @RequestMapping(value = "/bravo/import_warehouse_bill", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse importWarehouseBill(
            @RequestBody CreateWarehouseBillRequest request) {
        return this.warehouseService.importWarehouseBillByBravo(request);
    }

    @RequestMapping(value = "/bravo/approve_dept_transaction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approveDeptTransaction(String type, int id) {
        return this.deptService.approveDeptTransactionByBravo(id);
    }

    @RequestMapping(value = "/bravo/reload_cache", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reloadSmallUnit(String type, int id) {
        switch (type) {
            case "product_small_unit":
                this.productService.dataManager.reloadProductSmallUnit(
                        id
                );
                return ClientResponse.success(null);
            case "product":
                this.productService.dataManager.reloadProduct(
                        CacheType.PRODUCT,
                        id
                );
                return ClientResponse.success(null);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    @RequestMapping(value = "/bravo/check_complete_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse checkCompleteOrderDept(String type, int id) {
        return this.orderService.checkCompleteAgencyOrderDept(id);
    }

    @RequestMapping(value = "/bravo/accumulate_hbtl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse accumulateHBTL(int id) {
        return this.orderService.accumulateHBTL(id);
    }

    @RequestMapping(value = "/bravo/accumulate_order_delivery", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse delivery(String type, int id) {
        return this.orderService.accumulateOrderDelivery(id);
    }

    @RequestMapping(value = "/bravo/add_transaction_to_csdm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse addTransactionToDamme(String type, int id) {
        return this.orderService.addAccumulateOrderDelivery(id);
    }
}