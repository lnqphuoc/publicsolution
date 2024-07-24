package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.agency.AddNewAgencyRequest;
import com.app.server.data.request.order.CancelOrderByAppRequest;
import com.app.server.data.request.order.CancelOrderRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class AppController extends BaseController {
    @RequestMapping(value = "/app/cancel_order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy đơn hàng", notes = "")
    @ResponseBody
    public ClientResponse cancelOrder(
            @RequestBody CancelOrderByAppRequest request) {
        return this.orderService.cancelOrderByApp(
                request
        );
    }

    @RequestMapping(value = "/app/send_deal_price_info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thách giá", notes = "")
    @ResponseBody
    public ClientResponse cancelOrder() {
        return ClientResponse.success(null);
    }
}