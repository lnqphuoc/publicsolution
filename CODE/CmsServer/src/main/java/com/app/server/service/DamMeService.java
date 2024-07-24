package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.csdm.*;
import com.app.server.data.dto.cttl.*;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.product.ProgramProduct;
import com.app.server.data.entity.AgencyOrderDetailEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetListCTTLByTransactionRequest;
import com.app.server.data.request.cttl.GetResultCTTLOfAgencyRequest;
import com.app.server.data.request.cttl.TransactionCTTLRequest;
import com.app.server.data.request.damme.*;
import com.app.server.data.request.promo.AddChildOrderIntoCTLLRequest;
import com.app.server.data.request.promo.PromoTimeRequest;
import com.app.server.data.response.product.ProductMoneyResponse;
import com.app.server.database.DamMeDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DamMeService extends BaseService {

    private DamMeDB damMeDB;

    @Autowired
    public void setDamMeDB(DamMeDB damMeDB) {
        this.damMeDB = damMeDB;
    }

    private AccumulateCSDMService accumulateCSDMService;

    @Autowired
    public void setAccumulateCSDMService(AccumulateCSDMService accumulateCSDMService) {
        this.accumulateCSDMService = accumulateCSDMService;
    }

    /**
     * Danh sách đơn hàng con
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterTransactionByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
        try {
            if (request.getType() == CSDMTransactionType.DON_HANG.getId()) {
                return this.filterOrderByAgency(sessionData, request);
            } else if (request.getType() == CSDMTransactionType.HBTL.getId()) {
                return this.filterHBTLByAgency(sessionData, request);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse filterOrderByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setKey("t_agency.id");
        filterRequest.setType(TypeFilter.SELECTBOX);
        filterRequest.setValue(ConvertUtils.toString(request.getAgency_id()));
        request.getFilters().add(
                filterRequest
        );

        for (FilterRequest filter : request.getFilters()) {
            if (filter.getKey().equals("created_date")) {
                filter.setKey(
                        "t1.created_date"
                );
            }
        }

        JSONObject agency_csdm_info = this.promoDB.getAgencyCSDMInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new LinkedHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_csdm_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_CSDM, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("transaction_value", ConvertUtils.toLong(js.get("total_end_price")));
            js.put("type", CSDMTransactionType.DON_HANG.getId());

            js.put("transaction_status", js.get("status"));

            Integer status = mpTransaction.get(
                    CSDMTransactionType.DON_HANG.getKey() +
                            "_" +
                            ConvertUtils.toInt(js.get("id")));
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            js.put("created_date", js.get("order_created_date"));
            js.put("confirm_date", js.get("order_confirm_date"));

            this.getTransactionDetailCSDM(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    private void convertAgencyTransaction(Map<String, Integer> mpTransaction, JSONObject agency_csdm_info) {
        try {
            List<JSONObject> transactionList = JsonUtils.DeSerialize(
                    agency_csdm_info.get("data").toString(),
                    new TypeToken<List<JSONObject>>() {
                    }.getType()
            );

            LogUtil.printDebug(JsonUtils.Serialize(transactionList));

            for (JSONObject transaction : transactionList) {
                mpTransaction.put(
                        ConvertUtils.toString(transaction.get("type")) + "_" +
                                ConvertUtils.toInt(transaction.get("transactionId")),
                        ConvertUtils.toInt(transaction.get("status"))
                );
            }

            LogUtil.printDebug(JsonUtils.Serialize(mpTransaction));
        } catch (Exception ex) {
            LogUtil.printDebug("CTTL", ex);
        }
    }

    private void getTransactionDetailCSDM(JSONObject js) {
        /**
         * Giá trị
         * SL/DTT tích lũy
         * Đã thanh toán
         * Đã thanh toán hợp lệ
         * Còn phải thu
         * Còn phải thu hợp lệ
         * Kỳ hạn nợ
         */
        js.put("gia_tri", js.get("transaction_value"));
        js.put("so_luong_dtt_tich_luy", 0);
        js.put("da_thanh_toan", 0);
        js.put("da_thanh_toan_hop_le", 0);
        js.put("con_phai_thu", 0);
        js.put("con_phai_thu_hop_le", 0);
        js.put("ky_han_no", 0);
    }

    private ClientResponse filterHBTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setKey("t_agency.id");
        filterRequest.setType(TypeFilter.SELECTBOX);
        filterRequest.setValue(ConvertUtils.toString(request.getAgency_id()));
        request.getFilters().add(
                filterRequest
        );

        for (FilterRequest filter : request.getFilters()) {
            if (filter.getKey().equals("created_date")) {
                filter.setKey(
                        "t.created_date"
                );
            }
        }

        JSONObject agency_cttl_info = this.promoDB.getAgencyCSDMInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new ConcurrentHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_cttl_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_HBTL_CTTL, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("code",
                    ConvertUtils.toString(js.get("code"))
            );
            js.put("transaction_value", ConvertUtils.toLong(js.get("total_end_price")));
            js.put("type", CSDMTransactionType.HBTL.getId());

            Integer status = mpTransaction.get(
                    CSDMTransactionType.HBTL.getKey() + "_" +
                            ConvertUtils.toInt(js.get("id"))
            );
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            this.getTransactionDetailCSDM(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    /**
     * Bổ sung đơng hàng ăn tích lũy
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse addChildOrderIntoCTTL(
            SessionData sessionData,
            AddChildOrderIntoCTLLRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject promoJs = this.promoDB.getPromoJs(request.getPromo_id());
            if (promoJs == null ||
                    PromoCTTLStatus.STOPPED.getId() == ConvertUtils.toInt(promoJs.get("status"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            String condition_type = ConvertUtils.toString(promoJs.get("condition_type"));

            for (int iTrans = 0; iTrans < request.getTransactions().size(); iTrans++) {
                TransactionCTTLRequest transactionCTTLRequest = request.getTransactions().get(
                        iTrans
                );
                ClientResponse crAdd = this.insertTransactionCTTL(
                        request.getPromo_id(),
                        request.getAgency_id(),
                        transactionCTTLRequest,
                        sessionData.getId()
                );
                if (crAdd.failed()) {
                    crAdd.setMessage("[Thứ " + (iTrans + 1) + "]" + crAdd.getMessage());
                    return crAdd;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse insertTransactionCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            int staff_id) {
        try {
            CSDMTransactionType csdmTransactionType = CSDMTransactionType.from(
                    transactionCTTLRequest.getType()
            );
            if (csdmTransactionType == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            if (transactionCTTLRequest.getType() == CSDMTransactionType.DON_HANG.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrder(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null
                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                return this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        csdmTransactionType,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")),
                        ConvertUtils.toString(jsTransaction.get("code")),
                        jsTransaction,
                        staff_id,
                        0,
                        0,
                        0
                );
            } else if (transactionCTTLRequest.getType() == CSDMTransactionType.HBTL.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyHBTL(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                return this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        csdmTransactionType,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")) * -1,
                        ConvertUtils.toString(jsTransaction.get("code")),
                        jsTransaction,
                        staff_id, 0, 0,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")) * -1
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse insertTransaction(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CSDMTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id,
            long order_time,
            long dept_time,
            long tt
    ) {
        if (cttlTransactionType.getId() == CSDMTransactionType.DON_HANG.getId()) {
            return this.insertTransactionOrderCTTL(promo_id,
                    agency_id,
                    transactionCTTLRequest,
                    cttlTransactionType,
                    transaction_value,
                    code,
                    jsTransaction,
                    staff_id,
                    AppUtils.convertJsonToDate(jsTransaction.get("created_date")).getTime(),
                    DateTimeUtils.getMilisecondsNow(), tt);
        } else if (cttlTransactionType.getId() == CSDMTransactionType.HBTL.getId()) {
            return this.insertTransactionHBTLCTTL(promo_id,
                    agency_id,
                    transactionCTTLRequest,
                    cttlTransactionType,
                    transaction_value,
                    code,
                    jsTransaction,
                    staff_id, tt);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse insertTransactionOrderCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CSDMTransactionType CSDMTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id,
            long order_time,
            long dept_time,
            long tt
    ) {
        return this.accumulateCSDMService.addOrder(
                CSDMTransactionType.getKey(),
                agency_id,
                code,
                ConvertUtils.toInt(jsTransaction.get("id")),
                transaction_value,
                transactionCTTLRequest.getId(),
                CTTLTransactionSource.ADMIN.getId(),
                staff_id,
                order_time,
                dept_time,
                promo_id,
                tt,
                JsonUtils.Serialize(
                        this.getProductListProductTichLuyCSDM(
                                ConvertUtils.toInt(jsTransaction.get("id"))
                        )
                )
        );
    }

    private ClientResponse insertTransactionHBTLCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CSDMTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id, long tt
    ) {
        long now = DateTimeUtils.getMilisecondsNow();
        return this.accumulateCSDMService.addOrder(
                cttlTransactionType.getKey(),
                agency_id,
                code,
                0,
                transaction_value,
                transactionCTTLRequest.getId(),
                CTTLTransactionSource.ADMIN.getId(),
                staff_id,
                now,
                now,
                promo_id,
                tt,
                JsonUtils.Serialize(
                        this.getProductOfHBTLTichLuyCSDM(
                                transactionCTTLRequest.getId()
                        )
                )
        );
    }

    private List<JSONObject> getProductListProductTichLuyCSDM(int agencyOrderId) {
        return this.orderDB.getListProductNormal(agencyOrderId);
    }

    private List<JSONObject> getProductOfHBTLTichLuyCSDM(int id) {
        return this.orderDB.getProductListByHBTLForCSDM(id);
    }

    /**
     * Gỡ đơn hàng ăn tích lũy
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse removeChildOrderIntoCTTL(
            SessionData sessionData,
            AddChildOrderIntoCTLLRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject promoJs = this.promoDB.getPromoJs(request.getPromo_id());
            if (promoJs == null ||
                    PromoCTTLStatus.STOPPED.getId() == ConvertUtils.toInt(promoJs.get("status"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                    request.getPromo_id(), request.getAgency_id()
            );

            JSONObject promo_link_group = this.promoDB.getPromoLinkGroupByPromoId(
                    request.getPromo_id());

            for (TransactionCTTLRequest transactionCTTLRequest : request.getTransactions()) {
                ClientResponse crRemove = this.removeTransactionCTTL(
                        request.getPromo_id(),
                        request.getAgency_id(),
                        transactionCTTLRequest,
                        sessionData.getId(),
                        ConvertUtils.toInt(promoJs.get("status")),
                        promo_link_group == null ? false : true,
                        agency_cttl_info
                );
                if (crRemove.failed()) {
                    return crRemove;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse removeTransactionCTTL(
            int promo_id, int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            int staff_id,
            int status,
            boolean isLienKet,
            JSONObject agency_cttl_info
    ) {
        try {
            CSDMAccumulateType csdmAccumulateType = CSDMAccumulateType.from(
                    transactionCTTLRequest.getType()
            );
            if (csdmAccumulateType == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> transactionList = new ArrayList<>();

            if (agency_cttl_info != null) {
                transactionList = JsonUtils.DeSerialize(
                        agency_cttl_info.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
            }

            for (JSONObject transaction : transactionList) {
                LogUtil.printDebug(JsonUtils.Serialize(transaction));
                if (ConvertUtils.toInt(transaction.get("transactionId")) == transactionCTTLRequest.getId()
                        && transactionCTTLRequest.getType() == CSDMTransactionType.fromKey(ConvertUtils.toString(transaction.get("type"))).getId()
                        && ConvertUtils.toInt(transaction.get("isDivide")) == 1) {
                    ClientResponse crRemove = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crRemove.setMessage("Vui lòng không gỡ khoản tích lũy được tách bởi CTTL khác");
                    return crRemove;
                }
            }

            String code = "";
            String type = "";
            int order_id = 0;
            if (transactionCTTLRequest.getType() == CSDMTransactionType.DON_HANG.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrderDeptById(
                        transactionCTTLRequest.getId()
                );
                code = ConvertUtils.toString(jsTransaction.get("dept_code"));
                type = CSDMTransactionType.DON_HANG.getKey();
                order_id = ConvertUtils.toInt(jsTransaction.get("agency_order_id"));
            } else if (transactionCTTLRequest.getType() == CSDMAccumulateType.PHIEU_TANG.getId()) {
                code = "DEPT" + transactionCTTLRequest.getId();
                type = CSDMTransactionType.PHIEU.getKey();
            } else if (transactionCTTLRequest.getType() == CSDMTransactionType.HBTL.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrderDeptById(
                        transactionCTTLRequest.getId()
                );
                code = ConvertUtils.toString(jsTransaction.get("dept_code"));
                type = CSDMTransactionType.HBTL.getKey();
            } else if (transactionCTTLRequest.getType() == CSDMAccumulateType.PHIEU_GIAM.getId()) {
                code = "DEPT" + transactionCTTLRequest.getId();
                type = CSDMTransactionType.PHIEU.getKey();
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách thống kê CTTL
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse filterResultDamMe(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_RESULT_DAM_ME, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotal(query);
            for (JSONObject js : records) {
                Program program = this.dataManager.getProgramManager().getMpDamMe().get(
                        ConvertUtils.toInt(js.get("id"))
                );
                if (program == null) {
                    continue;
                }

                js.put("so_han_muc", this.promoDB.getListCSDMLimit(
                        ConvertUtils.toInt(js
                                .get("id"))).size());

                boolean is_cttl_product = PromoConditionCTTLType.from(
                        ConvertUtils.toString(js.get("condition_type"))).isCTTLProduct();
                boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                        ConvertUtils.toString(js.get("condition_type"))).isCTTLProductQuantity();

                this.summaryCTTL(
                        js,
                        is_cttl_product,
                        program.getMpProduct(),
                        is_cttl_product_quantity);
            }
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject summaryCTTL(
            JSONObject promo,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product_quantity) {
        int promo_id = ConvertUtils.toInt(promo.get("id"));
        List<JSONObject> agency_cttl_info_list = this.promoDB.getListAgencyOfCSDMJs(
                promo_id
        );


        int tong_khach_hang_dat = 0;
        int tong_khach_hang_tham_gia = agency_cttl_info_list.size();
        long tong_doanh_thu_tich_luy = 0;
        long tong_doanh_thu_tich_luy_hop_le = 0;
        long tong_doanh_thu_san_pham_tich_luy = 0;
        long tong_doanh_thu_san_pham_hop_le = 0;
        int tong_san_pham_tich_luy = 0;
        int tong_san_pham_hop_le = 0;
        long tong_gia_tri_tra_thuong = 0;
        long tong_thanh_toan_hop_le = 0;

        for (JSONObject agency_cttl_info : agency_cttl_info_list) {
            CSDMAgencyReward agencyReward = new CSDMAgencyReward();
            agencyReward.setId(ConvertUtils.toInt(agency_cttl_info.get("agency_id")));
            if (agency_cttl_info.get("reward") != null && !agency_cttl_info.get("reward").toString().isEmpty()) {
                agencyReward.parseReward(
                        JsonUtils.DeSerialize(agency_cttl_info.get("reward").toString(), JSONObject.class)
                );
            }
            if (agencyReward.isDat()) {
                tong_khach_hang_dat++;
            }

            List<CSDMTransactionData> transactionDataList =
                    JsonUtils.DeSerialize(
                            agency_cttl_info.get("data").toString(),
                            new TypeToken<List<CSDMTransactionData>>() {
                            }.getType()
                    );
            for (CSDMTransactionData cttlTransactionData : transactionDataList) {
                this.parseCTTLTransactionData(cttlTransactionData);
            }


            /**
             * tính kết quả tích lũy
             */
            convertResultCSDMOfAgencyResponse(
                    promo_id,
                    ConvertUtils.toString(promo.get("condition_type")),
                    agency_cttl_info,
                    is_cttl_product,
                    mpProductInPromo,
                    transactionDataList,
                    is_cttl_product_quantity,
                    0
            );

            CSDMAgencyResult cttlAgency = CSDMAgencyResult.from(
                    agency_cttl_info
            );

            tong_doanh_thu_tich_luy += agencyReward.getTotalValue();
            tong_doanh_thu_tich_luy_hop_le += transactionDataList.stream().reduce(
                    0L,
                    (total, object) -> total +
                            (object.isHopLe(is_cttl_product_quantity, 0) == true ? object.sumGiaTriThamGia(is_cttl_product_quantity) : 0),
                    Long::sum
            );

            tong_doanh_thu_san_pham_tich_luy += transactionDataList.stream().reduce(
                    0L,
                    (total, object) -> total +
                            object.sumDoanhThuThuanSanPhamTichLuy(
                                    is_cttl_product,
                                    mpProductInPromo),
                    Long::sum
            );

            tong_doanh_thu_san_pham_hop_le += transactionDataList.stream().reduce(
                    0L,
                    (total, object) -> total +
                            (object.isHopLe(is_cttl_product_quantity, 0) == true ? object.sumDoanhThuThuanSanPhamTichLuy(
                                    is_cttl_product,
                                    mpProductInPromo
                            ) : 0),
                    Long::sum
            );

            tong_san_pham_tich_luy += transactionDataList.stream().reduce(
                    0,
                    (total, object) -> total +
                            object.sumSanPhamTichLuy(),
                    Integer::sum
            );

            tong_thanh_toan_hop_le += cttlAgency.getTong_thanh_toan_hop_le();

            /**
             * Tích lũy hợp lệ thì ghi nhận sản phẩm hợp lệ
             */
            tong_san_pham_hop_le += transactionDataList.stream().reduce(
                    0,
                    (total, object) -> total +
                            (object.isHopLe(is_cttl_product_quantity, 0) == true ? object.sumSanPhamTichLuy() : 0),
                    Integer::sum
            );

            tong_gia_tri_tra_thuong += agencyReward.getRewardValue();
        }
        promo.put("tong_khach_hang_tham_gia", tong_khach_hang_tham_gia);
        promo.put("tong_khach_hang_dat", tong_khach_hang_dat);
        promo.put("tong_doanh_thu_tich_luy", tong_doanh_thu_tich_luy);
        promo.put("tong_doanh_thu_tich_luy_hop_le", tong_doanh_thu_tich_luy_hop_le);
        promo.put("tong_doanh_thu_san_pham_tich_luy", tong_doanh_thu_san_pham_tich_luy);
        promo.put("tong_doanh_thu_san_pham_hop_le", tong_doanh_thu_san_pham_hop_le);
        promo.put("tong_san_pham_tich_luy", tong_san_pham_tich_luy);
        promo.put("tong_san_pham_hop_le", tong_san_pham_hop_le);
        promo.put("tong_gia_tri_tra_thuong", tong_gia_tri_tra_thuong);
        return promo;
    }

    private void parseCTTLTransactionData(CSDMTransactionData cttlTransactionData) {
        try {
            if (cttlTransactionData.getType().equals(CSDMTransactionType.DON_HANG.getKey())) {
                cttlTransactionData.setTransaction_type(CSDMTransactionType.DON_HANG.getId());
                cttlTransactionData.setAccumulate_type(
                        CSDMAccumulateType.DON_HANG.getId()
                );
            } else if (cttlTransactionData.getType().equals(CSDMTransactionType.PHIEU.getKey())) {
                cttlTransactionData.setTransaction_type(CSDMTransactionType.PHIEU.getId());
                if (cttlTransactionData.getUpdateTotalValue() > 0) {
                    cttlTransactionData.setAccumulate_type(
                            CSDMAccumulateType.PHIEU_TANG.getId()
                    );
                } else {
                    cttlTransactionData.setAccumulate_type(
                            CSDMAccumulateType.PHIEU_GIAM.getId()
                    );
                }
            } else if (cttlTransactionData.getType().equals(CSDMTransactionType.HBTL.getKey())) {
                cttlTransactionData.setTransaction_type(CSDMTransactionType.HBTL.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
                cttlTransactionData.setAccumulate_type(
                        CSDMAccumulateType.HBTL.getId()
                );
            }

            JSONObject rsCTTLTransactionInfo = this.getCTTLTransactionInfo(
                    cttlTransactionData
            );

            cttlTransactionData.setTransaction_value(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("gia_tri"))
            );
            cttlTransactionData.setPayment_value(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("thanh_toan"))
            );
            cttlTransactionData.setPayment_date(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("ngay_thanh_toan"))
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
    }

    /**
     * Thống kê kết quả tích lũy theo đại lý
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse filterResultCTTLByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            JSONObject promo = this.promoDB.getPromoJs(
                    request.getId()
            );
            if (promo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            /**
             * ds hạn mức
             */
            List<JSONObject> hamMucList = new ArrayList<>();
            List<JSONObject> promoLimitList = this.damMeDB.getPromoLimitList(request.getId());
            List<JSONObject> promoOfferList = this.damMeDB.getPromoOfferList(request.getId());
            for (int iLimit = 0; iLimit < promoLimitList.size(); iLimit++) {
                JSONObject js = new JSONObject();
                js.put("level", iLimit + 1);
                js.put("from", promoLimitList.get(iLimit).get("from_value"));
                js.put("to", 0);
                js.put("discount", promoOfferList.get(iLimit).get("offer_value"));
                hamMucList.add(js);
            }

            promo.put("promo_limit_info", hamMucList);

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getId());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                promo.put("gift_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                ));
            }

            boolean is_cttl_product = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProduct();
            boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProductQuantity();

            promo.put("total_limit", this.promoDB.getTotalLimit(request.getId()));

            JSONObject data = new JSONObject();
            data.put("promo", promo);
            JSONObject result = new JSONObject();
            List<JSONObject> agency_cttl_info_list = this.promoDB.getListAgencyOfCSDMJs(
                    request.getId()
            );
            Map<Integer, CSDMAgencyReward> agencyRewardList = new ConcurrentHashMap<>();

            int tong_khach_hang_tham_gia = agency_cttl_info_list.size();
            int tong_khach_hang_dat = 0;
            long tong_gia_tri_tich_luy = 0;
            long tong_thanh_toan_hop_le = 0;
            long tong_uu_dai_tra_huong = 0;
            long tong_uu_dai_duoc_huong = 0;
            long tong_thanh_toan_con_thieu = 0;
            int tong_so_luong_ban_dau = 0;
            int tong_so_luong_tham_gia = 0;
            List<CSDMAgencyResult> cttlAgencyList = new ArrayList<>();
            for (JSONObject agency_cttl_info : agency_cttl_info_list) {
                CSDMAgencyReward agencyReward = new CSDMAgencyReward();
                agencyReward.setId(ConvertUtils.toInt(agency_cttl_info.get("agency_id")));
                if (agency_cttl_info.get("reward") != null && !agency_cttl_info.get("reward").toString().isEmpty()) {
                    agencyReward.parseReward(
                            JsonUtils.DeSerialize(agency_cttl_info.get("reward").toString(), JSONObject.class)
                    );
                }
                if (agencyReward.getLimitValue() > 0) {
                    tong_khach_hang_dat++;
                    tong_uu_dai_duoc_huong += agencyReward.getRewardValue();
                }

//                List<CSDMTransactionData> transactionDataList =
//                        JsonUtils.DeSerialize(
//                                agency_cttl_info.get("data").toString(),
//                                new TypeToken<List<CSDMTransactionData>>() {
//                                }.getType()
//                        );
//                for (CSDMTransactionData cttlTransactionData : transactionDataList) {
//                    this.parseCTTLTransactionData(cttlTransactionData);
//                }

                /**
                 * tính kết quả tích lũy
                 */
//                convertResultCSDMOfAgencyResponse(
//                        request.getId(),
//                        ConvertUtils.toString(promo.get("condition_type")),
//                        agency_cttl_info,
//                        is_cttl_product,
//                        new HashMap<>(),
//                        transactionDataList,
//                        is_cttl_product_quantity,
//                        0
//                );

                CSDMAgencyResult cttlAgency = CSDMAgencyResult.from(
                        agency_cttl_info
                );
                cttlAgency.setId(agencyReward.getId());
                cttlAgency.setTong_gia_tri_tham_gia(
                        agencyReward.getTotalValue()
                );
                cttlAgency.setTong_gia_tri_tich_luy(
                        agencyReward.getTotalValue()
                );
                cttlAgency.setTong_uu_dai_duoc_huong(
                        agencyReward.getRewardValue()
                );
                cttlAgency.setHan_muc_duoc_huong(
                        agencyReward.getLimitValue()
                );
                cttlAgency.setPhan_tram_duoc_huong(
                        agencyReward.getPercentValue()
                );

                tong_thanh_toan_hop_le += cttlAgency.getTong_thanh_toan_hop_le();
                tong_thanh_toan_con_thieu += cttlAgency.getTong_thanh_toan_con_thieu();
                tong_gia_tri_tich_luy += cttlAgency.getTong_gia_tri_tich_luy();
                tong_so_luong_ban_dau +=
                        ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")) < 0 ?
                                0 :
                                ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity"));
                tong_so_luong_tham_gia +=
                        this.getSoLuongThamGia(
                                ConvertUtils.toInt(agency_cttl_info.get("update_join_quantity")),
                                ConvertUtils.toInt(agencyReward.getRewardValue()));

                cttlAgencyList.add(cttlAgency);
                agencyRewardList.put(agencyReward.getId(), agencyReward);
            }

            tong_uu_dai_tra_huong = this.damMeDB.getTongUuDaiDamMeOfPromo(
                    request.getId()
            );

            JSONObject jsResult = new JSONObject();
            jsResult.put("tong_khach_hang_tham_gia", tong_khach_hang_tham_gia);
            jsResult.put("tong_khach_hang_dat", tong_khach_hang_dat);
            jsResult.put("tong_gia_tri_tich_luy", tong_gia_tri_tich_luy);
            jsResult.put("tong_thanh_toan_hop_le", tong_thanh_toan_hop_le);
            jsResult.put("tong_uu_dai_tra_thuong", tong_uu_dai_tra_huong);
            jsResult.put("tong_so_luong_ban_dau", tong_so_luong_ban_dau);
            jsResult.put("tong_so_luong_tham_gia", tong_so_luong_tham_gia);
            data.put("result", jsResult);

            int business_department_id = ConvertUtils.toInt(
                    this.filterUtils.getValueByKey(
                            request.getFilters(),
                            "business_department_id"));
            String search = this.filterUtils.getValueByType(
                    request.getFilters(),
                    "search");
            cttlAgencyList = cttlAgencyList.stream().filter(
                    x -> filterCTTLAgency(
                            x,
                            search,
                            0,
                            business_department_id)
            ).collect(Collectors.toList());

            int from = this.appUtils.getOffset(request.getPage());
            int to = from + ConfigInfo.PAGE_SIZE;
            int total = cttlAgencyList.size();
            if (to > total) {
                to = total;
            }
            List<CSDMAgencyResult> records = new ArrayList<>();
            if (from < total) {
                records = cttlAgencyList.subList(
                        from,
                        to);
                for (CSDMAgencyResult csdmAgencyResult : records) {
                    csdmAgencyResult.setUu_dai_duoc_huong(
                            this.getTongUuDaiDuocHuongOfCSDM(
                                    request.getId(),
                                    csdmAgencyResult.getId()
                            )
                    );
                }
            }

            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private long getTongUuDaiDuocHuongOfCSDM(int promo_id, int agency_id) {
        return this.damMeDB.getTongUuDaiDamMe(
                promo_id, agency_id
        );
    }

    private void convertResultCSDMOfAgencyResponse(
            int promo_id,
            String condition_type,
            JSONObject js,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product_quantity,
            long payment_date_data) {
        long tong_doanh_thu_thuan_tham_gia = this.getTongDoanhThuThuanThamGia(
                condition_type, js,
                transactionDataList,
                is_cttl_product_quantity
        );

        long tong_gia_tri_tham_gia = this.getTongGiaTriThamGiaOfAgency(
                condition_type, js,
                transactionDataList,
                is_cttl_product_quantity
        );

        js.put("tong_gia_tri_tham_gia", tong_gia_tri_tham_gia
        );
        JSONObject summaryTransactionInfo = this.
                summaryTransactionInfo(
                        condition_type,
                        transactionDataList,
                        mpProductInPromo,
                        is_cttl_product,
                        is_cttl_product_quantity, payment_date_data);
        long tong_gia_tri_tich_luy = this.getTongGiaTriTichLuyOfAgency(
                condition_type,
                js,
                is_cttl_product,
                mpProductInPromo,
                transactionDataList,
                is_cttl_product_quantity
        );
        js.put("tong_gia_tri_tich_luy", tong_gia_tri_tich_luy

        );
        long tong_dtt_tich_luy = this.getTongDTTTichLuyOfAgency(
                condition_type,
                js,
                is_cttl_product,
                mpProductInPromo,
                transactionDataList,
                is_cttl_product_quantity
        );
        js.put("tong_dtt_tich_luy", tong_dtt_tich_luy

        );
        long tong_thanh_toan = this.getTongThanhToanOfAgency(
                condition_type,
                js,
                transactionDataList
        );
        js.put("tong_thanh_toan", tong_thanh_toan
        );
        long tong_thanh_toan_hop_le = this.getTongThanhToanHopLeOfAgency(
                condition_type,
                js,
                transactionDataList,
                is_cttl_product,
                mpProductInPromo,
                is_cttl_product_quantity,
                payment_date_data
        );
        js.put("tong_thanh_toan_hop_le", tong_thanh_toan_hop_le
        );

        js.put("tong_thanh_toan_con_thieu",
                summaryTransactionInfo.get("tong_thanh_toan_con_thieu")
        );
        js.put("han_muc_uu_dai", null);
        js.put("uu_dai", this.getUuDaiOfAgency(
                promo_id,
                condition_type,
                js
        ));
        js.put("tong_uu_dai_da_tra_thuong", 0);
        js.put("tong_uu_dai_duoc_thuong", 0);
        js.put("tong_doanh_thu_thuan_tham_gia", tong_doanh_thu_thuan_tham_gia);
    }

    private JSONObject summaryTransactionInfo(
            String condition_type,
            List<CSDMTransactionData> transactionDataList,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity,
            long payment_date_data) {
        JSONObject result = new JSONObject();
        try {
            long tong_thanh_toan = 0;
            long tong_gia_tri = 0;
            long tong_thanh_toan_hop_le = 0;
            long tong_thanh_toan_con_thieu = 0;
            for (CSDMTransactionData cttlTransactionData : transactionDataList) {
                JSONObject rsTransactionInfo = this.getCTTLTransactionInfo(cttlTransactionData);
                long gia_tri = ConvertUtils.toLong(rsTransactionInfo.get("gia_tri"));
                long thanh_toan = ConvertUtils.toLong(rsTransactionInfo.get("thanh_toan"));
                tong_gia_tri += gia_tri;
                tong_thanh_toan += thanh_toan;
                tong_thanh_toan_hop_le +=
                        (cttlTransactionData.getPayment_date() > payment_date_data ||
                                thanh_toan < gia_tri) ?
                                0 :
                                tinhDTTTichLuyOfTransaction(
                                        cttlTransactionData,
                                        cttlTransactionData.getTransaction_type(),
                                        mpProductInPromo,
                                        is_cttl_product,
                                        is_cttl_product_quantity
                                );
                long gia_tri_tich_luy = 0;
                if (PromoConditionCTTLType.DTT.getKey().equals(condition_type)) {
                    gia_tri_tich_luy = cttlTransactionData.sumGiaTriTichLuy(
                            is_cttl_product,
                            mpProductInPromo,
                            is_cttl_product_quantity
                    );
                } else if (PromoConditionCTTLType.ORDER_PRICE.getKey().equals(condition_type)) {
                    gia_tri_tich_luy = cttlTransactionData.sumGiaTriTichLuy(
                            is_cttl_product,
                            mpProductInPromo,
                            is_cttl_product_quantity);
                } else if (PromoConditionCTTLType.PRODUCT_PRICE.getKey().equals(condition_type)) {
                    gia_tri_tich_luy =
                            cttlTransactionData.sumGiaTriTichLuy(
                                    is_cttl_product,
                                    mpProductInPromo,
                                    is_cttl_product_quantity
                            );
                } else if (PromoConditionCTTLType.PRODUCT_QUANTITY.getKey().equals(condition_type)) {
                    gia_tri_tich_luy = cttlTransactionData.sumGiaTriTichLuy(
                            is_cttl_product,
                            mpProductInPromo,
                            is_cttl_product_quantity
                    );
                }
                tong_thanh_toan_con_thieu += gia_tri_tich_luy == 0 ? 0 :
                        (gia_tri - thanh_toan);
            }

            result.put("tong_gia_tri", tong_gia_tri);
            result.put("tong_thanh_toan", tong_thanh_toan);
            result.put("tong_thanh_toan_hop_le", tong_thanh_toan_hop_le);
            result.put("tong_thanh_toan_con_thieu", tong_thanh_toan_con_thieu);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return result;
    }

    private JSONObject getCTTLTransactionInfo(
            CSDMTransactionData cttlTransactionData
    ) {
        JSONObject rs = new JSONObject();
        long gia_tri = 0;
        long thanh_toan = 0;
        long ngay_thanh_toan = 0;
        long uu_dai_dam_me = 0;
        try {
            if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.DON_HANG.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptOrderInfoByCode(
                        cttlTransactionData.getCode()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("payment_value"));
                    ngay_thanh_toan = transaction_info.get("payment_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("payment_date"))).getTime();
                    uu_dai_dam_me = ConvertUtils.toLong(transaction_info.get("uu_dai_dam_me"));
                }
            } else if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.PHIEU.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptOrderInfoByCode(
                        cttlTransactionData.getCode()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("payment_value"));
                    ngay_thanh_toan = transaction_info.get("payment_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("payment_date"))).getTime();
                }
            } else if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.HBTL.getId()) {
                JSONObject transaction_info = this.orderDB.getAgencyHBTL(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("total_end_price"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("total_end_price"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        rs.put("gia_tri", gia_tri);
        rs.put("thanh_toan", thanh_toan);
        rs.put("ngay_thanh_toan", ngay_thanh_toan);
        rs.put("uu_dai_dam_me", uu_dai_dam_me);
        return rs;
    }

    private long getUuDaiOfAgency(
            int promo_id,
            String condition_type,
            JSONObject js) {
        try {

        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongGiaTriTichLuyOfAgency(
            String condition_type,
            JSONObject js,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product_quantity) {
        try {
            if (PromoConditionCTTLType.DTT.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.ORDER_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_QUANTITY.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongDTTTichLuyOfAgency(
            String condition_type,
            JSONObject js,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product_quantity) {
        try {
            if (PromoConditionCTTLType.DTT.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.ORDER_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_QUANTITY.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTTichLuy(
                                        is_cttl_product,
                                        mpProductInPromo,
                                        is_cttl_product_quantity),
                        Long::sum
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongDoanhThuThuanThamGia(
            String condition_type,
            JSONObject js,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product_quantity) {
        try {
            if (PromoConditionCTTLType.DTT.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTThamGia(
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.ORDER_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_QUANTITY.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumDTTThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongGiaTriThamGiaOfAgency(
            String condition_type,
            JSONObject js,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product_quantity) {
        try {
            if (PromoConditionCTTLType.DTT.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriThamGia(
                                        is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.ORDER_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_PRICE.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            } else if (PromoConditionCTTLType.PRODUCT_QUANTITY.getKey().equals(condition_type)) {
                return transactionDataList.stream().reduce(
                        0L,
                        (total, object) -> total +
                                object.sumGiaTriThamGia(is_cttl_product_quantity),
                        Long::sum
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongThanhToanOfAgency(
            String condition_type,
            JSONObject js,
            List<CSDMTransactionData> transactionDataList) {
        try {
            return transactionDataList.stream().reduce(
                    0L,
                    (total, object) -> total + object.getTt(), Long::sum
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getTongThanhToanHopLeOfAgency(
            String condition_type,
            JSONObject js,
            List<CSDMTransactionData> transactionDataList,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product_quantity,
            long payment_date_data) {
        try {
            long tong_thanh_toan_hop_le = 0;
            JSONObject jsResultTransactionInfo = summaryTransactionInfo(
                    condition_type,
                    transactionDataList,
                    mpProductInPromo,
                    is_cttl_product,
                    is_cttl_product_quantity,
                    payment_date_data);
            tong_thanh_toan_hop_le += ConvertUtils.toLong(
                    jsResultTransactionInfo.get("tong_thanh_toan_hop_le"));
            return tong_thanh_toan_hop_le;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    /**
     * Danh sách khoản tích lũy của đại lý
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterTransactionCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
        try {
            JSONObject promo = this.promoDB.getPromoJs(
                    request.getPromo_id()
            );

            if (promo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            ProductCache gift_info = null;
            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getPromo_id());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                gift_info = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id")));
                promo.put("gift_info", gift_info);
            }

            Program program = this.dataManager.getProgramManager().getMpDamMe().get(
                    request.getPromo_id()
            );
            if (program == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            PromoTimeRequest payment_date_data = new PromoTimeRequest();

            boolean is_cttl_product = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProduct();

            boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProductQuantity();

            /**
             * ds hạn mức
             */
            List<JSONObject> promoLimitInfoList = this.getPromoLimitInfo(
                    request.getPromo_id(),
                    this.promoDB.getListPromoLimit(
                            request.getPromo_id()
                    )
            );

            List<JSONObject> hamMucList = new ArrayList<>();
            for (int iLimit = program.getLtProgramLimit().size() - 1; iLimit >= 0; iLimit--) {
                JSONObject js = new JSONObject();
                ProgramLimit programLimit = program.getLtProgramLimit().get(iLimit);
                js.put("level", programLimit.getLevel());
                js.put("from", programLimit.getLtProgramProductGroup().get(0).getFromValue());
                js.put("to", programLimit.getLtProgramProductGroup().get(0).getEndValue());
                js.put("discount", programLimit.getLtProgramProductGroup().get(0).getOfferValue());
                hamMucList.add(js);
            }

            promo.put("promo_limit_info", hamMucList);

            /**
             * Có lặp hay không
             */
            promo.put("is_loop_offer", this.checkPromoLoopOffer(
                    ConvertUtils.toString(promo.get("form_of_reward")),
                    promoLimitInfoList,
                    ConvertUtils.toString(promo.get("promo_end_value_type"))
            ));

            JSONObject promo_link_group = this.promoDB.getPromoLinkGroupByPromoId(
                    request.getPromo_id());

            /**
             * Số hạn mức của CTTL
             */
            promo.put("total_limit", this.promoDB.getTotalLimit(request.getPromo_id()));
            JSONObject data = new JSONObject();

            data.put("promo", promo);

            /**
             * Thông tin đại lý
             */
            data.put("agency_info",
                    this.dataManager.getAgencyManager().getAgencyBasicData(
                            request.getAgency_id()
                    )
            );

            long tong_gia_tri_tham_gia = 0;
            long tong_gia_tri_tich_luy = 0;
            long tong_gia_tri_tich_luy_da_thanh_toan = 0;
            long tong_thanh_toan = 0;
            long tong_thanh_toan_con_thieu = 0;
            long tong_thanh_toan_hop_le = 0;
            List<CSDMAgencyOffer> han_muc_uu_dai = new ArrayList<>();
            long tong_uu_dai_duoc_huong = 0;

            List<JSONObject> records = new ArrayList<>();
            int total = 0;
            JSONObject agency_cttl_info = this.promoDB.getAgencyCSDMInfoJs(
                    request.getPromo_id(),
                    request.getAgency_id()
            );
            if (agency_cttl_info == null) {
                return ClientResponse.success(data);
            }


            if (agency_cttl_info != null) {
                List<JSONObject> transactionList = JsonUtils.DeSerialize(
                        agency_cttl_info.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );

                LogUtil.printDebug(JsonUtils.Serialize(transactionList));
                List<CSDMTransactionData> transactionDataList = new ArrayList<>();
                for (JSONObject transaction : transactionList) {
                    CSDMTransactionData cttlTransactionData = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(transaction),
                            CSDMTransactionData.class
                    );
                    this.parseCTTLTransactionData(cttlTransactionData);
                    cttlTransactionData.finishPayment();

                    JSONObject transaction_info = null;
                    if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.DON_HANG.getId()) {
                        transaction_info = this.orderDB.getAgencyOrderBasicByAgencyOrderDeptId(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.PHIEU.getId()) {
                        transaction_info = this.damMeDB.getAgencyCSDMModifyValue(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CSDMTransactionType.HBTL.getId()) {
                        transaction_info = this.orderDB.getAgencyHBTL(
                                cttlTransactionData.getTransactionId()
                        );
                    }

                    long gia_tri = this.getGiaTriOfCTTLTransaction(
                            cttlTransactionData.getTransaction_type(),
                            transaction_info
                    );
                    long so_tien_thanh_toan = this.getDaThanhToanOfCTTLTransaction(
                            cttlTransactionData.getTransaction_type(),
                            transaction_info,
                            gia_tri
                    );

                    transactionDataList.add(cttlTransactionData);

                    if (is_cttl_product_quantity) {
                        tong_gia_tri_tham_gia += cttlTransactionData.sumGiaTriThamGia(
                                is_cttl_product_quantity
                        );
                    } else {
                        tong_gia_tri_tham_gia += gia_tri;
                    }

                    tong_thanh_toan_hop_le +=
                            (cttlTransactionData.getPayment_date() > payment_date_data.getEnd_date_millisecond() ||
                                    so_tien_thanh_toan < gia_tri) ? 0 :
                                    this.tinhDTTTichLuyOfTransaction(
                                            cttlTransactionData,
                                            cttlTransactionData.getTransaction_type(),
                                            program.getMpProduct(),
                                            is_cttl_product,
                                            is_cttl_product_quantity
                                    );
                    tong_thanh_toan += cttlTransactionData.sumGiaTriThanhToan();
                }

                int type = ConvertUtils.toInt(
                        this.filterUtils.getValueByKey(
                                request.getFilters(),
                                "type"));
                int status = ConvertUtils.toInt(
                        this.filterUtils.getValueByKey(
                                request.getFilters(),
                                "status"));
                String search = this.filterUtils.getValueByType(
                        request.getFilters(),
                        "search");
                PromoTimeRequest timeRequest = this.filterUtils.getValueByTime(
                        request.getFilters(),
                        "created_date");

                transactionDataList = transactionDataList.stream().filter(
                        x -> filterCTTLTransaction(
                                x,
                                search,
                                type,
                                status,
                                timeRequest)
                ).collect(Collectors.toList());

                Collections.sort(transactionDataList, (a, b) -> a.getCreatedTime() < b.getCreatedTime() ? -1 : a.getCreatedTime() == b.getCreatedTime() ? 0 : 1);

                LogUtil.printDebug(JsonUtils.Serialize(transactionDataList));
                int from = this.appUtils.getOffset(request.getPage());
                int to = from + ConfigInfo.PAGE_SIZE;
                total = transactionDataList.size();
                if (to > total) {
                    to = total;
                }
                List<CSDMTransactionData> subTransactionDataList;
                if (from < total) {
                    subTransactionDataList = transactionDataList.subList(
                            from,
                            to);
                } else {
                    subTransactionDataList = new ArrayList<>();
                }

                for (int iData = 0; iData < subTransactionDataList.size(); iData++) {
                    CSDMTransactionData cttlTransactionData = subTransactionDataList.get(iData);
                    CSDMTransactionInfo cttlTransactionInfo = new CSDMTransactionInfo();
                    cttlTransactionInfo.setId(
                            iData + 1
                    );
                    cttlTransactionInfo.setTransaction_id(
                            cttlTransactionData.getTransactionId());
                    this.convertTransactionDataToInfo(
                            request.getAgency_id(),
                            request.getPromo_id(),
                            cttlTransactionInfo,
                            cttlTransactionData
                    );

                    JSONObject transaction_info = null;
                    if (cttlTransactionInfo.getType() == CSDMTransactionType.DON_HANG.getId()) {
                        transaction_info = this.orderDB.getAgencyOrder(
                                cttlTransactionInfo.getTransaction_id()
                        );
                        transaction_info.put("uu_dai_dam_me",
                                this.orderDB.getUuDaiDamMeByPromo(
                                        cttlTransactionInfo.getTransaction_id(),
                                        request.getPromo_id()));
                    } else if (cttlTransactionInfo.getType() == CSDMTransactionType.PHIEU.getId()) {
                        transaction_info = this.damMeDB.getAgencyCSDMModifyValue(
                                cttlTransactionInfo.getTransaction_id()
                        );
                        if (transaction_info != null) {
                            transaction_info.put("total_end_price", transaction_info.get("data"));
                            transaction_info.put("description", transaction_info.get("note"));
                        }
                    } else if (cttlTransactionInfo.getType() == CSDMTransactionType.HBTL.getId()) {
                        transaction_info = this.orderDB.getAgencyHBTL(
                                cttlTransactionInfo.getTransaction_id()
                        );
                    }

                    if (transaction_info == null) {
                        continue;
                    }

                    transaction_info.put("confirm_date", cttlTransactionData.getPayment_date());
                    transaction_info.put("accumulate_type", cttlTransactionData.getAccumulate_type());
                    this.calculateResultTransactionCTTL(
                            cttlTransactionInfo,
                            cttlTransactionData,
                            transaction_info,
                            program.getMpProduct(),
                            is_cttl_product,
                            is_cttl_product_quantity,
                            payment_date_data.getEnd_date_millisecond()
                    );

                    JSONObject js = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(cttlTransactionInfo),
                            JSONObject.class
                    );
                    js.put("transaction_info", transaction_info);
                    js.put("staff_info", cttlTransactionInfo.getTransaction_source() == CTTLTransactionSource.AUTO.getId() ?
                            null :
                            this.dataManager.getStaffManager(
                            ).getStaff(cttlTransactionData.getStaffId()));

                    /**
                     * Trạng thái tích lũy
                     */
                    int accumulate_failed = cttlTransactionInfo.getSo_luong_dtt_tich_luy() == 0 ? 1
                            :
                            (
                                    cttlTransactionData.getTransaction_value() > cttlTransactionData.getPayment_value() ?
                                            2 :
                                            (
                                                    (cttlTransactionData.getPayment_date() > payment_date_data.getEnd_date_millisecond() ? 3 : 0)
                                            )
                            );
                    js.put("is_accumulated", accumulate_failed == 0 ? 1 : 0);
                    if (accumulate_failed != 0) {
                        js.put("message",
                                accumulate_failed == 1 ?
                                        "Giá trị tích lũy = 0" :
                                        accumulate_failed == 2 ?
                                                "Thanh toán chưa đủ" :
                                                "Thời gian thanh toán không hợp lệ");
                    }
                    records.add(
                            js
                    );
                }
            }


            /**
             * Kết quả tích lũy
             */
            CSDMAgencyReward cttlAgencyReward =
                    JsonUtils.DeSerialize(
                            agency_cttl_info.get("reward").toString(),
                            CSDMAgencyReward.class
                    );
            tong_gia_tri_tich_luy = cttlAgencyReward.getTotalValue();

            han_muc_uu_dai = this.convertDamMeReward(
                    agency_cttl_info
            );
            for (CSDMAgencyOffer agencyOffer : han_muc_uu_dai) {
                agencyOffer.convertGiftInfo(gift_info);
            }

            long de_dat_han_muc_sau = this.getNextOfferOfPromo(
                    promoLimitInfoList,
                    tong_gia_tri_tich_luy,
                    han_muc_uu_dai
            );

            /**
             * Ngày xác nhận tham gia:
             * Số lượng ban đầu:
             * Số lượng tham gia:
             */
            if (agency_cttl_info != null) {
                data.put("ngay_xac_nhan_tham_gia", agency_cttl_info.get("created_date"));
                data.put("so_luong_ban_dau",
                        ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")) < 0 ?
                                0 :
                                ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")));
                data.put("so_luong_tham_gia", this.getSoLuongThamGia(
                        ConvertUtils.toInt(agency_cttl_info.get("update_join_quantity")),
                        ConvertUtils.toInt(tong_uu_dai_duoc_huong)));
            }
            /**
             * Tổng ưu đãi được hưởng
             */
            tong_uu_dai_duoc_huong = this.damMeDB.getTongUuDaiDamMe(
                    request.getPromo_id(),
                    request.getAgency_id()
            );

            JSONObject result = new JSONObject();
            result.put("tong_gia_tri_tham_gia", tong_gia_tri_tham_gia);
            result.put("tong_gia_tri_tich_luy", tong_gia_tri_tich_luy);
            result.put("tong_gia_tri_tich_luy_da_thanh_toan", tong_gia_tri_tich_luy_da_thanh_toan);
            result.put("tong_thanh_toan", tong_thanh_toan);
            result.put("tong_thanh_toan_con_thieu", tong_thanh_toan_con_thieu);
            result.put("tong_thanh_toan_hop_le", tong_thanh_toan_hop_le);
            result.put("han_muc_uu_dai", han_muc_uu_dai);
            result.put("tong_uu_dai_duoc_huong", tong_uu_dai_duoc_huong);
            result.put("de_dat_han_muc_sau",
                    de_dat_han_muc_sau
            );

            /**
             * Hạn mức ưu đãi
             */
            result.put("han_muc_uu_dai",
                    han_muc_uu_dai
            );
            data.put("result", result);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<CSDMAgencyOffer> convertDamMeReward(JSONObject agencyCttlInfo) {
        List<CSDMAgencyOffer> result = new ArrayList<>();
        try {
            CSDMAgencyReward cttlAgencyReward =
                    JsonUtils.DeSerialize(
                            agencyCttlInfo.get("reward").toString(),
                            CSDMAgencyReward.class
                    );
            CSDMAgencyOffer cttlAgencyOffer = new CSDMAgencyOffer();
            cttlAgencyOffer.setLevel(cttlAgencyReward.getLimitValue());
            cttlAgencyOffer.setDiscount(cttlAgencyReward.getPercentValue());
            result.add(cttlAgencyOffer);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return result;
    }

    private int getSoLuongThamGia(int update_join_quantity, int tong_uu_dai_duoc_huong) {
        try {
            if (update_join_quantity == -1) {
                return tong_uu_dai_duoc_huong;
            }

            return update_join_quantity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private List<CTTLAgencyOffer> getAgencyRewardByPromo(
            int promo_id,
            int agency_id,
            int promo_status,
            List<JSONObject> promoLimitInfoList,
            JSONObject agency_cttl_info) {
        try {
            if (promo_status != PromoCTTLStatus.STOPPED.getId()) {
                ClientResponse reward = ClientResponse.success(new JSONObject());
                LogUtil.printDebug(JsonUtils.Serialize(reward.getData()));
                List<JSONObject> offers = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(reward.getData()),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
                List<CTTLAgencyOffer> agencyOfferList = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(offers.get(0).get("offers")),
                        new TypeToken<List<CTTLAgencyOffer>>() {
                        }.getType()
                );
                return agencyOfferList;
            } else {
                JSONObject reward = JsonUtils.DeSerialize(
                        agency_cttl_info.get("reward").toString(),
                        JSONObject.class
                );

                if (reward.get("ltTLCmsOffer") != null) {
                    List<CTTLAgencyOffer> agencyOfferList = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(reward.get("ltTLCmsOffer")),
                            new TypeToken<List<CTTLAgencyOffer>>() {
                            }.getType()
                    );
                    for (CTTLAgencyOffer agencyOffer : agencyOfferList) {
                        agencyOffer.parseGiftInfo();
                    }
                    return agencyOfferList;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    private List<JSONObject> getOfferForAgency(List<JSONObject> promoLimitInfoList) {
        List<JSONObject> offers = JsonUtils.DeSerialize(
                "[{\"money\":1000000,\"level\":2,\"gifts\":[]},{\"money\":1000000,\"level\":2,\"gifts\":[]}]",
                new TypeToken<List<JSONObject>>() {
                }.getType()
        );
        return offers;
    }

    private void calculateResultTransactionCTTL(
            CSDMTransactionInfo cttlTransactionInfo,
            CSDMTransactionData cttlTransactionData,
            JSONObject transaction_info,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity,
            long payment_date) {
        cttlTransactionInfo.setSo_luong_dtt_tich_luy(
                this.tinhGiaTriTichLuyOfTransaction(
                        cttlTransactionData,
                        cttlTransactionInfo.getType(),
                        mpProductInPromo,
                        is_cttl_product,
                        is_cttl_product_quantity
                )
        );

        cttlTransactionInfo.setGia_tri_tich_luy(
                this.tinhDTTTichLuyOfTransaction(
                        cttlTransactionData,
                        cttlTransactionInfo.getType(),
                        mpProductInPromo,
                        is_cttl_product,
                        is_cttl_product_quantity
                )
        );

        cttlTransactionInfo.setGia_tri(
                this.getGiaTriOfCTTLTransaction(
                        cttlTransactionInfo.getType(),
                        transaction_info
                )
        );

        cttlTransactionInfo.setDa_thanh_toan(
                this.getDaThanhToanOfCTTLTransaction(
                        cttlTransactionInfo.getType(),
                        transaction_info,
                        cttlTransactionInfo.getGia_tri()
                )
        );

        cttlTransactionInfo.setCon_phai_thu(
                this.getConPhaiThuOfCTTLTransaction(
                        cttlTransactionInfo.getType(),
                        cttlTransactionInfo
                )
        );

        cttlTransactionInfo.setDtt_tich_luy(
                this.tinhDTTTichLuyOfTransaction(
                        cttlTransactionData,
                        cttlTransactionInfo.getType(),
                        mpProductInPromo,
                        is_cttl_product,
                        is_cttl_product_quantity
                )
        );

        cttlTransactionInfo.setDa_thanh_toan_hop_le(
                (cttlTransactionData.getPayment_value() >= cttlTransactionData.getTransaction_value() &&
                        payment_date >= cttlTransactionData.getPayment_date()) ?
                        cttlTransactionInfo.getDtt_tich_luy() :
                        0
        );

        cttlTransactionInfo.setCon_phai_thu_hop_le(
                cttlTransactionInfo.getCon_phai_thu() > 0 ? cttlTransactionInfo.getCon_phai_thu() : 0
        );
    }

    private long getDaThanhToanOfCTTLTransaction(int type, JSONObject transaction_info, long gia_tri) {
        try {
            if (type == CSDMTransactionType.DON_HANG.getId()) {
                JSONObject jsDeptOrder = this.deptDB.getDeptOrderInfoByCode(
                        ConvertUtils.toString(transaction_info.get("dept_code"))
                );
                if (jsDeptOrder == null) {
                    return 0;
                } else {
                    return ConvertUtils.toLong(jsDeptOrder.get("payment_value"));
                }
            } else if (type == CSDMTransactionType.PHIEU.getId()) {
                return 0;
            } else if (type == CSDMTransactionType.HBTL.getId()) {
                return gia_tri;
            } else return gia_tri;
        } catch (Exception e) {
        }
        return 0;
    }

    private long getConPhaiThuOfCTTLTransaction(int type, CSDMTransactionInfo cttlTransactionInfo) {
        if (type == CSDMTransactionType.DON_HANG.getId()) {
            return cttlTransactionInfo.getGia_tri()
                    - cttlTransactionInfo.getDa_thanh_toan();
        } else if (type == CSDMTransactionType.PHIEU.getId()) {
            return 0;
        } else if (type == CSDMTransactionType.HBTL.getId()) {
            return 0;
        } else return 0;
    }

    private long getGiaTriOfCTTLTransaction(int type, JSONObject transaction_info) {
        try {
            if (type == CSDMTransactionType.DON_HANG.getId()) {
                return ConvertUtils.toLong(transaction_info.get("total_end_price"));
            } else if (type == CSDMTransactionType.PHIEU.getId()) {
                return ConvertUtils.toLong(transaction_info.get("transaction_value"));
            } else if (type == CSDMTransactionType.HBTL.getId()) {
                return ConvertUtils.toLong(transaction_info.get("total_end_price"));
            }
        } catch (Exception e) {

        }
        return 0;
    }

    private long getPaymentOfCTTLTransaction(int type, JSONObject transaction_info) {
        if (type == CSDMTransactionType.DON_HANG.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else if (type == CSDMTransactionType.PHIEU.getId()) {
            return 0;
        } else if (type == CSDMTransactionType.HBTL.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else return 0;
    }

    private void convertTransactionDataToInfo(
            int agency_id,
            int promo_id,
            CSDMTransactionInfo cttlTransactionInfo,
            CSDMTransactionData cttlTransactionData) {
        try {
            /*
            private int agency_id;
             */
            cttlTransactionInfo.setAgency_id(
                    agency_id
            );
            /*
            private int promo_id;
             */
            cttlTransactionInfo.setPromo_id(promo_id);
            /*
            private int transaction_id;
             */
            cttlTransactionInfo.setTransaction_id(
                    cttlTransactionData.getTransactionId()
            );
            /*
            private int type;
             */
            cttlTransactionInfo.setType(
                    CSDMTransactionType.fromKey(
                            cttlTransactionData.getType()
                    ).getId()
            );
            /*
            private Date created_date;
             */
            cttlTransactionInfo.setCreated_date(
                    DateTimeUtils.getDateTime(
                            cttlTransactionData.getCreatedTime()
                    )
            );
            /*
            private String code;
             */
            cttlTransactionInfo.setCode(cttlTransactionData.getCode());
            /*
            private String name;
             */

            /*
            private Integer dept_transaction_id;
             */
            cttlTransactionInfo.setDept_transaction_id(
                    null
            );
            /*
            private Integer agency_order_dept_id;
             */
            cttlTransactionInfo.setAgency_order_dept_id(
                    (CSDMTransactionType.DON_HANG.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
                            null
            );
            /*
            private Integer agency_hbtl_id;
             */
            cttlTransactionInfo.setAgency_hbtl_id(
                    (CSDMTransactionType.HBTL.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
                            null
            );
            /*
            private Integer agency_dept_dtt_id;
             */
            cttlTransactionInfo.setAgency_dept_dtt_id(
                    null
            );
            /*
            private int status;
             */
            cttlTransactionInfo.setStatus(
                    cttlTransactionData.getStatus()
            );
            /*
            private Date confirm_date;
             */
            cttlTransactionInfo.setConfirm_date(
                    null
            );
            /*
            private Date transaction_date;
             */
            cttlTransactionInfo.setTransaction_date(
                    DateTimeUtils.getDateTime(cttlTransactionData.getCreatedTime())
            );
            cttlTransactionInfo.setTransaction_source(
                    cttlTransactionData.getTransactionSource()
            );
            cttlTransactionInfo.setPayment_date(cttlTransactionData.getPayment_date());

            cttlTransactionInfo.setAccumulate_type(cttlTransactionData.getAccumulate_type());
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
    }

    private long tinhGiaTriTichLuyOfTransaction(
            CSDMTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        if (type == CSDMTransactionType.DON_HANG.getId()) {
            return cttlTransactionData.getTotalValue();
        } else if (type == CSDMTransactionType.PHIEU.getId()) {
            return cttlTransactionData.getUpdateTotalValue();
        } else if (type == CSDMTransactionType.HBTL.getId()) {
            return cttlTransactionData.getTotalValue();
        } else return 0;
    }

    private long tinhDTTTichLuyOfTransaction(
            CSDMTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        return cttlTransactionData.sumDTTTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
    }


    private long tinhGiaTriTichLuySanPhamOfTransaction(
            CSDMTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        if (type == CSDMTransactionType.DON_HANG.getId()) {
            return cttlTransactionData.sumDoanhThuThuanSanPhamTichLuy(
                    is_cttl_product,
                    mpProductInPromo);
        }
        return 0;
    }

    private boolean filterCTTLAgency(
            CSDMAgencyResult x,
            String search,
            int limit,
            int business_department_id) {
        if (
                (search.isEmpty() ||
                        x.getAgency_info().getCode().contains(search) ||
                        x.getAgency_info().getCode().toLowerCase().contains(search.toLowerCase()) ||
                        x.getAgency_info().getShop_name().contains(search) ||
                        x.getAgency_info().getShop_name().toLowerCase().contains(search.toLowerCase())) &&
                        (business_department_id == 0 ||
                                x.getAgency_info().getBusiness_department_id() == business_department_id) &&
                        (limit == 0 ||
                                x.getLimit() == limit)
        ) {
            return true;
        }
        return false;
    }

    private boolean filterCTTLTransaction(
            CSDMTransactionData x,
            String search,
            int type,
            int status,
            PromoTimeRequest timeRequest) {
        if (((search.isEmpty() ||
                x.getCode().contains(search) ||
                x.getCode().toLowerCase().contains(search.toLowerCase())) &&
                (type == 0 ||
                        type == x.getAccumulate_type()) &&
                (status == 0 || x.getStatus() == status) &&
                (timeRequest == null ||
                        (
                                (timeRequest.getStart_date_millisecond() == 0 || x.getCreatedTime() >= timeRequest.getStart_date_millisecond()) &&
                                        (timeRequest.getEnd_date_millisecond() == 0 || x.getCreatedTime() <= timeRequest.getEnd_date_millisecond())
                        )
                )
        )
        ) {
            return true;
        }
        return false;
    }

    public ClientResponse getResultCTTLInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject promo = this.promoDB.getPromoJs(
                    request.getId()
            );
            if (promo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getId());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                promo.put("gift_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                ));
            }

            promo.put("total_limit", this.promoDB.getTotalLimit(request.getId()));

            JSONObject data = new JSONObject();
            data.put("promo", promo);

            JSONObject result = new JSONObject();
            data.put("result", null);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getResultCTTLOfAgency(SessionData sessionData, GetResultCTTLOfAgencyRequest request) {
        try {
            JSONObject promo = this.promoDB.getPromoJs(
                    request.getPromo_id()
            );
            if (promo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getPromo_id());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                promo.put("gift_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                ));
            }

            /**
             * Danh sách hạn mức
             */
            List<JSONObject> promoLimitInfoList = this.getPromoLimitInfo(
                    request.getPromo_id(),
                    this.promoDB.getListPromoLimit(
                            request.getPromo_id()
                    )
            );
            promo.put("promo_limit_info", promoLimitInfoList);

            /**
             * Có lặp hay không
             */
            promo.put("is_loop_offer", this.checkPromoLoopOffer(
                    ConvertUtils.toString(promo.get("form_of_reward")),
                    promoLimitInfoList,
                    ConvertUtils.toString(promo.get("promo_end_value_type"))
            ));

            /**
             * Số hạn mức của CTTL
             */
            promo.put("total_limit", this.promoDB.getTotalLimit(request.getPromo_id()));
            JSONObject data = new JSONObject();
            data.put("promo", promo);

            /**
             * Thông tin đại lý
             */
            data.put("agency_info",
                    this.dataManager.getAgencyManager().getAgencyBasicData(
                            request.getAgency_id()
                    )
            );
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkPromoLoopOffer(
            String form_of_reward,
            List<JSONObject> promoLimitInfoList,
            String promo_end_value_type) {
        if (PromoFormOfRewardType.BAC_THANG.equals(form_of_reward) ||
                (PromoEndValueType.IS_NOT_NULL.getKey().equals(promo_end_value_type) &&
                        ConvertUtils.toInt(promoLimitInfoList.get(promoLimitInfoList.size() - 1).get("to")) == 0)
        ) {
            return false;
        }
        return true;
    }

    private List<JSONObject> getPromoLimitInfo(int promo_id, List<JSONObject> promoLimits) {
        List<JSONObject> promoLimitInfoList = new ArrayList<>();
        for (JSONObject promoLimit : promoLimits) {
            JSONObject promoLimitJs = new JSONObject();
            JSONObject promoLimitGroup = this.promoDB.getPromoLimitGroupList(
                    ConvertUtils.toInt(promoLimit.get("id"))
            ).get(0);
            promoLimitJs.put("level", promoLimit.get("level"));
            promoLimitJs.put("from", promoLimitGroup.get("from_value"));
            promoLimitJs.put("to", promoLimitGroup.get("end_value"));
            promoLimitInfoList.add(promoLimitJs);
        }
        return promoLimitInfoList;
    }

    private long getNextOfferOfPromo(
            List<JSONObject> promoLimitInfoList,
            long tich_luy,
            List<CSDMAgencyOffer> han_muc_uu_dai) {
        int level = 0;
        if (!han_muc_uu_dai.isEmpty()) {
            level = han_muc_uu_dai.get(0).getLevel();
        }
        for (JSONObject limit : promoLimitInfoList) {
            if (level < ConvertUtils.toInt(limit.get("level"))) {
                return ConvertUtils.toLong(limit.get("from")) - tich_luy;
            }
        }
        return 0;
    }

    public ClientResponse getListCTTLByTransaction(
            SessionData sessionData,
            GetListCTTLByTransactionRequest request) {
        try {
            JSONObject promo = this.promoDB.getPromoJs(
                    request.getPromo_id()
            );
            if (promo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            JSONObject promo_structure = this.promoDB.getPromoStructure(request.getPromo_id());
            if (promo_structure == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            List<String> promoList = new ArrayList<>();

            List<JSONObject> agency_cttl_info_list = this.promoDB.getListCTTLOfAgency(
                    request.getAgency_id(),
                    request.getPromo_id()
            );
            if (ConvertUtils.toInt(promo.get("status")) != PromoCTTLStatus.STOPPED.getId()) {
//                agency_cttl_info_list = this.callAgencyCTTLInfo(
//                        request.getPromo_id(),
//                        request.getAgency_id()
//                );
            }

            for (JSONObject agency_cttl_info : agency_cttl_info_list) {
                int promo_relate_id = ConvertUtils.toInt(agency_cttl_info.get("id"));
                List<JSONObject> transactionList = JsonUtils.DeSerialize(
                        agency_cttl_info.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );

                for (JSONObject transaction : transactionList) {
                    LogUtil.printDebug(JsonUtils.Serialize(transaction));
                    if (ConvertUtils.toInt(transaction.get("transactionId")) == request.getTransaction_id()
                            && request.getType() == CSDMTransactionType.fromKey(ConvertUtils.toString(transaction.get("type"))).getId()) {
                        promoList.add(ConvertUtils.toString(promo_relate_id));
                        break;
                    }
                }
            }
            LogUtil.printDebug(JsonUtils.Serialize(promoList));
            List<JSONObject> records = this.promoDB.getPromoCTTLInPromoIds(
                    JsonUtils.Serialize(promoList)
            );
            for (JSONObject record : records) {
                record.put("structure", PromoStructureType.DONG_THOI.getId());
                record.put("so_luong_dtt_tich_luy", 0);
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkPromoLink(int promo_id, int promo_relate_id) {
        try {
            JSONObject promo_link_group = this.promoDB.getPromoLinkGroupByPromoId(promo_id);
            if (promo_link_group != null &&
                    this.promoDB.getPromoLinkGroupByPromoId(promo_relate_id) != null) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return false;
    }

    /**
     * Danh sách chính sách đam mê
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterPromoDamMe(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO_DAM_ME, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("so_han_muc", this.promoDB.getListCSDMLimit(
                        ConvertUtils.toInt(js
                                .get("id"))).size());
            }
            int total = this.promoDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getOrderInfo(SessionData sessionData, GetOrderInfoByCodeRequest request) {
        try {
            JSONObject agencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            int agency_order_id = ConvertUtils.toInt(agencyOrder.get("id"));
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            String agency_order_code = ConvertUtils.toString(agencyOrder.get("code"));

            Map<Integer, Long> mpProductDTT = this.getProductDTTOfAgencyOrder(
                    agency_id,
                    agency_order_id,
                    agency_order_code
            );

            JSONObject jsAgencyCSDMModifyOrderCancel = this.damMeDB.getAgencyCSDMModifyOrder(
                    DamMeDieuChinhType.HUY_TICH_LUY.getKey(),
                    agency_order_id
            );
            if (jsAgencyCSDMModifyOrderCancel != null) {
                ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                clientResponse.setMessage("Đơn hàng thuộc loại hủy tích lũy");
                return clientResponse;
            }

            JSONObject jsOrderDeptNormal = this.orderDB.getOrderNormalDept(
                    ConvertUtils.toInt(agencyOrder.get("id"))
            );
            if (jsOrderDeptNormal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            JSONObject jsAgencyCSDMModifyOrder = this.damMeDB.getAgencyCSDMModifyOrder(
                    DamMeDieuChinhType.GIAM_GIA_TRI_DON_HANG.getKey(),
                    agency_order_id
            );

            long giam_gia_tri_don_hang = jsAgencyCSDMModifyOrder == null ? 0 :
                    ConvertUtils.toLong(jsAgencyCSDMModifyOrder.get("data"));
            long total_end_price = ConvertUtils.toLong(jsOrderDeptNormal.get("total_end_price"));
            long total_refund_price = ConvertUtils.toLong(jsOrderDeptNormal.get("total_refund_price"));
            long tong_giam_gia_tri_san_pham = 0;
            List<JSONObject> products = new ArrayList<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(ConvertUtils.toInt(agencyOrder.get("id")));
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
                    JSONObject jsProduct = new JSONObject();
                    jsProduct.put("id", agencyOrderDetailEntity.getProduct_id());
                    jsProduct.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                            agencyOrderDetailEntity.getProduct_id()
                    ));
                    jsProduct.put("price", agencyOrderDetailEntity.getProduct_price());
                    jsProduct.put("quantity", agencyOrderDetailEntity.getProduct_total_quantity());

                    /**
                     * tính tổng ưu đãi
                     */
                    jsProduct.put("uu_dai",
                            agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm() +
                                    agencyOrderDetailEntity.getProduct_total_promotion_price() +
                                    agencyOrderDetailEntity.getProduct_total_dm_price()
                    );


                    jsProduct.put("dtt",
                            mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) != null ?
                                    mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) :
                                    agencyOrderDetailEntity.getProduct_total_end_price()
                    );

                    ProductGiamThemRequest productGiamThemRequest = this.getAgencyCSDMModifyOrderOfProduct(
                            agency_order_id,
                            agencyOrderDetailEntity.getProduct_id()
                    );

                    jsProduct.put("giam_gia_tri_san_pham", productGiamThemRequest == null ?
                            0 :
                            1);
                    long da_giam_them = productGiamThemRequest == null ? 0 :
                            ConvertUtils.toLong(productGiamThemRequest.getGiam_them());
                    jsProduct.put("da_giam_san_pham", da_giam_them);
                    tong_giam_gia_tri_san_pham += da_giam_them;

                    jsProduct.put("da_giam_don_hang",
                            this.getPhanBoGiamGiaTriDonHang(
                                    total_end_price,
                                    mpProductDTT,
                                    giam_gia_tri_don_hang,
                                    agencyOrderDetailEntity.getProduct_id(),
                                    ConvertUtils.toLong(agencyOrderDetailEntity.getProduct_total_end_price())
                            ));
                    products.add(jsProduct);
                }
            }
            JSONObject order = new JSONObject();
            order.put("total_end_price", ConvertUtils.toLong(
                    jsOrderDeptNormal.get("total_end_price")
            ));
            order.put("code",
                    request.getCode()
            );


            order.put("giam_gia_tri_don_hang", giam_gia_tri_don_hang == 0 ?
                    0 :
                    1);
            order.put("tong_giam_gia_tri_san_pham", tong_giam_gia_tri_san_pham);
            order.put("tong_giam_gia_tri_don_hang", giam_gia_tri_don_hang);

            JSONObject data = new JSONObject();
            data.put("order", order);
            data.put("products", products);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private long getPhanBoGiamGiaTriDonHang(
            long totalDTT,
            Map<Integer, Long> mpProductDTT,
            long giamGiaTriDonHang,
            int product_id,
            long product_total_end_price) {
        try {
            if (giamGiaTriDonHang <= 0) {
                return 0;
            }

            if (mpProductDTT.isEmpty()) {
                return ConvertUtils.toLong(product_total_end_price * 1.0F / totalDTT * giamGiaTriDonHang);
            } else {
                return ConvertUtils.toLong(mpProductDTT.get(product_id) * 1.0F / totalDTT * giamGiaTriDonHang);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private Map<Integer, Long> getProductDTTOfAgencyOrder(int agency_id, int agency_order_id, String agency_order_code) {
        Map<Integer, Long> mpProductDTT = new HashMap<>();
        try {
            ClientResponse crProductDTT = this.accumulateCSDMService.getProductDTT(
                    agency_id,
                    agency_order_code,
                    agency_order_id
            );
            if (crProductDTT.failed()) {
                this.alertToTelegram(
                        "DTT sản phẩm của đơn " + agency_order_code +
                                ": " + crProductDTT.getMessage(),
                        ResponseStatus.FAIL
                );
                return mpProductDTT;
            }

            List<JSONObject> productList = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(crProductDTT.getData()),
                    new TypeToken<List<JSONObject>>() {
                    }.getType()
            );

            for (JSONObject k : productList) {
                mpProductDTT.put(
                        ConvertUtils.toInt(k.get("product_id")),
                        ConvertUtils.toLong(k.get("product_dtt")));
            }
            return mpProductDTT;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
            this.alertToTelegram(
                    "DTT sản phẩm của đơn " + agency_order_code +
                            ": " + ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
        return mpProductDTT;
    }

    private ProductGiamThemRequest getAgencyCSDMModifyOrderOfProduct(int agency_order_id, Integer product_id) {
        try {
            JSONObject js = this.damMeDB.getAgencyCSDMModifyOrderOfProduct(
                    DamMeDieuChinhType.GIAM_GIA_TRI_SAN_PHAM.getKey(),
                    agency_order_id,
                    product_id
            );
            if (js != null) {
                List<ProductGiamThemRequest> product_info =
                        JsonUtils.DeSerialize(
                                js.get("product_info").toString(),
                                new TypeToken<List<ProductGiamThemRequest>>() {
                                }.getType()
                        );
                for (ProductGiamThemRequest productGiamThemRequest : product_info) {
                    if (productGiamThemRequest.getId() == product_id) {
                        return productGiamThemRequest;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }

        return null;
    }

    /**
     * Danh sách chính sách đam mê của đơn hàng
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterPromoDamMeOfOrder(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_PROMO_DAM_ME,
                    request.getFilters(),
                    request.getSorts()
            );
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse dieuChinhDamMeRequest(SessionData sessionData, DieuChinhDamMeRequest request) {
        try {
            DamMeDieuChinhType damMeDieuChinhType = DamMeDieuChinhType.fromKey(
                    request.getOption()
            );
            if (damMeDieuChinhType == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            switch (damMeDieuChinhType) {
                case HUY_TICH_LUY:
                    return this.cancelAccumulateOrder(
                            sessionData,
                            request,
                            damMeDieuChinhType
                    );
                case GIAM_GIA_TRI_DON_HANG:
                    return this.decreaseAccumulateOrder(
                            sessionData,
                            request,
                            damMeDieuChinhType
                    );
                case GIAM_GIA_TRI_SAN_PHAM:
                    return this.decreaseAccumulateProductOfOrder(
                            sessionData,
                            request,
                            damMeDieuChinhType
                    );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse cancelAccumulateOrder(SessionData sessionData,
                                                 DieuChinhDamMeRequest request,
                                                 DamMeDieuChinhType damMeDieuChinhType) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject jsAgencyCSDMModifyOrder =
                    this.damMeDB.getAgencyCSDMModifyOrder(
                            damMeDieuChinhType.getKey(),
                            ConvertUtils.toInt(jsAgencyOrder.get("id"))
                    );
            if (jsAgencyCSDMModifyOrder != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int rsInsert = this.damMeDB.insertAgencyCSDMModifyOrder(
                    ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                    damMeDieuChinhType.getKey(),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    request.getGiam_gia_tri_don_hang(),
                    ConvertUtils.toString(jsAgencyOrder.get("code")),
                    "[]"
            );
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ClientResponse cr = this.accumulateCSDMService.cancelOrder(
                    CSDMTransactionType.DON_HANG.getKey(),
                    ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                    ConvertUtils.toString(jsAgencyOrder.get("code")),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    0,
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    CTTLTransactionSource.ADMIN.getId(),
                    sessionData.getId()
            );
            if (cr.failed()) {
                return cr;
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListPromoByOrder(SessionData sessionData, GetListPromoByOrderRequest request) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            List<String> promoList = new ArrayList<>();
            JSONObject data = new JSONObject();
            if (jsAgencyOrder != null) {
                List<JSONObject> records = this.promoDB.getListAgencyCSDMInfoJs(
                        ConvertUtils.toInt(jsAgencyOrder.get("agency_id"))
                );

                int transactionId = ConvertUtils.toInt(jsAgencyOrder.get("id"));

                for (JSONObject agency_cttl_info : records) {
                    int promo_relate_id = ConvertUtils.toInt(agency_cttl_info.get("program_id"));

                    Program program = this.dataManager.getProgramManager().getMpDamMe().get(
                            promo_relate_id
                    );

                    if (program == null) {
                        continue;
                    }
                    List<JSONObject> transactionList = JsonUtils.DeSerialize(
                            agency_cttl_info.get("data").toString(),
                            new TypeToken<List<JSONObject>>() {
                            }.getType()
                    );

                    for (JSONObject transaction : transactionList) {
                        LogUtil.printDebug(JsonUtils.Serialize(transaction));
                        if (ConvertUtils.toInt(transaction.get("transactionId")) == transactionId
                                && CTTLTransactionType.DON_HANG.getId() == CTTLTransactionType.fromKey(ConvertUtils.toString(transaction.get("type"))).getId()
                                && ConvertUtils.toInt(transaction.get("status")) == TransactionCTTLStatus.THOA.getId()) {
                            promoList.add(ConvertUtils.toString(promo_relate_id));
                            break;
                        }
                    }
                }
            }

            List<JSONObject> records = this.promoDB.getPromoCTTLInPromoIds(
                    JsonUtils.Serialize(promoList)
            );
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse decreaseAccumulateOrder(SessionData sessionData, DieuChinhDamMeRequest request,
                                                   DamMeDieuChinhType damMeDieuChinhType) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject jsAgencyCSDMModifyOrder =
                    this.damMeDB.getAgencyCSDMModifyOrder(
                            damMeDieuChinhType.getKey(),
                            ConvertUtils.toInt(jsAgencyOrder.get("id"))
                    );
            if (jsAgencyCSDMModifyOrder != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> products = this.getListProductInOrder(
                    jsAgencyOrder,
                    request.getGiam_gia_tri_don_hang()
            );

            int rsInsert = this.damMeDB.insertAgencyCSDMModifyOrder(
                    ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                    damMeDieuChinhType.getKey(),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    request.getGiam_gia_tri_don_hang(),
                    ConvertUtils.toString(jsAgencyOrder.get("code")),
                    JsonUtils.Serialize(products)
            );
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            ClientResponse cr = this.accumulateCSDMService.decreaseOrderPrice(
                    CSDMTransactionType.DON_HANG.getKey(),
                    ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                    ConvertUtils.toString(jsAgencyOrder.get("code")),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    request.getGiam_gia_tri_don_hang(),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    CTTLTransactionSource.ADMIN.getId(),
                    sessionData.getId()
            );
            if (cr.failed()) {
                return cr;
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<JSONObject> getListProductInOrder(JSONObject agencyOrder, long giam_gia_tri_don_hang) {
        List<JSONObject> products = new ArrayList<>();
        try {
            int agency_order_id = ConvertUtils.toInt(agencyOrder.get("id"));
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            String agency_order_code = ConvertUtils.toString(agencyOrder.get("code"));

            JSONObject jsOrderDeptNormal = this.orderDB.getOrderNormalDept(
                    ConvertUtils.toInt(agencyOrder.get("id"))
            );
            if (jsOrderDeptNormal == null) {
                return products;
            }

            long total_end_price = ConvertUtils.toLong(jsOrderDeptNormal.get("total_end_price"));

            Map<Integer, Long> mpProductDTT = this.getProductDTTOfAgencyOrder(
                    agency_id,
                    agency_order_id,
                    agency_order_code
            );
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(ConvertUtils.toInt(agencyOrder.get("id")));
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
                    JSONObject jsProduct = new JSONObject();
                    jsProduct.put("id", agencyOrderDetailEntity.getProduct_id());
                    jsProduct.put("price", agencyOrderDetailEntity.getProduct_price());
                    jsProduct.put("quantity", agencyOrderDetailEntity.getProduct_total_quantity());

                    /**
                     * tính tổng ưu đãi
                     */
                    jsProduct.put("uu_dai",
                            agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm() +
                                    agencyOrderDetailEntity.getProduct_total_promotion_price() +
                                    agencyOrderDetailEntity.getProduct_total_dm_price()
                    );


                    jsProduct.put("dtt",
                            mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) != null ?
                                    mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) :
                                    agencyOrderDetailEntity.getProduct_total_end_price()
                    );

                    ProductGiamThemRequest productGiamThemRequest = this.getAgencyCSDMModifyOrderOfProduct(
                            agency_order_id,
                            agencyOrderDetailEntity.getProduct_id()
                    );

                    jsProduct.put("giam_gia_tri_san_pham", productGiamThemRequest == null ?
                            0 :
                            1);
                    long da_giam_them = productGiamThemRequest == null ? 0 :
                            ConvertUtils.toLong(productGiamThemRequest.getGiam_them());
                    jsProduct.put("da_giam_san_pham", da_giam_them);

                    jsProduct.put("da_giam_don_hang",
                            this.getPhanBoGiamGiaTriDonHang(
                                    total_end_price,
                                    mpProductDTT,
                                    giam_gia_tri_don_hang,
                                    agencyOrderDetailEntity.getProduct_id(),
                                    ConvertUtils.toLong(agencyOrderDetailEntity.getProduct_total_end_price())
                            ));
                    products.add(jsProduct);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return products;
    }

    private ClientResponse decreaseAccumulateProductOfOrder(SessionData sessionData, DieuChinhDamMeRequest request,
                                                            DamMeDieuChinhType damMeDieuChinhType) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (request.getProducts().isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Chỉ được điều chỉnh giảm 1 lần
             */
            for (ProductGiamThemRequest productGiamThemRequest : request.getProducts()) {
                JSONObject jsAgencyCSDMModifyOrder =
                        this.damMeDB.getAgencyCSDMModifyOrderOfProduct(
                                damMeDieuChinhType.getKey(),
                                ConvertUtils.toInt(jsAgencyOrder.get("id")),
                                productGiamThemRequest.getId()
                        );
                if (jsAgencyCSDMModifyOrder != null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            int agency_order_id = ConvertUtils.toInt(jsAgencyOrder.get("id"));

            Map<Integer, JSONObject> products = new HashMap<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(
                    agency_order_id
            );
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
                    JSONObject jsProduct = new JSONObject();
                    jsProduct.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                            agencyOrderDetailEntity.getProduct_id()
                    ));
                    jsProduct.put("price", agencyOrderDetailEntity.getProduct_price());
                    jsProduct.put("quantity", agencyOrderDetailEntity.getProduct_total_quantity());

                    /**
                     * tính tổng ưu đãi
                     */
                    jsProduct.put("uu_dai",
                            agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm() +
                                    agencyOrderDetailEntity.getProduct_total_promotion_price() +
                                    agencyOrderDetailEntity.getProduct_total_end_price()
                    );


                    jsProduct.put("dtt", agencyOrderDetailEntity.getProduct_total_begin_price() -
                            agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm() -
                            agencyOrderDetailEntity.getProduct_total_promotion_price() -
                            agencyOrderDetailEntity.getProduct_total_end_price()
                    );


                    ProductGiamThemRequest productGiamThemRequest = this.getAgencyCSDMModifyOrderOfProduct(
                            agency_order_id,
                            agencyOrderDetailEntity.getProduct_id()
                    );

                    jsProduct.put("giam_gia_tri_san_pham", productGiamThemRequest == null ?
                            0 :
                            1);
                    long da_giam_them = productGiamThemRequest == null ? 0 :
                            ConvertUtils.toLong(productGiamThemRequest.getGiam_them());
                    jsProduct.put("da_giam_them", da_giam_them);
                    jsProduct.put("dtt", 0);
                    products.put(agencyOrderDetailEntity.getProduct_id(), jsProduct);
                }
            }

            List<String> productIds = new ArrayList<>();
            List<JSONObject> product_info_list = new ArrayList<>();
            Long tong_giam_them = 0L;
            for (ProductGiamThemRequest productGiamThemRequest : request.getProducts()) {
                JSONObject product_info =
                        products.get(productGiamThemRequest.getId());
                product_info.put("giam_them", productGiamThemRequest.getGiam_them());
                product_info.put("id", productGiamThemRequest.getId());
                tong_giam_them += productGiamThemRequest.getGiam_them();
                product_info_list.add(product_info);
                productIds.add(ConvertUtils.toString(productGiamThemRequest.getId()));
            }

            this.damMeDB.insertAgencyCSDMModifyOrderOfProduct(
                    ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                    damMeDieuChinhType.getKey(),
                    ConvertUtils.toInt(jsAgencyOrder.get("id")),
                    JsonUtils.Serialize(productIds),
                    JsonUtils.Serialize(product_info_list),
                    tong_giam_them
            );

            request.getProducts().stream().forEach(k -> {
                ClientResponse cr = this.accumulateCSDMService.decreaseProductPrice(
                        CSDMTransactionType.DON_HANG.getKey(),
                        ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                        ConvertUtils.toString(jsAgencyOrder.get("code")),
                        ConvertUtils.toInt(jsAgencyOrder.get("id")),
                        ConvertUtils.toInt(jsAgencyOrder.get("id")),
                        CTTLTransactionSource.ADMIN.getId(),
                        sessionData.getId(),
                        k.getId(),
                        k.getGiam_them());
            });

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Phiếu điều chỉnh tích lũy đam mê
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse createPhieuDieuChinhTichLuyDamMe(
            SessionData sessionData,
            CreatePhieuDieuChinhCSDMRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject promoJs = this.promoDB.getPromoJs(request.getPromo_id());
            if (promoJs == null ||
                    PromoCTTLStatus.STOPPED.getId() == ConvertUtils.toInt(promoJs.get("status"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            long now = DateTimeUtils.getMilisecondsNow();

            int rsInsert = this.damMeDB.insertAgencyCSDMModifyValue(
                    request.getAgency_id(),
                    request.getPromo_id(),
                    request.getType(),
                    request.getValue() * (request.getType() == CSDMAccumulateType.PHIEU_TANG.getId() ? 1 : -1),
                    request.getNote()
            );
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ClientResponse crCreate = this.accumulateCSDMService.createPhieuDieuChinh(
                    CSDMTransactionType.PHIEU.getKey(),
                    request.getAgency_id(),
                    "PHIEU" + rsInsert,
                    0,
                    request.getValue() * (request.getType() == CSDMAccumulateType.PHIEU_TANG.getId() ? 1 : -1),
                    rsInsert,
                    CTTLTransactionSource.ADMIN.getId(),
                    sessionData.getId(),
                    now,
                    now,
                    request.getPromo_id(),
                    0
            );
            if (crCreate.failed()) {
                this.damMeDB.createPhieuDieuChinhFailed(
                        rsInsert
                );

                return crCreate;
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lịch sử điều chỉnh tích lũy đam mê
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getLichSuChinhSuaTichLuyDamMe(SessionData sessionData, FilterListRequest request) {
        try {
            this.filterUtils.parseFilter(FunctionList.FILTER_DIEU_CHINH_TICH_LUY_DAM_ME_HISTORY, request);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_DIEU_CHINH_TICH_LUY_DAM_ME_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.damMeDB.filter(query,
                    this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.damMeDB.getTotal(query);
            JSONObject data = new JSONObject();
            for (JSONObject js : records) {
                if (!ConvertUtils.toString(js.get("type")).equals(DamMeDieuChinhType.HUY_TICH_LUY.getKey())) {
                    if (js.get("product_info") != null) {
                        List<JSONObject> product_info_list = JsonUtils.DeSerialize(
                                js.get("product_info").toString(),
                                new TypeToken<List<JSONObject>>() {
                                }.getType()
                        );
                        for (JSONObject jsProductInfo : product_info_list) {
                            jsProductInfo.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                                    ConvertUtils.toInt(jsProductInfo.get("id"))
                            ));
                        }

                        js.put("products", product_info_list);
                    }
                }

                js.put("agency_order_code", js.get("code"));
            }
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse dieuChinhGiaTriTichLuySanPham(SessionData sessionData, String data) {
        try {
            if (!data.contains("csdm")) {
                this.alertToTelegram(data, ResponseStatus.EXCEPTION);
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> records = this.damMeDB.getListDieuChinhDamMe(data);
            for (JSONObject js : records) {
                DieuChinhDamMeRequest request = new DieuChinhDamMeRequest();
                request.setCode(ConvertUtils.toString(js.get("agency_order_code")));
                request.setOption(DamMeDieuChinhType.GIAM_GIA_TRI_SAN_PHAM.getKey());

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js.get("product_id")));
                if (productCache == null) {
                    this.alertToTelegram(JsonUtils.Serialize(js), ResponseStatus.EXCEPTION);
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 *     private int id;
                 *     private long giam_them;
                 */
                ProductGiamThemRequest productGiamThemRequest = new ProductGiamThemRequest();
                productGiamThemRequest.setId(productCache.getId());
                productGiamThemRequest.setGiam_them(ConvertUtils.toLong(js.get("giam_them")));
                request.getProducts().add(productGiamThemRequest);
                ClientResponse crDC = this.decreaseAccumulateProductOfOrder(
                        sessionData,
                        request,
                        DamMeDieuChinhType.GIAM_GIA_TRI_SAN_PHAM
                );

                this.alertToTelegram(crDC.getMessage(), ResponseStatus.EXCEPTION);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}