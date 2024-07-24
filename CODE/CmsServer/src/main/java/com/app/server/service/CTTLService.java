package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.DeptConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.cttl.*;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.product.ProgramProduct;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.DeptOrderEntity;
import com.app.server.data.entity.PromoEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.cttl.FilterAgencyCTTLRequest;
import com.app.server.data.request.cttl.GetListCTTLByTransactionRequest;
import com.app.server.data.request.cttl.GetResultCTTLOfAgencyRequest;
import com.app.server.data.request.cttl.TransactionCTTLRequest;
import com.app.server.data.request.promo.AddChildOrderIntoCTLLRequest;
import com.app.server.data.request.promo.PromoTimeRequest;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
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
public class CTTLService extends ProgramService {
    private AccumulateService accumulateService;

    @Autowired
    public void setAccumulateService(AccumulateService accumulateService) {
        this.accumulateService = accumulateService;
    }

    /**
     * Danh sách đơn hàng con
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterTransactionByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
        try {
            if (request.getType() == CTTLTransactionType.DON_HANG.getId()) {
                return this.filterOrderChildCTTLByAgency(sessionData, request);
            } else if (request.getType() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                return this.filterTangCTTLByAgency(sessionData, request);
            } else if (request.getType() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                return this.filterDieuChinhCTTLByAgency(sessionData, request);
            } else if (request.getType() == CTTLTransactionType.HBTL.getId()) {
                return this.filterHBTLCTTLByAgency(sessionData, request);
            } else if (request.getType() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                return this.filterGiamCTTLByAgency(sessionData, request);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse filterOrderChildCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
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

        JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new LinkedHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_cttl_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_CHILD_CTTL, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("code", js.get("dept_code"));
            js.put("transaction_value", ConvertUtils.toLong(js.get("total_end_price")));
            js.put("type", CTTLTransactionType.DON_HANG.getId());

            js.put("transaction_status", js.get("status"));

            Integer status = mpTransaction.get(
                    CTTLTransactionType.DON_HANG.getKey() +
                            "_" +
                            ConvertUtils.toInt(js.get("id")));
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            js.put("created_date", js.get("order_created_date"));
            js.put("confirm_date", js.get("order_confirm_date"));

            this.getTransactionDetailCTTL(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    private void convertAgencyTransaction(Map<String, Integer> mpTransaction, JSONObject agency_cttl_info) {
        try {
            List<JSONObject> transactionList = JsonUtils.DeSerialize(
                    agency_cttl_info.get("data").toString(),
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

    private void getTransactionDetailCTTL(JSONObject js) {
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

    private ClientResponse filterTangCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
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

        JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new ConcurrentHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_cttl_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_TANG_CONG_NO_CTTL, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("code",
                    ConvertUtils.toString(js.get("doc_no")).isEmpty() ?
                            ("DEPT" + ConvertUtils.toInt(js.get("id"))) :
                            ConvertUtils.toString(js.get("doc_no"))
            );
            js.put("type", CTTLTransactionType.TANG_CONG_NO.getId());

            js.put("active",
                    !TransactionEffectValueType.NONE.getCode().equals(
                            ConvertUtils.toString(js.get("dtt_effect_type")
                            )
                    )
            );

            Integer status = mpTransaction.get(
                    CTTLTransactionType.TANG_CONG_NO.getKey() + "_" +
                            ConvertUtils.toInt(js.get("id"))
            );
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            this.getTransactionDetailCTTL(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    private ClientResponse filterGiamCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
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

        JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new ConcurrentHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_cttl_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_GIAM_CONG_NO_CTTL, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("code",
                    ConvertUtils.toString(js.get("doc_no")).isEmpty() ?
                            ("DEPT" + ConvertUtils.toInt(js.get("id"))) :
                            ConvertUtils.toString(js.get("doc_no"))
            );
            js.put("type", CTTLTransactionType.GIAM_CONG_NO.getId());

            js.put("active",
                    !TransactionEffectValueType.NONE.getCode().equals(
                            ConvertUtils.toString(js.get("dtt_effect_type")
                            )
                    )
            );

            Integer status = mpTransaction.get(
                    CTTLTransactionType.GIAM_CONG_NO.getKey() + "_" +
                            ConvertUtils.toInt(js.get("id"))
            );
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            this.getTransactionDetailCTTL(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    private ClientResponse filterDieuChinhCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
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

        JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                request.getPromo_id(),
                request.getAgency_id()
        );
        Map<String, Integer> mpTransaction = new ConcurrentHashMap<>();
        this.convertAgencyTransaction(mpTransaction, agency_cttl_info);

        String query = this.filterUtils.getQuery(FunctionList.LIST_DIEU_CHINH_CTTL, request.getFilters(), request.getSorts());

        JSONObject data = new JSONObject();
        List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
        int total = this.promoDB.getTotal(query);
        for (JSONObject js : records) {
            js.put("transaction_status", js.get("status"));
            js.put("code",
                    "DTT" + ConvertUtils.toInt(js.get("id"))
            );
            js.put("transaction_value", ConvertUtils.toLong(js.get("data")));
            js.put("type", CTTLTransactionType.DIEU_CHINH_DTT.getId());

            Integer status = mpTransaction.get(
                    CTTLTransactionType.DIEU_CHINH_DTT.getKey() + "_" +
                            ConvertUtils.toInt(js.get("id"))
            );
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            this.getTransactionDetailCTTL(js);
        }
        data.put("records", records);
        data.put("total", total);
        data.put("total_page", this.appUtils.getTotalPage(total));
        return ClientResponse.success(data);
    }

    private ClientResponse filterHBTLCTTLByAgency(SessionData sessionData, FilterAgencyCTTLRequest request) {
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

        JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
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
            js.put("type", CTTLTransactionType.HBTL.getId());

            Integer status = mpTransaction.get(
                    CTTLTransactionType.HBTL.getKey() + "_" +
                            ConvertUtils.toInt(js.get("id"))
            );
            if (status == null) {
                js.put("status", TransactionCTTLStatus.KHONG_THOA.getId());
            } else {
                js.put("status", status);
            }
            this.getTransactionDetailCTTL(js);
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
    public ClientResponse addTransactionIntoCTTL(
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

            Program program = this.dataManager.getProgramManager().getMpCTTL().get(
                    request.getPromo_id()
            );
            if (!this.checkProgramVisibility(
                    this.dataManager.getProgramManager().getAgency(request.getAgency_id()),
                    program
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            String condition_type = ConvertUtils.toString(promoJs.get("condition_type"));

            for (int iTrans = 0; iTrans < request.getTransactions().size(); iTrans++) {
                TransactionCTTLRequest transactionCTTLRequest = request.getTransactions().get(
                        iTrans
                );
                if ((condition_type.equals(PromoConditionType.PRODUCT_PRICE.getKey()) ||
                        condition_type.equals(PromoConditionType.PRODUCT_PRICE.getKey())) &&
                        (transactionCTTLRequest.getType() == CTTLTransactionType.DIEU_CHINH_DTT.getId() ||
                                transactionCTTLRequest.getType() == CTTLTransactionType.TANG_CONG_NO.getId() ||
                                transactionCTTLRequest.getType() == CTTLTransactionType.GIAM_CONG_NO.getId()
                        )
                ) {
                    ClientResponse crA = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crA.setMessage("CTTL sản phẩm không tích lũy cho TANGCN/GIAMCN/DTT");
                    return crA;
                }

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
            CTTLTransactionType cttlTransactionType = CTTLTransactionType.from(
                    transactionCTTLRequest.getType()
            );
            if (cttlTransactionType == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            if (transactionCTTLRequest.getType() == CTTLTransactionType.DON_HANG.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrderDeptById(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null ||
                        jsTransaction.get("dept_code") == null ||
                        jsTransaction.get("dept_code").toString().isEmpty()
                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                JSONObject agencyOrder = this.orderDB.getAgencyOrder(
                        ConvertUtils.toInt(jsTransaction.get("agency_order_id"))
                );
                if (agencyOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                JSONObject jsDeptOrder = this.deptDB.getDeptOrderInfoByCode(
                        jsTransaction.get("dept_code").toString()
                );
                if (jsDeptOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CTTL_ADD_ORDER_DEPT_FAILED);
                }

                this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        cttlTransactionType,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")),
                        ConvertUtils.toString(jsTransaction.get("dept_code")),
                        jsTransaction,
                        staff_id,
                        AppUtils.convertJsonToDate(agencyOrder.get("created_date")).getTime(),
                        DateTimeUtils.getMilisecondsNow(),
                        jsDeptOrder == null ? 0 :
                                ConvertUtils.toLong(jsDeptOrder.get("payment_value"))
                );
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                JSONObject jsTransaction = this.deptDB.getDeptTransactionJs(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                String dtt_effect_type = ConvertUtils.toString(jsTransaction.get("dtt_effect_type"));
                int dept_transaction_main_type_id = ConvertUtils.toInt(jsTransaction.get("dept_transaction_main_type_id"));
                return this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        cttlTransactionType,
                        dept_transaction_main_type_id == DeptTransactionMainType.INCREASE.getId() ?
                                ConvertUtils.toLong(jsTransaction.get("transaction_value")) :
                                (ConvertUtils.toLong(jsTransaction.get("transaction_value")) * -1),
                        ConvertUtils.toString(jsTransaction.get("doc_no")).isEmpty() ?
                                ("DEPT" + ConvertUtils.toString(transactionCTTLRequest.getId())) :
                                ConvertUtils.toString(jsTransaction.get("doc_no")),
                        jsTransaction,
                        staff_id, 0, 0,
                        ConvertUtils.toLong(jsTransaction.get("payment_value"))
                );
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                JSONObject jsTransaction = this.deptDB.getAgencyDeptDtt(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                return this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        cttlTransactionType,
                        ConvertUtils.toLong(jsTransaction.get("data")),
                        "DTT" + transactionCTTLRequest.getId(),
                        jsTransaction,
                        staff_id, 0, 0,
                        ConvertUtils.toLong(jsTransaction.get("data"))
                );
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.HBTL.getId()) {
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
                        cttlTransactionType,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")) * -1,
                        ConvertUtils.toString(jsTransaction.get("code")),
                        jsTransaction,
                        staff_id, 0, 0,
                        ConvertUtils.toLong(jsTransaction.get("total_end_price")) * -1
                );
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                JSONObject jsTransaction = this.deptDB.getDeptTransactionJs(
                        transactionCTTLRequest.getId()
                );
                if (jsTransaction == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                String dtt_effect_type = ConvertUtils.toString(jsTransaction.get("dtt_effect_type"));
                int dept_transaction_main_type_id = ConvertUtils.toInt(jsTransaction.get("dept_transaction_main_type_id"));

                return this.insertTransaction(
                        promo_id,
                        agency_id,
                        transactionCTTLRequest,
                        cttlTransactionType,
                        dept_transaction_main_type_id == DeptTransactionMainType.INCREASE.getId() ?
                                ConvertUtils.toLong(jsTransaction.get("transaction_value")) :
                                (ConvertUtils.toLong(jsTransaction.get("transaction_value")) * -1),
                        ConvertUtils.toString(jsTransaction.get("doc_no")).isEmpty() ?
                                ("DEPT" + ConvertUtils.toString(transactionCTTLRequest.getId())) :
                                ConvertUtils.toString(jsTransaction.get("doc_no")),
                        jsTransaction,
                        staff_id, 0, 0,
                        ConvertUtils.toLong(jsTransaction.get("transaction_value"))
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
            CTTLTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id,
            long order_time,
            long dept_time,
            long tt
    ) {
        if (cttlTransactionType.getId() == CTTLTransactionType.DON_HANG.getId()) {
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
        } else if (cttlTransactionType.getId() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return this.insertTransactionDeptCTTL(promo_id,
                    agency_id,
                    transactionCTTLRequest,
                    cttlTransactionType,
                    transaction_value,
                    code,
                    jsTransaction,
                    staff_id, tt
            );
        } else if (cttlTransactionType.getId() == CTTLTransactionType.TANG_CONG_NO.getId()) {
            return this.insertTransactionDeptCTTL(promo_id,
                    agency_id,
                    transactionCTTLRequest,
                    cttlTransactionType,
                    transaction_value,
                    code,
                    jsTransaction,
                    staff_id, tt);
        } else if (cttlTransactionType.getId() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return this.insertTransactionDTTCTTL(promo_id,
                    agency_id,
                    transactionCTTLRequest,
                    cttlTransactionType,
                    transaction_value,
                    code,
                    jsTransaction,
                    staff_id, tt);
        } else if (cttlTransactionType.getId() == CTTLTransactionType.HBTL.getId()) {
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
            CTTLTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id,
            long order_time,
            long dept_time,
            long tt
    ) {
        return this.accumulateService.adminAddTransaction(
                cttlTransactionType.getKey(),
                agency_id,
                code,
                ConvertUtils.toInt(jsTransaction.get("agency_order_id")),
                transaction_value,
                transactionCTTLRequest.getId(),
                CTTLTransactionSource.ADMIN.getId(),
                staff_id,
                order_time,
                dept_time,
                promo_id,
                tt
        );
    }

    private ClientResponse insertTransactionDeptCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CTTLTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id,
            long tt
    ) {
        long now = DateTimeUtils.getMilisecondsNow();
        return this.accumulateService.adminAddTransaction(
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
                tt
        );
    }

    private ClientResponse insertTransactionDTTCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CTTLTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id, long tt
    ) {
        long now = DateTimeUtils.getMilisecondsNow();
        return this.accumulateService.adminAddTransaction(
                cttlTransactionType.getKey(),
                agency_id,
                code,
                0,
                transaction_value,
                transactionCTTLRequest.getId(),
                CTTLTransactionSource.ADMIN.getId(),
                staff_id, now, now, promo_id, tt
        );
    }

    private ClientResponse insertTransactionHBTLCTTL(
            int promo_id,
            int agency_id,
            TransactionCTTLRequest transactionCTTLRequest,
            CTTLTransactionType cttlTransactionType,
            long transaction_value,
            String code,
            JSONObject jsTransaction,
            int staff_id, long tt
    ) {
        long now = DateTimeUtils.getMilisecondsNow();
        return this.accumulateService.adminAddTransaction(
                cttlTransactionType.getKey(),
                agency_id,
                code,
                0,
                transaction_value,
                transactionCTTLRequest.getId(),
                CTTLTransactionSource.ADMIN.getId(),
                staff_id,
                now,
                now, promo_id, tt
        );
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
            CTTLTransactionType cttlTransactionType = CTTLTransactionType.from(
                    transactionCTTLRequest.getType()
            );
            if (cttlTransactionType == null) {
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

            if (status != PromoCTTLStatus.STOPPED.getId()
                    && isLienKet) {
                transactionList = this.callGetListTransactionCTTLInfo(
                        promo_id,
                        agency_id
                );
            }

            for (JSONObject transaction : transactionList) {
                LogUtil.printDebug(JsonUtils.Serialize(transaction));
                if (ConvertUtils.toInt(transaction.get("transactionId")) == transactionCTTLRequest.getId()
                        && transactionCTTLRequest.getType() == CTTLTransactionType.fromKey(ConvertUtils.toString(transaction.get("type"))).getId()
                        && ConvertUtils.toInt(transaction.get("isDivide")) == 1) {
                    ClientResponse crRemove = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crRemove.setMessage("Vui lòng không gỡ khoản tích lũy được tách bởi CTTL khác");
                    return crRemove;
                }
            }

            String code = "";
            String type = "";
            int order_id = 0;
            if (transactionCTTLRequest.getType() == CTTLTransactionType.DON_HANG.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrderDeptById(
                        transactionCTTLRequest.getId()
                );
                code = ConvertUtils.toString(jsTransaction.get("dept_code"));
                type = CTTLTransactionType.DON_HANG.getKey();
                order_id = ConvertUtils.toInt(jsTransaction.get("agency_order_id"));
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                code = "DEPT" + transactionCTTLRequest.getId();
                type = CTTLTransactionType.TANG_CONG_NO.getKey();
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                code = "DTT" + transactionCTTLRequest.getId();
                type = CTTLTransactionType.DIEU_CHINH_DTT.getKey();
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.HBTL.getId()) {
                JSONObject jsTransaction = this.orderDB.getAgencyOrderDeptById(
                        transactionCTTLRequest.getId()
                );
                code = ConvertUtils.toString(jsTransaction.get("dept_code"));
                type = CTTLTransactionType.HBTL.getKey();
            } else if (transactionCTTLRequest.getType() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                code = "DEPT" + transactionCTTLRequest.getId();
                type = CTTLTransactionType.GIAM_CONG_NO.getKey();
            }

            return this.accumulateService.adminRemoveTransaction(
                    type,
                    agency_id,
                    code,
                    order_id,
                    promo_id,
                    transactionCTTLRequest.getId(),
                    CTTLTransactionSource.ADMIN.getId(),
                    staff_id
            );
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
    public ClientResponse filterResultCTTL(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_RESULT_CTTL, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotal(query);
            for (JSONObject js : records) {
                Program program = this.dataManager.getProgramManager().getMpCTTL().get(
                        ConvertUtils.toInt(js.get("id"))
                );
                if (program == null) {
                    continue;
                }

                boolean is_cttl_product = PromoConditionCTTLType.from(
                        ConvertUtils.toString(js.get("condition_type"))).isCTTLProduct();
                boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                        ConvertUtils.toString(js.get("condition_type"))).isCTTLProductQuantity();

//                this.summaryCTTL(
//                        js,
//                        is_cttl_product,
//                        program.getMpProduct(),
//                        is_cttl_product_quantity);
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
        int require_confirm_join = ConvertUtils.toInt(promo.get("require_confirm_join"));
        List<JSONObject> agency_cttl_info_list = this.promoDB.getListAgencyOfCTTLJs(
                promo_id, require_confirm_join
        );
        PromoTimeRequest payment_date_data =
                JsonUtils.DeSerialize(
                        promo.get("payment_date_data").toString(),
                        PromoTimeRequest.class
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
            CTTLAgencyReward agencyReward = new CTTLAgencyReward();
            agencyReward.setId(ConvertUtils.toInt(agency_cttl_info.get("agency_id")));
            if (agency_cttl_info.get("reward") != null && !agency_cttl_info.get("reward").toString().isEmpty()) {
                agencyReward.parseReward(
                        JsonUtils.DeSerialize(agency_cttl_info.get("reward").toString(), JSONObject.class)
                );
            }
            if (agencyReward.isDat()) {
                tong_khach_hang_dat++;
            }

            List<CTTLTransactionData> transactionDataList =
                    JsonUtils.DeSerialize(
                            agency_cttl_info.get("data").toString(),
                            new TypeToken<List<CTTLTransactionData>>() {
                            }.getType()
                    );
            for (CTTLTransactionData cttlTransactionData : transactionDataList) {
                this.parseCTTLTransactionData(cttlTransactionData);
            }


            /**
             * tính kết quả tích lũy
             */
            convertResultCTTLOfAgencyResponse(
                    promo_id,
                    ConvertUtils.toString(promo.get("condition_type")),
                    agency_cttl_info,
                    is_cttl_product,
                    mpProductInPromo,
                    transactionDataList,
                    is_cttl_product_quantity,
                    payment_date_data.getEnd_date_millisecond()
            );

            CTTLAgencyResult cttlAgency = CTTLAgencyResult.from(
                    agency_cttl_info
            );

            tong_doanh_thu_tich_luy += cttlAgency.getTong_gia_tri_tich_luy();
            tong_doanh_thu_tich_luy_hop_le += transactionDataList.stream().reduce(
                    0L,
                    (total, object) -> total +
                            (object.isHopLe(is_cttl_product_quantity, payment_date_data.getEnd_date_millisecond()) == true ? object.sumGiaTriThamGia(is_cttl_product_quantity) : 0),
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
                            (object.isHopLe(is_cttl_product_quantity, payment_date_data.getEnd_date_millisecond()) == true ? object.sumDoanhThuThuanSanPhamTichLuy(
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
                            (object.isHopLe(is_cttl_product_quantity, payment_date_data.getEnd_date_millisecond()) == true ? object.sumSanPhamTichLuy() : 0),
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

    private void parseCTTLTransactionData(CTTLTransactionData cttlTransactionData) {
        try {
            if (cttlTransactionData.getType().equals(CTTLTransactionType.DON_HANG.getKey())) {
                cttlTransactionData.setTransaction_type(CTTLTransactionType.DON_HANG.getId());
            } else if (cttlTransactionData.getType().equals(CTTLTransactionType.TANG_CONG_NO.getKey())) {
                cttlTransactionData.setTransaction_type(CTTLTransactionType.TANG_CONG_NO.getId());
            } else if (cttlTransactionData.getType().equals(CTTLTransactionType.DIEU_CHINH_DTT.getKey())) {
                cttlTransactionData.setTransaction_type(CTTLTransactionType.DIEU_CHINH_DTT.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
            } else if (cttlTransactionData.getType().equals(CTTLTransactionType.HBTL.getKey())) {
                cttlTransactionData.setTransaction_type(CTTLTransactionType.HBTL.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
            } else if (cttlTransactionData.getType().equals(CTTLTransactionType.GIAM_CONG_NO.getKey())) {
                cttlTransactionData.setTransaction_type(CTTLTransactionType.GIAM_CONG_NO.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
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

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getId());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                promo.put("gift_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                ));
            }

            PromoTimeRequest payment_date_data =
                    JsonUtils.DeSerialize(
                            promo.get("payment_date_data").toString(),
                            PromoTimeRequest.class
                    );

            Program program = this.dataManager.getProgramManager().getMpCTTL().get(
                    request.getId()
            );

            boolean is_cttl_product = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProduct();
            boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProductQuantity();

            promo.put("total_limit", this.promoDB.getTotalLimit(request.getId()));

            JSONObject data = new JSONObject();
            data.put("promo", promo);
            JSONObject result = new JSONObject();

            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("program_id");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            filterRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(filterRequest);

            int require_confirm_join = ConvertUtils.toInt(promo.get("require_confirm_join"));
            FilterRequest sqlFilter = new FilterRequest();
            sqlFilter.setKey("");
            sqlFilter.setValue("(" + require_confirm_join + "=0" +
                    " OR (" + require_confirm_join + "=1 AND t.confirm_join_quantity > 0))");
            sqlFilter.setType(TypeFilter.SQL);
            request.getFilters().add(sqlFilter);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_RESULT_CTTL_ACGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> agency_cttl_info_list = this.promoDB.filter(
                    query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit()
            );
            int total = this.promoDB.getTotal(query);


            List<JSONObject> cttlAgencyList = new ArrayList<>();
            for (JSONObject agency_cttl_info : agency_cttl_info_list) {
//                CTTLAgencyReward agencyReward = new CTTLAgencyReward();
//                agencyReward.setId(ConvertUtils.toInt(agency_cttl_info.get("agency_id")));
//                if (agency_cttl_info.get("reward") != null && !agency_cttl_info.get("reward").toString().isEmpty()) {
//                    agencyReward.parseReward(
//                            JsonUtils.DeSerialize(agency_cttl_info.get("reward").toString(), JSONObject.class)
//                    );
//                }


//                List<CTTLTransactionData> transactionDataList =
//                        JsonUtils.DeSerialize(
//                                agency_cttl_info.get("data").toString(),
//                                new TypeToken<List<CTTLTransactionData>>() {
//                                }.getType()
//                        );
//                long tt = 0;
//                for (CTTLTransactionData cttlTransactionData : transactionDataList) {
//                    this.parseCTTLTransactionData(cttlTransactionData);
//
//                    tt += cttlTransactionData.getTt();
//                }
//
//                /**
//                 * tính kết quả tích lũy
//                 */
//                convertResultCTTLOfAgencyResponse(
//                        request.getId(),
//                        ConvertUtils.toString(promo.get("condition_type")),
//                        agency_cttl_info,
//                        is_cttl_product,
//                        program.getMpProduct(),
//                        transactionDataList,
//                        is_cttl_product_quantity,
//                        payment_date_data.getEnd_date_millisecond()
//                );
//
                CTTLAgencyResult cttlAgency = CTTLAgencyResult.from(
                        agency_cttl_info
                );
//                cttlAgency.setTong_uu_dai_duoc_huong(
//                        agencyReward.getRewardValue()
//                );
//
//                /**
//                 * Kiểm tra tổng thanh toán >= tổng thanh toán hợp lệ
//                 */
//                if (tt < cttlAgency.getTong_thanh_toan_hop_le()) {
//                    this.alertToTelegram(
//                            " Đại lý " + agencyReward.getId() + " tổng thanh toán < tổng thanh toán hợp lệ",
//                            ResponseStatus.FAIL
//                    );
//                }

                JSONObject jsAgency = new JSONObject();
                jsAgency.put("id", cttlAgency.getId());
                jsAgency.put("agency_id", cttlAgency.getAgency_id());
                jsAgency.put("agency_info", cttlAgency.getAgency_info());
                cttlAgencyList.add(jsAgency);
            }

            int business_department_id = ConvertUtils.toInt(
                    this.filterUtils.getValueByKey(
                            request.getFilters(),
                            "business_department_id"));

            data.put("records", cttlAgencyList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void convertResultCTTLOfAgencyResponse(
            int promo_id,
            String condition_type,
            JSONObject js,
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            List<CTTLTransactionData> transactionDataList,
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
            List<CTTLTransactionData> transactionDataList,
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
            for (CTTLTransactionData cttlTransactionData : transactionDataList) {
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
            CTTLTransactionData cttlTransactionData
    ) {
        JSONObject rs = new JSONObject();
        long gia_tri = 0;
        long thanh_toan = 0;
        long ngay_thanh_toan = 0;
        try {
            if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DON_HANG.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptOrderInfoByCode(
                        cttlTransactionData.getCode()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("payment_value"));
                    ngay_thanh_toan = this.getNgayThanhToan(transaction_info);
                }
            } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.TANG_CONG_NO.getId()) {
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
            } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                JSONObject transaction_info = this.deptDB.getAgencyDeptDtt(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("data"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("data"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.HBTL.getId()) {
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
            } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptTransactionJs(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    ngay_thanh_toan = transaction_info.get("payment_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("payment_date"))).getTime();
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        rs.put("gia_tri", gia_tri);
        rs.put("thanh_toan", thanh_toan);
        rs.put("ngay_thanh_toan", ngay_thanh_toan);

        return rs;
    }

    private long getNgayThanhToan(JSONObject transaction_info) {
        try {
            return transaction_info.get("payment_date") == null ? 0L :
                    DateTimeUtils.getDateTime(
                            ConvertUtils.toString(
                                    transaction_info.get("payment_date"))).getTime();
        } catch (Exception e) {
            LogUtil.printDebug(Module.PROMO.name(), e);
        }
        return 0;
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
            List<CTTLTransactionData> transactionDataList,
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
            List<CTTLTransactionData> transactionDataList,
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
            List<CTTLTransactionData> transactionDataList,
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
            List<CTTLTransactionData> transactionDataList,
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
            List<CTTLTransactionData> transactionDataList) {
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
            List<CTTLTransactionData> transactionDataList,
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

            Program program = this.dataManager.getProgramManager().getMpCTTL().get(
                    request.getPromo_id()
            );
            if (program == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            PromoTimeRequest payment_date_data =
                    JsonUtils.DeSerialize(
                            promo.get("payment_date_data").toString(),
                            PromoTimeRequest.class
                    );

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

            promo.put("promo_limit_info", promoLimitInfoList);

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
            List<CTTLAgencyOffer> han_muc_uu_dai = new ArrayList<>();
            long tong_uu_dai_duoc_huong = 0;

            List<JSONObject> records = new ArrayList<>();
            int total = 0;
            JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                    request.getPromo_id(),
                    request.getAgency_id()
            );

            if (agency_cttl_info != null) {
                List<JSONObject> transactionList = JsonUtils.DeSerialize(
                        agency_cttl_info.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
                if (ConvertUtils.toInt(promo.get("status")) != PromoCTTLStatus.STOPPED.getId()
                        && promo_link_group != null) {
                    transactionList = this.callGetListTransactionCTTLInfo(
                            request.getPromo_id(),
                            request.getAgency_id()
                    );
                }

                LogUtil.printDebug(JsonUtils.Serialize(transactionList));
                List<CTTLTransactionData> transactionDataList = new ArrayList<>();
                for (JSONObject transaction : transactionList) {
                    CTTLTransactionData cttlTransactionData = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(transaction),
                            CTTLTransactionData.class
                    );
                    this.parseCTTLTransactionData(cttlTransactionData);

                    JSONObject transaction_info = null;
                    if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DON_HANG.getId()) {
                        transaction_info = this.orderDB.getAgencyOrderBasicByAgencyOrderDeptId(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                        transaction_info = this.deptDB.getAgencyDeptDtt(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.HBTL.getId()) {
                        transaction_info = this.orderDB.getAgencyHBTL(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
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

                    long gia_tri_tich_luy = cttlTransactionData.sumGiaTriTichLuy(
                            is_cttl_product,
                            program.getMpProduct(),
                            is_cttl_product_quantity);
                    long gia_tri_tich_luy_da_thanh_toan = cttlTransactionData.sumGiaTriTichLuyThanhToan(
                            is_cttl_product,
                            program.getMpProduct(),
                            is_cttl_product_quantity, payment_date_data.getEnd_date_millisecond());

                    tong_gia_tri_tich_luy += gia_tri_tich_luy;
                    tong_gia_tri_tich_luy_da_thanh_toan += gia_tri_tich_luy_da_thanh_toan;
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

                    if (gia_tri_tich_luy != 0) {
                        tong_thanh_toan_con_thieu += gia_tri - cttlTransactionData.sumGiaTriThanhToan();
                    }
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
                List<CTTLTransactionData> subTransactionDataList;
                if (from < total) {
                    subTransactionDataList = transactionDataList.subList(
                            from,
                            to);
                } else {
                    subTransactionDataList = new ArrayList<>();
                }

                for (int iData = 0; iData < subTransactionDataList.size(); iData++) {
                    CTTLTransactionData cttlTransactionData = subTransactionDataList.get(iData);
                    CTTLTransactionInfo cttlTransactionInfo = new CTTLTransactionInfo();
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
                    if (cttlTransactionInfo.getType() == CTTLTransactionType.DON_HANG.getId()) {
                        transaction_info = this.orderDB.getAgencyOrderBasicByAgencyOrderDeptId(
                                cttlTransactionInfo.getTransaction_id()
                        );

                        JSONObject dept_order = this.deptDB.getDeptOrderInfoByCode(
                                ConvertUtils.toString(transaction_info.get("dept_code"))
                        );
                        if (transaction_info != null && dept_order != null) {
                            transaction_info.put("dept_cycle", dept_order.get("dept_cycle"));
                        }
                    } else if (cttlTransactionInfo.getType() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
                                cttlTransactionInfo.getTransaction_id()
                        );
                        JSONObject dept_order = this.deptDB.getDeptOrderInfoByTransactionId(
                                ConvertUtils.toInt(transaction_info.get("id"))
                        );
                        if (transaction_info != null && dept_order != null) {
                            transaction_info.put("dept_cycle", dept_order.get("dept_cycle"));
                        }
                    } else if (cttlTransactionInfo.getType() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                        transaction_info = this.deptDB.getAgencyDeptDtt(
                                cttlTransactionInfo.getTransaction_id()
                        );
                    } else if (cttlTransactionInfo.getType() == CTTLTransactionType.HBTL.getId()) {
                        transaction_info = this.orderDB.getAgencyHBTL(
                                cttlTransactionInfo.getTransaction_id()
                        );
                    } else if (cttlTransactionInfo.getType() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
                                cttlTransactionInfo.getTransaction_id()
                        );
                        if (transaction_info != null) {
                            transaction_info.put("code", transaction_info.get("doc_no"));
                        }
                    }
                    transaction_info.put("confirm_date", cttlTransactionData.getPayment_date());

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
            han_muc_uu_dai = this.getAgencyRewardByPromo(
                    request.getPromo_id(),
                    request.getAgency_id(),
                    ConvertUtils.toInt(promo.get("status")),
                    promoLimitInfoList,
                    agency_cttl_info);
            for (CTTLAgencyOffer agencyOffer : han_muc_uu_dai) {
                agencyOffer.convertGiftInfo(gift_info);
            }

            if (ConvertUtils.toString(promo.get("offer_info")).contains(PromoOfferType.GIFT_OFFER.getKey())) {
                tong_uu_dai_duoc_huong = han_muc_uu_dai.stream().reduce(
                        0L,
                        (t, object) -> t + object.sumGift(),
                        Long::sum);
            } else {
                tong_uu_dai_duoc_huong = han_muc_uu_dai.stream().reduce(
                        0L,
                        (t, object) -> t + object.getMoney(),
                        Long::sum);
            }

            long de_dat_han_muc_sau = this.getNextOfferOfPromo(
                    promoLimitInfoList,
                    tong_gia_tri_tich_luy
            );

            /**
             * Ngày xác nhận tham gia:
             * Số lượng ban đầu:
             * Số lượng tham gia:
             */
            if (agency_cttl_info != null) {
                int require_confirm_join = ConvertUtils.toInt(promo.get("require_confirm_join"));
                int confirm_join_quantity = ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity"));
                data.put("ngay_xac_nhan_tham_gia", (require_confirm_join == 0 || (require_confirm_join == 1 &&
                        confirm_join_quantity > 0)) == true ? agency_cttl_info.get("created_date") : null);
                data.put("so_luong_ban_dau",
                        ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")) < 0 ?
                                0 :
                                ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")));
                data.put("so_luong_tham_gia", this.getSoLuongThamGia(
                        ConvertUtils.toInt(agency_cttl_info.get("update_join_quantity")),
                        ConvertUtils.toInt(tong_uu_dai_duoc_huong)));
            }

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
                ClientResponse reward = this.accumulateService.getReward(
                        promo_id,
                        Arrays.asList(agency_id)
                );
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
            CTTLTransactionInfo cttlTransactionInfo,
            CTTLTransactionData cttlTransactionData,
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
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            JSONObject jsDeptOrder = this.deptDB.getDeptOrderInfoByCode(
                    ConvertUtils.toString(transaction_info.get("dept_code"))
            );
            if (jsDeptOrder == null) {
                return 0;
            } else {
                return ConvertUtils.toLong(jsDeptOrder.get("payment_value"));
            }
        } else if (type == CTTLTransactionType.TANG_CONG_NO.getId()) {
            JSONObject jsDeptOrder = this.deptDB.getDeptOrderInfoByTransactionId(
                    ConvertUtils.toInt(transaction_info.get("id"))
            );
            if (jsDeptOrder == null) {
                return 0;
            } else {
                return ConvertUtils.toLong(jsDeptOrder.get("payment_value"));
            }
        } else if (type == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return gia_tri;
        } else if (type == CTTLTransactionType.HBTL.getId()) {
            return gia_tri;
        } else if (type == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return gia_tri;
        } else return gia_tri;
    }

    private long getConPhaiThuOfCTTLTransaction(int type, CTTLTransactionInfo cttlTransactionInfo) {
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            return cttlTransactionInfo.getGia_tri()
                    - cttlTransactionInfo.getDa_thanh_toan();
        } else if (type == CTTLTransactionType.TANG_CONG_NO.getId()) {
            return cttlTransactionInfo.getGia_tri()
                    - cttlTransactionInfo.getDa_thanh_toan();
        } else if (type == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return 0;
        } else if (type == CTTLTransactionType.HBTL.getId()) {
            return 0;
        } else if (type == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return 0;
        } else return 0;
    }

    private long getGiaTriOfCTTLTransaction(int type, JSONObject transaction_info) {
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else if (type == CTTLTransactionType.TANG_CONG_NO.getId()) {
            return ConvertUtils.toLong(transaction_info.get("transaction_value"));
        } else if (type == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return ConvertUtils.toLong(transaction_info.get("data"));
        } else if (type == CTTLTransactionType.HBTL.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else if (type == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return ConvertUtils.toLong(transaction_info.get("transaction_value"));
        } else return 0;
    }

    private long getPaymentOfCTTLTransaction(int type, JSONObject transaction_info) {
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else if (type == CTTLTransactionType.TANG_CONG_NO.getId()) {
            return ConvertUtils.toLong(transaction_info.get("transaction_value"));
        } else if (type == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return ConvertUtils.toLong(transaction_info.get("data"));
        } else if (type == CTTLTransactionType.HBTL.getId()) {
            return ConvertUtils.toLong(transaction_info.get("total_end_price"));
        } else if (type == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return ConvertUtils.toLong(transaction_info.get("transaction_value"));
        } else return 0;
    }

    private void convertTransactionDataToInfo(
            int agency_id,
            int promo_id,
            CTTLTransactionInfo cttlTransactionInfo,
            CTTLTransactionData cttlTransactionData) {
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
                    CTTLTransactionType.fromKey(
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
                    (CTTLTransactionType.TANG_CONG_NO.getId() == cttlTransactionInfo.getType() ||
                            CTTLTransactionType.GIAM_CONG_NO.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
                            null
            );
            /*
            private Integer agency_order_dept_id;
             */
            cttlTransactionInfo.setAgency_order_dept_id(
                    (CTTLTransactionType.DON_HANG.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
                            null
            );
            /*
            private Integer agency_hbtl_id;
             */
            cttlTransactionInfo.setAgency_hbtl_id(
                    (CTTLTransactionType.HBTL.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
                            null
            );
            /*
            private Integer agency_dept_dtt_id;
             */
            cttlTransactionInfo.setAgency_dept_dtt_id(
                    (CTTLTransactionType.DIEU_CHINH_DTT.getId() == cttlTransactionInfo.getType()) ? cttlTransactionInfo.getTransaction_id() :
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
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
    }

    private long tinhGiaTriTichLuyOfTransaction(
            CTTLTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            return cttlTransactionData.sumGiaTriTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
        } else if (type == CTTLTransactionType.TANG_CONG_NO.getId()) {
            return cttlTransactionData.sumGiaTriTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
        } else if (type == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
            return cttlTransactionData.sumGiaTriTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
        } else if (type == CTTLTransactionType.HBTL.getId()) {
            return cttlTransactionData.sumGiaTriTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
        } else if (type == CTTLTransactionType.GIAM_CONG_NO.getId()) {
            return cttlTransactionData.sumGiaTriTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
        } else return 0;
    }

    private long tinhDTTTichLuyOfTransaction(
            CTTLTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        return cttlTransactionData.sumDTTTichLuy(is_cttl_product, mpProductInPromo, is_cttl_product_quantity);
    }


    private long tinhGiaTriTichLuySanPhamOfTransaction(
            CTTLTransactionData cttlTransactionData,
            int type,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product,
            boolean is_cttl_product_quantity) {
        if (type == CTTLTransactionType.DON_HANG.getId()) {
            return cttlTransactionData.sumDoanhThuThuanSanPhamTichLuy(
                    is_cttl_product,
                    mpProductInPromo);
        }
        return 0;
    }

    /**
     * Danh sách đại lý tích lũy
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterAgencyOfCTTL(SessionData sessionData, FilterListByIdRequest request) {
        try {
            JSONObject jsPromo = this.promoDB.getPromoJs(request.getId());
            if (jsPromo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int require_confirm_join = ConvertUtils.toInt(jsPromo.get("require_confirm_join"));
            JSONObject data = new JSONObject();
            List<JSONObject> agencyList = this.promoDB.getListAgencyOfCTTLJs(
                    request.getId(),
                    require_confirm_join
            );
            List<CTTLAgencyResult> cttlAgencyList = new ArrayList<>();
            for (JSONObject agency : agencyList) {
                CTTLAgencyResult cttlAgency = CTTLAgencyResult.from(
                        agency
                );

                /**
                 * tính kết quả tích lũy
                 */

                cttlAgencyList.add(cttlAgency);
            }

            int limit = ConvertUtils.toInt(
                    this.filterUtils.getValueByType(
                            request.getFilters(),
                            "limit"));
            int business_department_id = ConvertUtils.toInt(
                    this.filterUtils.getValueByType(
                            request.getFilters(),
                            "business_department_id"));
            String search = this.filterUtils.getValueByType(
                    request.getFilters(),
                    "search");

            cttlAgencyList.stream().filter(
                    x -> filterCTTLAgency(
                            x,
                            search,
                            limit,
                            business_department_id)
            ).collect(Collectors.toList());

            int from = this.appUtils.getOffset(request.getPage());
            int to = from + ConfigInfo.PAGE_SIZE;
            int total = cttlAgencyList.size();
            if (to > total) {
                to = total;
            }
            List<CTTLAgencyResult> records = new ArrayList<>();
            if (from < total) {
                records = cttlAgencyList.subList(
                        from,
                        to);
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

    private boolean filterCTTLAgency(
            CTTLAgencyResult x,
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
            CTTLTransactionData x,
            String search,
            int type,
            int status,
            PromoTimeRequest timeRequest) {
        if (((search.isEmpty() ||
                x.getCode().contains(search) ||
                x.getCode().toLowerCase().contains(search.toLowerCase())) &&
                (type == 0 ||
                        type == x.getTransaction_type()) &&
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

            /**
             * Danh sách hạn mức
             */
            List<JSONObject> promoLimitInfoList = this.getPromoLimitInfo(
                    request.getId(),
                    this.promoDB.getListPromoLimit(
                            request.getId()
                    )
            );
            promo.put("promo_limit_info", promoLimitInfoList);

            JSONObject data = new JSONObject();
            data.put("promo", promo);

            int tong_khach_hang_tham_gia = 0;
            int tong_khach_hang_dat = 0;
            long tong_gia_tri_tich_luy = 0;
            long tong_thanh_toan_hop_le = 0;
            long tong_uu_dai_tra_huong = 0;
            long tong_uu_dai_duoc_huong = 0;
            long tong_thanh_toan_con_thieu = 0;
            int tong_so_luong_ban_dau = 0;
            int tong_so_luong_tham_gia = 0;


            ClientResponse crStatistic = this.accumulateService.getStatistic(request.getId());
            if (crStatistic.success() && crStatistic.getData() != null) {
                JSONObject statistic = JsonUtils.DeSerialize(JsonUtils.Serialize(crStatistic.getData()), JSONObject.class);
                tong_khach_hang_tham_gia = ConvertUtils.toInt(statistic.get("joinedAgencyTotal"));
                tong_khach_hang_dat = ConvertUtils.toInt(statistic.get("rewardAgencyTotal"));
                tong_gia_tri_tich_luy = ConvertUtils.toLong(statistic.get("tlTotal"));
                tong_thanh_toan_hop_le = ConvertUtils.toLong(statistic.get("paymentTotal"));
                tong_uu_dai_tra_huong = ConvertUtils.toLong(statistic.get("rewardMoneyTotal"))
                        + ConvertUtils.toLong(statistic.get("rewardGiftTotal"));
                tong_uu_dai_duoc_huong = ConvertUtils.toLong(statistic.get("joinedAgencyTotal"));
                tong_thanh_toan_con_thieu = ConvertUtils.toLong(statistic.get("tong_thanh_toan_con_thieu"));
                tong_so_luong_ban_dau = this.promoDB.getTongSoLuongBanDau(request.getId());
                tong_so_luong_tham_gia = this.promoDB.getTongSoLuongThamGia(request.getId());
            }


            int require_confirm_join = ConvertUtils.toInt(promo.get("require_confirm_join"));
            if (require_confirm_join == YesNoStatus.YES.getValue()) {
                tong_khach_hang_tham_gia = this.promoDB.getTongKhachHangThamGia(request.getId(), require_confirm_join);
            }
            JSONObject jsResult = new JSONObject();
            jsResult.put("tong_khach_hang_tham_gia", tong_khach_hang_tham_gia);
            jsResult.put("tong_khach_hang_dat", tong_khach_hang_dat);
            jsResult.put("tong_gia_tri_tich_luy", tong_gia_tri_tich_luy);
            jsResult.put("tong_thanh_toan_hop_le", tong_thanh_toan_hop_le);
            jsResult.put("tong_uu_dai_tra_thuong", tong_uu_dai_tra_huong);
            jsResult.put("tong_so_luong_ban_dau", tong_so_luong_ban_dau);
            jsResult.put("tong_so_luong_tham_gia", tong_so_luong_tham_gia);
            data.put("result", jsResult);
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
            ProductCache gift_info = null;

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getPromo_id());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                gift_info = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id")));
                promo.put("gift_info", gift_info);
            }

            Program program = this.dataManager.getProgramManager().getMpCTTL().get(request.getPromo_id());

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

            JSONObject promo_link_group = this.promoDB.getPromoLinkGroupByPromoId(
                    request.getPromo_id());

            boolean is_cttl_product = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProduct();
            boolean is_cttl_product_quantity = PromoConditionCTTLType.from(
                    ConvertUtils.toString(promo.get("condition_type"))).isCTTLProductQuantity();

            PromoTimeRequest payment_date_data =
                    JsonUtils.DeSerialize(
                            promo.get("payment_date_data").toString(),
                            PromoTimeRequest.class
                    );

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

            JSONObject agency_cttl_info = this.promoDB.getAgencyCTTLInfoJs(
                    request.getPromo_id(),
                    request.getAgency_id()
            );
            if (agency_cttl_info == null) {
                return ClientResponse.success(data);
            }


            long tong_gia_tri_tham_gia = 0;
            long tong_gia_tri_tich_luy = 0;
            long tong_gia_tri_tich_luy_da_thanh_toan = 0;
            long tong_thanh_toan = 0;
            long tong_thanh_toan_con_thieu = 0;
            long tong_thanh_toan_hop_le = 0;
            List<CTTLAgencyOffer> han_muc_uu_dai = new ArrayList<>();
            long tong_uu_dai_duoc_huong = 0;

            if (request.getAgency_id() != 0 && agency_cttl_info != null) {
                List<JSONObject> transactionList = JsonUtils.DeSerialize(
                        agency_cttl_info.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
                if (ConvertUtils.toInt(promo.get("status")) != PromoCTTLStatus.STOPPED.getId()
                        && promo_link_group != null) {
                    transactionList = this.callGetListTransactionCTTLInfo(
                            request.getPromo_id(),
                            request.getAgency_id()
                    );
                }

                LogUtil.printDebug(JsonUtils.Serialize(transactionList));
                List<CTTLTransactionData> transactionDataList = new ArrayList<>();
                for (JSONObject transaction : transactionList) {
                    CTTLTransactionData cttlTransactionData = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(transaction),
                            CTTLTransactionData.class
                    );
                    this.parseCTTLTransactionData(cttlTransactionData);

                    JSONObject transaction_info = null;
                    if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DON_HANG.getId()) {
                        transaction_info = this.orderDB.getAgencyOrderBasicByAgencyOrderDeptId(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.TANG_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.DIEU_CHINH_DTT.getId()) {
                        transaction_info = this.deptDB.getAgencyDeptDtt(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.HBTL.getId()) {
                        transaction_info = this.orderDB.getAgencyHBTL(
                                cttlTransactionData.getTransactionId()
                        );
                    } else if (cttlTransactionData.getTransaction_type() == CTTLTransactionType.GIAM_CONG_NO.getId()) {
                        transaction_info = this.deptDB.getDeptTransactionJs(
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

                    long gia_tri_tich_luy = cttlTransactionData.sumGiaTriTichLuy(
                            is_cttl_product,
                            program.getMpProduct(),
                            is_cttl_product_quantity);
                    long gia_tri_tich_luy_da_thanh_toan = cttlTransactionData.sumGiaTriTichLuyThanhToan(
                            is_cttl_product,
                            program.getMpProduct(),
                            is_cttl_product_quantity, payment_date_data.getEnd_date_millisecond());

                    tong_gia_tri_tich_luy += gia_tri_tich_luy;
                    tong_gia_tri_tich_luy_da_thanh_toan += gia_tri_tich_luy_da_thanh_toan;
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

                    if (gia_tri_tich_luy != 0) {
                        tong_thanh_toan_con_thieu += gia_tri - cttlTransactionData.sumGiaTriThanhToan();
                    }
                }
            }

            /**
             * Kết quả tích lũy
             */
            han_muc_uu_dai = this.getAgencyRewardByPromo(
                    request.getPromo_id(),
                    request.getAgency_id(),
                    ConvertUtils.toInt(promo.get("status")),
                    promoLimitInfoList,
                    agency_cttl_info);
            for (CTTLAgencyOffer agencyOffer : han_muc_uu_dai) {
                agencyOffer.convertGiftInfo(gift_info);
            }

            if (ConvertUtils.toString(promo.get("offer_info")).contains(PromoOfferType.GIFT_OFFER.getKey())) {
                tong_uu_dai_duoc_huong = han_muc_uu_dai.stream().reduce(
                        0L,
                        (t, object) -> t + object.sumGift(),
                        Long::sum);
            } else {
                tong_uu_dai_duoc_huong = han_muc_uu_dai.stream().reduce(
                        0L,
                        (t, object) -> t + object.getMoney(),
                        Long::sum);
            }

            long de_dat_han_muc_sau = this.getNextOfferOfPromo(
                    promoLimitInfoList,
                    tong_gia_tri_tich_luy
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
             * Ngày xác nhận tham gia:
             * Số lượng ban đầu:
             * Số lượng tham gia:
             */
            int require_confirm_join = ConvertUtils.toInt(promo.get("require_confirm_join"));
            int confirm_join_quantity = ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity"));
            result.put("ngay_xac_nhan_tham_gia", (require_confirm_join == 0 || (require_confirm_join == 1 &&
                    confirm_join_quantity > 0)) == true ? agency_cttl_info.get("created_date") : null);
            result.put("so_luong_ban_dau",
                    ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")) < 0 ?
                            0 :
                            ConvertUtils.toInt(agency_cttl_info.get("confirm_join_quantity")));
            result.put("so_luong_tham_gia", this.getSoLuongThamGia(
                    ConvertUtils.toInt(agency_cttl_info.get("update_join_quantity")),
                    ConvertUtils.toInt(tong_uu_dai_duoc_huong)));
            data.put("result", result);
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

    private long getNextOfferOfPromo(List<JSONObject> promoLimitInfoList, long tich_luy) {
        for (JSONObject limit : promoLimitInfoList) {
            if (ConvertUtils.toLong(limit.get("from")) > tich_luy) {
                return ConvertUtils.toLong(limit.get("from")) - tich_luy;
            }
        }
        return 0;
    }

    public ClientResponse calculateProgressCTTL(BasicRequest request) {
        try {
            this.accumulateService.calculateProgressCTTL(request.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
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

                if (ConvertUtils.toInt(agency_cttl_info.get("status")) != PromoCTTLStatus.STOPPED.getId() &&
                        this.checkPromoLink(request.getPromo_id(), promo_relate_id)) {
                    transactionList = this.callGetListTransactionCTTLInfo(
                            promo_relate_id,
                            request.getAgency_id()
                    );
                }

                for (JSONObject transaction : transactionList) {
                    LogUtil.printDebug(JsonUtils.Serialize(transaction));
                    if (ConvertUtils.toInt(transaction.get("transactionId")) == request.getTransaction_id()
                            && request.getType() == CTTLTransactionType.fromKey(ConvertUtils.toString(transaction.get("type"))).getId()) {
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
                int structure = this.convertCTTLRelate(
                        promo_structure,
                        ConvertUtils.toInt(record.get("id")),
                        request.getPromo_id());
                record.put("structure", structure);
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

    private int getCTTLTransactionInfoByPromo(
            int promo_id,
            int agency_id,
            int transaction_id,
            int type,
            int structure,
            int status) {
        if (structure == PromoStructureType.LIEN_KET.getId() &&
                status != PromoCTTLStatus.STOPPED.getId()) {
            List<JSONObject> transactionList = this.callGetListTransactionCTTLInfo(
                    promo_id,
                    agency_id
            );
            for (JSONObject transaction : transactionList) {

            }
        } else {

        }
        return 0;
    }

    private List<JSONObject> callGetListTransactionCTTLInfo(int promo_id, int agency_id) {
        try {
            ClientResponse cr = this.accumulateService.callGetDataLienKet(promo_id, agency_id);
            if (cr.failed()) {
                return new ArrayList<>();
            }
            JSONObject result = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(cr.getData()),
                    JSONObject.class);
            if (result != null &&
                    result.get("ltTLTransaction") != null &&
                    !result.get("ltTLTransaction").toString().isEmpty()) {
                this.promoDB.saveCTTLTransactionTamTinh(
                        promo_id,
                        agency_id,
                        JsonUtils.Serialize(result.get("ltTLTransaction"))
                );

                return JsonUtils.DeSerialize(
                        JsonUtils.Serialize(result.get("ltTLTransaction")),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    private int convertCTTLRelate(JSONObject promo_structure, int promo_relate_id, int promo_id) {
        try {
            JSONObject promo_link_group = this.promoDB.getPromoLinkGroupByPromoId(promo_id);
            if (promo_link_group != null &&
                    this.promoDB.getPromoLinkGroupByPromoId(promo_relate_id) != null) {
                return PromoStructureType.LIEN_KET.getId();
            } else if (promo_structure.get("promo_dong_thoi_tru_gtdtt").toString().contains(
                    "\"" + promo_relate_id + "\""
            )) {
                return PromoStructureType.DONG_THOI_TRU_GIA_TRI_DA_TINH_THUONG.getId();
            } else if (promo_structure.get("promo_loai_tru").toString().contains(
                    "\"" + promo_relate_id + "\""
            )) {
                return PromoStructureType.LOAI_TRU.getId();
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return PromoStructureType.DONG_THOI.getId();
    }

    public ClientResponse getCTTLInfo(SessionData sessionData, GetResultCTTLOfAgencyRequest request) {
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

            promo.put("total_limit", this.promoDB.getTotalLimit(request.getPromo_id()));

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

            JSONObject data = new JSONObject();
            data.put("promo", promo);

            if (request.getAgency_id() != 0) {
                /**
                 * Thông tin đại lý
                 */
                data.put("agency_info",
                        this.dataManager.getAgencyManager().getAgencyBasicData(
                                request.getAgency_id()
                        )
                );
            }

            JSONObject jsResult = new JSONObject();
            data.put("result", jsResult);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterCTTLByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getId());
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                request.setIsLimit(0);
                String query = this.filterUtils.getQuery(
                        FunctionList.LIST_CTTL_WAITING,
                        request.getFilters(),
                        request.getSorts()
                );
                List<JSONObject> records = this.promoDB.filter(query,
                        this.appUtils.getOffset(request.getPage()),
                        ConfigInfo.PAGE_SIZE,
                        request.getIsLimit());
                for (JSONObject record : records) {
                    Program program = this.getProgramById(
                            ConvertUtils.toInt(record.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    int allow = this.checkProgramFilter(
                            agency,
                            program,
                            Source.WEB,
                            deptInfo) == true ? 1 : 2;
                    JSONObject promo = JsonUtils.DeSerialize(
                            this.dataManager.getProgramManager().getMpPromoRunning().get(program.getId()),
                            JSONObject.class);
                    if (promo != null
                            && (request.getAllow() == 0 || request.getAllow() == allow)
                            && allow == 1) {
                        record.put("allow", allow);
                        GetResultCTTLOfAgencyRequest getResultCTTLOfAgencyRequest =
                                new GetResultCTTLOfAgencyRequest();
                        getResultCTTLOfAgencyRequest.setAgency_id(
                                request.getId());
                        getResultCTTLOfAgencyRequest.setPromo_id(program.getId());
                        ClientResponse crGetResultCTTLOfAgency =
                                getResultCTTLOfAgency(sessionData, getResultCTTLOfAgencyRequest);
                        if (crGetResultCTTLOfAgency.success()) {
                            record.put("tk_info", crGetResultCTTLOfAgency.getData());
                        }
                        promoBasicDataList.add(record);
                    }
                }
            }
            JSONObject data = new JSONObject();
            data.put("promos", promoBasicDataList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}