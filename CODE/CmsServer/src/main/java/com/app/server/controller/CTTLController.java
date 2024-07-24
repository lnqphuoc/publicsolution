package com.app.server.controller;


import com.app.server.constants.path.PromoPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetListCTTLByTransactionRequest;
import com.app.server.data.request.cttl.GetResultCTTLOfAgencyRequest;
import com.app.server.data.request.promo.AddChildOrderIntoCTLLRequest;
import com.app.server.data.request.promo.PromoApplyObjectRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.CTTLService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class CTTLController extends BaseController {
    private CTTLService cttlService;

    @Autowired
    public void setCttlService(CTTLService cttlService) {
        this.cttlService = cttlService;
    }

    @RequestMapping(value = PromoPath.FILTER_CHILD_ORDER_CTTL_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterTransactionByAgency(
            @RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.filterTransactionByAgency(sessionData, request);
    }

    /**
     * Bổ sung đơn hàng con vào tích lũy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.ADD_CHILD_ORDER_INTO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse addChildOrderIntoCTTL(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.addTransactionIntoCTTL(sessionData, request);
    }

    /**
     * Bổ sung đơn hàng con vào tích lũy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.REMOVE_CHILD_ORDER_INTO_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse removeChildOrderIntoCTTL(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.removeChildOrderIntoCTTL(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_RESULT_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterResultCTTL(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.filterResultCTTL(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_RESULT_CTTL_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê chi tiết kết quả theo danh sách đại lý")
    public ClientResponse filterResultCTTLByAgency(@RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.filterResultCTTLByAgency(sessionData, request);
    }

    @RequestMapping(value = PromoPath.FILTER_TRANSACTION_CTTL_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterTransactionCTTL(@RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.filterTransactionCTTLByAgency(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_RESULT_CTTL_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getResultCTTLInfo(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.getResultCTTLInfo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_RESULT_CTTL_OF_AGENCY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getResultCTTLOfAgency(GetResultCTTLOfAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.getResultCTTLOfAgency(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CALCULATE_PROGRESS_CTTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse calculateProgressCTTL(@RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.calculateProgressCTTL(request);
    }

    @RequestMapping(value = PromoPath.GET_LIST_CTTL_BY_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getListCTTLByTransaction(@RequestBody GetListCTTLByTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.getListCTTLByTransaction(sessionData, request);
    }

    @RequestMapping(value = PromoPath.GET_CTTL_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getCTTLInfo(GetResultCTTLOfAgencyRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.getCTTLInfo(sessionData, request);
    }

    @RequestMapping(
            value = PromoPath.FILTER_CTTL_BY_AGENCY,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterCTTLByAgency(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return cttlService.filterCTTLByAgency(sessionData, request);
    }
}