package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.PromoPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.promo.*;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class PromoController extends BaseController {
    @RequestMapping(value = PromoPath.FILTER_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filerPromo(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filerPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoInfo(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoInfo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CREATE_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse createPromo(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        if (request.checkPromoIsDamMe()) {
            return promoService.createDamMe(sessionData, request);
        } else {
            return promoService.createPromo(sessionData, request);
        }
    }

    @RequestMapping(value = PromoPath.EDIT_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editPromo(@RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.STOP_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse stopPromo(@RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.stopPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CANCEL_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelPromo(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.cancelPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.APPROVE_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approvePromo(@RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approvePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DELETE_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deletePromo(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.deletePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_HISTORY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoHistory(GetPromoHistoryRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoHistory(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_HISTORY_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoHistoryDetail(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoHistoryDetail(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_BY_PRODUCT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoByProduct(GetPromoByProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoByProduct(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_PROMO_CTKM, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Tất cả chương trình trả ngay được sắp xếp", notes = "")
    public ClientResponse filerPromoCTKM(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filerPromoCTKM(sessionData, request);
    }

    @RequestMapping(value = PromoPath.SORT_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Sắp xếp thứ tự", notes = "")
    public ClientResponse sortPromo(
            @RequestBody SortPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.promoService.sortPromo(request, sessionData);
    }

    @RequestMapping(
            value = PromoPath.FILTER_PROMO_BY_AGENCY,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterPromoByAgency(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterPromoByAgency(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DOUBLE_CHECK_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse doubleCheckPromo(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.doubleCheckPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.APPROVE_ALL_PROMO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approveAllPromo(@RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approveAllPromo(sessionData);
    }

    @RequestMapping(value = PromoPath.FILTER_COMBO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filerCombo(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterCombo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filerPromoHuntSale(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterPromoHuntSale(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CREATE_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse createPromoHuntSale(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.createPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_PROMO_HUNT_SALE_SORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Tất cả chương trình săn sale được sắp xếp", notes = "")
    public ClientResponse filerPromoCTSSSort(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filerPromoCTSSSort(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_HUNT_SALE_BY_PRODUCT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoCTSSByProduct(GetPromoByProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoHuntSaleByProduct(sessionData, request);
    }

    @RequestMapping(value = PromoPath.EDIT_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editPromoHuntSale(@RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.STOP_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse stopPromoHuntSale(@RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.stopPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CANCEL_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelPromoHuntSale(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.cancelPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.APPROVE_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approvePromoHuntSale(@RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approvePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DELETE_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deletePromoHuntSale(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.deletePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CREATE_COMBO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse createCombo(@RequestBody CreateComboRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.createCombo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_COMBO_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getComboDetail(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getComboDetail(sessionData, request);
    }

    @RequestMapping(value = PromoPath.EDIT_COMBO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editCombo(@RequestBody CreateComboRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editCombo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DOUBLE_CHECK_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse doubleCheckPromoHuntSale(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.doubleCheckPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.SORT_PROMO_HUNT_SALE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Sắp xếp thứ tự", notes = "")
    public ClientResponse sortPromoHuntSale(
            @RequestBody SortPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.promoService.sortPromoHuntSale(request, sessionData);
    }

    @RequestMapping(value = PromoPath.FILTER_PROMO_HUNT_SALE_BY_COMBO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoCTSSByProduct(@RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoHuntSaleByCombo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_PROMO_BY_DEAL_PRICE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getPromoByDealPrice(GetPromoByDealPriceRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getPromoByDealPrice(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filerPromoCTTL(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterPromoCTTL(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CREATE_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse createPromoCTTL(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.createPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.EDIT_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editPromoCTTL(@RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.APPROVE_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approvePromoCTTL(@RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approvePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.STOP_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse stopPromoCTTL(@RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    @RequestMapping(value = PromoPath.CANCEL_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelPromoCTTL(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.cancelPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DELETE_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deletePromoCTTL(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.deletePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DOUBLE_CHECK_PROMO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse doubleCheckPromoCTTL(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.doubleCheckPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_LIST_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterListAgency(@RequestBody PromoApplyObjectRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterListAgency(sessionData, request);
    }
}