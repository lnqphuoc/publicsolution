package com.app.server.controller;


import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.PromoPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetListCTTLByTransactionRequest;
import com.app.server.data.request.cttl.GetResultCTTLOfAgencyRequest;
import com.app.server.data.request.damme.CreatePhieuDieuChinhCSDMRequest;
import com.app.server.data.request.damme.DieuChinhDamMeRequest;
import com.app.server.data.request.damme.GetListPromoByOrderRequest;
import com.app.server.data.request.damme.GetOrderInfoByCodeRequest;
import com.app.server.data.request.promo.*;
import com.app.server.enums.DamMeDieuChinhType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.service.CTTLService;
import com.app.server.service.DamMeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class DamMeController extends BaseController {
    private DamMeService damMeService;

    @Autowired
    public void setDamMeService(DamMeService damMeService) {
        this.damMeService = damMeService;
    }

    /**
     * Tạo đam mê
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.CREATE_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tạo đam mê")
    public ClientResponse createPromo(@RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.createDamMe(sessionData, request);
    }

    /**
     * Danh sách các đơn hàng được phép tích lũy vào đam mê
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.FILTER_CHILD_ORDER_DAM_ME_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách các đơn hàng được phép tích lũy vào đam mê")
    public ClientResponse filterTransactionByAgency(
            @RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.filterTransactionByAgency(sessionData, request);
    }

    /**
     * Bổ sung đơn hàng con vào đam mê
     *
     * @param request
     * @return
     */
    @ApiOperation("Bổ sung đơn hàng con vào đam mê")
    @RequestMapping(value = PromoPath.ADD_CHILD_ORDER_INTO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse addChildOrderIntoDamMe(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.addChildOrderIntoCTTL(sessionData, request);
    }

    /**
     * Từ chối đơn hàng con vào tích lũy
     *
     * @param request
     * @return
     */
    @ApiOperation("Từ chối đơn hàng con vào đam mê")
    @RequestMapping(value = PromoPath.REMOVE_CHILD_ORDER_INTO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse removeChildOrderIntoDamMe(@RequestBody AddChildOrderIntoCTLLRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.removeChildOrderIntoCTTL(sessionData, request);
    }

    /**
     * Danh sách thống kê đam mê
     *
     * @param request
     * @return
     */
    @ApiOperation("Danh sách thống kê đam mê")
    @RequestMapping(value = PromoPath.FILTER_RESULT_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterResultDamMe(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.filterResultDamMe(sessionData, request);
    }

    /**
     * Thống kê chi tiết đam mê
     *
     * @param request
     * @return
     */
    @ApiOperation("Thống kê chi tiết đam mê")
    @RequestMapping(value = PromoPath.FILTER_RESULT_DAM_ME_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterResultDamMeByAgency(@RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.filterResultCTTLByAgency(sessionData, request);
    }

    /**
     * Thống kê chi tiết đam của của Đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.FILTER_TRANSACTION_DAM_ME_BY_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Thống kê chi tiết đam mê của Đại lý")
    public ClientResponse filterTransactionDamMeByAgency(@RequestBody FilterAgencyCTTLRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.filterTransactionCTTLByAgency(sessionData, request);
    }

    /**
     * Danh sách đam mê của khoản tích lũy
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.GET_LIST_DAM_ME_BY_TRANSACTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách đam mê của khoản tích lũy")
    public ClientResponse getListDamMeByTransaction(@RequestBody GetListCTTLByTransactionRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.getListCTTLByTransaction(sessionData, request);
    }


    /**
     * Danh sách đam mê của đại lý
     *
     * @param request
     * @return
     */
    @RequestMapping(
            value = PromoPath.FILTER_DAM_ME_BY_AGENCY,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách đam mê của đại lý")
    public ClientResponse filterDamMeByAgency(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.filterDameMeByAgency(sessionData, request);
    }

    /**
     * Danh sách đam mê
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.FILTER_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách đam mê")
    public ClientResponse filerPromoDamMe(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.filterPromoDamMe(sessionData, request);
    }

    /**
     * Chi tiết đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.GET_ORDER_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Chi tiết đơn hàng")
    public ClientResponse getOrderInfo(GetOrderInfoByCodeRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.getOrderInfo(sessionData, request);
    }

    /**
     * Danh sách đam mê của đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.FILTER_PROMO_DAM_ME_OF_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách đam mê của đơn hàng")
    public ClientResponse getListPromoByOrder(@RequestBody GetListPromoByOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.getListPromoByOrder(sessionData, request);
    }

    /**
     * Điều chỉnh tích lũy đam mê trên đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.DIEU_CHINH_TICH_LUY_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Danh sách đam mê của đơn hàng")
    public ClientResponse dieuChinhTichLuyDamMe(@RequestBody DieuChinhDamMeRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.dieuChinhDamMeRequest(sessionData, request);
    }

    @RequestMapping(value = PromoPath.EDIT_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse editPromoCSDM(@RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.editDamMe(sessionData, request);
    }

    @RequestMapping(value = PromoPath.APPROVE_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse approvePromoCSDM(@RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.approvePromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.STOP_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse stopPromoCSDM(@RequestBody StopPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.stopPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.CANCEL_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse cancelPromoCSDM(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.cancelPromo(sessionData, request);
    }

    @RequestMapping(value = PromoPath.DELETE_PROMO_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse deletePromoCSDM(@RequestBody CancelPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return promoService.deletePromo(sessionData, request);
    }

    /**
     * Phiếu điều chỉnh tích lũy đam mê trên đơn hàng
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.CREATE_PHIEU_DIEU_CHINH_TICH_LUY_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Tạo phiếu điều chỉnh tích lũy đam mê")
    public ClientResponse createPhieuDieuChinhTichLuyDamMe(@RequestBody CreatePhieuDieuChinhCSDMRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.createPhieuDieuChinhTichLuyDamMe(sessionData, request);
    }

    /**
     * Lịch sử điều chỉnh đam mê
     *
     * @param request
     * @return
     */
    @RequestMapping(value = PromoPath.LICH_SU_DIEU_CHINH_TICH_LUY_DAM_ME, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Lịch sử điều chỉnh đơn hàng")
    public ClientResponse getLichSuDieuChinhTichLuyDamMe(@RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return damMeService.getLichSuChinhSuaTichLuyDamMe(sessionData, request);
    }

    /**
     * Điều chỉnh giá trị tích lũy của sản phẩm
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/promo/sp_dieu_chinh_gia_tri_san_pham", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation("Điều chỉnh giá trị tích lũy của sản phẩm")
    public ClientResponse dieuChinhGiaTriTichLuySanPham(@RequestBody String data) {
        SessionData sessionData = this.getSessionData();
        return damMeService.dieuChinhGiaTriTichLuySanPham(sessionData, data);
    }
}