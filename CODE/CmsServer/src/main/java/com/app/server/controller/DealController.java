package com.app.server.controller;

import com.app.server.constants.path.DealPath;
import com.app.server.constants.path.DeptPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.CancelRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.deal.CreateDealPriceSettingRequest;
import com.app.server.data.request.deal.ResponseDealPriceRequest;
import com.app.server.data.request.order.CancelOrderRequest;
import com.app.server.database.BaseDB;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Basic;

@RestController
@CrossOrigin(origins = "*")
public class DealController extends BaseController {
    @RequestMapping(value = DealPath.FILTER_DEAL_PRICE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách thách giá", notes = "")
    public ClientResponse filterDealPrice(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.filterDealPrice(sessionData, request);
    }

    @RequestMapping(value = DealPath.UPDATE_DEAL_PRICE_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Cập nhật thiết lập thách giá", notes = "")
    public ClientResponse createDealPriceSetting(
            @RequestBody CreateDealPriceSettingRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.createDealPriceSetting(sessionData, request);
    }

    @RequestMapping(value = DealPath.GET_DEAL_PRICE_SETTING_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thiết lập thách giá", notes = "")
    public ClientResponse getDealPriceSettingDetail() {
        SessionData sessionData = this.getSessionData();
        return this.dealService.getDealPriceSetting(sessionData);
    }

    @RequestMapping(value = DealPath.CONFIRM_DEAL_PRICE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận thách giá", notes = "")
    public ClientResponse confirmDealPrice(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.confirmDealPrice(sessionData, request);
    }

    @RequestMapping(value = DealPath.RESPONSE_DEAL_PRICE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Phản hồi thách giá", notes = "")
    public ClientResponse responseDealPrice(
            @RequestBody ResponseDealPriceRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.responseDealPrice(sessionData, request);
    }

    @RequestMapping(value = DealPath.GET_DEAL_PRICE_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thách giá", notes = "")
    public ClientResponse getDealPriceDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.getDealPriceDetail(sessionData, request);
    }

    @RequestMapping(value = DealPath.CANCEL_DEAL_PRICE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy thách giá", notes = "")
    public ClientResponse cancelDealPrice(
            @RequestBody CancelRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.dealService.cancelDealPrice(sessionData, request);
    }
}