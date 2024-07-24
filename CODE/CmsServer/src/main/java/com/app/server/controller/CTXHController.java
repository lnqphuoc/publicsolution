package com.app.server.controller;


import com.app.server.constants.path.CTXHPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.*;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetListCTTLByTransactionRequest;
import com.app.server.data.request.ctxh.AddAgencyToCTXHRequest;
import com.app.server.data.request.ctxh.CreateVoucherReleasePeriodRequest;
import com.app.server.data.request.ctxh.EditVoucherReleasePeriodRequest;
import com.app.server.data.request.damme.CreatePhieuDieuChinhCSDMRequest;
import com.app.server.data.request.damme.DieuChinhDamMeRequest;
import com.app.server.data.request.damme.GetListPromoByOrderRequest;
import com.app.server.data.request.damme.GetOrderInfoByCodeRequest;
import com.app.server.data.request.promo.*;
import com.app.server.response.ClientResponse;
import com.app.server.service.CTXHService;
import com.healthmarketscience.sqlbuilder.CreateViewQuery;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class CTXHController extends BaseController {
    private CTXHService ctxhService;

    @Autowired
    public void setCtxhService(CTXHService ctxhService) {
        this.ctxhService = ctxhService;
    }

    /**
     * Tạo chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.CREATE_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tạo chương trình xếp hạng")
    public ClientResponse createPromo(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.createCTXH(sessionData, request);
    }

    /**
     * Danh sách các đơn hàng được phép tích lũy vào chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_CHILD_ORDER_CTXH_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách các đơn hàng được phép tích lũy vào chương trình xếp hạng")
    public ClientResponse filterTransactionByAgency(
            @RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterTransactionByAgency(sessionData, request);
    }

    /**
     * Bổ sung đơn hàng con vào chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @ApiOperation("Bổ sung đơn hàng con vào chương trình xếp hạng")
    @RequestMapping(value = CTXHPath.ADD_CHILD_ORDER_INTO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse addChildOrderIntoDamMe(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.addChildOrderIntoCTTL(sessionData, request);
    }

    /**
     * Từ chối đơn hàng con vào tích lũy
     *
     * @param request
     * @return
     */
    @ApiOperation("Từ chối đơn hàng con vào chương trình xếp hạng")
    @RequestMapping(value = CTXHPath.REMOVE_CHILD_ORDER_INTO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse removeChildOrderIntoDamMe(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.removeChildOrderIntoCTTL(sessionData, request);
    }

    /**
     * Danh sách thống kê chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @ApiOperation("Danh sách thống kê chương trình xếp hạng")
    @RequestMapping(value = CTXHPath.FILTER_RESULT_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterResultCTXH(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterResultCTXH(sessionData, request);
    }

    /**
     * Thống kê chi tiết chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @ApiOperation("Thống kê chi tiết chương trình xếp hạng")
    @RequestMapping(value = CTXHPath.GET_RESULT_CTXH_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getResult(@RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getResult(sessionData, request);
    }

    /**
     * Danh sách đại lý của chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @ApiOperation("Danh sách đại lý của chương trình xếp hạng")
    @RequestMapping(value = CTXHPath.FILTER_RESULT_CTXH_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterResultByAgency(@RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterResultByAgency(sessionData, request);
    }

    /**
     * Thống kê chi tiết đam của của Đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_TRANSACTION_CTXH_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê chi tiết chương trình xếp hạng của Đại lý")
    public ClientResponse filterTransactionDamMeByAgency(@RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterTransactionCTTLByAgency(sessionData, request);
    }

    /**
     * Danh sách chương trình xếp hạng của khoản tích lũy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.GET_LIST_CTXH_BY_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách chương trình xếp hạng của khoản tích lũy")
    public ClientResponse getListDamMeByTransaction(@RequestBody GetListCTTLByTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getListCTTLByTransaction(sessionData, request);
    }


    /**
     * Danh sách chương trình xếp hạng của đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(
            value = CTXHPath.FILTER_CTXH_BY_AGENCY,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách chương trình xếp hạng của đại lý")
    public ClientResponse filterCTXHByAgency(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterCTXHByAgency(sessionData, request);
    }

    /**
     * Danh sách chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách chương trình xếp hạng")
    public ClientResponse filerPromoDamMe(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterPromoCTXH(sessionData, request);
    }

    /**
     * Chi tiết đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.GET_ORDER_INFO_FOR_CTXH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Chi tiết đơn hàng")
    public ClientResponse getOrderInfo(GetOrderInfoByCodeRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getOrderInfo(sessionData, request);
    }

    /**
     * Danh sách chương trình xếp hạng của đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_PROMO_CTXH_OF_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách chương trình xếp hạng của đơn hàng")
    public ClientResponse getListPromoByOrder(@RequestBody GetListPromoByOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getListPromoByOrder(sessionData, request);
    }

    /**
     * Điều chỉnh tích lũy chương trình xếp hạng trên đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.DIEU_CHINH_TICH_LUY_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách chương trình xếp hạng của đơn hàng")
    public ClientResponse dieuChinhTichLuyDamMe(@RequestBody DieuChinhDamMeRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.dieuChinhDamMeRequest(sessionData, request);
    }

    @RequestMapping(value = CTXHPath.EDIT_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editPromoCSDM(@RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editCTXH(sessionData, request);
    }

    @RequestMapping(value = CTXHPath.APPROVE_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approvePromoCSDM(@RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approvePromo(sessionData, request);
    }

    @RequestMapping(value = CTXHPath.STOP_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse stopPromoCSDM(@RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.stopPromo(sessionData, request);
    }

    @RequestMapping(value = CTXHPath.CANCEL_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelPromoCSDM(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.cancelPromo(sessionData, request);
    }

    @RequestMapping(value = CTXHPath.DELETE_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deletePromoCSDM(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.deletePromo(sessionData, request);
    }

    /**
     * Phiếu điều chỉnh tích lũy chương trình xếp hạng trên đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.CREATE_PHIEU_DIEU_CHINH_TICH_LUY_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tạo phiếu điều chỉnh tích lũy chương trình xếp hạng")
    public ClientResponse createPhieuDieuChinhTichLuyDamMe(@RequestBody CreatePhieuDieuChinhCSDMRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.createPhieuDieuChinhTichLuyDamMe(sessionData, request);
    }

    /**
     * Lịch sử điều chỉnh chương trình xếp hạng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.LICH_SU_DIEU_CHINH_TICH_LUY_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử điều chỉnh đơn hàng")
    public ClientResponse getLichSuDieuChinhTichLuyDamMe(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getLichSuChinhSuaTichLuyDamMe(sessionData, request);
    }

    /**
     * Danh sách nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách nguyên tắc")
    public ClientResponse filerVoucherReleasePeriod(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filerVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Tạo nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.CREATE_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tạo nguyên tắc")
    public ClientResponse createVoucherReleasePeriod(@RequestBody CreateVoucherReleasePeriodRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.createVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Chi tiết nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.GET_VOUCHER_RELEASE_PERIOD_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Chi tiết nguyên tắc")
    public ClientResponse getVoucherReleasePeriodDetail(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.getVoucherReleasePeriodDetail(sessionData, request);
    }

    /**
     * Kích hoạt nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.ACTIVE_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Kích hoạt nguyên tắc")
    public ClientResponse activeVoucherReleasePeriod(@RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.activeVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Hủy nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.CANCEL_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Hủy nguyên tắc")
    public ClientResponse cancelVoucherReleasePeriod(@RequestBody CancelRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.cancelVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Danh sách voucher
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_VOUCHER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách voucher by VRP")
    public ClientResponse filterVoucher(@RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterVoucherByVRP(sessionData, request);
    }

    /**
     * Chỉnh sửa nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.EDIT_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Chỉnh sửa nguyên tắc")
    public ClientResponse editVoucherReleasePeriod(@RequestBody EditVoucherReleasePeriodRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.editVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Kiểm tra nguyên tắc phát hành voucher
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.DOUBLE_CHECK_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Kiểm tra nguyên tắc phát hành voucher")
    public ClientResponse doubleVoucherReleasePeriod(@RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.doubleVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Tìm kiếm nguyên tắc phát hành voucher
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.SEARCH_VOUCHER_RELEASE_PERIOD, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tìm kiếm nguyên tắc phát hành voucher nguyên tắc phát hành voucher")
    public ClientResponse searchVoucherReleasePeriod(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.searchVoucherReleasePeriod(sessionData, request);
    }

    /**
     * Danh sách voucher được ưu đãi của BXH
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_VOUCHER_BY_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách voucher được ưu đãi của BXH")
    public ClientResponse filterVoucher(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterVoucherByCTXH(sessionData, request);
    }

    /**
     * Danh sách voucher được ưu đãi của BXH
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.ADD_AGENCY_TO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Bổ sung đại lý vào BXH")
    public ClientResponse addAgencyToCTXH(@RequestBody AddAgencyToCTXHRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.addAgencyToCTXH(sessionData, request);
    }

    /**
     * Kiểm tra ctxh
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.DOUBLE_CHECK_PROMO_CTXH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Kiểm tra nguyên tắc phát hành voucher")
    public ClientResponse doubleCheckPromoCTXH(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.doubleCheckPromoCTXH(sessionData, request);
    }

    /**
     * Danh sách BXH có thể tham gia
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.GET_LIST_CTXH_CAN_JOIN, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách BXH có thể tham gia")
    public ClientResponse getListCTXHCanJoin(BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.getListCTXHCanJoin(sessionData, request);
    }

    /**
     * Danh sách nguyên tắc
     *
     * @param request
     * @return
     */
    @RequestMapping(value = CTXHPath.FILTER_VOUCHER_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách voucher của đại lý")
    public ClientResponse filterVoucherAgency(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return ctxhService.filterVoucherAgency(sessionData, request);
    }
}