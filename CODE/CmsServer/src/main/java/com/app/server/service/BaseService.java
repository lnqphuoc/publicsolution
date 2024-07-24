package com.app.server.service;

import com.app.server.adapter.TelegramAdapter;
import com.app.server.config.ConfigInfo;
import com.app.server.constants.DeptConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.dept.DeptAgencyInfo;
import com.app.server.data.dto.dept.DeptOrderClearingData;
import com.app.server.data.dto.dept.DeptTransactionDetailData;
import com.app.server.data.dto.mission.MissionCKSData;
import com.app.server.data.dto.notify.NotifyData;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.dto.product.ProductGroup;
import com.app.server.data.dto.program.*;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.product.Product;
import com.app.server.data.dto.program.product.ProgramOrderProduct;
import com.app.server.data.dto.promo.PromoOrderData;
import com.app.server.data.dto.promo.PromoProductInfoData;
import com.app.server.data.dto.staff.BusinessDepartment;
import com.app.server.data.dto.visibility.ProductVisibilitySetting;
import com.app.server.data.entity.*;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.dept.EditDeptDttRequest;
import com.app.server.database.*;
import com.app.server.enums.*;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BaseService {
    protected AppUtils appUtils;

    @Autowired
    public void setAppUtil(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    public DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    protected ValidateUtils validateUtils;

    @Autowired
    public void setValidateUtils(ValidateUtils validateUtils) {
        this.validateUtils = validateUtils;
    }

    protected FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    protected MasterService masterService;

    @Autowired
    public void setMasterService(MasterService masterService) {
        this.masterService = masterService;
    }

    protected DeptDB deptDB;

    @Autowired
    public void setDeptDB(DeptDB deptDB) {
        this.deptDB = deptDB;
    }

    protected OrderDB orderDB;

    @Autowired
    public void setOrderDB(OrderDB orderDB) {
        this.orderDB = orderDB;
    }

    protected StaffDB staffDB;

    @Autowired
    public void setStaffDB(StaffDB staffDB) {
        this.staffDB = staffDB;
    }

    protected AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    protected ProductDB productDB;

    @Autowired
    public void setProductDB(ProductDB productDB) {
        this.productDB = productDB;
    }

    protected ACoinDB aCoinDB;

    @Autowired
    public void setACoinDB(ACoinDB aCoinDB) {
        this.aCoinDB = aCoinDB;
    }

    protected WarehouseDB warehouseDB;

    @Autowired
    public void setWarehouseDB(WarehouseDB warehouseDB) {
        this.warehouseDB = warehouseDB;
    }

    protected PriceDB priceDB;

    @Autowired
    public void setPriceDB(PriceDB priceDB) {
        this.priceDB = priceDB;
    }

    protected SortUtil sortUtil;

    @Autowired
    public void setSortUtil(SortUtil sortUtil) {
        this.sortUtil = sortUtil;
    }

    protected ProductUtils productUtils;

    @Autowired
    public void setProductUtils(ProductUtils productUtils) {
        this.productUtils = productUtils;
    }

    protected PromoDB promoDB;

    @Autowired
    public void setPromoDB(PromoDB promoDB) {
        this.promoDB = promoDB;
    }

    protected NotifyDB notifyDB;

    @Autowired
    public void setNotifyDB(NotifyDB notifyDB) {
        this.notifyDB = notifyDB;
    }

    protected BannerDB bannerDB;

    @Autowired
    public void setBannerDB(BannerDB bannerDB) {
        this.bannerDB = bannerDB;
    }

    protected VisibilityDB visibilityDB;

    @Autowired
    public void setVisibilityDB(VisibilityDB visibilityDB) {
        this.visibilityDB = visibilityDB;
    }

    protected DealDB dealDB;

    @Autowired
    public void setDealDB(DealDB dealDB) {
        this.dealDB = dealDB;
    }

    protected BravoService bravoService;

    @Autowired
    public void setBravoService(BravoService bravoService) {
        this.bravoService = bravoService;
    }

    protected PomDB pomDB;

    @Autowired
    public void setPomDB(PomDB pomDB) {
        this.pomDB = pomDB;
    }

    /**
     * Nguyên tắc cấn trừ
     * <p>
     * -	Thứ tự trừ công nợ ưu tiên:
     * o	Đơn dang dở
     * o	Đơn đến hạn trước
     * o	Đơn ghi nhận công nợ trước. (Nếu nhiều đơn có ngày đến hạn giống nhau thì xét thời gian đơn hàng ghi nhận trước)
     */
    protected boolean clearingDeptTransaction(Integer dept_transaction_id,
                                              Integer agency_id,
                                              Date payment_date,
                                              String note,
                                              List<JSONObject> rsDeptOrderPaymentNone,
                                              String transactionEffectValueType,
                                              boolean hasCongNoAm,
                                              Date time
    ) {
        DeptTransactionEntity dept_transaction = this.deptDB.getDeptTransaction(dept_transaction_id);
        if (dept_transaction == null) {
            return false;
        }
        Long transaction_value = dept_transaction.getTransaction_value();
        Long transaction_used_value = dept_transaction.getTransaction_used_value();

        /**
         * Sắp xếp theo thứ tự ưu tiên
         */
        List<DeptOrderClearingData> rsDeptOrder = this.sortDeptOrder(
                rsDeptOrderPaymentNone);

        /**
         * Số tiền sử dụng
         */
        long transaction_use_value = transaction_used_value;

        /**
         * Duyệt cấn trừ theo thứ tự
         */
        Long remain_value = transaction_value - transaction_used_value;
        for (DeptOrderClearingData deptSort : rsDeptOrder) {
            DeptOrderClearingData deptOrderClearingData =
                    JsonUtils.DeSerialize(JsonUtils.Serialize(this.deptDB.getDeptOrderInfo(deptSort.getId())), DeptOrderClearingData.class);
            Long current_payment_value = 0L;
            Long total_payment_value = 0L;
            current_payment_value = deptOrderClearingData.getTransaction_value() - deptOrderClearingData.getPayment_value();
            if (current_payment_value.longValue() > remain_value.longValue()) {
                current_payment_value = remain_value;
            }
            total_payment_value = current_payment_value + deptOrderClearingData.getPayment_value();

            /**
             * set trạng thái hoàn thành
             */
            if (total_payment_value.longValue() == deptOrderClearingData.getTransaction_value().longValue()) {
                deptOrderClearingData.setStatus(DeptOrderStatus.FINISH.getId());
            }

            /**
             * Cập nhật thanh toán cho đơn hàng
             */
            boolean rsExcutePaymentDeptOrder = this.deptDB.excutePaymentDeptOrder(
                    deptOrderClearingData.getId(),
                    current_payment_value,
                    deptOrderClearingData.getStatus(),
                    payment_date);
            if (!rsExcutePaymentDeptOrder) {
                return false;
            }

            boolean rsInsertDeptClearing = this.insertDeptClearing(deptOrderClearingData.getId(),
                    dept_transaction_id,
                    current_payment_value,
                    note,
                    note);
            if (!rsInsertDeptClearing) {
                return false;
            }

            /**
             * Tính thưởng acoin nếu có
             */
            this.tinhThuongAcoin(
                    transactionEffectValueType,
                    deptOrderClearingData,
                    current_payment_value,
                    payment_date,
                    hasCongNoAm,
                    agency_id,
                    note,
                    time
            );

            /**
             * Ghi nhận thanh toán cho tích lũy
             */
            this.ghiNhanThanhToanChoTichLuy(
                    agency_id,
                    deptOrderClearingData.getDept_type_id(),
                    deptOrderClearingData.getDept_transaction_id(),
                    current_payment_value,
                    deptOrderClearingData.getCode(),
                    deptOrderClearingData.getDept_transaction_sub_type_id()
            );

            /**
             * + số tiền sử dụng
             */
            transaction_use_value += current_payment_value;

            remain_value -= current_payment_value;
            if (remain_value <= 0) {
                break;
            }
        }

        boolean rsUpdateTransactionUsedValue = this.deptDB.updateTransactionUsedValue(
                dept_transaction_id,
                transaction_use_value);
        if (!rsUpdateTransactionUsedValue) {
            return false;
        }

        return true;
    }

    private void ghiNhanThanhToanChoTichLuy(
            int agency_id,
            Integer dept_type_id,
            Integer transaction_id,
            long value,
            String code,
            int dept_transaction_sub_type_id) {
        try {
            String type = "";
            if (dept_type_id == DeptType.DEPT_DON_HANG.getId() ||
                    dataManager.getConfigManager().isHanMucHenGiao(dept_transaction_sub_type_id) ||
                    dataManager.getConfigManager().isHanMucHopDong(dept_transaction_sub_type_id)
            ) {
                type = CTTLTransactionType.DON_HANG.getKey();
                transaction_id = this.orderDB.getAgencyOrderDeptByDeptCode(
                        code
                );
            } else if (
                    dept_type_id == DeptType.DEPT_DECREASE.getId()) {
                type = CTTLTransactionType.GIAM_CONG_NO.getKey();
                code = "DEPT" + transaction_id;
            } else {
                type = CTTLTransactionType.TANG_CONG_NO.getKey();
                code = "DEPT" + transaction_id;
            }

            this.callGhiNhanThanhToanChoTichLuy(
                    type,
                    agency_id,
                    code,
                    value,
                    transaction_id,
                    CTTLTransactionSource.AUTO.getId(),
                    0
            );
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    private void callGhiNhanThanhToanChoTichLuy(
            String type,
            int agency_id,
            String code,
            long value,
            int transaction_id,
            int transaction_source,
            int staff_id
    ) {
        try {
            /**
             * - /tl/add_payment
             + key : reload key
             + type (string) 1: order....
             + agency_id (int)
             + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             + value (long: giá trị thanh toán)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/add_payment?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&value=" + value
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            boolean isSucess = false;
            if (rs != null && rs.success()) {
                isSucess = true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    private boolean tinhThuongAcoin(
            String transactionEffectValueType,
            DeptOrderClearingData deptOrderClearingData,
            Long current_payment_value,
            Date payment_date,
            boolean hasCongNoAm,
            Integer agency_id,
            String note,
            Date time
    ) {
        try {
            if (TransactionEffectValueType.INCREASE.getCode().equals(transactionEffectValueType)) {
                int acoin = this.dataManager.getAcoinReward(
                        deptOrderClearingData.getDept_time(),
                        this.getDeptOrderPaymentDeadline(
                                deptOrderClearingData.getDept_time(),
                                deptOrderClearingData.getDept_cycle()),
                        current_payment_value,
                        payment_date,
                        deptOrderClearingData.getDept_cycle(),
                        hasCongNoAm
                );

                if (acoin > 0) {
                    /**
                     * Cộng acoin cho đơn hàng nếu là công nợ đơn hàng
                     */
                    if (DeptType.DEPT_DON_HANG.getId() == deptOrderClearingData.getDept_type_id()) {
                        boolean rsIncreaseAcoinForAgencyOrder = this.aCoinDB.increaseAcoinForAgencyOrder(deptOrderClearingData.getDept_type_data(), acoin);
                        if (!rsIncreaseAcoinForAgencyOrder) {
                            return false;
                        }
                    }

                    /**
                     * Cộng acoin cho công nợ đơn hàng
                     */
                    boolean rsIncreaseAcoinForDeptOrder = this.aCoinDB.increaseAcoinForDeptOrder(deptOrderClearingData.getId(), acoin);
                    if (!rsIncreaseAcoinForDeptOrder) {
                        return false;
                    }

                    /**
                     * cộng acoin cho đại lý
                     */
                    ClientResponse rsIncreaseAcoin = this.increaseACoin(
                            agency_id,
                            acoin,
                            note,
                            time
                    );
                    if (rsIncreaseAcoin.failed()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    private boolean insertDeptClearing(Integer dept_order_id, Integer dept_transaction_id, Long dept_clearing_value, String dept_clearing_data, String note) {
        try {
            DeptClearingEntity deptClearingEntity = new DeptClearingEntity();
            deptClearingEntity.setDept_order_id(dept_order_id);
            deptClearingEntity.setDept_transaction_id(dept_transaction_id);
            deptClearingEntity.setNote(note);
            deptClearingEntity.setDept_clearing_value(dept_clearing_value);
            deptClearingEntity.setDept_clearing_data(dept_clearing_data);
            deptClearingEntity.setCreated_date(DateTimeUtils.getNow());
            this.deptDB.insertDeptClearing(deptClearingEntity);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return true;
    }


    /**
     * Sắp xếp thứ tự công nợ đơn
     * theo thứ tự ưu tiên trừ công nợ
     *
     * @param rsDeptOrderPaymentNone
     * @return
     */
    private List<DeptOrderClearingData> sortDeptOrder(List<JSONObject> rsDeptOrderPaymentNone) {
        /**
         * Nguyên tắc cấn trừ
         * -	Thứ tự trừ công nợ ưu tiên:
         * o	Đơn dang dở
         * o	Đơn đến hạn trước
         * o	Đơn ghi nhận công nợ trước. (Nếu nhiều đơn có ngày đến hạn giống nhau thì xét thời gian đơn hàng ghi nhận trước)
         */
        List<DeptOrderClearingData> rs = new ArrayList<>();
        for (JSONObject jsonObject : rsDeptOrderPaymentNone) {
            DeptOrderClearingData deptOrderClearingData = JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), DeptOrderClearingData.class);
            deptOrderClearingData.setDept_time(DateTimeUtils.getDateTime(jsonObject.get("dept_time").toString()));


            /**
             * Check công nợ dang dở
             */
            if (this.checkDeptOrderPaymentDoing(deptOrderClearingData.getPayment_value(), deptOrderClearingData.getTransaction_value())) {
                deptOrderClearingData.setPayment_doing(1);
            } else {
                deptOrderClearingData.setPayment_doing(0);
            }

            /**
             * Han thanh toan
             */
            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderClearingData.getDept_time(), deptOrderClearingData.getDept_cycle());
            deptOrderClearingData.setPayment_deadline(payment_deadline);
            /**
             * Ghi nhận công nợ trước: dept_time
             */
            rs.add(deptOrderClearingData);
        }

        Collections.sort(rs, new Comparator<DeptOrderClearingData>() {
            @Override
            public int compare(DeptOrderClearingData a, DeptOrderClearingData b) {
                /**
                 * công nợ dang dở
                 */
                int cmp0 = a.getPayment_doing() < b.getPayment_doing() ? 1 : a.getPayment_doing() == b.getPayment_doing() ? 0 : -1;
                if (cmp0 != 0) {
                    return cmp0;
                }

                /**
                 * Đến hạn trước
                 */
                int cmp1 = a.getPayment_deadline().getTime() > b.getPayment_deadline().getTime() ?
                        1 :
                        a.getPayment_deadline().getTime() == b.getPayment_deadline().getTime() ? 0 : -1;
                if (cmp1 != 0) {
                    return cmp1;
                }

                /**
                 * Phát sinh trước
                 */
                int cmp2 = a.getDept_time().getTime() > b.getDept_time().getTime() ?
                        1 :
                        a.getDept_time().getTime() == b.getDept_time().getTime() ? 0 : -1;
                if (cmp2 != 0) {
                    return cmp2;
                }

                /**
                 * Id trước
                 */
                int cmp3 = a.getId() < b.getDept_time().getTime() ? 1 : -1;
                return cmp3;
            }
        });
        return rs;
    }

    protected Date getDeptOrderPaymentDeadline(Date dept_time, Integer dept_cycle) {
        if (dept_cycle == null) {
            dept_cycle = 0;
        }

        /**
         * Ngày phát sinh công nợ
         */
        Date dept_date_create = DateTimeUtils.getDateTime(dept_time, "yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dept_date_create);
        calendar.add(Calendar.DATE, dept_cycle - 1);
        String d = DateTimeUtils.toString(calendar.getTime(), "yyyy-MM-dd");
        Date dept_date_deadline = DateTimeUtils.getDateTime(d + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
        return dept_date_deadline;
    }

    protected boolean checkDeptOrderPaymentDoing(Long payment_value, Long transaction_value) {
        if (payment_value.longValue() > 0
                && payment_value.longValue() < transaction_value.longValue()) {
            return true;
        }
        return false;
    }

    protected DeptOrderEntity createDeptOrderEntity(
            Integer agency_id,
            Integer dept_type_id,
            Integer dept_transaction_main_type_id,
            Integer dept_transaction_sub_type_id,
            Date dept_time,
            int dept_cycle,
            String cn_effect_type,
            String dtt_effect_type,
            String tt_effect_type,
            String acoin_effect_type,
            Long transaction_value,
            Long dept_cycle_end,
            String dept_type_data,
            String note,
            Integer dept_transaction_id,
            Long payment_value,
            int status,
            int order_data_index,
            String doc_no,
            String code) {
        DeptOrderEntity deptOrderEntity = new DeptOrderEntity();
        /**
         * agency_id
         */
        deptOrderEntity.setAgency_id(agency_id);
        /**
         * dept_type_id;
         */
        deptOrderEntity.setDept_type_id(dept_type_id);
        /**
         * dept_transaction_main_type_id;
         */
        deptOrderEntity.setDept_transaction_main_type_id(dept_transaction_main_type_id);
        /**
         * dept_transaction_sub_type_id;
         */
        deptOrderEntity.setDept_transaction_sub_type_id(dept_transaction_sub_type_id);
        /**
         * dept_time;
         */
        deptOrderEntity.setDept_time(dept_time);
        /**
         * dept_cycle;
         */
        deptOrderEntity.setDept_cycle(dept_cycle);
        /**
         * cn_effect_type,
         */
        deptOrderEntity.setCn_effect_type(cn_effect_type);
        /**
         * dtt_effect_type,
         */
        deptOrderEntity.setDtt_effect_type(dtt_effect_type);
        /**
         * tt_effect_type,
         */
        deptOrderEntity.setTt_effect_type(tt_effect_type);
        /**
         * acoin_effect_type,
         */
        deptOrderEntity.setAcoin_effect_type(acoin_effect_type);
        /**
         * transaction_value,
         */
        deptOrderEntity.setTransaction_value(transaction_value);
        /**
         * dept_cycle_end,
         */
        deptOrderEntity.setDept_cycle_end(dept_cycle_end);
        /**
         * dept_type_data,
         */
        deptOrderEntity.setDept_type_data(dept_type_data);

        /**
         * note
         */
        deptOrderEntity.setNote(note);
        /**
         * status
         */
        deptOrderEntity.setStatus(status);
        deptOrderEntity.setDept_transaction_id(dept_transaction_id);

        deptOrderEntity.setPayment_value(payment_value);

        deptOrderEntity.setOrder_data_index(order_data_index);
        deptOrderEntity.setDoc_no(doc_no);
        deptOrderEntity.setCode(code);
        return deptOrderEntity;
    }

    protected boolean increaseCNO(int agency_id,
                                  Long transaction_value,
                                  Integer dept_transaction_id,
                                  Integer creator_id,
                                  Long current_dept,
                                  Integer dept_cycle,
                                  Long dept_limit,
                                  Long ngd_limit,
                                  String note,
                                  Date time) {
        boolean rsIncreaseCNO = this.deptDB.increaseCNO(agency_id, transaction_value);
        if (!rsIncreaseCNO) {
            return false;
        }

        /**
         * lưu lịch sử thay đổi công nợ
         */
        this.insertDeptAgencyHistory(agency_id,
                transaction_value,
                dept_transaction_id,
                TransactionEffectType.CNO,
                note,
                creator_id,
                current_dept,
                dept_cycle,
                dept_limit,
                ngd_limit,
                time);
        return true;
    }

    protected void insertDeptAgencyHistory(int agency_id,
                                           Long transaction_value,
                                           Integer dept_transaction_id,
                                           TransactionEffectType transactionEffectType,
                                           String note,
                                           Integer creator_id,
                                           Long current_dept,
                                           Integer dept_cycle,
                                           Long dept_limit,
                                           Long ngd_limit,
                                           Date time) {
        DeptAgencyHistoryEntity deptAgencyHistoryEntity = new DeptAgencyHistoryEntity();
        deptAgencyHistoryEntity.setAgency_id(agency_id);
        deptAgencyHistoryEntity.setStatus(ActiveStatus.ACTIVATED.getValue());
        deptAgencyHistoryEntity.setData(transaction_value);
        deptAgencyHistoryEntity.setType(transactionEffectType == null ? "" : transactionEffectType.getCode());
        deptAgencyHistoryEntity.setCreated_date(time);
        deptAgencyHistoryEntity.setCreator_id(creator_id);
        deptAgencyHistoryEntity.setDept_cycle(dept_cycle);
        deptAgencyHistoryEntity.setDept_cycle_end(current_dept + transaction_value);
        deptAgencyHistoryEntity.setDept_limit(dept_limit);
        deptAgencyHistoryEntity.setNgd_limit(ngd_limit);
        deptAgencyHistoryEntity.setNote(note);
        this.deptDB.insertDeptAgencyHistory(deptAgencyHistoryEntity);
    }

    protected DeptAgencyInfoEntity initDeptAgencyDateByStartDate(DeptAgencyInfoEntity deptAgencyInfoEntity, int agency_id) {
        try {
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyInfo(agency_id);
            } else {
                /**
                 * Kỳ hạn nợ
                 */
                DeptSettingEntity deptCycle = this.deptDB.getDeptCycleSettingForAgency(agency_id);
                if (deptCycle == null) {
                    deptAgencyInfoEntity.setDept_cycle(0);
                } else {
                    deptAgencyInfoEntity.setDept_cycle(deptCycle.getDept_cycle());
                }

                /**
                 * Hạn mức nợ
                 */
                DeptSettingEntity deptCycleLimit = this.deptDB.getDeptLimitSettingForAgency(agency_id);
                if (deptCycleLimit == null) {
                    deptAgencyInfoEntity.setDept_limit(0L);
                } else {
                    deptAgencyInfoEntity.setDept_limit(deptCycleLimit.getDept_limit());
                }

                /**
                 * Hạn mức gối đầu
                 */
                DeptSettingEntity deptNGDLimit = this.deptDB.getNGDLimitSettingForAgency(agency_id);
                if (deptNGDLimit == null) {
                    deptAgencyInfoEntity.setNgd_limit(0L);
                } else {
                    deptAgencyInfoEntity.setNgd_limit(deptNGDLimit.getNgd_limit());
                }
            }

            Date dept_date = DateTimeUtils.getNow();

            /**
             *
             */
            List<JSONObject> deptOrderPaymentNoneList = this.deptDB.getDeptOrderPaymentNone(agency_id);

            /**
             * Tiền thu trong ngày
             */
            deptAgencyInfoEntity.setTt(0L);

            long total_dept_agency = deptAgencyInfoEntity.getCurrent_dept();

            deptAgencyInfoEntity.setCurrent_dept(total_dept_agency);
            deptAgencyInfoEntity.setDept_cycle_end(total_dept_agency);
            deptAgencyInfoEntity.setDept_cycle_start(total_dept_agency);

            /**
             * Doanh thu thuần trong ngày
             */
            deptAgencyInfoEntity.setDtt(0L);

            /**
             * tổng mua hàng trong ngày
             */
            deptAgencyInfoEntity.setTotal_price_order(0L);

            /**
             * tổng thanh toán trong ngày
             */
            deptAgencyInfoEntity.setTotal_price_payment(0L);

            /**
             * Nợ gối đầu
             */
            long no_goi_dau = 0;
            long no_xau = 0;
            long no_qua_han = 0;
            long no_trong_han = 0;
            long no_den_han = 0;
            if (deptAgencyInfoEntity.getCurrent_dept() > 0) {
                for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
                    DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
                    Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
                    long no_con_lai = 0;
                    long no_goi_dau_con_lai = deptAgencyInfoEntity.getNgd_limit() - no_goi_dau;
                    long dept_money = deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
                    if (no_goi_dau_con_lai > 0) {
                        long no_doi_dau_input = Math.min(no_goi_dau_con_lai, dept_money);
                        no_goi_dau += no_doi_dau_input;
                        no_con_lai = dept_money - no_doi_dau_input;
                    } else {
                        no_con_lai = dept_money;
                    }

                    if (this.isNoXau(payment_deadline)) {
                        no_xau += no_con_lai;
                        no_qua_han += no_con_lai;
                    } else if (this.isNoQuaHan(payment_deadline)) {
                        no_qua_han += no_con_lai;
                    } else if (this.isNoDenHan(payment_deadline)) {
                        no_den_han += no_con_lai;
                        no_trong_han += no_con_lai;
                    } else if (this.isNoTrongHan(payment_deadline)) {
                        no_trong_han += no_con_lai;
                    }
                }
            }

            deptAgencyInfoEntity.setNgd(no_goi_dau);
            deptAgencyInfoEntity.setNx(no_xau);
            deptAgencyInfoEntity.setNqh(no_qua_han);
            deptAgencyInfoEntity.setNdh(no_den_han);
            deptAgencyInfoEntity.setNth(no_trong_han);

            boolean rsUpdate = this.deptDB.updateDeptAgencyInfo(deptAgencyInfoEntity);
            return deptAgencyInfoEntity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return null;
    }

    /**
     * Khởi tạo dữ liệu công nợ hiện tại của đại lý
     *
     * @param agency_id
     * @return
     */
    protected DeptAgencyInfoEntity initDeptAgencyInfo(Integer agency_id) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = new DeptAgencyInfoEntity();
                deptAgencyInfoEntity.setAgency_id(agency_id);
                deptAgencyInfoEntity.setStatus(ActiveStatus.ACTIVATED.getValue());
                deptAgencyInfoEntity.setCreated_date(DateTimeUtils.getNow());
                deptAgencyInfoEntity.setCreated_date(DateTimeUtils.getNow());
                int rsInsert = this.deptDB.insertDeptAgencyInfo(deptAgencyInfoEntity);
                if (rsInsert <= 0) {
                    return null;
                }
                /**
                 * commit limit
                 */
                deptAgencyInfoEntity.setId(rsInsert);
                deptAgencyInfoEntity.setCommit_limit(this.dataManager.getConfigManager().getCommitLimit());
            }

            /**
             * Kỳ hạn nợ
             */
            DeptSettingEntity deptCycle = this.deptDB.getDeptCycleSettingForAgency(agency_id);
            if (deptCycle == null) {
                deptAgencyInfoEntity.setDept_cycle(0);
            } else {
                deptAgencyInfoEntity.setDept_cycle(deptCycle.getDept_cycle());
            }

            /**
             * Hạn mức nợ
             */
            DeptSettingEntity deptCycleLimit = this.deptDB.getDeptLimitSettingForAgency(agency_id);
            if (deptCycleLimit == null) {
                deptAgencyInfoEntity.setDept_limit(0L);
            } else {
                deptAgencyInfoEntity.setDept_limit(deptCycleLimit.getDept_limit());
            }

            /**
             * Hạn mức gối đầu
             */
            DeptSettingEntity deptNGDLimit = this.deptDB.getNGDLimitSettingForAgency(agency_id);
            if (deptNGDLimit == null) {
                deptAgencyInfoEntity.setNgd_limit(0L);
            } else {
                deptAgencyInfoEntity.setNgd_limit(deptNGDLimit.getNgd_limit());
            }

            this.deptDB.updateDeptAgencyInfo(deptAgencyInfoEntity);

            return deptAgencyInfoEntity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return null;
    }

    private long getTotalDeptAgency(DeptAgencyInfoEntity deptAgencyInfoEntity, List<JSONObject> deptOrderPaymentNoneList, long payment_today) {
        long total = 0;

        for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
            DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
            total += deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
        }
        return deptAgencyInfoEntity.getDept_cycle_start() - payment_today - total;
    }

    protected long getDeptAgencyNoGoiDau(Long total_dept_agency, Long ngd_limit) {
        long total = 0;
        if (total_dept_agency < 0) {
            return 0;
        }

        if (ngd_limit == 0) {
            return 0;
        }

        if (total_dept_agency.longValue() < ngd_limit.longValue()) {
            total = total_dept_agency.longValue();
        } else {
            total = ngd_limit.longValue();
        }
        return total;
    }

    protected long getDeptAgencyNoQuaHan(List<JSONObject> deptOrderPaymentNoneList, Long ngd_limit) {
        long total = 0;
        for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
            DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
            if (payment_deadline.getTime() < DateTimeUtils.getMilisecondsNow()) {
                total += deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
            }
        }
        if (total < ngd_limit.longValue()) {
            total = 0;
        }
        return total;
    }

    protected long getDeptAgencyNoDenHan(List<JSONObject> deptOrderPaymentNoneList, Long ngd_limit) {
        long total = 0;
        for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
            DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
            if (DateTimeUtils.getNow("yyyy-MM-dd").equals(DateTimeUtils.toString(payment_deadline, "yyyy-MM-dd"))) {
                total += deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
            }
        }

        if (total < ngd_limit.longValue()) {
            total = 0;
        }
        return total;
    }

    protected long getDeptAgencyNoTrongHan(List<JSONObject> deptOrderPaymentNoneList, Long ngd_limit) {
        long total = 0;
        for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
            DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
            if (DateTimeUtils.getDateTime(DateTimeUtils.getNow("yyyy-MM-dd") + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime() <= payment_deadline.getTime()) {
                total += deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
            }
        }

        if (total < ngd_limit.longValue()) {
            return 0;
        } else {

        }
        return total;
    }

    protected long getDeptAgencyNoXau(List<JSONObject> deptOrderPaymentNoneList, long current_dept, Long ngd_limit) {
        long total = 0;
        for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
            DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
            /**
             * Quá hạn và quá n ngày
             */
            if (payment_deadline.getTime() < DateTimeUtils.getMilisecondsNow()) {
                Date date_to_no_xau = this.getDateToNoXau(payment_deadline);

                if (date_to_no_xau.getTime() < DateTimeUtils.getMilisecondsNow()) {
                    total += deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
                }
            }
        }

        if (total > 0 && current_dept > 0) {
            if (current_dept < ngd_limit) {
                return 0;
            } else {
                if (total > ngd_limit) {
                    return ngd_limit;
                } else {
                    return ngd_limit - total;
                }
            }
        }

        return 0;
    }

    protected Date getDateToNoXau(Date payment_deadline) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(payment_deadline);

        calendar.add(Calendar.DATE, this.dataManager.getConfigManager().getNumberDateToNoXau());
        Date rs = calendar.getTime();
        return rs;
    }

    private long getTotalTtAgencyToday(int agency_id) {
        try {
            Date today = DateTimeUtils.getNow();
            String from_date = DateTimeUtils.toString(today, "yyyy-MM-dd") + " 00:00:00";
            String to_date = DateTimeUtils.toString(today, "yyyy-MM-dd") + " 23:59:59";
            long total_tt_increase = this.deptDB.getTotalTtAgencyByDate(
                    agency_id,
                    TransactionEffectValueType.INCREASE.getCode(),
                    from_date,
                    to_date);
            long total_tt_decrease = this.deptDB.getTotalTtAgencyByDate(
                    agency_id,
                    TransactionEffectValueType.DECREASE.getCode(),
                    from_date,
                    to_date);

            return total_tt_increase - total_tt_decrease;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return 0;
    }

    private long getTotalDttAgencyToday(int agency_id) {
        try {
            Date today = DateTimeUtils.getNow();
            String from_date = DateTimeUtils.toString(today, "yyyy-MM-dd") + " 00:00:00";
            String to_date = DateTimeUtils.toString(today, "yyyy-MM-dd") + " 23:59:59";
            long total_tt_increase = this.deptDB.getTotalDttAgencyByDate(
                    agency_id,
                    TransactionEffectValueType.INCREASE.getCode(),
                    from_date,
                    to_date);
            long total_tt_decrease = this.deptDB.getTotalDttAgencyByDate(
                    agency_id,
                    TransactionEffectValueType.DECREASE.getCode(),
                    from_date,
                    to_date);

            return total_tt_increase + total_tt_decrease;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return 0;
    }

    protected boolean increaseDTT(
            int agency_id,
            Long transaction_value,
            Integer dept_transaction_id,
            String note,
            Date time,
            boolean isEdit,
            int staff_id,
            String code) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                return false;
            }
            boolean rsIncreaseDTT = this.deptDB.increaseDTT(agency_id, transaction_value);
            if (!rsIncreaseDTT) {
                return false;
            }

            EditDeptDttRequest request = new EditDeptDttRequest();
            request.setAgency_id(agency_id);
            request.setNote(note);
            request.setData(transaction_value);
            request.setType(
                    ChangeValueType.INCREASE.getId()
            );
            int rsSaveDeptDttHistory = this.deptDB.saveDeptDttHistory(
                    request.getAgency_id(),
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    deptAgencyInfoEntity.getTotal_dtt_cycle(),
                    deptAgencyInfoEntity.getTotal_dtt_cycle() +
                            (request.getData() *
                                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    request.getNote(),
                    staff_id,
                    DateTimeUtils.toString(time),
                    isEdit == true ? "Điều chỉnh" : "-",
                    code
            );
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    private ClientResponse increaseACoin(int agency_id, int acoin_value, String note, Date time) {
        try {
            JSONObject jsAgency = this.agencyDB.getAgencyInfoById(agency_id);
            if (jsAgency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int current_point = ConvertUtils.toInt(jsAgency.get("current_point"));

            boolean rsDecreaseAcoin = this.aCoinDB.increaseACoin(agency_id, acoin_value);
            if (!rsDecreaseAcoin) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu lịch sử
             */
            AgencyAcoinHistoryEntity agencyAcoinHistoryEntity = this.saveAgencyAcoinHistory(
                    agency_id,
                    acoin_value,
                    current_point,
                    current_point + acoin_value,
                    note,
                    time
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse decreaseACoin(
            int agency_id,
            long acoin_value,
            String note,
            Date time) {
        try {
            JSONObject jsAgency = this.agencyDB.getAgencyInfoById(agency_id);
            if (jsAgency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int current_point = ConvertUtils.toInt(jsAgency.get("current_point"));
            long acoin = acoin_value;
            if (current_point < acoin_value) {
                acoin = current_point;
            }

            boolean rsDecreaseAcoin = this.aCoinDB.decreaseACoin(agency_id, acoin);
            if (!rsDecreaseAcoin) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu lịch sử
             */
            AgencyAcoinHistoryEntity agencyAcoinHistoryEntity = this.saveAgencyAcoinHistory(
                    agency_id,
                    acoin_value * -1,
                    current_point,
                    current_point - acoin,
                    note,
                    time
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected AgencyAcoinHistoryEntity saveAgencyAcoinHistory(
            int agency_id,
            long point, long old_point, long current_point,
            String note,
            Date time) {
        try {
            AgencyAcoinHistoryEntity agencyAcoinHistoryEntity = new AgencyAcoinHistoryEntity();
            agencyAcoinHistoryEntity.setAgency_id(agency_id);
            agencyAcoinHistoryEntity.setPoint(point);
            agencyAcoinHistoryEntity.setOld_point(old_point);
            agencyAcoinHistoryEntity.setCurrent_point(current_point);
            agencyAcoinHistoryEntity.setCreated_date(time);
            agencyAcoinHistoryEntity.setNote(note);
            int rs = this.agencyDB.insertAgencyAcoinHistory(agencyAcoinHistoryEntity);
            if (rs > 0) {
                agencyAcoinHistoryEntity.setId(rs);
                return agencyAcoinHistoryEntity;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return null;
    }

    protected double getAgencyHMKD(
            Double dept_limit,
            Double ngd_limit,
            Double current_dept,
            double totalValueDoingDept) {
        return dept_limit
                + ngd_limit - current_dept - totalValueDoingDept;
    }

    protected int getTonKho(int product_id) {
        try {
            JSONObject rs = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (rs != null) {
                int tonkho = ConvertUtils.toInt(rs.get("quantity_start_today"))
                        + ConvertUtils.toInt(rs.get("quantity_import_today"))
                        - ConvertUtils.toInt(rs.get("quantity_export_today"))
                        - ConvertUtils.toInt(rs.get("quantity_waiting_approve_today"))
                        - ConvertUtils.toInt(rs.get("quantity_waiting_ship_today"));
                if (tonkho > 0) {
                    return tonkho;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return 0;
    }

    protected ClientResponse insertWarehouseBill(int agency_id,
                                                 WarehouseBillType warehouseBillType,
                                                 String order_code,
                                                 List<JSONObject> products,
                                                 int creator_id) {
        try {
            WarehouseBillEntity warehouseBillEntity = new WarehouseBillEntity();
            warehouseBillEntity.setCode(order_code);
            warehouseBillEntity.setWarehouse_bill_type_id(warehouseBillType.getValue());
            warehouseBillEntity.setOrder_code(order_code);
            warehouseBillEntity.setCreated_date(DateTimeUtils.getNow());
            warehouseBillEntity.setCreator_id(creator_id);
            warehouseBillEntity.setConfirmed_date(DateTimeUtils.getNow());
            warehouseBillEntity.setConfirmer_id(creator_id);
            warehouseBillEntity.setWarehouse_id(this.dataManager.getWarehouseManager().getWarehouseSell().getId());
            warehouseBillEntity.setStatus(WarehouseBillStatus.CONFIRMED.getId());
            warehouseBillEntity.setWarehouse_export_bill_type_id(WarehouseExportBillType.XUAT_BAN.getValue());
            warehouseBillEntity.setAgency_id(agency_id);
            int rsInsert = this.warehouseDB.insertWarehouseBill(warehouseBillEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            warehouseBillEntity.setId(rsInsert);

            for (JSONObject jsProduct : products) {
                int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                int product_total_quantity = ConvertUtils.toInt(jsProduct.get("product_total_quantity"));
                WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                warehouseBillDetailEntity.setWarehouse_bill_id(warehouseBillEntity.getId());
                warehouseBillDetailEntity.setProduct_id(product_id);
                warehouseBillDetailEntity.setProduct_quantity(product_total_quantity);
                warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());
                int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected boolean decreaseQuantityWaitingShipToday(
            int product_id,
            int product_quantity,
            String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                this.alertToTelegram(
                        "decreaseQuantityWaitingShipToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }
            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_waiting_ship_today"));

            if (quantity - product_quantity < 0) {
                this.alertToTelegram("decreaseQuantityWaitingShipToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                boolean rs = this.warehouseDB.decreaseQuantityWaitingShipToday1(
                        product_id,
                        quantity);
                if (!rs) {
                    this.alertToTelegram("decreaseQuantityWaitingShipToday: " + product_id + " FAILED",
                            ResponseStatus.FAIL);
                }
            } else {
                boolean rs = this.warehouseDB.decreaseQuantityWaitingShipToday1(product_id,
                        product_quantity);
                if (!rs) {
                    this.alertToTelegram("decreaseQuantityWaitingShipToday: " + product_id + " FAILED",
                            ResponseStatus.FAIL);
                }
            }

            /**
             * Lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.WAITING_SHIP,
                    product_id,
                    quantity,
                    product_quantity * -1,
                    note);

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    protected boolean decreaseQuantityWaitingApproveToday(
            int product_id, int product_quantity,
            String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                this.alertToTelegram("decreaseQuantityWaitingApproveToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }
            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_waiting_approve_today"));
            if (quantity - product_quantity < 0) {
                this.alertToTelegram("decreaseQuantityWaitingApproveToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }
            boolean rs = this.warehouseDB.decreaseQuantityWaitingApproveToday1(
                    product_id,
                    product_quantity);
            if (!rs) {
                this.alertToTelegram("decreaseQuantityWaitingApproveToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }

            /**
             * Lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.WAITING_APPROVE,
                    product_id,
                    quantity,
                    product_quantity * -1,
                    note);

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    protected boolean increaseQuantityWaitingApproveToday(
            int product_id,
            int product_quantity,
            String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                this.alertToTelegram("increaseQuantityWaitingApproveToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                return false;
            }

            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_waiting_approve_today"));
            boolean rs = this.warehouseDB.increaseQuantityWaitingApproveToday1(
                    product_id,
                    product_quantity);
            if (!rs) {
                this.alertToTelegram("increaseQuantityWaitingApproveToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                return false;
            }

            /**
             * lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.WAITING_APPROVE,
                    product_id,
                    quantity,
                    product_quantity,
                    note);
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    protected boolean increaseQuantityExportToday(
            int product_id, int product_quantity,
            String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                this.alertToTelegram("increaseQuantityExportToday: " +
                                "product_id: " + product_id + "-" +
                                "product_quantity: " + product_quantity +
                                "FAILED",
                        ResponseStatus.FAIL);
                return false;
            }
            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_export_today"));
            boolean rs = this.warehouseDB.increaseQuantityExportToday1(product_id,
                    product_quantity);
            if (!rs) {
                this.alertToTelegram("increaseQuantityExportToday: " +
                                "product_id: " + product_id + "-" +
                                "product_quantity: " + product_quantity +
                                "FAILED",
                        ResponseStatus.FAIL);
                return false;
            }

            /**
             * Lưu lịch sử
             */
            this.saveWarehouseStockHistory(WarehouseChangeType.EXPORT,
                    product_id,
                    quantity,
                    product_quantity,
                    note);
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    public void alertToTelegram(String message, ResponseStatus responseStatus) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String errorIcon = "\uD83D\uDFE5";
            String successIcon = "✅";
            stringBuilder.append(
                            responseStatus.getValue() == ResponseStatus.FAIL.getValue()
                                    ? errorIcon
                                    : successIcon)
                    .append("[" + ConfigInfo.ENV_LEVEL + "][cms]")
                    .append(" " + message);
            TelegramAdapter.sendMsg(
                    stringBuilder.toString(),
                    ConfigInfo.CHAT_EXCEPTION_ID);
            LogUtil.printDebug(message);
        } catch (Exception ex) {
            LogUtil.printDebug("ALERT", ex);
        }
    }

    public void reportToTelegram(String message, ResponseStatus responseStatus) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String errorIcon = "\uD83D\uDFE5";
            String successIcon = "✅";
            stringBuilder.append(
                            responseStatus.getValue() == ResponseStatus.FAIL.getValue()
                                    ? errorIcon
                                    : successIcon)
                    .append("[" + ConfigInfo.ENV_LEVEL + "][cms]")
                    .append(" " + message);
            TelegramAdapter.sendMsg(
                    stringBuilder.toString(),
                    ConfigInfo.CHAT_REPORT_ID);
            LogUtil.printDebug(message);
        } catch (Exception ex) {
            LogUtil.printDebug("ALERT", ex);
        }
    }

    protected ClientResponse decreaseWarehouseWaitingApprove(List<ProductData> productDataList, String note) {
        for (ProductData product : productDataList) {
            int product_id = product.getId();
            int product_total_quantity = product.getQuantity();

            boolean rsDecreaseQuantityWaitingApprove = this.decreaseQuantityWaitingApproveToday(
                    product_id, product_total_quantity,
                    note);
            if (!rsDecreaseQuantityWaitingApprove) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_UPDATE_FAILED);
            }
        }
        return ClientResponse.success(null);
    }

    protected ClientResponse increaseWarehouseWaitingApprove(List<ProductData> productDataList, String note) {
        for (ProductData product : productDataList) {
            int product_id = product.getId();
            int product_total_quantity = product.getQuantity();

            boolean rsIncrease = this.increaseQuantityWaitingApproveToday(
                    product_id,
                    product_total_quantity,
                    note);
            if (!rsIncrease) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_UPDATE_FAILED);
            }
        }
        return ClientResponse.success(null);
    }

    protected ClientResponse decreaseWarehouseWaitingShip(List<ProductData> productDataList, String note) {
        for (ProductData product : productDataList) {
            int product_id = product.getId();
            int product_total_quantity = product.getQuantity();

            boolean rsDecreaseQuantityWaitingShip = this.decreaseQuantityWaitingShipToday(
                    product_id, product_total_quantity,
                    note);
            if (!rsDecreaseQuantityWaitingShip) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_UPDATE_FAILED);
            }
        }
        return ClientResponse.success(null);
    }

    protected boolean increaseQuantityWaitingShipToday(
            int product_id, int product_quantity,
            String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                /**
                 * thông báo qua tele
                 */
                this.alertToTelegram("increaseQuantityWaitingShipToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                return false;
            }
            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_waiting_ship_today"));
            boolean rs = this.warehouseDB.increaseQuantityWaitingShipToday1(product_id,
                    product_quantity);
            if (!rs) {
                this.alertToTelegram("increaseQuantityWaitingShipToday: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                return false;
            }
            /**
             * lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.WAITING_SHIP,
                    product_id, quantity, product_quantity, note);
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    protected ClientResponse increaseWarehouseWaitingShip(List<ProductData> productDataList, String note) {
        for (ProductData product : productDataList) {
            int product_id = product.getId();
            int product_total_quantity = product.getQuantity();
            boolean rsDecreaseQuantityWaitingShip = this.increaseQuantityWaitingShipToday(
                    product_id, product_total_quantity, note);
            if (!rsDecreaseQuantityWaitingShip) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_UPDATE_FAILED);
            }
        }
        return ClientResponse.success(null);
    }

    protected boolean isNoXau(Date payment_deadline) {
        if (payment_deadline.getTime() < DateTimeUtils.getMilisecondsNow()) {
            Date date_to_no_xau = this.getDateToNoXau(payment_deadline);
            if (date_to_no_xau.getTime() < DateTimeUtils.getMilisecondsNow()) {
                return true;
            }
        }
        return false;
    }

    protected boolean isNoQuaHan(Date payment_deadline) {
        if (payment_deadline.getTime() < DateTimeUtils.getMilisecondsNow()) {
            return true;
        }
        return false;
    }

    protected boolean isNoDenHan(Date payment_deadline) {
        if (DateTimeUtils.getNow("yyyy-MM-dd").equals(DateTimeUtils.toString(payment_deadline, "yyyy-MM-dd"))) {
            return true;
        }
        return false;
    }

    protected boolean isNoTrongHan(Date payment_deadline) {
        if (DateTimeUtils.getDateTime(DateTimeUtils.getNow("yyyy-MM-dd") + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime() <= payment_deadline.getTime()) {
            return true;
        }
        return false;
    }

    public ClientResponse updateDeptAgencyInfo(int agency_id) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = initDeptAgencyInfo(agency_id);
            }

            if (deptAgencyInfoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Tính toán dữ liệu công nợ
             * - nth
             * - nqh
             * - nx
             * - ndh
             */

            /**
             *
             */
            List<JSONObject> deptOrderPaymentNoneList = this.deptDB.getDeptOrderPaymentNone(agency_id);

            /**
             * Nợ gối đầu
             */
            long no_goi_dau = 0;
            long no_xau = 0;
            long no_qua_han = 0;
            long no_trong_han = 0;
            long no_den_han = 0;

            if (deptAgencyInfoEntity.getCurrent_dept() > 0) {
                for (JSONObject deptOrderPaymentNone : deptOrderPaymentNoneList) {
                    DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(deptOrderPaymentNone), DeptOrderEntity.class);
                    Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
                    long no_con_lai = 0;
                    long no_goi_dau_con_lai = deptAgencyInfoEntity.getNgd_limit() - no_goi_dau;
                    long dept_money = deptOrderEntity.getTransaction_value() - deptOrderEntity.getPayment_value();
                    if (no_goi_dau_con_lai > 0) {
                        long no_doi_dau_input = Math.min(no_goi_dau_con_lai, dept_money);
                        no_goi_dau += no_doi_dau_input;
                        no_con_lai = dept_money - no_doi_dau_input;
                    } else {
                        no_con_lai = dept_money;
                    }

                    if (this.isNoXau(payment_deadline)) {
                        no_xau += no_con_lai;
                        no_qua_han += no_con_lai;
                    } else if (this.isNoQuaHan(payment_deadline)) {
                        no_qua_han += no_con_lai;
                    } else if (this.isNoDenHan(payment_deadline)) {
                        no_den_han += no_con_lai;
                        no_trong_han += no_con_lai;
                    } else if (this.isNoTrongHan(payment_deadline)) {
                        no_trong_han += no_con_lai;
                    }
                }
            }

            deptAgencyInfoEntity.setNgd(no_goi_dau);
            deptAgencyInfoEntity.setNx(no_xau);
            deptAgencyInfoEntity.setNqh(no_qua_han);
            deptAgencyInfoEntity.setNdh(no_den_han);
            deptAgencyInfoEntity.setNth(no_trong_han);

            boolean rsUpdate = this.deptDB.updateDeptAgencyInfo(deptAgencyInfoEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            LogUtil.printDebug("[" + agency_id + "] Cập nhật thông tin nợ thành công");

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected Date getFirstDateOfYear() {
        int year = DateTimeUtils.getYear(DateTimeUtils.getNow());
        return DateTimeUtils.getDateTime(year + "-01-01", "yyyy-MM-dd");
    }

    protected void saveWarehouseStockHistory(
            WarehouseChangeType warehouseChangeType,
            int product_id,
            int before_value,
            int change_value,
            String note) {
        try {
            WarehouseStockHistoryEntity warehouseStockHistoryEntity =
                    new WarehouseStockHistoryEntity();
            warehouseStockHistoryEntity.setProduct_id(product_id);
            warehouseStockHistoryEntity.setBefore_value(before_value);
            warehouseStockHistoryEntity.setChange_value(change_value);
            warehouseStockHistoryEntity.setAfter_value(Math.max(before_value + change_value, 0));
            warehouseStockHistoryEntity.setCreated_date(DateTimeUtils.getNow());
            warehouseStockHistoryEntity.setType(warehouseChangeType.getCode());
            warehouseStockHistoryEntity.setNote(note);
            int rsInsert = this.warehouseDB.insertWarehouseStockHistory(warehouseStockHistoryEntity);
            if (rsInsert <= 0) {
                /**
                 *
                 */
                this.alertToTelegram("saveWarehouseStockHistory: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
    }

    protected int getProductVisibilityByAgency(
            int agency_id,
            int product_id) {
        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (agency == null) {
            return VisibilityType.HIDE.getId();
        }
        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                product_id
        );
        return this.checkProductVisibility(agency, productCache);
    }

    private int checkProductVisibility(JSONObject agency, ProductCache productCache) {
        if (productCache == null || productCache.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

//        /**
//         * Kiểm tra Catalog
//         */
//        if (!this.getVisibilityByCatalog(
//                ConvertUtils.toInt(agency.get("id")),
//                productCache.getMat_hang_id(),
//                productCache.getPlsp_id(),
//                ConvertUtils.toInt(agency.get("require_catalog")))) {
//            return VisibilityType.HIDE.getId();
//        }

        ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                productCache.getProduct_group_id());
        if (productGroup == null || productGroup.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category pltth = this.dataManager.getProductManager().getCategoryById(
                productGroup.getCategory_id()
        );
        if (pltth == null || pltth.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category plsp = this.dataManager.getProductManager().getCategoryById(
                pltth.getParent_id()
        );
        if (plsp == null || plsp.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                plsp.getParent_id()
        );
        if (mat_hang == null || mat_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category nganh_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang.getParent_id()
        );
        if (nganh_hang == null || nganh_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        if (productCache.getBrand_id() != null && productCache.getBrand_id() != 0) {
            BrandEntity brand = this.dataManager.getProductManager().getMpBrand().get(productCache.getBrand_id());
            if (brand == null || brand.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
                return VisibilityType.HIDE.getId();
            }
        }

        JSONObject settingAgency = this.getVisibilityByObject(
                SettingObjectType.AGENCY.getCode(),
                ConvertUtils.toInt(agency.get("id")),
                productCache.getId(),
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                productCache.getBrand_id()
        );
        if (settingAgency != null) {
            int visibility = ConvertUtils.toInt(settingAgency.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingCity = this.getVisibilityByObject(
                SettingObjectType.CITY.getCode(),
                ConvertUtils.toInt(agency.get("city_id")),
                productCache.getId(),
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                productCache.getBrand_id()
        );
        if (settingCity != null) {
            int visibility = ConvertUtils.toInt(settingCity.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingRegion = this.getVisibilityByObject(
                SettingObjectType.REGION.getCode(),
                ConvertUtils.toInt(agency.get("region_id")),
                productCache.getId(),
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                productCache.getBrand_id()
        );
        if (settingRegion != null) {
            int visibility = ConvertUtils.toInt(settingRegion.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingMembership = this.getVisibilityByObject(
                SettingObjectType.MEMBERSHIP.getCode(),
                ConvertUtils.toInt(agency.get("membership_id")),
                productCache.getId(),
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                productCache.getBrand_id()
        );
        if (settingMembership != null) {
            int visibility = ConvertUtils.toInt(settingMembership.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        return VisibilityType.SHOW.getId();
    }

    private boolean getVisibilityByCatalog(
            int agency_id,
            int mat_hang_id,
            Integer plsp_id,
            int require_catalog) {
        try {
            if (require_catalog == 0) {
                return true;
            }
            JSONObject jsCatalogMatHang = this.productDB.getCatalogByCategory(
                    agency_id,
                    mat_hang_id,
                    AgencyCatalogDetailStatus.APPROVED.getId()
            );
            if (jsCatalogMatHang != null) {
                return true;
            }

            if (plsp_id != null) {
                JSONObject jsCatalogPLSP = this.productDB.getCatalogByCategory(
                        agency_id,
                        plsp_id,
                        AgencyCatalogDetailStatus.APPROVED.getId()
                );
                if (jsCatalogPLSP != null) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LogUtil.printDebug("", e);
        }
        return false;
    }

    private JSONObject getVisibilityByObject(
            String visibility_object_type,
            int visibility_object_id,
            int product_id,
            int nhom_hang_id,
            int pltth_id,
            int plsp_id,
            int mat_hang_id,
            Integer brand_id) {
        Date date = DateTimeUtils.getNow();
        //JSONObject settingObject = this.visibilityDB.getVisibilityByObject(visibility_object_type, visibility_object_id);
        ProductVisibilitySetting settingObject = this.dataManager.getProductManager().getVisibilityByObject(
                visibility_object_type,
                visibility_object_id,
                date
        );
        if (settingObject != null) {
            if (product_id != 0) {
//                JSONObject settingProduct = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.PRODUCT.getCode(),
//                        product_id
//                );

                JSONObject settingProduct = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.PRODUCT.getCode(),
                        product_id,
                        date
                );

                if (settingProduct != null) {
                    return settingProduct;
                }
            }
            if (nhom_hang_id != 0) {
//                JSONObject settingNhomHang = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.PRODUCT_GROUP.getCode(),
//                        nhom_hang_id
//                );
                JSONObject settingNhomHang = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.PRODUCT_GROUP.getCode(),
                        nhom_hang_id,
                        date
                );
                if (settingNhomHang != null) {
                    return settingNhomHang;
                }
            }

            if (pltth_id != 0) {
//                JSONObject settingPLTTH = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.BRAND_CATEGORY.getCode(),
//                        pltth_id
//                );
                JSONObject settingPLTTH = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.BRAND_CATEGORY.getCode(),
                        pltth_id,
                        date
                );
                if (settingPLTTH != null) {
                    return settingPLTTH;
                }
            }
            if (plsp_id != 0) {
//                JSONObject settingPLSP = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.PRODUCT_CATEGORY.getCode(),
//                        plsp_id
//                );
                JSONObject settingPLSP = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.PRODUCT_CATEGORY.getCode(),
                        plsp_id,
                        date
                );
                if (settingPLSP != null) {
                    return settingPLSP;
                }
            }

            if (mat_hang_id != 0) {
//                JSONObject settingMatHang = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.ITEM_CATEGORY.getCode(),
//                        mat_hang_id
//                );
                JSONObject settingMatHang = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.ITEM_CATEGORY.getCode(),
                        mat_hang_id,
                        date
                );
                if (settingMatHang != null) {
                    return settingMatHang;
                }
            }
            if (brand_id != null && brand_id != 0) {
//                JSONObject settingBrand = this.visibilityDB.getVisibilityBySettingId(
//                        settingObject.getId(),
//                        VisibilityDataType.BRAND.getCode(),
//                        brand_id
//                );
                JSONObject settingBrand = settingObject.checkDataRunning(
                        settingObject.getId(),
                        VisibilityDataType.BRAND.getCode(),
                        brand_id,
                        date
                );
                if (settingBrand != null) {
                    return settingBrand;
                }
            }
        }
        return null;
    }

    private JSONObject getProductVisibilitySettingByVisibilityDataType(String visibility_data_type, int visibility_data_id, JSONObject agency) {
        try {
            if (agency == null) {
                return null;
            }

            /**
             * Theo đại lý
             */
            JSONObject settingAgency = this.productDB.getProductVisibilitySettingByVisibility(
                    visibility_data_type,
                    visibility_data_id,
                    SettingObjectType.AGENCY.getCode(),
                    ConvertUtils.toInt(agency.get("id"))
            );
            if (settingAgency != null) {
                return settingAgency;
            }

            /**
             * Theo tỉnh thành
             */
            JSONObject settingCity = this.productDB.getProductVisibilitySettingByVisibility(
                    visibility_data_type,
                    visibility_data_id,
                    SettingObjectType.CITY.getCode(),
                    ConvertUtils.toInt(agency.get("city_id"))
            );
            if (settingCity != null) {
                return settingCity;
            }

            /**
             * Theo khu vực
             */
            JSONObject settingRegion = this.productDB.getProductVisibilitySettingByVisibility(
                    visibility_data_type,
                    visibility_data_id,
                    SettingObjectType.REGION.getCode(),
                    ConvertUtils.toInt(agency.get("region_id"))
            );
            if (settingRegion != null) {
                return settingRegion;
            }

            /**
             * Theo cấp bậc
             */
            JSONObject settingMembership = this.productDB.getProductVisibilitySettingByVisibility(
                    visibility_data_type,
                    visibility_data_id,
                    SettingObjectType.MEMBERSHIP.getCode(),
                    ConvertUtils.toInt(agency.get("membership_id"))
            );
            if (settingMembership != null) {
                return settingMembership;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }

        return null;
    }

    protected ProductCache getProductPriceByObject(
            double price_original_current,
            SettingObjectType settingObjectType,
            int price_object_id,
            int product_id) {
        JSONObject price = this.priceDB.getProductPriceByObjectAndProduct(
                settingObjectType.getCode(),
                price_object_id,
                product_id
        );
        if (price != null) {
            ProductCache productCache = new ProductCache();
            productCache.setId(product_id);
            productCache.setMinimum_purchase(
                    ConvertUtils.toInt(price.get("minimum_purchase"))
            );
            productCache.setPrice(
                    this.convertProductPrice(
                            price_original_current,
                            ConvertUtils.toLong(price.get("price_original")),
                            ConvertUtils.toInt(price.get("is_auto")),
                            ConvertUtils.toString(price.get("price_setting_type")),
                            ConvertUtils.toString(price.get("price_data_type")),
                            ConvertUtils.toDouble(price.get("price_setting_value"))
                    ));
            return productCache;
        }
        return null;
    }

    protected Double convertProductPrice(
            double price_original_current,
            double price_new,
            int is_auto,
            String price_setting_type,
            String price_data_type,
            double price_setting_value) {
        PriceSettingType priceSettingType = PriceSettingType.from(price_setting_type);
        switch (priceSettingType) {
            case INCREASE: {
                if (price_original_current == -1) {
                    return -1.0;
                } else if (is_auto == 0) {
                    return price_new;
                } else {
                    if (PriceDataType.PERCENT.getCode().equals(price_data_type)) {
                        double price = price_original_current * 1.0F + price_original_current * 1.0F * price_setting_value / 100;
                        return this.appUtils.roundPrice(price);
                    } else if (PriceDataType.MONEY.getCode().equals(price_data_type)) {
                        return ConvertUtils.toDouble(price_original_current + price_setting_value);
                    }
                }
                return price_new;
            }
            case DECREASE: {
                if (price_original_current == -1) {
                    return -1.0;
                } else if (PriceDataType.PERCENT.getCode().equals(price_data_type)) {
                    double price = Math.max(0, price_original_current * 1.0F - price_original_current * 1.0F * price_setting_value / 100);
                    return this.appUtils.roundPrice(price);
                } else if (PriceDataType.MONEY.getCode().equals(price_data_type)) {
                    return ConvertUtils.toDouble(Math.max(0, price_original_current - price_setting_value));
                }
            }
            case CONSTANT: {
                return ConvertUtils.toDouble(price_setting_value);
            }
            case CONTACT: {
                return -1.0;
            }
            default:
                return null;
        }
    }

    protected ProductCache getFinalPriceByAgency(
            int product_id,
            int agency_id,
            int city_id,
            int region_id,
            int membership_id) {
        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(product_id);
        if (productCache == null) {
            return null;
        }

        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (ConvertUtils.toInt(agency.get("block_price")) == YesNoStatus.YES.getValue()) {
            ProductCache productPrice = new ProductCache();
            productPrice.setPrice(-1);
            productPrice.setId(product_id);
            productPrice.setMinimum_purchase(productCache.getMinimum_purchase());
            return productPrice;
        }

        ProductCache agency_price = this.getProductPriceByObject(
                productCache.getPrice(),
                SettingObjectType.AGENCY,
                agency_id,
                product_id);
        if (agency_price != null) {
            if (agency_price.getMinimum_purchase() == 0) {
                agency_price.setMinimum_purchase(productCache.getMinimum_purchase());
            }
            return agency_price;
        }

        ProductCache city_price = this.getProductPriceByObject(
                productCache.getPrice(),
                SettingObjectType.CITY,
                city_id,
                product_id);
        if (city_price != null) {
            if (city_price.getMinimum_purchase() == 0) {
                city_price.setMinimum_purchase(productCache.getMinimum_purchase());
            }
            return city_price;
        }

        ProductCache region_price = this.getProductPriceByObject(
                productCache.getPrice(),
                SettingObjectType.REGION,
                region_id,
                product_id);
        if (region_price != null) {
            if (region_price.getMinimum_purchase() == 0) {
                region_price.setMinimum_purchase(productCache.getMinimum_purchase());
            }
            return region_price;
        }

        ProductCache membership_price = this.getProductPriceByObject(
                productCache.getPrice(),
                SettingObjectType.MEMBERSHIP,
                membership_id,
                product_id);
        if (membership_price != null) {
            if (membership_price.getMinimum_purchase() == 0) {
                membership_price.setMinimum_purchase(productCache.getMinimum_purchase());
            }
            return membership_price;
        }

        return productCache;
    }

    protected ProductCache getFinalPriceByCity(
            long price_original_current,
            int product_id,
            int city_id,
            int region_id) {
        ProductCache city_price = this.getProductPriceByObject(
                price_original_current,
                SettingObjectType.CITY,
                city_id,
                product_id);
        if (city_price != null) {
            return city_price;
        }

        ProductCache region_price = this.getProductPriceByObject(
                price_original_current,
                SettingObjectType.REGION,
                region_id,
                product_id);
        if (region_price != null) {
            return region_price;
        }

        return this.dataManager.getProductManager().getProductBasicData(
                product_id
        );
    }

    protected ProductCache getFinalPriceByRegion(
            long price_original_current,
            int product_id,
            int region_id) {
        ProductCache region_price = this.getProductPriceByObject(
                price_original_current,
                SettingObjectType.REGION,
                region_id,
                product_id);
        if (region_price != null) {
            return region_price;
        }

        return this.dataManager.getProductManager().getProductBasicData(
                product_id
        );
    }

    protected ProductCache getFinalPriceByMembership(
            long price_original_current,
            int product_id,
            int membership_id) {
        ProductCache membership_price = this.getProductPriceByObject(
                price_original_current,
                SettingObjectType.MEMBERSHIP,
                membership_id,
                product_id);
        if (membership_price != null) {
            return membership_price;
        }

        return this.dataManager.getProductManager().getProductBasicData(
                product_id
        );
    }

    protected ProductCache getFinalPriceByObject(
            long price_original_current,
            String price_object_type,
            int price_object_id,
            int product_id) {
        SettingObjectType settingObjectType =
                SettingObjectType.from(price_object_type);
        switch (settingObjectType) {
            case AGENCY: {
                JSONObject agencyInfo = this.dataManager.getAgencyManager().getAgencyInfo(
                        price_object_id
                );
                if (agencyInfo == null) {
                    return null;
                }

                return this.getFinalPriceByAgency(
                        product_id,
                        price_object_id,
                        ConvertUtils.toInt(agencyInfo.get("city_id")),
                        ConvertUtils.toInt(agencyInfo.get("region_id")),
                        ConvertUtils.toInt(agencyInfo.get("membership_id"))
                );
            }
            case CITY: {
                int region_id = this.dataManager.getProductManager().getRegionFromCity(price_object_id);
                return this.getFinalPriceByCity(
                        price_original_current,
                        product_id,
                        price_object_id,
                        region_id
                );
            }
            case REGION: {
                return this.getFinalPriceByRegion(
                        price_original_current,
                        product_id,
                        price_object_id
                );
            }
            case MEMBERSHIP: {
                return this.getFinalPriceByMembership(
                        price_original_current,
                        product_id,
                        price_object_id
                );
            }
        }
        return null;
    }

    protected ClientResponse checkVisibilityWhenCreateOrder(
            int agency_id,
            int city_id,
            int region_id,
            int membership_id,
            List<ProgramOrderProduct> productRequests
    ) {
        for (int iP = 0; iP < productRequests.size(); iP++) {
            int visibility = this.getProductVisibilityByAgency(
                    agency_id,
                    productRequests.get(iP).getProductId()
            );

            if (VisibilityType.HIDE.getId() == visibility) {
                ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_VISIBILITY);
                clientResponse.setMessage("[Thứ " + (iP + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }

            ProductCache price = this.getFinalPriceByAgency(
                    productRequests.get(iP).getProductId(),
                    agency_id,
                    city_id,
                    region_id,
                    membership_id
            );

            if (price.getPrice() < 0) {
                ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_FAIL_BY_PRODUCT_PRICE_CONTACT);
                clientResponse.setMessage("[Thứ " + (iP + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return ClientResponse.success(null);
    }

    protected ProductDataSetting getProductSetting(Agency agency, Product product) {
        ProductCache productPrice = this.getFinalPriceByAgency(
                product.getId(),
                agency.getId(),
                agency.getCityId(),
                agency.getRegionId(),
                agency.getMembershipId());

        ProductDataSetting productDataSetting = new ProductDataSetting();
        productDataSetting.setProductId(product.getId());
        productDataSetting.setProductPrice(productPrice.getPrice());
        productDataSetting.setProductMinimumPurchase(productPrice.getMinimum_purchase());
        return productDataSetting;
    }

    protected Order getLatestCompletedOrder(int agencyId, String fromDate, String endDate) {
        JSONObject rs = orderDB.getLatestCompletedOrder(agencyId, fromDate, endDate);
        if (rs != null) {
            Order order = new Order();
            order.setId(ConvertUtils.toInt(rs.get("id")));
            order.setConfirmDeliveryDate(DateTimeUtils.getDateTime(ConvertUtils.toString(rs.get("confirm_delivery_date"))));
            return order;
        }

        return null;
    }

    protected int countFailedCommit(int id) {
        return this.orderDB.countFailedCommit(id);
    }

    protected ClientResponse ghiNhanCongNoDonHang(
            int agency_order_id,
            int agency_id,
            long total_end_price,
            String order_code,
            int han_muc_id,
            int staff_id,
            Date time,
            Integer order_dept_cycle,
            int order_data_index,
            String dept_code
    ) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(
                    agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(
                        deptAgencyInfoEntity,
                        agency_id);
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }

            long current_dept = deptAgencyInfoEntity.getCurrent_dept();

            int dept_cycle = order_dept_cycle != null ? order_dept_cycle : deptAgencyInfoEntity.getDept_cycle();

            /**
             * Ghi nhận công nợ cho đơn hàng
             */
            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = this.dataManager.getConfigManager().getHanMuc(
                    han_muc_id
            );
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (deptTransactionSubTypeEntity.getDept_type_id() != DeptType.DEPT_DON_HANG.getId()) {
                /**
                 * Lưu dept transaction
                 */
                DeptTransactionEntity cancelTransactionEntity = this.createDeptTransactionEntity(
                        deptTransactionSubTypeEntity.getId(),
                        deptTransactionSubTypeEntity.getDept_transaction_main_type_id(),
                        deptTransactionSubTypeEntity.getDept_type_id(),
                        deptTransactionSubTypeEntity.getCn_effect_type(),
                        deptTransactionSubTypeEntity.getDtt_effect_type(),
                        deptTransactionSubTypeEntity.getTt_effect_type(),
                        deptTransactionSubTypeEntity.getAcoin_effect_type(),
                        total_end_price,
                        agency_id,
                        deptAgencyInfoEntity.getDept_cycle_end(),
                        order_code,
                        "",
                        DeptTransactionStatus.CONFIRMED.getId(),
                        DateTimeUtils.getNow(),
                        null,
                        deptTransactionSubTypeEntity.getFunction_type(),
                        staff_id,
                        null,
                        DateTimeUtils.getNow(),
                        DateTimeUtils.getNow(),
                        order_code,
                        0L,
                        0L
                );
                int rsInsertTransaction = this.deptDB.createDeptTransaction(cancelTransactionEntity);
                if (rsInsertTransaction <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                cancelTransactionEntity.setId(rsInsertTransaction);
            }
            boolean rsIncreaseCNO = this.increaseCNO(
                    agency_id,
                    total_end_price,
                    null,
                    null,
                    deptAgencyInfoEntity.getCurrent_dept(),
                    dept_cycle,
                    deptAgencyInfoEntity.getDept_limit(),
                    deptAgencyInfoEntity.getNgd_limit(),
                    deptTransactionSubTypeEntity.getName(),
                    time
            );
            if (!rsIncreaseCNO) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INCREASE_DEPT_FAILED);
            }

            /**
             * Tăng dtt
             */
            if (deptTransactionSubTypeEntity.getDtt_effect_type().equals(TransactionEffectValueType.INCREASE.getCode())) {
                boolean rsIncreaseDTT = this.increaseDTT(
                        agency_id,
                        total_end_price,
                        null,
                        deptTransactionSubTypeEntity.getName(),
                        time,
                        false,
                        staff_id,
                        TransactionType.DON_HANG.getKey() + "-" + order_code
                );
                if (!rsIncreaseDTT) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INCREASE_DEPT_FAILED);
                }
            }

            boolean rsIncreaseTotalPriceOrder = this.deptDB.increaseTotalPriceOrder(
                    agency_id,
                    total_end_price);
            if (!rsIncreaseTotalPriceOrder) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            long total_payment = Math.max(
                    0,
                    current_dept < 0 ?
                            Math.min(
                                    Math.abs(current_dept),
                                    total_end_price) : 0);
            boolean hasCongNoAm = current_dept < 0;

            DeptOrderEntity deptOrderEntity = this.createDeptOrderEntity(
                    agency_id,
                    deptTransactionSubTypeEntity.getDept_type_id(),
                    DeptTransactionMainType.INCREASE.getId(),
                    deptTransactionSubTypeEntity.getId(),
                    DateTimeUtils.getNow(),
                    dept_cycle,
                    deptTransactionSubTypeEntity.getCn_effect_type(),
                    deptTransactionSubTypeEntity.getDtt_effect_type(),
                    deptTransactionSubTypeEntity.getTt_effect_type(),
                    deptTransactionSubTypeEntity.getAcoin_effect_type(),
                    total_end_price,
                    deptAgencyInfoEntity.getDept_cycle_end(),
                    order_code,
                    "",
                    null,
                    0L,
                    DeptOrderStatus.WAITING.getId(),
                    order_data_index,
                    dept_code,
                    dept_code
            );
            deptOrderEntity.setCreated_date(DateTimeUtils.getNow());
            int rsCreateDeptOrder = this.deptDB.createDeptOrder(deptOrderEntity);
            if (rsCreateDeptOrder <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            deptOrderEntity.setId(rsCreateDeptOrder);

            /**
             * GHi nhận ngày phát sinh công nợ của đại lý
             */
            this.agencyDB.saveNgayGhiNhanCongNo(
                    agency_id
            );

            JSONObject agencyOrder = this.orderDB.getAgencyOrder(agency_order_id);
            if (ConvertUtils.toInt(agencyOrder.get("total")) == 1) {
                boolean rsSetOrderIncreaseDept = this.orderDB.setOrderIncreaseDept(
                        agency_order_id, IncreaseDeptStatus.YES.getValue());
                if (!rsSetOrderIncreaseDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            /**
             * Nếu tồn tại thanh toán chưa dùng hết
             */
            ClientResponse crCanTru = this.canTruCongNo(
                    agency_id,
                    hasCongNoAm,
                    time
            );
            if (crCanTru.failed()) {
                return crCanTru;
            }

            /**
             * Đại lý có kỳ hạn nợ = 0 thì phạt acoin nếu
             * giá trị đơn hàng lớn hơn công nợ âm hiện tại
             */
            if (total_end_price - total_payment > 0 &&
                    deptAgencyInfoEntity.getDept_cycle() == 0) {
                /**
                 * Nợ quá hạn
                 */
                this.orderDB.setDeptOrderNQH(
                        deptOrderEntity.getId()
                );

                /**
                 * Phạt acoin
                 */
                long acoin_punish = ConvertUtils.toInt((total_end_price - total_payment) /
                        this.dataManager.getConfigManager().getAcoinRateDefault());
                ClientResponse crDecreaseAcoin = this.decreaseACoin(
                        agency_id,
                        acoin_punish,
                        "KHN_0: " + order_code,
                        time
                );
                if (crDecreaseAcoin.failed()) {
                    return crDecreaseAcoin;
                }

                this.deptDB.punishAcoinDeptOrder(deptOrderEntity.getId(),
                        acoin_punish
                );

                this.aCoinDB.decreaseAcoinForAgencyOrder(
                        order_code, acoin_punish);
            }


            /**
             * Tính lại thông tin nợ của đại lý
             */
            this.updateDeptAgencyInfo(
                    agency_id);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse canTruCongNo(
            int agency_id,
            boolean hasCongNoAm,
            Date time
    ) {
        try {
            List<JSONObject> deptTransactionRemainList = this.deptDB.getListDeptTransactionRemain(agency_id);
            if (deptTransactionRemainList.size() > 0) {
                List<JSONObject> rsDeptOrderPaymentNone = this.deptDB.getDeptOrderPaymentNone(agency_id);
                if (rsDeptOrderPaymentNone.size() > 0) {
                    for (JSONObject jsDeptTransaction : deptTransactionRemainList) {
                        DeptTransactionEntity deptTransactionEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(jsDeptTransaction), DeptTransactionEntity.class);
                        if (deptTransactionEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        rsDeptOrderPaymentNone = this.deptDB.getDeptOrderPaymentNone(
                                agency_id);
                        if (rsDeptOrderPaymentNone.size() > 0) {
                            boolean rsCanTru = this.clearingDeptTransaction(
                                    deptTransactionEntity.getId(),
                                    deptTransactionEntity.getAgency_id(),
                                    time,
                                    deptTransactionEntity.getDescription(),
                                    rsDeptOrderPaymentNone,
                                    deptTransactionEntity.getAcoin_effect_type(),
                                    hasCongNoAm,
                                    time);
                            if (!rsCanTru) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                    }
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse canTruCongNoToDeptOrder(
            DeptTransactionEntity deptTransactionEntity,
            JSONObject dept_order,
            Date time
    ) {
        try {
            if (deptTransactionEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            boolean rsCanTru = this.clearingDeptTransaction(
                    deptTransactionEntity.getId(),
                    deptTransactionEntity.getAgency_id(),
                    deptTransactionEntity.getConfirmed_time(),
                    deptTransactionEntity.getDescription(),
                    Arrays.asList(dept_order),
                    deptTransactionEntity.getAcoin_effect_type(),
                    false,
                    time);
            if (!rsCanTru) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected boolean decreaseCNO(int agency_id,
                                  Long transaction_value,
                                  Integer dept_transaction_id,
                                  Integer creator_id,
                                  Long current_dept,
                                  Integer dept_cycle,
                                  Long dept_limit,
                                  Long ngd_limit,
                                  String note,
                                  Date time) {
        boolean rsDecreaseCNO = this.deptDB.decreaseCNO(agency_id, transaction_value);
        if (!rsDecreaseCNO) {
            return false;
        }

        /**
         * lưu lịch sử thay đổi công nợ
         */
        this.insertDeptAgencyHistory(agency_id,
                transaction_value * -1,
                dept_transaction_id,
                TransactionEffectType.CNO,
                note,
                creator_id,
                current_dept,
                dept_cycle,
                dept_limit,
                ngd_limit,
                time);

        return true;
    }

    protected boolean decreaseDTT(
            int agency_id,
            Long transaction_value,
            Integer dept_transaction_id,
            String note,
            Date time,
            boolean isEdit,
            int staff_id,
            String code) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                return false;
            }
            boolean rsDecreaseDTT = this.deptDB.decreaseDTT(agency_id, transaction_value);
            if (!rsDecreaseDTT) {
                return false;
            }

            EditDeptDttRequest request = new EditDeptDttRequest();
            request.setAgency_id(agency_id);
            request.setNote(note);
            request.setData(transaction_value);
            request.setType(
                    ChangeValueType.DECREASE.getId()
            );
            int rsSaveDeptDttHistory = this.deptDB.saveDeptDttHistory(
                    request.getAgency_id(),
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    deptAgencyInfoEntity.getTotal_dtt_cycle(),
                    deptAgencyInfoEntity.getTotal_dtt_cycle() +
                            (request.getData() *
                                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    request.getNote(),
                    staff_id,
                    DateTimeUtils.toString(time),
                    isEdit == true ? "Điều chỉnh" : "-",
                    code
            );
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    protected ClientResponse updateLevelMembership(
            int agency_id,
            int new_membership_id,
            int old_membership,
            String new_agency_code,
            String old_agency_code,
            int modifier_id) {
        try {
            boolean rsUpdateAgencyMembership = this.agencyDB.updateAgencyMembership(
                    agency_id,
                    new_membership_id,
                    new_agency_code,
                    modifier_id);
            if (!rsUpdateAgencyMembership) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.UPDATE_MEMBERSHIP_FAILED);
            }

            /**
             * Lưu lịch sử
             */
            this.agencyDB.saveAgencyMembershipHistory(
                    agency_id,
                    new_membership_id,
                    old_membership,
                    new_agency_code,
                    old_agency_code,
                    modifier_id
            );

            /**
             * Đồng bộ Bravo
             */
            ClientResponse crSyncMembership = this.bravoService.syncAgencyMembership(
                    agency_id,
                    new_membership_id,
                    new_agency_code);
            if (crSyncMembership.failed()) {
                this.agencyDB.syncAgencyInfoFail(
                        agency_id,
                        crSyncMembership.getMessage(),
                        2
                );
            }

            this.agencyDB.insertAgencyWaitingGenerateMission(agency_id);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.AGENCY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected String generateAgencyCode(
            int regionId,
            int cityId,
            int membershipId,
            int agencyId) {
        /**
         * Tạo mã KH
         * Mã tỉnh + STT theo tỉnh + mã KVKD + 2 số cuối của năm + '_' + cấp bậc KH
         */
        String cityCode = dataManager.getProductManager().getCityCodeById(cityId);
        String regionCode = dataManager.getProductManager().getRegionCodeById(regionId);
        String membershipCode = dataManager.getProductManager().getMpMembershipCodeById(membershipId);
        int cityIndex = this.generateIndexAgencyInCity(cityId, 0);
        if (cityIndex == -1) {
            /**
             * Tạo lại mã code
             */
            cityIndex = this.generateIndexAgencyInCity(cityId, 0);
        }
        if (cityIndex == -1) {
            return "";
        }
        String indexCodeCity = appUtils.convertCityIndexToCode(cityIndex);
        String year = appUtils.getLastTwoNumberOfYear();
        String agencyCode = cityCode + indexCodeCity + regionCode + year + "_" + membershipCode;
        return agencyCode;
    }

    protected String generateAgencyCodeV2(
            int agencyId, int business_department_id) {
        BusinessDepartment businessDepartment = this.dataManager.getProductManager().getMpBusinessDepartment().get(business_department_id);
        if (businessDepartment == null) {
            return "";
        }
        int count = this.agencyDB.countAgencyApproved(business_department_id);
        String agencyCode = businessDepartment.getCode() +
                String.format("%03d", count + 1);
        return agencyCode;
    }

    protected int generateIndexAgencyInCity(int cityId, int agencyId) {
        int currentIndexCity = this.agencyDB.getCurrentIndexAgencyInCity(cityId);
        if (currentIndexCity >= 0) {
            int nextIndexCity = currentIndexCity + 1;
            /**
             * save index
             */
            if (currentIndexCity == 0) {
                int rsSaveCityIndex = this.agencyDB.saveCityIndex(cityId, nextIndexCity, agencyId);
                if (rsSaveCityIndex > 0) {
                    return nextIndexCity;
                }
            } else {
                boolean rsSaveCityIndex = this.agencyDB.updateCityIndex(cityId, nextIndexCity, agencyId);
                if (rsSaveCityIndex) {
                    return nextIndexCity;
                }
            }
        }
        return -1;
    }

    protected int getNumberData(Cell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_STRING)
            return Integer.parseInt(cell.getStringCellValue());
        return (int) cell.getNumericCellValue();
    }

    protected long getLongData(Cell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_STRING)
            return Long.parseLong(cell.getStringCellValue());
        return (long) cell.getNumericCellValue();
    }

    protected String getStringData(Cell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_STRING)
            return cell.getStringCellValue().trim();
        else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    protected String getDateData(Cell cell) {
        Date date = cell.getDateCellValue();
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return simpleDateFormat.format(date);
        }
        return "";
    }

    public ClientResponse importDepSetting() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_setting.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 1) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {


                        DeptSettingEntity deptSettingEntity = new DeptSettingEntity();

                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        long dept_limit = getLongData(cell);
                        cell = ltCell.get(4);
                        long ngd_limit = getLongData(cell);
                        cell = ltCell.get(5);
                        int dept_cycle = getNumberData(cell);


                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        deptSettingEntity.setAgency_id(agencyEntity.getId());
                        deptSettingEntity.setStart_date(DateTimeUtils.getNow());
                        deptSettingEntity.setConfirmed_date(DateTimeUtils.getNow());
                        deptSettingEntity.setStatus(DeptSettingStatus.CONFIRMED.getId());
                        deptSettingEntity.setAgency_include("[\"" + agencyEntity.getId() + "\"]");
                        deptSettingEntity.setDept_limit(dept_limit);
                        deptSettingEntity.setNgd_limit(ngd_limit);
                        deptSettingEntity.setDept_cycle(dept_cycle);
                        int rsInsert = this.deptDB.insertDeptSetting(deptSettingEntity);
                        if (rsInsert <= 0) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        DeptAgencyInfoEntity deptAgencyInfoEntity = new DeptAgencyInfoEntity();

                        deptAgencyInfoEntity.setAgency_id(agencyEntity.getId());
                        deptAgencyInfoEntity.setDept_cycle(dept_cycle);
                        deptAgencyInfoEntity.setNgd_limit(ngd_limit);
                        deptAgencyInfoEntity.setDept_limit(dept_limit);
                        deptAgencyInfoEntity.setCreated_date(DateTimeUtils.getNow());
                        deptAgencyInfoEntity.setStatus(1);
                        deptAgencyInfoEntity.setCommit_limit(this.dataManager.getConfigManager().getCommitLimit());

                        this.deptDB.insertDeptAgencyInfo(deptAgencyInfoEntity);
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importDeptTransactionThanhToan() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(3);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 4) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {
                        DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                                this.dataManager.getConfigManager().getHanMuc(
                                        2
                                );
                        DeptTransactionEntity deptTransactionEntity =
                                new DeptTransactionEntity();

                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        long transaction_value = getLongData(cell);
                        cell = ltCell.get(4);
                        Date date = DateTimeUtils.getDateTime(getDateData(cell), "dd/MM/yyyy");


                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            //return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        deptTransactionEntity.setAgency_id(agencyEntity.getId());
                        deptTransactionEntity.setCreated_date(date);
                        deptTransactionEntity.setDept_transaction_sub_type_id(deptTransactionSubTypeEntity.getId());
                        deptTransactionEntity.setDept_transaction_main_type_id(
                                deptTransactionSubTypeEntity.getDept_transaction_main_type_id());
                        deptTransactionEntity.setDept_type_id(
                                deptTransactionSubTypeEntity.getDept_type_id());
                        deptTransactionEntity.setDept_time(date);
                        deptTransactionEntity.setTransaction_value(transaction_value);
                        deptTransactionEntity.setTransaction_used_value(transaction_value);
                        deptTransactionEntity.setConfirmed_time(date);
                        deptTransactionEntity.setDept_function_type(deptTransactionSubTypeEntity.getFunction_type());
                        deptTransactionEntity.setStatus(DeptTransactionStatus.CONFIRMED.getId());
                        deptTransactionEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                        deptTransactionEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                        deptTransactionEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                        deptTransactionEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());
                        this.deptDB.createDeptTransaction(deptTransactionEntity);
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importDeptOrder() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {


                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        Date dept_time = DateTimeUtils.getDateTime(getDateData(cell), "dd/MM/yyyy");
                        cell = ltCell.get(4);
                        String order_code = getStringData(cell);
                        cell = ltCell.get(6);
                        int dept_cycle = getNumberData(cell);
                        cell = ltCell.get(7);
                        long transaction_value = getLongData(cell);
                        cell = ltCell.get(8);
                        long payment_value = getLongData(cell);
                        cell = ltCell.get(9);
                        Date payment_date = DateTimeUtils.getDateTime(getDateData(cell), "dd/MM/yyyy");

                        cell = ltCell.get(12);
                        int han_muc_id = getNumberData(cell);
                        cell = ltCell.get(13);
                        String note = getStringData(cell);

                        DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                                this.dataManager.getConfigManager().getHanMuc(
                                        han_muc_id
                                );
                        if (deptTransactionSubTypeEntity == null) {
                            continue;
                        }

                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            //return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        if (deptTransactionSubTypeEntity.getDept_type_id() != DeptType.DEPT_DON_HANG.getId()) {
                            DeptTransactionEntity deptTransactionEntity =
                                    new DeptTransactionEntity();
                            deptTransactionEntity.setAgency_id(agencyEntity.getId());
                            deptTransactionEntity.setCreated_date(dept_time);
                            deptTransactionEntity.setDept_transaction_sub_type_id(deptTransactionSubTypeEntity.getId());
                            deptTransactionEntity.setDept_transaction_main_type_id(
                                    deptTransactionSubTypeEntity.getDept_transaction_main_type_id());
                            deptTransactionEntity.setDept_type_id(
                                    deptTransactionSubTypeEntity.getDept_type_id());
                            deptTransactionEntity.setDept_time(dept_time);
                            deptTransactionEntity.setTransaction_value(transaction_value);
                            deptTransactionEntity.setTransaction_used_value(transaction_value);
                            deptTransactionEntity.setConfirmed_time(dept_time);
                            deptTransactionEntity.setDept_function_type(deptTransactionSubTypeEntity.getFunction_type());
                            deptTransactionEntity.setStatus(DeptTransactionStatus.CONFIRMED.getId());
                            deptTransactionEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                            deptTransactionEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                            deptTransactionEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                            deptTransactionEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());
                            this.deptDB.createDeptTransaction(deptTransactionEntity);
                        }

                        DeptOrderEntity deptOrderEntity = new DeptOrderEntity();
                        deptOrderEntity.setAgency_id(agencyEntity.getId());

                        deptOrderEntity.setDept_transaction_sub_type_id(
                                deptTransactionSubTypeEntity.getId());

                        deptOrderEntity.setDept_transaction_main_type_id(
                                deptTransactionSubTypeEntity.getDept_transaction_main_type_id()
                        );

                        deptOrderEntity.setDept_type_id(
                                deptTransactionSubTypeEntity.getDept_type_id()
                        );

                        deptOrderEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                        deptOrderEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                        deptOrderEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                        deptOrderEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());

                        deptOrderEntity.setTransaction_value(
                                transaction_value
                        );

                        deptOrderEntity.setDept_cycle(dept_cycle);
                        deptOrderEntity.setDept_cycle_end(0L);

                        deptOrderEntity.setDept_type_data(order_code);

                        deptOrderEntity.setNote(note);

                        deptOrderEntity.setStatus(
                                transaction_value == payment_value ? 2 :
                                        deptOrderEntity.getDept_type_id() == DeptType.DEPT_DECREASE.getId()
                                                ? 2 : 1
                        );

                        deptOrderEntity.setCreated_date(dept_time);

                        /**
                         * Ngày phát sinh công nợ
                         */
                        deptOrderEntity.setDept_time(dept_time);

                        deptOrderEntity.setPayment_value(payment_value);

                        deptOrderEntity.setPayment_date(payment_date);
                        this.deptDB.createDeptOrder(deptOrderEntity);
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importDeptAgencyInfo() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {


                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        long dept_cycle_start = getLongData(cell);
                        cell = ltCell.get(6);
                        long dept_cycle_end = getLongData(cell);
                        cell = ltCell.get(12);
                        long dtt = getLongData(cell);
                        cell = ltCell.get(13);
                        long tt = getLongData(cell);

                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(
                                agencyEntity.getId()
                        );

                        deptAgencyInfoEntity.setAgency_id(agencyEntity.getId());
                        deptAgencyInfoEntity.setDtt(dtt);
                        deptAgencyInfoEntity.setTotal_dtt_cycle(dtt);
                        deptAgencyInfoEntity.setTt(tt);
                        deptAgencyInfoEntity.setTotal_tt_cycle(tt);
                        deptAgencyInfoEntity.setDept_cycle_start(dept_cycle_start);
                        deptAgencyInfoEntity.setDept_cycle_end(dept_cycle_end);
                        deptAgencyInfoEntity.setCurrent_dept(dept_cycle_end);
                        boolean rsUpdate = this.deptDB.updateDeptAgencyInfo(deptAgencyInfoEntity);
                        if (!rsUpdate) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        this.updateDeptAgencyInfo(agencyEntity.getId());
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importOrder() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {


                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        Date create_date = DateTimeUtils.getDateTime(
                                getDateData(cell), "dd/MM/yyyy"
                        );
                        cell = ltCell.get(4);
                        String order_code = getStringData(cell);
                        cell = ltCell.get(6);
                        long dept_cycle_end = getNumberData(cell);
                        cell = ltCell.get(7);
                        long total_end_price = getNumberData(cell);

                        cell = ltCell.get(12);
                        int han_muc_id = getNumberData(cell);
                        cell = ltCell.get(13);
                        String note = getStringData(cell);

                        DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                                this.dataManager.getConfigManager().getHanMuc(
                                        han_muc_id
                                );
                        if (deptTransactionSubTypeEntity == null) {
                            continue;
                        }

                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        if (deptTransactionSubTypeEntity.getDept_type_id() == DeptType.DEPT_DON_HANG.getId()
                                && total_end_price > 0) {


                            AgencyOrderEntity agencyOrderEntity =
                                    new AgencyOrderEntity();
                            agencyOrderEntity.setCreated_date(create_date);
                            agencyOrderEntity.setConfirm_delivery_date(create_date);
                            agencyOrderEntity.setCode(order_code);
                            agencyOrderEntity.setAgency_id(agencyEntity.getId());
                            agencyOrderEntity.setTotal_begin_price(total_end_price);
                            agencyOrderEntity.setTotal_end_price(total_end_price);
                            agencyOrderEntity.setUpdate_status_date(create_date);
                            agencyOrderEntity.setMembership_id(agencyOrderEntity.getMembership_id());
                            agencyOrderEntity.setSource(SourceOrderType.CMS.getValue());
                            agencyOrderEntity.setStatus(OrderStatus.COMPLETE.getKey());

                            int rsInsertAgencyOrder = this.orderDB.insertAgencyOrder(agencyOrderEntity);
                            if (rsInsertAgencyOrder <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected DeptTransactionEntity createDeptTransactionEntity(
            Integer dept_transaction_sub_type_id,
            Integer dept_transaction_main_type_id,
            Integer dept_type_id,
            String cn_effect_type,
            String dtt_effect_type,
            String tt_effect_type,
            String acoin_effect_type,
            Long transaction_value,
            Integer agency_id,
            Long dept_cycle_end,
            String dept_type_data,
            String note,
            Integer status,
            Date created_date,
            Integer modifier_id,
            String dept_function_type,
            Integer creator_id,
            Date modified_date,
            Date dept_time,
            Date confirmed_time,
            String description,
            Long acoin,
            Long transaction_used_value
    ) {
        DeptTransactionEntity deptTransactionEntity = new DeptTransactionEntity();
        /**
         * dept_transaction_sub_type_id;
         */
        deptTransactionEntity.setDept_transaction_sub_type_id(dept_transaction_sub_type_id);

        /**
         * dept_transaction_main_type_id;
         */
        deptTransactionEntity.setDept_transaction_main_type_id(dept_transaction_main_type_id);

        /**
         * dept_type_id
         */
        deptTransactionEntity.setDept_type_id(dept_type_id);

        /**
         * cn_effect_type
         */
        deptTransactionEntity.setCn_effect_type(cn_effect_type);

        /**
         * dtt_effect_type
         */
        deptTransactionEntity.setDtt_effect_type(dtt_effect_type);

        /**
         * tt_effect_type
         */
        deptTransactionEntity.setTt_effect_type(tt_effect_type);

        /**
         * acoin_effect_type
         */
        deptTransactionEntity.setAcoin_effect_type(acoin_effect_type);

        /**
         * transaction_value
         */
        deptTransactionEntity.setTransaction_value(transaction_value);

        /**
         * agency_id
         */
        deptTransactionEntity.setAgency_id(agency_id);


        /**
         * dept_cycle_end
         */
        deptTransactionEntity.setDept_cycle_end(dept_cycle_end);

        /**
         * dept_type_data
         */
        deptTransactionEntity.setDept_type_data(dept_type_data);

        /**
         * note
         */
        deptTransactionEntity.setNote(note);

        /**
         * status
         */
        deptTransactionEntity.setStatus(status);

        /**
         * created_date
         */
        deptTransactionEntity.setCreated_date(created_date);

        /**
         * dept_function_type
         */
        deptTransactionEntity.setDept_function_type(dept_function_type);

        /**
         * creator_id
         */
        deptTransactionEntity.setCreator_id(creator_id);

        /**
         *dept_time
         */
        deptTransactionEntity.setDept_time(dept_time);

        /**
         * confirmed_time
         */
        deptTransactionEntity.setConfirmed_time(confirmed_time);

        /**
         * description
         */
        deptTransactionEntity.setDescription(description);

        /**
         * private Long acoin = 0L;
         */
        deptTransactionEntity.setAcoin(acoin);

        /**
         * transaction_used_value
         */
        deptTransactionEntity.setTransaction_used_value(transaction_used_value);

        return deptTransactionEntity;
    }

    protected List<BasicResponse> convertSettingData(String setting_type, String setting_value) {
        List<BasicResponse> responses = new ArrayList<>();
        switch (SettingType.from(setting_type)) {
            case CSBH_CTKM: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    PromoEntity promo = this.promoDB.getPromoInfo(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(promo.getId(), promo.getCode(), promo.getName()));
                }
                return responses;
            }
            case LOAI_SAN_PHAM: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    ItemType itemType = ItemType.from(ConvertUtils.toInt(data));
                    responses.add(new BasicResponse(
                            itemType.getKey(), itemType.getValue(), itemType.getValue()));
                }
                return responses;
            }
            case THUONG_HIEU: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    BrandEntity brand = this.dataManager.getProductManager().getMpBrand().get(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            brand.getId(), brand.getName(), brand.getName()
                    ));
                }
                return responses;
            }
            case MAT_HANG: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    Category category = this.dataManager.getProductManager().getCategoryById(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            category.getId(), category.getName(), category.getName()
                    ));
                }
                return responses;
            }
            case PHAN_LOAI_THEO_SP: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    Category category = this.dataManager.getProductManager().getCategoryById(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            category.getId(), category.getName(), category.getName()
                    ));
                }
                return responses;
            }
            case PHAN_LOAI_THEO_THUONG_HIEU: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    Category category = this.dataManager.getProductManager().getCategoryById(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            category.getId(), category.getName(), category.getName()
                    ));
                }
                return responses;
            }
            case NHOM_HANG: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            productGroup.getId(), productGroup.getName(), productGroup.getName()
                    ));
                }
                return responses;
            }
            case DANH_SACH_SAN_PHAM: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    ProductCache productData = this.dataManager.getProductManager().getProductBasicData(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            productData.getId(), productData.getCode(), productData.getFull_name()
                    ));
                }
                return responses;
            }
            case SAN_PHAM: {
                List<String> dataList = this.appUtils.convertStringToArray(
                        ConvertUtils.toString(setting_value)
                );
                for (String data : dataList) {
                    ProductCache productData = this.dataManager.getProductManager().getProductBasicData(
                            ConvertUtils.toInt(data)
                    );
                    responses.add(new BasicResponse(
                            productData.getId(), productData.getCode(), productData.getFull_name()
                    ));
                }
                return responses;
            }
            default:
                return responses;
        }
    }

    public void callPushNotifyToAgency(
            String fb,
            String title,
            String body,
            String image) {
        try {
            this.dataManager.getProductManager().getReloadService().pushNotify(
                    fb,
                    "LIST",
                    title,
                    body,
                    image);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public void callPushPopupToAgency(
            String fb,
            String title,
            String body,
            String image,
            String data,
            long time) {
        try {
            ClientResponse clientResponse = this.dataManager.getProductManager().getReloadService().pushPopup(
                    fb,
                    "LIST",
                    title,
                    body,
                    image,
                    data,
                    time);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    protected List<String> getListFirebaseAgency(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids) {
        List<JSONObject> accountList = this.agencyDB.getListFirebaseAgencyAccount(
                agency_ids,
                city_ids,
                region_ids,
                membership_ids
        );

        List<String> fbList = accountList.stream().map(
                e -> e.get("firebase_token").toString()
        ).collect(Collectors.toList());

        return fbList;
    }

    protected ClientResponse pushNotifyToAgency(
            int staff_id,
            NotifyAutoContentType notifyAutoContentType,
            String image,
            String setting_type,
            String setting_value,
            String setting_data,
            int agency_id) {
        try {
            /**
             * Luu lich su thong bao
             */

            NotifyData notifyData = this.dataManager.getConfigManager()
                    .getNotifyDataByContentType(
                            notifyAutoContentType,
                            setting_data);
            if (notifyData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String agencyIds = JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agency_id)));
            boolean rsSaveHistory = this.saveNotifyHistory(
                    notifyData.getTitle(),
                    "",
                    notifyData.getDescription(),
                    1,
                    DateTimeUtils.getNow(),
                    staff_id,
                    agencyIds,
                    "[]",
                    "[]",
                    "[]",
                    setting_type,
                    setting_value
            );
            if (!rsSaveHistory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<String> fbAgencyList = this.getListFirebaseAgency(
                    agencyIds,
                    "[]",
                    "[]",
                    "[]"
            );
            if (!fbAgencyList.isEmpty()) {
                ClientResponse crSaveNotifyWaitingPush = this.saveNotifyWaitingPush(
                        fbAgencyList,
                        notifyData.getTitle(),
                        notifyData.getDescription(),
                        image, 1);
            }
        } catch (Exception ex) {

        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse pushNotifyToAgencyByQuangBa(
            int staff_id,
            NotifyAutoContentType notifyAutoContentType,
            String image,
            String image_url,
            String setting_type,
            String setting_value,
            String setting_data,
            String title,
            String description,
            int agency_id) {
        try {
            /**
             * Luu lich su thong bao
             */

            NotifyData notifyData = this.dataManager.getConfigManager()
                    .getNotifyDataByContentType(
                            notifyAutoContentType,
                            setting_data);
            if (notifyData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String agencyIds = JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agency_id)));
            boolean rsSaveHistory = this.saveNotifyHistory(
                    title,
                    image,
                    description,
                    1,
                    DateTimeUtils.getNow(),
                    staff_id,
                    agencyIds,
                    "[]",
                    "[]",
                    "[]",
                    setting_type,
                    setting_value
            );
            if (!rsSaveHistory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<String> fbAgencyList = this.getListFirebaseAgency(
                    agencyIds,
                    "[]",
                    "[]",
                    "[]"
            );
            if (!fbAgencyList.isEmpty()) {
                ClientResponse crSaveNotifyWaitingPush = this.saveNotifyWaitingPush(
                        fbAgencyList,
                        title,
                        description,
                        image_url, 1);
            }
        } catch (Exception ex) {

        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse pushPopupToAgency(
            int staff_id,
            NotifyAutoContentType notifyAutoContentType,
            String image,
            String image_url,
            String setting_type,
            String setting_value,
            String setting_data,
            String title,
            String description,
            int agency_id,
            String data) {
        try {
            /**
             * Luu lich su thong bao
             */

            NotifyData notifyData = this.dataManager.getConfigManager()
                    .getNotifyDataByContentType(
                            notifyAutoContentType,
                            setting_data);
            if (notifyData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String agencyIds = JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agency_id)));
            boolean rsSaveHistory = this.saveNotifyHistory(
                    title,
                    image,
                    description,
                    1,
                    DateTimeUtils.getNow(),
                    staff_id,
                    agencyIds,
                    "[]",
                    "[]",
                    "[]",
                    setting_type,
                    setting_value
            );
            if (!rsSaveHistory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<String> fbAgencyList = this.getListFirebaseAgency(
                    agencyIds,
                    "[]",
                    "[]",
                    "[]"
            );
            if (!fbAgencyList.isEmpty()) {
                ClientResponse crSaveNotifyWaitingPush = this.savePopupWaitingPush(
                        fbAgencyList,
                        title,
                        description,
                        image, 1,
                        data);
            }
        } catch (Exception ex) {

        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected boolean saveNotifyHistory(
            String name,
            String image,
            String description,
            Integer status,
            Date created_date,
            Integer creator_id,
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            String setting_type,
            String setting_value) {
        try {
            List<String> agencyList = JsonUtils.DeSerialize(
                    agency_ids,
                    new TypeToken<List<String>>() {
                    }.getType()
            );
            for (String agency : agencyList) {
                NotifyHistoryEntity notifyHistoryEntity = new NotifyHistoryEntity();

            /*
            private String name;
             */
                notifyHistoryEntity.setName(name);
            /*
            private String image;
             */
                notifyHistoryEntity.setImage(image);
            /*
             description;
             */
                notifyHistoryEntity.setDescription(description);
            /*
            private int status = 1;
             */
                notifyHistoryEntity.setStatus(1);
            /*
            private Date created_date;
             */
                notifyHistoryEntity.setCreated_date(created_date);
            /*
            private Integer creator_id;
             */
                notifyHistoryEntity.setCreator_id(creator_id);

            /*
            private String agency_ids = "[]";
             */
                notifyHistoryEntity.setAgency_ids("[\"" + agency + "\"]");

            /*
            private String city_ids = "[]";
            */
                notifyHistoryEntity.setCity_ids(city_ids);
            /*
            private String region_ids = "[]";
             */
                notifyHistoryEntity.setRegion_ids(region_ids);
            /*
            private String membership_ids = "[]";
             */
                notifyHistoryEntity.setMembership_ids(membership_ids);
            /*
            private String setting_type;
             */
                notifyHistoryEntity.setSetting_type(setting_type);
            /*
            private String setting_value;
             */
                notifyHistoryEntity.setSetting_value(setting_value);
                int rsInsert = this.notifyDB.insertNotifyHistory(notifyHistoryEntity);
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name(), ex);
        }
        return false;
    }

    protected ClientResponse saveNotifyWaitingPush(
            List<String> firebase,
            String title,
            String description, String image, int a) {
        try {
            int total = firebase.size();
            Pageable pageable = PageRequest.of(0, 1);
            Page<String> pages = new PageImpl<String>(firebase, pageable, total);
            int totalPage = this.appUtils.getTotalPageBySize(total, ConfigInfo.PAGE_SIZE);
            for (int i = 0; i < totalPage; i++) {
                pageable = PageRequest.of(i, ConfigInfo.PAGE_SIZE);
                Page<String> page2s = new PageImpl<String>(firebase, pageable, total);
                final int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(),
                        firebase.size());
                final int fromIndex = Math.max(toIndex - pageable.getPageSize(), 0);
                List<String> fb = firebase.subList(fromIndex, toIndex);

                int rs = this.notifyDB.insertNotifyWaitingPush(
                        JsonUtils.Serialize(fb),
                        title,
                        description,
                        image.isEmpty() ? "" :
                                ConfigInfo.IMAGE_PUSH_NOTIFY_URL + image,
                        NotifyWaitingPushStatus.WAITING.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse savePopupWaitingPush(
            List<String> firebase,
            String title,
            String description, String image, int a, String data) {
        try {
            int total = firebase.size();
            Pageable pageable = PageRequest.of(0, 1);
            Page<String> pages = new PageImpl<String>(firebase, pageable, total);
            int totalPage = this.appUtils.getTotalPageBySize(total, ConfigInfo.PAGE_SIZE);
            for (int i = 0; i < totalPage; i++) {
                pageable = PageRequest.of(i, ConfigInfo.PAGE_SIZE);
                Page<String> page2s = new PageImpl<String>(firebase, pageable, total);
                final int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(),
                        firebase.size());
                final int fromIndex = Math.max(toIndex - pageable.getPageSize(), 0);
                List<String> fb = firebase.subList(fromIndex, toIndex);

                int rs = this.notifyDB.insertPopupWaitingPush(
                        JsonUtils.Serialize(fb),
                        title,
                        description,
                        image.isEmpty() ? "" :
                                ConfigInfo.IMAGE_PUSH_NOTIFY_URL + "/notify/" + image,
                        NotifyWaitingPushStatus.WAITING.getId(),
                        data);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected void addFilterAgencyData(SessionData sessionData, FilterListRequest request) {
        FilterRequest filterRequest = this.dataManager.getStaffManager().getFilterAgency(
                this.dataManager.getStaffManager().getStaffManageData(
                        sessionData.getId()
                ));
        if (filterRequest != null) {
            request.getFilters().add(filterRequest);
        }
    }

    protected void addFilterAgencyDataForListAgency(SessionData sessionData, FilterListRequest request) {
        FilterRequest filterRequest = this.dataManager.getStaffManager().getFilterAgencyForListAgency(
                this.dataManager.getStaffManager().getStaffManageData(
                        sessionData.getId()
                ));
        if (filterRequest != null) {
            request.getFilters().add(filterRequest);
        }
    }

    protected JSONObject getDeptInfo(int agency_id) {
        try {
            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(agency_id);
            if (agencyEntity == null) {
                return null;
            }
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(
                    agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(
                        deptAgencyInfoEntity, agency_id);
                if (deptAgencyInfoEntity == null) {
                    return null;
                }
            }

            JSONObject deptInfo = JsonUtils.DeSerialize(JsonUtils.Serialize(deptAgencyInfoEntity), JSONObject.class);
            if (deptInfo == null) {
                return null;
            }

            /**
             * gia tri don hang dang thuc hien
             */
            long totalValueDoingDept = this.orderDB.getTotalPriceOrderDoing(
                    agency_id);
            deptInfo.put("total_value_order_doing", totalValueDoingDept);

            long hmkd = ConvertUtils.toLong(
                    this.getAgencyHMKD(
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                            totalValueDoingDept)
            );
            deptInfo.put("hmkd", hmkd);
            deptInfo.put("acoin", agencyEntity.getCurrent_point());

            long cno = ConvertUtils.toLong(deptInfo.get("current_dept"));
            if (cno < 0) {
                deptInfo.put("current_dept", cno * -1);
            }
            deptInfo.put("cno", cno);
            deptInfo.put("nqh_current", deptInfo.get("nqh"));
            deptInfo.put("hmgd", deptInfo.get("ngd_limit"));
            return deptInfo;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return null;
    }

    public ClientResponse importDept() {
        try {

        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importDeptStartCycle() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    List<Cell> ltCell = new ArrayList<>();
                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        // If the cell is missing from the file, generate a blank one
                        // (Works by specifying a MissingCellPolicy)
                        Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
                        ltCell.add(cell);
                    }
//                    Iterator<Cell> cellIterator = row.cellIterator();
//                    List<Cell> ltCell = new ArrayList<>();
//                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {
                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        long dept_cycle_start = getLongData(cell);
                        cell = ltCell.get(6);

                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(
                                agencyEntity.getId()
                        );

                        deptAgencyInfoEntity.setAgency_id(agencyEntity.getId());
                        deptAgencyInfoEntity.setDept_cycle_start(dept_cycle_start);
                        deptAgencyInfoEntity.setDept_cycle_end(dept_cycle_start);
                        deptAgencyInfoEntity.setCurrent_dept(dept_cycle_start);
                        boolean rsUpdate = this.deptDB.updateDeptAgencyInfo(deptAgencyInfoEntity);
                        if (!rsUpdate) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        this.updateDeptAgencyInfo(agencyEntity.getId());
                    }
                }
                index++;
            }
            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importDeptTransaction(MultipartFile request) {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/dept_order.xlsx";
//            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(request.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    List<Cell> ltCell = new ArrayList<>();
                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        // If the cell is missing from the file, generate a blank one
                        // (Works by specifying a MissingCellPolicy)
                        Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
                        ltCell.add(cell);
                    }
                    if (!ltCell.isEmpty()) {
                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(1);
                        cell = ltCell.get(2);
                        cell = ltCell.get(3);
                        Date dept_time = DateTimeUtils.getDateTime(getDateData(cell), "dd/MM/yyyy");
                        cell = ltCell.get(4);
                        String order_code = getStringData(cell);
                        cell = ltCell.get(5);
                        String dept_type = getStringData(cell);
                        cell = ltCell.get(6);
                        int dept_cycle = getNumberData(cell);
                        cell = ltCell.get(7);
                        long transaction_value = getLongData(cell);
                        cell = ltCell.get(8);
                        cell = ltCell.get(9);
                        cell = ltCell.get(10);
                        cell = ltCell.get(11);
                        int han_muc_id = getNumberData(cell);
                        cell = ltCell.get(12);
                        String han_muc_name = getStringData(cell);
                        cell = ltCell.get(13);
                        String note = getStringData(cell);
                        cell = ltCell.get(14);
                        int dtt_effect_id = getNumberData(cell);
                        cell = ltCell.get(15);
                        int tt_effect_id = getNumberData(cell);
                        int acoin_effect_id = TransactionEffectValueType.NONE.getId();
                        LogUtil.printDebug(phone + note + transaction_value + han_muc_name);
                        DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                                this.dataManager.getConfigManager().getHanMuc(
                                        han_muc_id
                                );
                        if (deptTransactionSubTypeEntity == null) {
                            continue;
                        }
//                        if (!phone.equals("0842424394")) {
//                            continue;
//                        }

                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        if (deptTransactionSubTypeEntity.getDept_type_id() != DeptType.DEPT_DON_HANG.getId()) {
                            DeptTransactionEntity deptTransactionEntity =
                                    new DeptTransactionEntity();
                            deptTransactionEntity.setAgency_id(agencyEntity.getId());
                            deptTransactionEntity.setCreated_date(dept_time);
                            deptTransactionEntity.setDept_transaction_sub_type_id(deptTransactionSubTypeEntity.getId());
                            deptTransactionEntity.setDept_time(dept_time);
                            deptTransactionEntity.setTransaction_value(transaction_value);
                            deptTransactionEntity.setTransaction_used_value(0L);
                            deptTransactionEntity.setConfirmed_time(dept_time);
                            deptTransactionEntity.setDept_function_type(deptTransactionSubTypeEntity.getFunction_type());
                            deptTransactionEntity.setStatus(DeptTransactionStatus.WAITING.getId());
                            deptTransactionEntity.setDescription(note);
                            if (han_muc_id != 12) {
                                deptTransactionEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                                deptTransactionEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                                deptTransactionEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                                deptTransactionEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());
                                deptTransactionEntity.setDept_transaction_main_type_id(
                                        deptTransactionSubTypeEntity.getDept_transaction_main_type_id());
                                deptTransactionEntity.setDept_type_id(
                                        deptTransactionSubTypeEntity.getDept_type_id());
                            } else {
                                DeptType deptType =
                                        DeptType.fromLabel(dept_type);
                                deptTransactionEntity.setDept_transaction_main_type_id(
                                        deptType.getId() == DeptType.DEPT_INCREASE.getId() ?
                                                DeptTransactionMainType.INCREASE.getId() :
                                                DeptTransactionMainType.DECREASE.getId());
                                deptTransactionEntity.setDept_type_id(
                                        deptType.getId());
                                deptTransactionEntity.setCn_effect_type(
                                        deptType.getId() == DeptType.DEPT_INCREASE.getId() ?
                                                DeptTransactionMainType.INCREASE.getCode() :
                                                DeptTransactionMainType.DECREASE.getCode()
                                );
                                deptTransactionEntity.setDtt_effect_type(
                                        TransactionEffectValueType.from(dtt_effect_id).getCode());
                                deptTransactionEntity.setTt_effect_type(
                                        TransactionEffectValueType.from(tt_effect_id).getCode());
                                deptTransactionEntity.setAcoin_effect_type(
                                        TransactionEffectValueType.from(acoin_effect_id).getCode());
                            }
                            int rsDeptTransaction = this.deptDB.createDeptTransaction(deptTransactionEntity);
                            if (rsDeptTransaction <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                            deptTransactionEntity.setId(rsDeptTransaction);

                            ClientResponse crApprove = this.approveDeptTransactionOne(
                                    deptTransactionEntity.getId(),
                                    1,
                                    dept_time,
                                    dept_cycle
                            );
                            if (crApprove.failed()) {
                                return crApprove;
                            }
                        } else {
                            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(
                                    agencyEntity.getId());
                            if (deptAgencyInfoEntity == null) {
                                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(
                                        deptAgencyInfoEntity,
                                        agencyEntity.getId());
                                if (deptAgencyInfoEntity == null) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                                }
                            }

                            AgencyOrderEntity agencyOrderEntity =
                                    new AgencyOrderEntity();
                            agencyOrderEntity.setCreated_date(dept_time);
                            agencyOrderEntity.setConfirm_delivery_date(dept_time);
                            agencyOrderEntity.setCode(order_code);
                            agencyOrderEntity.setAgency_id(agencyEntity.getId());
                            agencyOrderEntity.setTotal_begin_price(transaction_value);
                            agencyOrderEntity.setTotal_end_price(transaction_value);
                            agencyOrderEntity.setUpdate_status_date(dept_time);
                            agencyOrderEntity.setMembership_id(agencyOrderEntity.getMembership_id());
                            agencyOrderEntity.setSource(SourceOrderType.CMS.getValue());
                            agencyOrderEntity.setStatus(OrderStatus.COMPLETE.getKey());

                            int rsInsertAgencyOrder = this.orderDB.insertAgencyOrder(agencyOrderEntity);
                            if (rsInsertAgencyOrder <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                            agencyOrderEntity.setId(rsInsertAgencyOrder);

                            this.increaseCNO(
                                    agencyEntity.getId(),
                                    transaction_value,
                                    null,
                                    0,
                                    deptAgencyInfoEntity.getCurrent_dept(),
                                    deptAgencyInfoEntity.getDept_cycle(),
                                    deptAgencyInfoEntity.getDept_limit(),
                                    deptAgencyInfoEntity.getNgd_limit(),
                                    deptTransactionSubTypeEntity.getName(),
                                    dept_time
                            );

                            this.increaseDTT(
                                    agencyEntity.getId(),
                                    transaction_value,
                                    null,
                                    note,
                                    dept_time,
                                    false,
                                    0,
                                    order_code
                            );

                            DeptOrderEntity deptOrderEntity = this.createDeptOrderEntity(
                                    agencyEntity.getId(),
                                    DeptType.DEPT_DON_HANG.getId(),
                                    DeptTransactionMainType.INCREASE.getId(),
                                    deptTransactionSubTypeEntity.getDept_type_id(),
                                    dept_time,
                                    dept_cycle,
                                    deptTransactionSubTypeEntity.getCn_effect_type(),
                                    deptTransactionSubTypeEntity.getDtt_effect_type(),
                                    deptTransactionSubTypeEntity.getTt_effect_type(),
                                    deptTransactionSubTypeEntity.getAcoin_effect_type(),
                                    transaction_value,
                                    deptAgencyInfoEntity.getDept_cycle_end(),
                                    order_code,
                                    note,
                                    null,
                                    0L,
                                    DeptOrderStatus.WAITING.getId(),
                                    0,
                                    order_code,
                                    order_code
                            );
                            deptOrderEntity.setCreated_date(dept_time);
                            deptOrderEntity.setCreator_id(1);
                            int rsCreateDeptOrder = this.deptDB.createDeptOrder(deptOrderEntity);
                            if (rsCreateDeptOrder <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                            deptOrderEntity.setId(rsCreateDeptOrder);

                            boolean rsSetOrderIncreaseDept = this.orderDB.setOrderIncreaseDept(
                                    agencyOrderEntity.getId(),
                                    IncreaseDeptStatus.YES.getValue());
                            if (!rsSetOrderIncreaseDept) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }

                            /**
                             * Nếu tồn tại thanh toán chưa dùng hết
                             */
                            ClientResponse crCanTru = this.canTruCongNo(
                                    agencyEntity.getId(),
                                    deptAgencyInfoEntity.getCurrent_dept() < 0 ? true : false,
                                    dept_time
                            );
                            if (crCanTru.failed()) {
                                return crCanTru;
                            }
                        }
                    }
                }
                index++;
            }
//            file.close();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse approveDeptTransactionOne(
            int dept_transaction_id,
            int staff_id,
            Date dept_time,
            Integer ky_han_no) {
        try {
            if (dept_transaction_id == 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            DeptTransactionEntity deptTransactionEntity = this.deptDB.getDeptTransaction(dept_transaction_id);
            if (deptTransactionEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (DeptTransactionStatus.WAITING.getId() != deptTransactionEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            AgencyBasicData agency = this.dataManager.getAgencyManager().getAgencyBasicData(deptTransactionEntity.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = this.dataManager.getConfigManager().getHanMuc(deptTransactionEntity.getDept_transaction_sub_type_id());
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(deptTransactionEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                /**
                 * Khởi tạo dữ liệu công nợ đầu ngày
                 */
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, deptTransactionEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            int dept_cycle = 0;
            long dept_cycle_end = 0;
            if (ky_han_no == null) {
                JSONObject jsDeptAgencyInfo = this.deptDB.getDeptInfo(deptTransactionEntity.getAgency_id());
                if (jsDeptAgencyInfo != null) {
                    DeptAgencyInfo deptAgencyInfo = JsonUtils.DeSerialize(JsonUtils.Serialize(jsDeptAgencyInfo), DeptAgencyInfo.class);
                    dept_cycle = deptAgencyInfo.getDept_cycle();
                    dept_cycle_end = deptAgencyInfo.getDept_cycle_end();
                }
            } else {
                dept_cycle = ky_han_no;
                dept_cycle_end = ky_han_no;
            }

            if (dept_time == null) {
                dept_time = DateTimeUtils.getNow();
            }

            /**
             * Cập nhật trạng thái giao dịch
             */
            boolean rsApproveDeptAgency = this.deptDB.approveDeptTransaction(
                    deptTransactionEntity.getId(),
                    DateTimeUtils.toString(dept_time),
                    staff_id
            );
            if (!rsApproveDeptAgency) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.agencyDB.saveNgayGhiNhanCongNo(agency.getId());

            if (this.dataManager.getConfigManager().isHBTL(
                    deptTransactionEntity.getDept_transaction_sub_type_id())) {
                if (!DeptConstants.SOURCE_BRAVO.equals(deptTransactionEntity.getSource())) {
                    int rsHBTL = this.orderDB.saveAgencyHBTL(
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getAgency_id(),
                            deptTransactionEntity.getTransaction_value(),
                            deptTransactionEntity.getTransaction_value()
                    );
                    if (rsHBTL > 0) {
                        this.orderDB.updateCodeAgencyHBTL(
                                rsHBTL,
                                "HBTL" + rsHBTL
                        );
                        /**
                         * Ghi nhận tích lũy của Hàng bán trả lại
                         */
                        this.ghiNhanTichLuyOfHangBanTraLai(
                                rsHBTL
                        );
                    } else {
                        this.alertToTelegram(
                                "Ghi nhận tích lũy của Hàng bán trả lại: " +
                                        deptTransactionEntity.getDoc_no() +
                                        " failed",
                                ResponseStatus.FAIL
                        );
                    }
                }
            } else {
                /**
                 * Ghi nhận tích lũy của transaction cho CTTL DTT
                 */
                this.ghiNhanTichLuyOfTransaction(
                        deptTransactionEntity
                );
            }

            /**
             * Ghi nhận tích lũy CTXH của transaction
             */
            this.ghiNhanCTXHOfTransaction(
                    deptTransactionEntity
            );

            /**
             * Ảnh hưởng công nợ nếu có
             */
            if (!TransactionEffectValueType.NONE.getCode().equals(deptTransactionEntity.getCn_effect_type())) {
                boolean rsExcuteTransactionEffect = this.excuteTransactionEffect(
                        TransactionEffectType.CNO,
                        deptTransactionEntity.getCn_effect_type(),
                        deptTransactionEntity.getTransaction_value(),
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getId(),
                        staff_id,
                        deptAgencyInfoEntity.getCurrent_dept(),
                        deptAgencyInfoEntity.getDept_cycle(),
                        deptAgencyInfoEntity.getDept_limit(),
                        deptAgencyInfoEntity.getNgd_limit(),
                        deptTransactionSubTypeEntity.getName(),
                        deptAgencyInfoEntity,
                        dept_time,
                        TransactionType.CNO.getKey() + "-" + deptTransactionEntity.getId()
                );
                if (!rsExcuteTransactionEffect) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            /**
             * Ảnh hưởng doanh thu thuần nếu có
             */
            if (!TransactionEffectValueType.NONE.getCode().equals(deptTransactionEntity.getDtt_effect_type())) {
                boolean rsExcuteTransactionEffect = this.excuteTransactionEffect(
                        TransactionEffectType.DTT,
                        deptTransactionEntity.getDtt_effect_type(),
                        deptTransactionEntity.getTransaction_value(),
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getId(),
                        staff_id,
                        deptAgencyInfoEntity.getCurrent_dept(),
                        deptAgencyInfoEntity.getDept_cycle(),
                        deptAgencyInfoEntity.getDept_limit(),
                        deptAgencyInfoEntity.getNgd_limit(),
                        deptTransactionSubTypeEntity.getName(),
                        deptAgencyInfoEntity,
                        dept_time,
                        TransactionType.CNO.getKey() + "-" + deptAgencyInfoEntity.getId());
                if (!rsExcuteTransactionEffect) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            /**
             * Ảnh hưởng ảnh hưởng tiền thu nếu có
             */
            if (!TransactionEffectValueType.NONE.getCode().equals(deptTransactionEntity.getTt_effect_type())) {
                boolean rsExcuteTransactionEffect = this.excuteTransactionEffect(
                        TransactionEffectType.TT,
                        deptTransactionEntity.getTt_effect_type(),
                        deptTransactionEntity.getTransaction_value(),
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getId(),
                        staff_id,
                        deptAgencyInfoEntity.getCurrent_dept(),
                        deptAgencyInfoEntity.getDept_cycle(),
                        deptAgencyInfoEntity.getDept_limit(),
                        deptAgencyInfoEntity.getNgd_limit(),
                        deptTransactionSubTypeEntity.getName(),
                        deptAgencyInfoEntity,
                        dept_time,
                        TransactionType.CNO.getKey() + "-" + deptTransactionEntity.getId());
                if (!rsExcuteTransactionEffect) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }


            /**
             * Tăng công nợ
             */
            if (DeptTransactionMainType.INCREASE.getId() == deptTransactionEntity.getDept_transaction_main_type_id()) {
                boolean hasCongNoAm = deptAgencyInfoEntity.getCurrent_dept() < 0;
                /**
                 * Ghi nhận dữ liệu công nợ đơn hàng
                 */
                DeptOrderEntity deptOrderEntity = this.createDeptOrderEntity(
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getDept_type_id(),
                        deptTransactionEntity.getDept_transaction_main_type_id(),
                        deptTransactionEntity.getDept_transaction_sub_type_id(),
                        dept_time,
                        dept_cycle,
                        deptTransactionEntity.getCn_effect_type(),
                        deptTransactionEntity.getDtt_effect_type(),
                        deptTransactionEntity.getTt_effect_type(),
                        deptTransactionEntity.getAcoin_effect_type(),
                        deptTransactionEntity.getTransaction_value(),
                        dept_cycle_end,
                        deptTransactionEntity.getDept_type_data(),
                        deptTransactionEntity.getDescription(),
                        deptTransactionEntity.getId(),
                        0L,
                        DeptOrderStatus.WAITING.getId(),
                        0,
                        deptTransactionEntity.getDoc_no(),
                        "DEPT" + deptTransactionEntity.getId()
                );
                deptOrderEntity.setCreated_date(DateTimeUtils.getNow());
                deptOrderEntity.setCreator_id(staff_id);
                int rsCreateDeptOrder = this.deptDB.createDeptOrder(deptOrderEntity);
                if (rsCreateDeptOrder <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                deptOrderEntity.setId(rsCreateDeptOrder);

                if (deptOrderEntity.getTransaction_value() + deptAgencyInfoEntity.getCurrent_dept() > 0
                        && deptAgencyInfoEntity.getDept_cycle() == 0) {

                    /**
                     * Nợ quá hạn
                     */
                    this.orderDB.setDeptOrderNQH(deptOrderEntity.getId());

                    long nqh = Math.min(
                            deptOrderEntity.getTransaction_value(),
                            deptOrderEntity.getTransaction_value() + deptAgencyInfoEntity.getCurrent_dept());
                    /**
                     * Phạt acoin
                     */
                    long acoin_punish = ConvertUtils.toInt(
                            nqh /
                                    this.dataManager.getConfigManager().getAcoinRateDefault());
                    ClientResponse crDecreaseAcoin = this.decreaseACoin(
                            deptOrderEntity.getAgency_id(),
                            acoin_punish,
                            "KHN_0: " + deptOrderEntity.getNote() + "-" + this.appUtils.priceFormat(
                                    nqh
                            ),
                            dept_time
                    );
                    if (crDecreaseAcoin.failed()) {
                        return crDecreaseAcoin;
                    }

                    this.deptDB.punishAcoinDeptOrder(deptOrderEntity.getId(),
                            acoin_punish
                    );
                }

                /**
                 * Cấn trừ đơn hàng nếu công nợ âm
                 */
                List<JSONObject> deptTransactionRemainList = this.deptDB.getListDeptTransactionRemain(deptTransactionEntity.getAgency_id());
                if (deptTransactionRemainList.size() > 0) {
                    List<JSONObject> rsDeptOrderPaymentNone = this.deptDB.getDeptOrderPaymentNone(deptTransactionEntity.getAgency_id());
                    if (rsDeptOrderPaymentNone.size() > 0) {
                        for (JSONObject jsDeptTransaction : deptTransactionRemainList) {
                            DeptTransactionEntity ctDeptTransaction = JsonUtils.DeSerialize(JsonUtils.Serialize(jsDeptTransaction), DeptTransactionEntity.class);
                            if (ctDeptTransaction == null) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                            boolean rsCanTru = this.clearingDeptTransaction(
                                    ctDeptTransaction.getId(),
                                    ctDeptTransaction.getAgency_id(),
                                    ctDeptTransaction.getConfirmed_time(),
                                    ctDeptTransaction.getDescription(),
                                    rsDeptOrderPaymentNone,
                                    ctDeptTransaction.getAcoin_effect_type(),
                                    hasCongNoAm,
                                    dept_time);
                            if (!rsCanTru) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                    }
                }

                /**
                 * Phạt A-coin  nếu có
                 */
                if (TransactionEffectValueType.DECREASE.getCode().equals(deptTransactionEntity.getAcoin_effect_type())) {
                    long acoin = deptTransactionEntity.getTransaction_value() / this.dataManager.getConfigManager().getAcoinRateDefault();
                    boolean rsSaveAcoinDeptOrder = this.deptDB.punishAcoinDeptOrder(
                            deptOrderEntity.getId(),
                            acoin);
                    if (!rsSaveAcoinDeptOrder) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    ClientResponse rsDecreaseAcoin = this.decreaseACoin(
                            deptTransactionEntity.getAgency_id(), acoin,
                            deptTransactionSubTypeEntity.getName() + ": " +
                                    deptTransactionEntity.getDescription() + "-" +
                                    this.appUtils.priceFormat(deptTransactionEntity.getTransaction_value())
                            ,
                            dept_time
                    );
                    if (rsDecreaseAcoin.failed()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }


            } else if (DeptTransactionMainType.DECREASE.getId() == deptTransactionEntity.getDept_transaction_main_type_id()) {
                /**
                 * Nếu không phải là giao dịch thanh toán thì lưu vào công nợ theo hàng
                 */
                if (!TransactionFunctionType.THANH_TOAN.getCode().equals(deptTransactionSubTypeEntity.getFunction_type())) {
                    /**
                     * Ghi nhận vào dữ liệu công nợ đơn hàng
                     */
                    DeptOrderEntity deptOrderEntity = this.createDeptOrderEntity(
                            deptTransactionEntity.getAgency_id(),
                            deptTransactionEntity.getDept_type_id(),
                            deptTransactionEntity.getDept_transaction_main_type_id(),
                            deptTransactionEntity.getDept_transaction_sub_type_id(),
                            dept_time,
                            dept_cycle,
                            deptTransactionEntity.getCn_effect_type(),
                            deptTransactionEntity.getDtt_effect_type(),
                            deptTransactionEntity.getTt_effect_type(),
                            deptTransactionEntity.getAcoin_effect_type(),
                            deptTransactionEntity.getTransaction_value(),
                            dept_cycle_end,
                            deptTransactionEntity.getDept_type_data(),
                            deptTransactionEntity.getDescription(),
                            deptTransactionEntity.getId(),
                            deptTransactionEntity.getTransaction_value(),
                            DeptOrderStatus.FINISH.getId(),
                            0,
                            deptTransactionEntity.getDoc_no(),
                            "DEPT" + deptTransactionEntity.getId()
                    );
                    deptOrderEntity.setCreated_date(DateTimeUtils.getNow());
                    deptOrderEntity.setCreator_id(staff_id);
                    int rsCreateOrderDept = this.deptDB.createDeptOrder(deptOrderEntity);
                    if (rsCreateOrderDept <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    deptOrderEntity.setId(rsCreateOrderDept);
                } else {
                    boolean rsIncreaseTotalPricePayment = this.deptDB.increaseTotalPricePayment(deptTransactionEntity.getAgency_id(),
                            deptTransactionEntity.getTransaction_value());
                    if (!rsIncreaseTotalPricePayment) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                /**
                 * Cấn trừ đơn hàng nếu công nợ dương
                 */

                List<JSONObject> deptTransactionRemainList = this.deptDB.getListDeptTransactionRemain(deptTransactionEntity.getAgency_id());
                if (deptTransactionRemainList.size() > 0) {
                    List<JSONObject> rsDeptOrderPaymentNone = this.deptDB.getDeptOrderPaymentNone(deptTransactionEntity.getAgency_id());
                    if (rsDeptOrderPaymentNone.size() > 0) {
                        for (JSONObject jsDeptTransaction : deptTransactionRemainList) {
                            DeptTransactionEntity ctDeptTransaction = JsonUtils.DeSerialize(JsonUtils.Serialize(jsDeptTransaction), DeptTransactionEntity.class);
                            if (ctDeptTransaction != null) {
                                boolean rsCanTru = this.clearingDeptTransaction(
                                        ctDeptTransaction.getId(),
                                        ctDeptTransaction.getAgency_id(),
                                        ctDeptTransaction.getConfirmed_time(),
                                        ctDeptTransaction.getDescription(),
                                        rsDeptOrderPaymentNone,
                                        ctDeptTransaction.getAcoin_effect_type(),
                                        false,
                                        dept_time);
                                if (!rsCanTru) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }
                        }
                    }
                }

                /**
                 * Cập nhật số tiền thực hiện cho cam kết nếu có
                 */
                JSONObject commit = this.deptDB.getCommitNeedFinishByAgency(deptTransactionEntity.getAgency_id());
                if (commit != null) {
                    /**
                     * thực hiện cam kết
                     */
                    long committed_money = ConvertUtils.toLong(commit.get("committed_money"));
                    long completed_money = ConvertUtils.toLong(commit.get("completed_money"));
                    int agency_order_commit_id = ConvertUtils.toInt(commit.get("id"));

                    long money = deptTransactionEntity.getTransaction_value();
                    if (deptTransactionEntity.getTransaction_value() > committed_money - completed_money) {
                        money = committed_money - completed_money;
                    }

                    if (committed_money == completed_money + money) {
                        boolean rsFinishCommit = this.deptDB.finishCommit(
                                agency_order_commit_id
                        );
                        if (!rsFinishCommit) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    } else {
                        boolean rsIncreaseCompleteMoneyCommit = this.deptDB.increaseCompleteMoneyCommit(
                                agency_order_commit_id,
                                money
                        );
                        if (!rsIncreaseCompleteMoneyCommit) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }
                }
            }

            /**
             * Cap nhat thong tin no cho dai ly
             */
            ClientResponse crUpdateDeptAgencyInfo = this.updateDeptAgencyInfo(deptTransactionEntity.getAgency_id());
            if (crUpdateDeptAgencyInfo.failed()) {
                return crUpdateDeptAgencyInfo;
            }

            if (deptTransactionSubTypeEntity.getFunction_type().equals("THANH_TOAN")) {
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.APPROVE_TRANSACTION_THANH_THANH,
                        "",
                        NotifyAutoContentType.APPROVE_TRANSACTION_THANH_THANH.getType(),
                        "[]",
                        "Anh Tin đã nhận được khoản thanh toán " + this.appUtils.priceFormat(deptTransactionEntity.getTransaction_value()) + " của Quý khách.",
                        deptTransactionEntity.getAgency_id()
                );
            } else if (DeptType.DEPT_INCREASE.getId() == deptTransactionEntity.getDept_type_id()) {
                DeptAgencyInfoEntity deptEndCycle = this.deptDB.getDeptAgencyInfo(deptTransactionEntity.getAgency_id());
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.APPROVE_TRANSACTION_INCREASE,
                        "",
                        NotifyAutoContentType.APPROVE_TRANSACTION_INCREASE.getType(),
                        "[]",
                        "Công nợ của Quý khách đã tăng " +
                                this.appUtils.priceFormat(deptTransactionEntity.getTransaction_value()) +
                                ". Dư nợ cuối kỳ " + this.appUtils.priceFormat(deptEndCycle.getCurrent_dept()) + "." +
                                " Nội dung: " + deptTransactionEntity.getDescription(),
                        deptTransactionEntity.getAgency_id()
                );
            } else if (DeptType.DEPT_DECREASE.getId() == deptTransactionEntity.getDept_type_id()) {
                DeptAgencyInfoEntity deptEndCycle = this.deptDB.getDeptAgencyInfo(deptTransactionEntity.getAgency_id());
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.APPROVE_TRANSACTION_DECREASE,
                        "",
                        NotifyAutoContentType.APPROVE_TRANSACTION_DECREASE.getType(),
                        "[]",
                        "Công nợ của Quý khách đã giảm " +
                                this.appUtils.priceFormat(deptTransactionEntity.getTransaction_value()) +
                                ". Dư nợ cuối kỳ " + this.appUtils.priceFormat(deptEndCycle.getCurrent_dept()) + "." +
                                " Nội dung: " + deptTransactionEntity.getDescription(),
                        deptTransactionEntity.getAgency_id()
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected boolean excuteTransactionEffect(TransactionEffectType cno,
                                              String effect_type,
                                              Long transaction_value,
                                              int agency_id,
                                              int dept_transaction_id,
                                              int creator_id,
                                              Long current_dept,
                                              Integer dept_cycle,
                                              Long dept_limit,
                                              Long ngd_limit,
                                              String note,
                                              DeptAgencyInfoEntity deptAgencyInfoEntity,
                                              Date time,
                                              String code) {
        switch (cno) {
            case CNO: {
                if (TransactionEffectValueType.INCREASE.getCode().equals(effect_type)) {
                    boolean rsIncreaseCNO = this.increaseCNO(agency_id,
                            transaction_value,
                            dept_transaction_id,
                            creator_id,
                            current_dept,
                            dept_cycle,
                            dept_limit,
                            ngd_limit,
                            note,
                            time);
                    if (!rsIncreaseCNO) {
                        return false;
                    }
                } else {
                    boolean rsDecreaseCNO = this.decreaseCNO(agency_id,
                            transaction_value,
                            dept_transaction_id,
                            creator_id,
                            current_dept,
                            dept_cycle,
                            dept_limit,
                            ngd_limit,
                            note,
                            time
                    );
                    if (!rsDecreaseCNO) {
                        return false;
                    }
                }
                return true;
            }
            case DTT: {
                if (TransactionEffectValueType.INCREASE.getCode().equals(effect_type)) {
                    boolean rsIncreaseDTT = this.increaseDTT(
                            agency_id,
                            transaction_value,
                            dept_transaction_id,
                            note,
                            time,
                            false,
                            creator_id,
                            code);
                    if (!rsIncreaseDTT) {
                        return false;
                    }
                } else {
                    boolean rsDecreaseDTT = this.decreaseDTT(
                            agency_id,
                            transaction_value,
                            dept_transaction_id,
                            note,
                            time,
                            false,
                            creator_id,
                            code);
                    if (!rsDecreaseDTT) {
                        return false;
                    }
                }
                return true;
            }
            case TT: {
                if (TransactionEffectValueType.INCREASE.getCode().equals(effect_type)) {
                    boolean rsIncreaseTT = this.increaseTT(
                            agency_id,
                            transaction_value,
                            dept_transaction_id,
                            creator_id,
                            deptAgencyInfoEntity.getTotal_tt_cycle(),
                            note,
                            time);
                    if (!rsIncreaseTT) {
                        return false;
                    }
                } else {
                    boolean rsDecreaseTT = this.decreaseTT(
                            agency_id,
                            transaction_value,
                            dept_transaction_id,
                            creator_id,
                            deptAgencyInfoEntity.getTotal_tt_cycle(),
                            note,
                            time);
                    if (!rsDecreaseTT) {
                        return false;
                    }
                }
                return true;
            }
            case A_COIN: {
                if (TransactionEffectValueType.DECREASE.getCode().equals(effect_type)) {
                    int acoin = ConvertUtils.toInt(transaction_value / this.dataManager.getConfigManager().getAcoinRateDefault());
                    boolean decreaseAcoin = this.aCoinDB.decreaseACoin(agency_id, acoin);
                    if (!decreaseAcoin) {
                        return false;
                    }
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    protected boolean decreaseTT(
            int agency_id,
            Long transaction_value,
            int dept_transaction_id,
            int modifier_id,
            Long total_tt_cycle,
            String note,
            Date time) {
        try {
            boolean rsDecreaseTT = this.deptDB.decreaseTT(agency_id, transaction_value);
            if (!rsDecreaseTT) {
                return false;
            }

            /**
             * Lưu lịch sử
             */
            EditDeptDttRequest request = new EditDeptDttRequest();
            request.setAgency_id(agency_id);
            request.setNote(note);
            request.setData(transaction_value);
            request.setType(
                    ChangeValueType.DECREASE.getId()
            );
            int rsSaveHistory = this.deptDB.saveDeptTtHistory(
                    request.getAgency_id(),
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    total_tt_cycle,
                    total_tt_cycle +
                            (request.getData() *
                                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    request.getNote(),
                    1,
                    DateTimeUtils.toString(time)
            );

            /**
             * Tính lại cấp bậc
             */
            ClientResponse crUpdateAgencyMembership = this.updateAgencyMembership(
                    agency_id,
                    modifier_id,
                    false);
            if (crUpdateAgencyMembership.failed()) {
                this.alertToTelegram("Tính lại cấp bậc bị lỗi", ResponseStatus.FAIL);
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return false;
    }

    protected boolean increaseTT(
            int agency_id,
            Long transaction_value,
            int dept_transaction_id,
            int creator_id,
            Long total_tt_cycle,
            String note,
            Date time) {
        try {
            boolean rsIncreaseDTT = this.deptDB.increaseTT(agency_id, transaction_value);
            if (!rsIncreaseDTT) {
                return false;
            }

            /**
             * Lưu lịch sử
             */
            EditDeptDttRequest request = new EditDeptDttRequest();
            request.setAgency_id(agency_id);
            request.setNote(note);
            request.setData(transaction_value);
            request.setType(
                    ChangeValueType.INCREASE.getId()
            );
            int rsSaveHistory = this.deptDB.saveDeptTtHistory(
                    request.getAgency_id(),
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    total_tt_cycle,
                    total_tt_cycle +
                            (request.getData() *
                                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    request.getNote(),
                    1,
                    DateTimeUtils.toString(time)
            );

            /**
             * Tính lại cấp bậc
             */
            ClientResponse crUpdateAgencyMembership = this.updateAgencyMembership(
                    agency_id,
                    creator_id,
                    true);
            if (crUpdateAgencyMembership.failed()) {
                this.alertToTelegram("Tính lại cấp bậc bị lỗi", ResponseStatus.FAIL);
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return false;
    }

    protected ClientResponse updateAgencyMembership(
            int agency_id,
            int modifier_id,
            boolean upLevel
    ) {
        try {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(agency_id);
            if (agencyEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyInfo(agency_id);
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }
            if (upLevel) {
                if (MembershipType.THANH_VIEN.getKey() == agencyEntity.getMembership_id()) {
                    if (deptAgencyInfoEntity.getTotal_tt_cycle() >=
                            this.dataManager.getProductManager().getMpMembership().get(
                                    MembershipType.BACH_KIM.getKey()
                            ).getMoney_require()) {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                MembershipType.BACH_KIM.getKey()
                        );
                        ClientResponse crUpLevelMembership = this.updateLevelMembership(
                                agency_id,
                                MembershipType.BACH_KIM.getKey(),
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + MembershipType.BACH_KIM.getValue(),
                                    ResponseStatus.FAIL);
                        }
                        return crUpLevelMembership;
                    } else if (deptAgencyInfoEntity.getTotal_tt_cycle() >=
                            this.dataManager.getProductManager().getMpMembership().get(
                                    MembershipType.BAC.getKey()
                            ).getMoney_require()) {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                MembershipType.BAC.getKey()
                        );
                        ClientResponse crUpLevelMembership = this.updateLevelMembership(
                                agency_id,
                                MembershipType.BAC.getKey(),
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + MembershipType.BACH_KIM.getValue(),
                                    ResponseStatus.FAIL);
                        }
                        return crUpLevelMembership;
                    }
                } else {
                    if (agencyEntity.getMembership_id() != MembershipType.BACH_KIM.getKey()
                            && deptAgencyInfoEntity.getTotal_tt_cycle() >=
                            this.dataManager.getProductManager().getMpMembership().get(
                                    MembershipType.BACH_KIM.getKey()
                            ).getMoney_require()) {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                MembershipType.BACH_KIM.getKey()
                        );
                        ClientResponse crUpdateLevelMembership = this.updateLevelMembership(
                                agency_id,
                                MembershipType.BACH_KIM.getKey(),
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpdateLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + MembershipType.BACH_KIM.getValue(),
                                    ResponseStatus.FAIL);
                        }
                        return crUpdateLevelMembership;
                    }
                }
            } else {
                int current_membership_id = agencyEntity.getMembership_id();
                int start_membership_id = agencyEntity.getMembership_cycle_start_id();
                if (current_membership_id != start_membership_id
                        && deptAgencyInfoEntity.getTotal_tt_cycle() <
                        this.dataManager.getProductManager().getMpMembership().get(current_membership_id).getMoney_require()
                ) {
                    if (MembershipType.THANH_VIEN.getKey() != start_membership_id) {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                start_membership_id
                        );
                        ClientResponse crUpdateLevelMembership = this.updateLevelMembership(
                                agency_id,
                                start_membership_id,
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpdateLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + start_membership_id,
                                    ResponseStatus.FAIL);
                        }
                    } else if (deptAgencyInfoEntity.getTotal_tt_cycle() >=
                            this.dataManager.getProductManager().getMpMembership().get(MembershipType.BAC.getKey()).getMoney_require()) {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                MembershipType.BAC.getKey()
                        );
                        ClientResponse crUpdateLevelMembership = this.updateLevelMembership(
                                agency_id,
                                MembershipType.BAC.getKey(),
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpdateLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + MembershipType.BAC.getKey(),
                                    ResponseStatus.FAIL);
                        }
                    } else {
                        String old_agency_code = agencyEntity.getCode();
                        String new_agency_code = this.getNewAgencyCode(
                                old_agency_code,
                                start_membership_id
                        );
                        ClientResponse crUpdateLevelMembership = this.updateLevelMembership(
                                agency_id,
                                start_membership_id,
                                agencyEntity.getMembership_id(),
                                new_agency_code,
                                old_agency_code,
                                modifier_id);
                        if (crUpdateLevelMembership.failed()) {
                            this.alertToTelegram("upLevelMembership " + agency_id + " " + start_membership_id,
                                    ResponseStatus.FAIL);
                        }
                    }
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String getNewAgencyCode(String old_agency_code, int membership_id) {
        return old_agency_code.split("_")[0] + "_" + dataManager.getProductManager().getMpMembershipCodeById(membership_id);
    }

    public ClientResponse importDeptDTT(MultipartFile file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(4);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    List<Cell> ltCell = new ArrayList<>();
                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        // If the cell is missing from the file, generate a blank one
                        // (Works by specifying a MissingCellPolicy)
                        Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
                        ltCell.add(cell);
                    }
                    if (!ltCell.isEmpty()) {
                        Cell cell = ltCell.get(0);
                        String phone = getStringData(cell);
                        cell = ltCell.get(3);
                        String type = getStringData(cell);
                        cell = ltCell.get(4);
                        long data = getLongData(cell);
                        cell = ltCell.get(5);
                        Date dept_time = DateTimeUtils.getDateTime(getDateData(cell), "dd/MM/yyyy");
                        cell = ltCell.get(6);
                        String note = getStringData(cell);
//                        if (!phone.equals("0842424394")) {
//                            continue;
//                        }
                        AgencyEntity agencyEntity = this
                                .agencyDB.getAgencyEntityByPhone(phone);
                        if (agencyEntity == null) {
                            LogUtil.printDebug("ERRRRRR: " + index);
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        EditDeptDttRequest request = new EditDeptDttRequest();
                        request.setAgency_id(agencyEntity.getId());
                        request.setNote(note);
                        request.setData(data);
                        request.setType(
                                type.equals("Tăng") ? 1 : 2
                        );

                        DeptAgencyInfoEntity deptAgencyInfoEntity =
                                this.deptDB.getDeptAgencyInfo(request.getAgency_id());
                        if (deptAgencyInfoEntity == null) {
                            deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(
                                    deptAgencyInfoEntity,
                                    agencyEntity.getId());
                            if (deptAgencyInfoEntity == null) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                            }
                        }

                        boolean rsEditDeptDtt = this.deptDB.editDeptDtt(
                                request.getAgency_id(),
                                request.getData() * (request.getType() == ChangeValueType.INCREASE.getId() ? 1 : -1),
                                1,
                                DateTimeUtils.toString(dept_time)
                        );
                        if (!rsEditDeptDtt) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        int rsSaveDeptDttHistory = this.deptDB.saveDeptDttHistory(
                                request.getAgency_id(),
                                (request.getData() *
                                        (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                                deptAgencyInfoEntity.getTotal_dtt_cycle(),
                                deptAgencyInfoEntity.getTotal_dtt_cycle() +
                                        (request.getData() *
                                                (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                                request.getNote(),
                                1,
                                DateTimeUtils.toString(dept_time),
                                "Điều chỉnh",
                                ""
                        );

                        if (rsSaveDeptDttHistory <= 0) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }
                }
                index++;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected int getProductGroupVisibilityByAgency(int agency_id, int product_group_id) {
        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (agency == null) {
            return VisibilityType.HIDE.getId();
        }

        ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                product_group_id);
        if (productGroup == null || productGroup.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category pltth = this.dataManager.getProductManager().getCategoryById(
                productGroup.getCategory_id()
        );
        if (pltth == null || pltth.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category plsp = this.dataManager.getProductManager().getCategoryById(
                pltth.getParent_id()
        );
        if (plsp == null || plsp.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                plsp.getParent_id()
        );
        if (mat_hang == null || mat_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category nganh_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang.getParent_id()
        );
        if (nganh_hang == null || nganh_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        /**
         * Kiểm tra Catalog
         */
        if (!this.getVisibilityByCatalog(
                ConvertUtils.toInt(agency.get("id")),
                mat_hang.getId(),
                plsp.getId(),
                ConvertUtils.toInt(agency.get("require_catalog")))) {
            return VisibilityType.HIDE.getId();
        }

        JSONObject settingAgency = this.getVisibilityByObject(
                SettingObjectType.AGENCY.getCode(),
                ConvertUtils.toInt(agency.get("id")),
                0,
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingAgency != null) {
            int visibility = ConvertUtils.toInt(settingAgency.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingCity = this.getVisibilityByObject(
                SettingObjectType.CITY.getCode(),
                ConvertUtils.toInt(agency.get("city_id")),
                0,
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingCity != null) {
            int visibility = ConvertUtils.toInt(settingCity.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingRegion = this.getVisibilityByObject(
                SettingObjectType.REGION.getCode(),
                ConvertUtils.toInt(agency.get("region_id")),
                0,
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingRegion != null) {
            int visibility = ConvertUtils.toInt(settingRegion.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingMembership = this.getVisibilityByObject(
                SettingObjectType.MEMBERSHIP.getCode(),
                ConvertUtils.toInt(agency.get("membership_id")),
                0,
                productGroup.getId(),
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingMembership != null) {
            int visibility = ConvertUtils.toInt(settingMembership.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        return VisibilityType.SHOW.getId();
    }

    protected int getPLSPVisibilityByAgency(int agency_id, int plsp_id) {
        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (agency == null) {
            return VisibilityType.HIDE.getId();
        }

        Category plsp = this.dataManager.getProductManager().getCategoryById(
                plsp_id
        );
        if (plsp == null || plsp.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                plsp.getParent_id()
        );
        if (mat_hang == null || mat_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category nganh_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang.getParent_id()
        );
        if (nganh_hang == null || nganh_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        /**
         * Kiểm tra Catalog
         */
        if (!this.getVisibilityByCatalog(
                ConvertUtils.toInt(agency.get("id")),
                mat_hang.getId(),
                plsp.getId(),
                ConvertUtils.toInt(agency.get("require_catalog")))) {
            return VisibilityType.HIDE.getId();
        }

        JSONObject settingAgency = this.getVisibilityByObject(
                SettingObjectType.AGENCY.getCode(),
                ConvertUtils.toInt(agency.get("id")),
                0,
                0,
                0,
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingAgency != null) {
            int visibility = ConvertUtils.toInt(settingAgency.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingCity = this.getVisibilityByObject(
                SettingObjectType.CITY.getCode(),
                ConvertUtils.toInt(agency.get("city_id")),
                0,
                0,
                0,
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingCity != null) {
            int visibility = ConvertUtils.toInt(settingCity.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingRegion = this.getVisibilityByObject(
                SettingObjectType.REGION.getCode(),
                ConvertUtils.toInt(agency.get("region_id")),
                0,
                0,
                0,
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingRegion != null) {
            int visibility = ConvertUtils.toInt(settingRegion.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingMembership = this.getVisibilityByObject(
                SettingObjectType.MEMBERSHIP.getCode(),
                ConvertUtils.toInt(agency.get("membership_id")),
                0,
                0,
                0,
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingMembership != null) {
            int visibility = ConvertUtils.toInt(settingMembership.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        return VisibilityType.SHOW.getId();
    }

    protected int getPLTTHVisibilityByAgency(int agency_id, int pltth_id) {
        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (agency == null) {
            return VisibilityType.HIDE.getId();
        }

        Category pltth = this.dataManager.getProductManager().getCategoryById(
                pltth_id
        );
        if (pltth == null || pltth.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category plsp = this.dataManager.getProductManager().getCategoryById(
                pltth.getParent_id()
        );
        if (plsp == null || plsp.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                plsp.getParent_id()
        );
        if (mat_hang == null || mat_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category nganh_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang.getParent_id()
        );
        if (nganh_hang == null || nganh_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        /**
         * Kiểm tra Catalog
         */
        if (!this.getVisibilityByCatalog(
                ConvertUtils.toInt(agency.get("id")),
                mat_hang.getId(),
                plsp.getId(),
                ConvertUtils.toInt(agency.get("require_catalog")))) {
            return VisibilityType.HIDE.getId();
        }

        JSONObject settingAgency = this.getVisibilityByObject(
                SettingObjectType.AGENCY.getCode(),
                ConvertUtils.toInt(agency.get("id")),
                0,
                0,
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingAgency != null) {
            int visibility = ConvertUtils.toInt(settingAgency.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingCity = this.getVisibilityByObject(
                SettingObjectType.CITY.getCode(),
                ConvertUtils.toInt(agency.get("city_id")),
                0,
                0,
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingCity != null) {
            int visibility = ConvertUtils.toInt(settingCity.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingRegion = this.getVisibilityByObject(
                SettingObjectType.REGION.getCode(),
                ConvertUtils.toInt(agency.get("region_id")),
                0,
                0,
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingRegion != null) {
            int visibility = ConvertUtils.toInt(settingRegion.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingMembership = this.getVisibilityByObject(
                SettingObjectType.MEMBERSHIP.getCode(),
                ConvertUtils.toInt(agency.get("membership_id")),
                0,
                0,
                pltth.getId(),
                plsp.getId(),
                mat_hang.getId(),
                0
        );
        if (settingMembership != null) {
            int visibility = ConvertUtils.toInt(settingMembership.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        return VisibilityType.SHOW.getId();
    }

    protected int getMatHangVisibilityByAgency(int agency_id, int mat_hang_id) {
        JSONObject agency = this.agencyDB.getAgencyInfoById(agency_id);
        if (agency == null) {
            return VisibilityType.HIDE.getId();
        }

        Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang_id
        );
        if (mat_hang == null || mat_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        Category nganh_hang = this.dataManager.getProductManager().getCategoryById(
                mat_hang.getParent_id()
        );
        if (nganh_hang == null || nganh_hang.getStatus() != ActiveStatus.ACTIVATED.getValue()) {
            return VisibilityType.HIDE.getId();
        }

        /**
         * Kiểm tra Catalog
         */
        if (!this.getVisibilityByCatalog(
                ConvertUtils.toInt(agency.get("id")),
                mat_hang.getId(),
                null,
                ConvertUtils.toInt(agency.get("require_catalog")))) {
            return VisibilityType.HIDE.getId();
        }

        JSONObject settingAgency = this.getVisibilityByObject(
                SettingObjectType.AGENCY.getCode(),
                ConvertUtils.toInt(agency.get("id")),
                0,
                0,
                0,
                0,
                mat_hang.getId(),
                0
        );
        if (settingAgency != null) {
            int visibility = ConvertUtils.toInt(settingAgency.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingCity = this.getVisibilityByObject(
                SettingObjectType.CITY.getCode(),
                ConvertUtils.toInt(agency.get("city_id")),
                0,
                0,
                0,
                0,
                mat_hang.getId(),
                0
        );
        if (settingCity != null) {
            int visibility = ConvertUtils.toInt(settingCity.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingRegion = this.getVisibilityByObject(
                SettingObjectType.REGION.getCode(),
                ConvertUtils.toInt(agency.get("region_id")),
                0,
                0,
                0,
                0,
                mat_hang.getId(),
                0
        );
        if (settingRegion != null) {
            int visibility = ConvertUtils.toInt(settingRegion.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        JSONObject settingMembership = this.getVisibilityByObject(
                SettingObjectType.MEMBERSHIP.getCode(),
                ConvertUtils.toInt(agency.get("membership_id")),
                0,
                0,
                0,
                0,
                mat_hang.getId(),
                0
        );
        if (settingMembership != null) {
            int visibility = ConvertUtils.toInt(settingMembership.get("visibility"));
            return VisibilityType.from(visibility).getId();
        }

        return VisibilityType.SHOW.getId();
    }

    protected int getLoaiSPVisibilityByAgency(int agency_id, int loai_sp) {
        return VisibilityType.SHOW.getId();
    }

    protected JSONObject convertComboInCart(int item_id, Agency agency) {
        JSONObject comboInfo = this.promoDB.getComboInfo(
                item_id);
        if (comboInfo != null) {
            comboInfo.put("minimum_purchase", 1);
            comboInfo.put("step", 1);
            comboInfo.put("is_combo", 1);
            List<ProductData> products = new ArrayList<>();
            List<JSONObject> comboDetailList = this.promoDB.getListProductInCombo(item_id);
            int show = VisibilityType.SHOW.getId();
            long total_price = 0;
            for (JSONObject comboDetail : comboDetailList) {
                int product_id = ConvertUtils.toInt(comboDetail.get("product_id"));
                int product_quantity = ConvertUtils.toInt(comboDetail.get("product_quantity"));
                long product_price = 0;

                ProductCache productPrice = this.getFinalPriceByAgency(
                        product_id,
                        agency.getId(),
                        agency.getCityId(),
                        agency.getRegionId(),
                        agency.getMembershipId()
                );
                total_price += productPrice.getPrice() < 0 ? 0 : (productPrice.getPrice() * product_quantity);

                ProductCache productCache = this.dataManager.getProductManager()
                        .getProductBasicData(product_id);
                if (productCache != null) {
                    show = show == VisibilityType.SHOW.getId() ? this.getProductVisibilityByAgency(
                            agency.getId(),
                            product_id
                    ) : VisibilityType.HIDE.getId();
                    if (show == VisibilityType.HIDE.getId()) {
                        return null;
                    }

                    ProductData product = new ProductData(
                            product_id,
                            productCache.getCode(),
                            productCache.getFull_name(),
                            product_quantity,
                            productCache.getImages()
                    );
                    product.setQuantity(product_quantity);
                    product.setPrice(product_price);
                    products.add(product);
                }
            }

            if (show == VisibilityType.HIDE.getId()) {
                return null;
            }
            comboInfo.put("price", total_price);
            comboInfo.put("products", products);
            comboInfo.put("is_hunt_sale", 1);

            return comboInfo;
        }
        return null;
    }

    protected String convertPromoProductToString(List<PromoProductInfoData> data) {
        JsonArray arr = new JsonArray();
        try {
            for (PromoProductInfoData promoProductInfoData : data) {
                JsonObject obProgramProductInfo = new JsonObject();
                obProgramProductInfo.addProperty("product_id", promoProductInfoData.getProduct_id());
                JsonArray arrProgram = new JsonArray();

                for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                    JsonObject obProgram = new JsonObject();
                    obProgram.addProperty("promo_id", promoOrderData.getPromo_id());
                    obProgram.addProperty("promo_code", promoOrderData.getPromo_code());
                    obProgram.addProperty("promo_name", promoOrderData.getPromo_name());
                    obProgram.addProperty("promo_description", promoOrderData.getPromo_description());
                    obProgram.addProperty("promo_percent", promoOrderData.getPromo_percent());
                    arrProgram.add(obProgram);
                }
                obProgramProductInfo.add("promo", arrProgram);
                arr.add(obProgramProductInfo);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return arr.toString();
    }

    protected List<PromoProductInfoData> convertPromoProductToList(String data) {
        try {
            List<PromoProductInfoData> promoProductInfoDataList = JsonUtils.DeSerialize(
                    data,
                    new TypeToken<List<PromoProductInfoData>>() {
                    }.getType());
            return promoProductInfoDataList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return new ArrayList<>();
    }

    protected void addFilterOrderData(SessionData sessionData, FilterListRequest request) {
        List<FilterRequest> filterRequestList = this.dataManager.getStaffManager().getFilterOrder(
                sessionData.getId());
        for (FilterRequest filterRequest : filterRequestList) {
            request.getFilters().add(filterRequest);
        }
    }

    public void ghiNhanTichLuyOfTransaction(
            DeptTransactionEntity deptTransactionEntity
    ) {
        try {
            Date now = DateTimeUtils.getNow();
            long order_time = now.getTime();
            long dept_time = now.getTime();

            if (deptTransactionEntity.getDtt_effect_type().equals(TransactionEffectValueType.NONE.getCode())) {
                return;
            }

            callGhiNhanTichLuyOfTransaction(
                    deptTransactionEntity.getDept_transaction_main_type_id() ==
                            DeptTransactionMainType.INCREASE.getId() ? CTTLTransactionType.TANG_CONG_NO.getKey() :
                            CTTLTransactionType.GIAM_CONG_NO.getKey(),
                    deptTransactionEntity.getAgency_id(),
                    (deptTransactionEntity.getDoc_no() == null || deptTransactionEntity.getDoc_no().isEmpty()) ?
                            ("DEPT" + deptTransactionEntity.getId()) :
                            deptTransactionEntity.getDoc_no(),
                    0,
                    deptTransactionEntity.getDept_transaction_main_type_id() ==
                            DeptTransactionMainType.INCREASE.getId()
                            ? deptTransactionEntity.getTransaction_value() : (deptTransactionEntity.getTransaction_value() * -1),
                    deptTransactionEntity.getId(),
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    order_time,
                    dept_time
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    public void ghiNhanTichLuyOfHangBanTraLai(
            int agency_hbtl_id
    ) {
        try {
            JSONObject hbtl = this.orderDB.getAgencyHBTL(agency_hbtl_id);
            if (hbtl == null) {
                return;
            }
            long now = DateTimeUtils.getMilisecondsNow();

            callGhiNhanTichLuyOfTransaction(
                    CTTLTransactionType.HBTL.getKey(),
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    ConvertUtils.toString(hbtl.get("code")),
                    0,
                    ConvertUtils.toLong(hbtl.get("total_end_price")) * -1,
                    agency_hbtl_id,
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    now,
                    now
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private void callGhiNhanTichLuyOfTransaction(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            int transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time
    ) {
        try {
            /**
             * - /tl/add_transaction
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + order_id (int: mã đơn hàng cha)
             *     + value (int: giá trị dùng cho doanh thu thuần...)
             *     + transaction_source (int)
             *     + staff_id (int)
             *
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/add_transaction?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&value=" + value
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id
                    + "&order_time=" + order_time
                    + "&dept_time=" + dept_time;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.success()) {
                return;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    public void ghiNhanCTXHOfTransaction(
            DeptTransactionEntity deptTransactionEntity
    ) {
        try {
            if (this.dataManager.getConfigManager().isHBTL(
                    deptTransactionEntity.getDept_transaction_sub_type_id())) {
                if (!DeptConstants.SOURCE_BRAVO.equals(deptTransactionEntity.getSource())) {
                    int rsHBTL = this.orderDB.saveAgencyHBTL(
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getAgency_id(),
                            deptTransactionEntity.getTransaction_value(),
                            deptTransactionEntity.getTransaction_value()
                    );
                    if (rsHBTL > 0) {
                        this.orderDB.updateCodeAgencyHBTL(
                                rsHBTL,
                                "HBTL" + rsHBTL
                        );
                        /**
                         * Ghi nhận tích lũy CTXH của Hàng bán trả lại
                         */
                        this.ghiNhanCTXHOfHangBanTraLai(
                                rsHBTL
                        );
                    } else {
                        this.alertToTelegram(
                                "Ghi nhận tích lũy của Hàng bán trả lại: " +
                                        deptTransactionEntity.getDoc_no() +
                                        " failed",
                                ResponseStatus.FAIL
                        );
                    }
                }
            } else {
                Date now = DateTimeUtils.getNow();
                long order_time = now.getTime();
                long dept_time = now.getTime();

                if (deptTransactionEntity.getDtt_effect_type().equals(TransactionEffectValueType.NONE.getCode())) {
                    return;
                }

                callGhiNhanCTXHOfTransaction(
                        deptTransactionEntity.getDept_transaction_main_type_id() ==
                                DeptTransactionMainType.INCREASE.getId() ? CTTLTransactionType.TANG_CONG_NO.getKey() :
                                CTTLTransactionType.GIAM_CONG_NO.getKey(),
                        deptTransactionEntity.getAgency_id(),
                        (deptTransactionEntity.getDoc_no() == null || deptTransactionEntity.getDoc_no().isEmpty()) ?
                                ("DEPT" + deptTransactionEntity.getId()) :
                                deptTransactionEntity.getDoc_no(),
                        0,
                        deptTransactionEntity.getDept_transaction_main_type_id() ==
                                DeptTransactionMainType.INCREASE.getId()
                                ? deptTransactionEntity.getTransaction_value() : (deptTransactionEntity.getTransaction_value() * -1),
                        deptTransactionEntity.getId(),
                        CTTLTransactionSource.AUTO.getId(),
                        0,
                        order_time,
                        dept_time
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private void callGhiNhanCTXHOfTransaction(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            int transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time
    ) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /bxh/add_transaction
                     *     + key : reload key
                     *     + type (string) 1: order....
                     *     + agency_id (int)
                     *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
                     *     + order_id (int: mã đơn hàng cha)
                     *     + value (int: giá trị dùng cho doanh thu thuần...)
                     *     + transaction_source (int)
                     *     + staff_id (int)
                     *
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/bxh/add_transaction";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("type", type);
                    body.add("agency_id", ConvertUtils.toString(agency_id));
                    body.add("code", code);
                    body.add("order_id", ConvertUtils.toString(order_id));
                    body.add("transaction_id", ConvertUtils.toString(transaction_id));
                    body.add("transaction_source", ConvertUtils.toString(transaction_source));
                    body.add("staff_id", ConvertUtils.toString(staff_id));
                    body.add("order_time", ConvertUtils.toString(order_time));
                    body.add("dept_time", ConvertUtils.toString(dept_time));
                    body.add("value", ConvertUtils.toString(value));
                    LogUtil.printDebug("CTXH: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("CTXH: " + JsonUtils.Serialize(rs));
                    if (rs != null && rs.getBody().success()) {
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    public void ghiNhanCTXHOfHangBanTraLai(
            int agency_hbtl_id
    ) {
        try {
            JSONObject hbtl = this.orderDB.getAgencyHBTL(agency_hbtl_id);
            if (hbtl == null) {
                return;
            }
            long now = DateTimeUtils.getMilisecondsNow();

            callGhiNhanCTXHOfTransaction(
                    CTTLTransactionType.HBTL.getKey(),
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    ConvertUtils.toString(hbtl.get("code")),
                    0,
                    ConvertUtils.toLong(hbtl.get("total_end_price")) * -1,
                    agency_hbtl_id,
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    now,
                    now
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }
}