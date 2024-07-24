package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.DeptConstants;
import com.app.server.constants.MissionConstants;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.dept.DeptTransactionDetailData;
import com.app.server.data.dto.mission.MissionCKSData;
import com.app.server.data.entity.*;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.dept.*;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.promo.PromoTimeRequest;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DeptService extends BaseService {

    private AccumulateService accumulateService;

    @Autowired
    public void setAccumulateService(AccumulateService accumulateService) {
        this.accumulateService = accumulateService;
    }

    private AccumulateCTXHService accumulateCTXHService;

    @Autowired
    public void setAccumulateCTXHService(AccumulateCTXHService accumulateCTXHService) {
        this.accumulateCTXHService = accumulateCTXHService;
    }

    private AccumulateMissionService accumulateMissionService;

    @Autowired
    public void setAccumulateMissionService(AccumulateMissionService accumulateMissionService) {
        this.accumulateMissionService = accumulateMissionService;
    }

    public ClientResponse filterDeptTransaction(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgencyData(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_TRANSACTION, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.deptDB.filterDeptTransaction(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
                jsonObject.put("modifier_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("modifier_id"))
                ));
            }
            int total = this.deptDB.getTotalDeptTransaction(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterDeptOrder(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_ORDER, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.deptDB.filterDeptOrder(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
                if (jsonObject.get("dept_type_data") != null &&
                        !ConvertUtils.toString(jsonObject.get("dept_type_data")).isEmpty()) {
                    JSONObject agencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                            ConvertUtils.toString(jsonObject.get("dept_type_data"))
                    );
                    if (agencyOrder != null) {
                        jsonObject.put("agency_order_id", ConvertUtils.toInt(agencyOrder.get("id")));
                    }
                }

                if (ConvertUtils.toInt(jsonObject.get("dept_type_id")) == DeptType.DEPT_DON_HANG.getId()
                ) {
                    jsonObject.put("dept_type_data",
                            ConvertUtils.toString(jsonObject.get("code")));
                } else if (ConvertUtils.toInt(jsonObject.get("dept_transaction_sub_type_id")) == DeptConstants.HAN_MUC_HEN_GIAO ||
                        ConvertUtils.toInt(jsonObject.get("dept_transaction_sub_type_id")) == DeptConstants.HAN_MUC_HOP_DONG) {
                    jsonObject.put("dept_type_data", jsonObject.get("dept_type_data"));
                } else {
                    jsonObject.put("dept_type_data", "");
                }

                if (ConvertUtils.toInt(jsonObject.get("is_nqh")) == 1 &&
                        ConvertUtils.toInt(jsonObject.get("is_nqh_over")) == 0) {
                    jsonObject.put("date_miss_commit", this.getDateMissCommitByNQHOver(jsonObject));
                }
            }
            int total = this.deptDB.getTotalDeptOrder(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private Date getDateMissCommitByNQHOver(JSONObject jsDeptOrder
    ) {
        try {
            JSONObject agency_info =
                    this.agencyDB.getAgencyInfoById(
                            ConvertUtils.toInt(jsDeptOrder.get("agency_id"))
                    );
            if (agency_info == null) {
                return null;
            }
            int number_day_nqh_miss_commit = ConvertUtils.toInt(agency_info.get("number_day_nqh_miss_commit"));

            if (number_day_nqh_miss_commit == 0) {
                return null;
            }
            if (jsDeptOrder.get("nqh_date") == null) {
                return null;
            }
            return this.appUtils.getDateAfterDay(
                    AppUtils.convertJsonToDate(jsDeptOrder.get("nqh_date")),
                    number_day_nqh_miss_commit
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return null;
    }

    /**
     * Duyệt giao dịch
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse approveDeptTransaction(
            SessionData sessionData,
            ApproveDeptTransactionRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            for (Integer dept_transaction_id : request.getIds()) {
                DeptTransactionEntity deptTransactionEntity = this.deptDB.getDeptTransaction(dept_transaction_id);
                if (deptTransactionEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                ClientResponse crApproveDeptTransaction = this.approveDeptTransactionOne(
                        dept_transaction_id,
                        sessionData.getId(),
                        DateTimeUtils.getNow(),
                        null
                );
                if (crApproveDeptTransaction.failed()) {
                    return crApproveDeptTransaction;
                }

                /**
                 * Tích lũy nhiệm vụ
                 */
                this.accumulateTichLuyNhiemVu(dept_transaction_id);

                /**
                 * Tăng công nợ thì kiểm tra nqh, nếu có thi tich luy nhiem vu
                 */
                if (DeptType.DEPT_INCREASE.getId() == deptTransactionEntity.getDept_type_id()) {
                    this.kiemTraTichLuyNQH(
                            deptTransactionEntity
                    );
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void kiemTraTichLuyNQH(
            DeptTransactionEntity deptTransactionEntity) {
        try {
            Date now = new Date();
            if (checkNgayCuoiMission(now)) {
                return;
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(deptTransactionEntity.getAgency_id());
            if (deptAgencyInfoEntity.getNqh() > 0 &&
                    getDeptOrderPaymentDeadline(deptTransactionEntity.getConfirmed_time(), deptAgencyInfoEntity.getDept_cycle()).before(now)) {
                this.accumulateTichLuyNhiemVuNQH(
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getDoc_no(),
                        deptTransactionEntity.getDept_type_id(),
                        deptTransactionEntity.getTransaction_value());
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
    }

    public ClientResponse rejectDeptTransaction(SessionData sessionData, RejectDeptTransactionRequest request) {
        try {
            for (Integer dept_transaction_id : request.getIds()) {
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

                /**
                 * Cập nhật trạng thái giao dịch
                 */
                boolean rsRejectDeptAgency = this.deptDB.rejectDeptTransaction(deptTransactionEntity.getId(), request.getNote());
                if (!rsRejectDeptAgency) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getDeptTransactionInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsDeptInfo = this.deptDB.getDeptInfo(request.getId());
            JSONObject data = new JSONObject();
            data.put("dept", jsDeptInfo);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editDeptTransaction(SessionData sessionData, EditDeptTransactionRequest request) {
        try {
            AgencyBasicData agency = this.dataManager.getAgencyManager().getAgencyBasicData(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            DeptTransactionEntity oldTransactionEntity = this.deptDB.getDeptTransaction(request.getId());
            if (oldTransactionEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (DeptTransactionStatus.WAITING.getId() != oldTransactionEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            /**
             * Hạn mục công việc: loại khác thì dữ liệu ảnh hưởng lấy theo dữ liệu gửi lên,
             * ngược lại thì lấy theo cấu hình
             */
            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = this.deptDB.getHanMuc(request.getDept_transaction_sub_type_id());
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Kiểm tra mã đơn hàng nếu có
             */
            if (TransactionFunctionType.PHI_PHAT_SINH.getCode().equals(deptTransactionSubTypeEntity.getFunction_type()) && request.getDept_type_data() != null
                    && !request.getDept_type_data().isEmpty()) {
                JSONObject jsOrder = this.orderDB.getOrderByOrderCode(request.getDept_type_data());
                if (jsOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
                }
            }

            DeptTransactionEntity deptTransactionEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), DeptTransactionEntity.class);
            deptTransactionEntity.setCreator_id(oldTransactionEntity.getCreator_id());
            deptTransactionEntity.setCreated_date(oldTransactionEntity.getCreated_date());

            if (deptTransactionSubTypeEntity.getCan_edit_effect() == 0) {
                deptTransactionEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                deptTransactionEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                deptTransactionEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                deptTransactionEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());
                deptTransactionEntity.setDept_type_id(deptTransactionSubTypeEntity.getDept_type_id());
            } else {
                if (DeptTransactionMainType.INCREASE.getId() == request.getDept_transaction_main_type_id()) {
                    deptTransactionEntity.setCn_effect_type(TransactionEffectValueType.INCREASE.getCode());
                    deptTransactionEntity.setDept_type_id(DeptType.DEPT_INCREASE.getId());
                } else {
                    deptTransactionEntity.setCn_effect_type(TransactionEffectValueType.DECREASE.getCode());
                    deptTransactionEntity.setDept_type_id(DeptType.DEPT_DECREASE.getId());
                }
            }
            deptTransactionEntity.setDept_function_type(deptTransactionSubTypeEntity.getFunction_type());
            deptTransactionEntity.setModifier_id(sessionData.getId());
            deptTransactionEntity.setModified_date(DateTimeUtils.getNow());
            deptTransactionEntity.setStatus(DeptTransactionStatus.WAITING.getId());
            boolean rsUpdate = this.deptDB.updateDeptTransaction(deptTransactionEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createDeptTransaction(SessionData sessionData, CreateDeptTransactionRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * HBTL nhận từ BRAVO
             */
            if (DeptConstants.GHI_NHAN_TRA_HANG == request.getDept_transaction_sub_type_id()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "HBTL nhận từ BRAVO");
            }

            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = this.deptDB.getHanMuc(request.getDept_transaction_sub_type_id());
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Kiểm tra mã đơn hàng nếu có
             */
            if (TransactionFunctionType.PHI_PHAT_SINH.getCode().equals(deptTransactionSubTypeEntity.getFunction_type()) && request.getDept_type_data() != null
                    && !request.getDept_type_data().isEmpty()) {
                JSONObject jsOrder = this.orderDB.getOrderByOrderCode(request.getDept_type_data());
                if (jsOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
                }
            }

            AgencyBasicData agency = this.dataManager.getAgencyManager().getAgencyBasicData(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            DeptTransactionEntity deptTransactionEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), DeptTransactionEntity.class);

            /**
             * Hạn mục công việc: loại khác thì dữ liệu ảnh hưởng lấy theo dữ liệu gửi lên,
             * ngược lại thì lấy theo cấu hình
             */


            if (deptTransactionSubTypeEntity.getCan_edit_effect() == 0) {
                deptTransactionEntity.setCn_effect_type(deptTransactionSubTypeEntity.getCn_effect_type());
                deptTransactionEntity.setDtt_effect_type(deptTransactionSubTypeEntity.getDtt_effect_type());
                deptTransactionEntity.setTt_effect_type(deptTransactionSubTypeEntity.getTt_effect_type());
                deptTransactionEntity.setAcoin_effect_type(deptTransactionSubTypeEntity.getAcoin_effect_type());
                deptTransactionEntity.setDept_transaction_main_type_id(deptTransactionSubTypeEntity.getDept_transaction_main_type_id());
                deptTransactionEntity.setDept_type_id(deptTransactionSubTypeEntity.getDept_type_id());
            } else {
                if (DeptTransactionMainType.INCREASE.getId() == request.getDept_transaction_main_type_id()) {
                    deptTransactionEntity.setCn_effect_type(TransactionEffectValueType.INCREASE.getCode());
                    deptTransactionEntity.setDept_type_id(DeptType.DEPT_INCREASE.getId());
                } else {
                    deptTransactionEntity.setCn_effect_type(TransactionEffectValueType.DECREASE.getCode());
                    deptTransactionEntity.setDept_type_id(DeptType.DEPT_DECREASE.getId());
                }
            }
            deptTransactionEntity.setDescription(request.getDescription());
            deptTransactionEntity.setDept_function_type(deptTransactionSubTypeEntity.getFunction_type());
            deptTransactionEntity.setCreator_id(sessionData.getId());
            deptTransactionEntity.setModifier_id(sessionData.getId());

            Date now = DateTimeUtils.getNow();
            deptTransactionEntity.setCreated_date(now);
            deptTransactionEntity.setStatus(DeptTransactionStatus.WAITING.getId());
            deptTransactionEntity.setDoc_no(this.generateDeptCode(now.getTime()));
            int rsInsert = this.deptDB.createDeptTransaction(deptTransactionEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.deptDB.updateCodeForDeptTransaction(
                    rsInsert,
                    "DEPT" + rsInsert
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String generateDeptCode(long time) {
        return "APP" + ConvertUtils.toString(time);
    }

    public ClientResponse filterDeptSetting(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_SETTING, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.deptDB.filterDeptSetting(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                List<String> agency_include = JsonUtils.DeSerialize(
                        jsonObject.get("agency_include").toString(), new TypeToken<List<String>>() {
                        }.getType());
                int agency_id = ConvertUtils.toInt(agency_include.get(0));
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(agency_id));

                jsonObject.put("modifier_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("modifier_id"))
                ));
            }
            int total = this.deptDB.getTotalDeptSetting(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createDeptSetting(SessionData sessionData, CreateDeptSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            for (AgencyBasicData agencyBasicData : request.getDept_apply_object().getDept_agency_includes()) {
                DeptSettingEntity deptSettingEntity = new DeptSettingEntity();
                deptSettingEntity.setAgency_include("[\"" + agencyBasicData.getId() + "\"]");
                deptSettingEntity.setAgency_id(agencyBasicData.getId());
                deptSettingEntity.setDept_cycle(request.getDept_cycle());
                deptSettingEntity.setDept_limit(request.getDept_limit());
                deptSettingEntity.setNgd_limit(request.getNgd_limit());
                deptSettingEntity.setStatus(DeptSettingStatus.WAITING.getId());
                deptSettingEntity.setCreator_id(sessionData.getId());
                deptSettingEntity.setModifier_id(sessionData.getId());
                deptSettingEntity.setCreated_date(DateTimeUtils.getNow());
                deptSettingEntity.setModified_date(DateTimeUtils.getNow());
                deptSettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
                deptSettingEntity.setEnd_date(request.getEnd_date() != null ? DateTimeUtils.getDateTime(request.getEnd_date()) : null);
                deptSettingEntity.setNote(request.getNote());
                int rsInsert = this.deptDB.insertDeptSetting(deptSettingEntity);
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<String> convertDeptSettingAgencyIncludeToString(List<AgencyBasicData> agencyBasicDataList) {
        List<String> agencyIncludeList = new ArrayList<>();
        for (AgencyBasicData agencyBasicData : agencyBasicDataList) {
            agencyIncludeList.add(ConvertUtils.toString(agencyBasicData.getId()));
        }
        return agencyIncludeList;
    }

    public ClientResponse approveDeptSetting(SessionData sessionData, ApproveDeptSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            for (Integer dept_setting_id : request.getIds()) {
                DeptSettingEntity deptSettingEntity = this.deptDB.getDeptSetting(dept_setting_id);
                if (deptSettingEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                if (DeptSettingStatus.WAITING.getId() != deptSettingEntity.getStatus()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                }

                /**
                 * Nếu thiết lập có thời gian kết thúc thì thời gian không được nhỏ hơn hiện tại
                 */
                if (deptSettingEntity.getEnd_date() != null && deptSettingEntity.getEnd_date().getTime() <= DateTimeUtils.getNow().getTime()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
                }

                Date confirmed_date = DateTimeUtils.getNow();
                Date start_date = deptSettingEntity.getStart_date();
                if (start_date.getTime() < confirmed_date.getTime()) {
                    start_date = confirmed_date;
                }
                boolean rsApproveDeptSetting = this.deptDB.approveDeptSetting(dept_setting_id, start_date, confirmed_date);
                if (!rsApproveDeptSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Nếu thời gian bắt đầu là ngày hôm nay
                 * thiết lập công nợ ngay cho đại lý
                 */
                if (start_date.getTime() <= confirmed_date.getTime()
                        && (deptSettingEntity.getEnd_date() == null || deptSettingEntity.getEnd_date().getTime() >= confirmed_date.getTime())) {
                    List<String> strAgencyIdList = JsonUtils.DeSerialize(deptSettingEntity.getAgency_include(), new TypeToken<List<String>>() {
                    }.getType());

                    for (String strAgencyId : strAgencyIdList) {
                        LogUtil.printDebug("strAgencyId:" + strAgencyId);
                        int agency_id = ConvertUtils.toInt(strAgencyId);
                        DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
                        if (deptAgencyInfoEntity == null) {
                            /**
                             * Khởi tạo dữ liệu công nợ nếu đại lý chưa có dữ liệu
                             */
                            deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agency_id);
                            if (deptAgencyInfoEntity == null) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        } else {
                            /**
                             * Cập nhật lại kỳ hạn nợ, hạn mức nợ, hạn mức gối đầu của đại lý
                             */
                            ClientResponse rsUpdateDeptAgencyInfoByDeptSetting = this.updateDeptAgencyInfoByDeptSetting(
                                    agency_id,
                                    deptSettingEntity.getDept_limit(),
                                    deptSettingEntity.getDept_cycle(),
                                    deptSettingEntity.getNgd_limit()
                            );
                            if (rsUpdateDeptAgencyInfoByDeptSetting.failed()) {
                                return rsUpdateDeptAgencyInfoByDeptSetting;
                            }

                            /**
                             * Cập nhật lại thông tin nợ của đại lý
                             */
                            ClientResponse rsUpdateDeptAgencyInfo = this.updateDeptAgencyInfo(agency_id);
                            if (rsUpdateDeptAgencyInfo.failed()) {
                                return rsUpdateDeptAgencyInfo;
                            }
                        }

                        /**
                         * Luu lich su
                         */
                        this.insertDeptAgencyHistory(agency_id,
                                0L,
                                null,
                                null,
                                "Thiết lập",
                                sessionData.getId(),
                                deptAgencyInfoEntity.getCurrent_dept(),
                                deptAgencyInfoEntity.getDept_cycle(),
                                deptAgencyInfoEntity.getDept_limit(),
                                deptAgencyInfoEntity.getNgd_limit(),
                                DateTimeUtils.getNow()

                        );

                        this.pushNotifyToAgency(
                                0,
                                NotifyAutoContentType.CONG_NO_CHI_TIET,
                                "",
                                NotifyAutoContentType.CONG_NO_CHI_TIET.getType(),
                                "[]",
                                "Chính sách công nợ dành cho Quý khách vừa được cập nhật. Vui lòng vào trang Công nợ để xem chi tiết.",
                                agency_id
                        );
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private DeptAgencyInfoEntity calculateDeptAgencyInfo(DeptAgencyInfoEntity deptAgencyInfoEntity, int agency_id) {
        LogUtil.printDebug("calculateDeptAgencyInfo");
        try {
            deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agency_id);
            return deptAgencyInfoEntity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return null;
    }

    private ClientResponse updateDeptAgencyInfoByDeptSetting(Integer agency_id, Long dept_limit, Integer dept_cycle, Long ngd_limit) {
        if (dept_limit != null) {
            boolean rsUpdateDeptLimit = this.deptDB.updateDeptLimitAgencyInfo(agency_id, dept_limit);
            if (!rsUpdateDeptLimit) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        }

        if (dept_cycle != null) {
            boolean rsUpdateDeptCycle = this.deptDB.updateDeptCycleAgencyInfo(agency_id, dept_cycle);
            if (!rsUpdateDeptCycle) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        }
        if (ngd_limit != null) {
            boolean rsUpdateDeptNGDLimit = this.deptDB.updateDeptNGDLimitAgencyInfo(agency_id, ngd_limit);
            if (!rsUpdateDeptNGDLimit) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        }
        return ClientResponse.success(null);
    }

    public ClientResponse runDeptAgencyDateByEndDate() {
        ClientResponse clientResponse = ClientResponse.success(null);
        try {
            List<JSONObject> jsAgencyList = this.agencyDB.getAllAgencyActive();
            for (JSONObject jsAgency : jsAgencyList) {
                AgencyBasicData agencyBasicData = JsonUtils.DeSerialize(JsonUtils.Serialize(jsAgency), AgencyBasicData.class);
                ClientResponse clientResponse1 = this.initDeptAgencyDateByEndDate(
                        agencyBasicData);
                if (clientResponse1.failed()) {
                    this.alertToTelegram(
                            "Lưu công nợ cuối ngày: " + agencyBasicData.getCode() + " FAIL",
                            ResponseStatus.FAIL
                    );
                    clientResponse = clientResponse1;
                }
            }

            if (clientResponse.success()) {
                this.reportToTelegram(
                        "Lưu công nợ cuối ngày: FINISH",
                        ResponseStatus.SUCCESS
                );
            } else {
                this.reportToTelegram(
                        "Lưu công nợ cuối ngày: FAIL",
                        ResponseStatus.FAIL
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return clientResponse;
    }

    public ClientResponse runDeptAgencyDateByStartDate() {
        try {
            List<JSONObject> jsAgencyList = this.agencyDB.getAllAgencyActive();
            for (JSONObject jsAgency : jsAgencyList) {
                AgencyBasicData agencyBasicData = JsonUtils.DeSerialize(JsonUtils.Serialize(jsAgency), AgencyBasicData.class);
                DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyBasicData.getId());
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyBasicData.getId());
                if (deptAgencyInfoEntity == null) {
                    this.alertToTelegram(
                            "Reset công nợ đầu ngày " + agencyBasicData.getCode() + " FAILED",
                            ResponseStatus.FAIL
                    );
                }
            }

            this.reportToTelegram(
                    "Reset công nợ đầu ngày: FINISH",
                    ResponseStatus.SUCCESS
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse initDeptAgencyDateByEndDate(AgencyBasicData agencyBasicData) {
        try {
            DeptAgencyInfoEntity deptAgencyInfo = this.deptDB.getDeptAgencyInfo(agencyBasicData.getId());
            if (deptAgencyInfo == null) {
                deptAgencyInfo = this.initDeptAgencyInfo(agencyBasicData.getId());
            }

            Date dept_date = DateTimeUtils.getNow();

            /**
             * dept_agency_date
             */
            DeptAgencyDateEntity deptAgencyDateEntity = this.deptDB.getDeptAgencyDate(agencyBasicData.getId(), dept_date);
            if (deptAgencyDateEntity == null) {
                deptAgencyDateEntity = new DeptAgencyDateEntity();
                deptAgencyDateEntity.setAgency_id(agencyBasicData.getId());
                deptAgencyDateEntity.setDept_cycle_start(deptAgencyInfo.getDept_cycle_start());
                deptAgencyDateEntity.setStatus(ActiveStatus.ACTIVATED.getValue());
                deptAgencyDateEntity.setDept_date(dept_date);
                deptAgencyDateEntity.setCreated_date(dept_date);
            }

            /**
             * Cập nhật công nợ cuối ngày cho ngày trước
             */
            deptAgencyDateEntity.setNgd(deptAgencyInfo.getNgd());
            deptAgencyDateEntity.setNqh(deptAgencyInfo.getNqh());
            deptAgencyDateEntity.setNth(deptAgencyInfo.getNth());
            deptAgencyDateEntity.setNx(deptAgencyInfo.getNx());
            deptAgencyDateEntity.setNdh(deptAgencyInfo.getNdh());
            deptAgencyDateEntity.setTt(deptAgencyInfo.getTt());
            deptAgencyDateEntity.setTotal_tt_cycle(deptAgencyInfo.getTotal_tt_cycle());
            deptAgencyDateEntity.setDtt(deptAgencyInfo.getDtt());
            deptAgencyDateEntity.setTotal_dtt_cycle(deptAgencyInfo.getTotal_dtt_cycle());

            /**
             * công nợ cuối kỳ
             */
            deptAgencyDateEntity.setDept_cycle_end(deptAgencyInfo.getCurrent_dept());

            /**
             * tổng mua hàng
             */
            Long total_price_order = this.orderDB.getTotalPriceOrderFinishToday(deptAgencyInfo.getAgency_id());
            deptAgencyDateEntity.setTotal_price_order(total_price_order);

            /**
             * tổng thanh toán
             */
            Long total_price_payment = this.deptDB.getTotalPaymentToday(deptAgencyInfo.getAgency_id());
            deptAgencyDateEntity.setTotal_price_payment(total_price_payment);

            int rsInsert = this.deptDB.saveDeptAgencyDate(deptAgencyDateEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    private Date getPrevDate(Date now) {
        Date dept_date_create = DateTimeUtils.getDateTime(now, "yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dept_date_create);

        calendar.add(Calendar.DATE, -1);
        Date rs = calendar.getTime();
        return rs;
    }

    public ClientResponse editDeptSetting(SessionData sessionData, EditDeptSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            DeptSettingEntity deptSettingEntity = this.deptDB.getDeptSetting(request.getId());
            if (deptSettingEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (DeptSettingStatus.WAITING.getId() != deptSettingEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            /**
             * Không điều chỉnh đại lý
             */
            deptSettingEntity.setDept_cycle(request.getDept_cycle());
            deptSettingEntity.setDept_limit(request.getDept_limit());
            deptSettingEntity.setNgd_limit(request.getNgd_limit());
            deptSettingEntity.setStatus(DeptSettingStatus.WAITING.getId());
            deptSettingEntity.setModified_date(DateTimeUtils.getNow());
            deptSettingEntity.setModifier_id(sessionData.getId());
            deptSettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            deptSettingEntity.setEnd_date(request.getEnd_date() != null ? DateTimeUtils.getDateTime(request.getEnd_date()) : null);
            deptSettingEntity.setNote(request.getNote());
            boolean rsUpdate = this.deptDB.updateDeptSetting(deptSettingEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse rejectDeptSetting(SessionData sessionData, RejectDeptSettingRequest request) {
        try {
            for (Integer dept_setting_id : request.getIds()) {
                DeptSettingEntity deptSettingEntity = this.deptDB.getDeptSetting(dept_setting_id);
                if (deptSettingEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                if (DeptSettingStatus.WAITING.getId() != deptSettingEntity.getStatus()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                }

                /**
                 * Cập nhật trạng thái giao dịch
                 */
                boolean rsRejectDeptSetting = this.deptDB.rejectDeptSetting(deptSettingEntity.getId(), request.getNote());
                if (!rsRejectDeptSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editDeptCycleOfDeptOrder(SessionData sessionData, EditDeptCycleOfDeptOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            DeptOrderEntity deptOrderEntity = this.deptDB.getDeptOrder(request.getDept_order_id());
            if (deptOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (deptOrderEntity.getDept_cycle() == request.getDept_cycle()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_CYCLE_NEW_NOT_EQUAL_DEPT_CYCLE_OLD);
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(deptOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, deptOrderEntity.getAgency_id());
            }

            Date payment_deadline = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());

            /**
             * Công nợ chưa thanh toán hoặc chưa quá hạn
             */
            if (deptOrderEntity.getPayment_value().longValue() > 0
                    || payment_deadline.before(DateTimeUtils.getNow())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_DEPT_CYCLE_OF_DEPT_ORDER_INVALID);
            }

            deptOrderEntity.setDept_cycle(request.getDept_cycle());
            deptOrderEntity.setModifier_id(sessionData.getId());
            deptOrderEntity.setModified_date(DateTimeUtils.getNow());

            boolean rsEdit = this.deptDB.editDeptCycleOfDeptOrder(
                    deptOrderEntity.getId(),
                    deptOrderEntity.getDept_cycle(),
                    deptOrderEntity.getModifier_id(),
                    deptOrderEntity.getModified_date()
            );
            if (!rsEdit) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * nếu quá hạn
             */
            Date payment_deadline_new = this.getDeptOrderPaymentDeadline(deptOrderEntity.getDept_time(), deptOrderEntity.getDept_cycle());
            if (deptOrderEntity.getIs_nqh() == 0 &&
                    payment_deadline_new.before(DateTimeUtils.getNow())) {
                this.orderDB.setDeptOrderNQH(deptOrderEntity.getId());

                /**
                 * Phạt acoin
                 */
                this.punishAcoin(deptOrderEntity.getId());
            }

            /**
             * Cập nhật lại công nợ hiện tại
             */
            ClientResponse crUpdateDeptAgencyInfo = this.updateDeptAgencyInfo(deptOrderEntity.getAgency_id());
            if (crUpdateDeptAgencyInfo.failed()) {
                return crUpdateDeptAgencyInfo;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void punishAcoin(int dept_order_id) {
        try {
            DeptOrderEntity deptOrderEntity = this.deptDB.getDeptOrder(dept_order_id);
            if (deptOrderEntity == null) {
                return;
            }

            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(
                    deptOrderEntity.getAgency_id()
            );
            if (agencyEntity == null) {
                return;
            }

            int acoin_rate_default = this.dataManager.getConfigManager().getAcoinRateDefault();

            /**
             * Tính toán acoin giảm
             */
            long total_value_dept_acoin = deptOrderEntity.getTransaction_value()
                    - deptOrderEntity.getPayment_value();
            int acoin_value = ConvertUtils.toInt(total_value_dept_acoin / acoin_rate_default);
            int acoin_punish_value = 0;
            if (acoin_value > agencyEntity.getCurrent_point().intValue()) {
                acoin_punish_value = agencyEntity.getCurrent_point().intValue();
            } else {
                acoin_punish_value = acoin_value;
            }

            /**
             * Giảm trừ acoin của công nợ đơn hàng
             */
            boolean rsDecreaseAcoinDeptOrder = this.aCoinDB.decreaseACoinDeptOrder(
                    deptOrderEntity.getId(),
                    acoin_value);
            if (rsDecreaseAcoinDeptOrder) {
                /**
                 * Thông báo telegram
                 */
            }

            /**
             * Giảm trừ acoin của đại lý
             */
            ClientResponse rsDecreaseACoin = this.decreaseACoin(
                    deptOrderEntity.getAgency_id(),
                    acoin_punish_value,
                    "Nợ quá hạn: " + deptOrderEntity.getNote() + "-" + this.appUtils.priceFormat(total_value_dept_acoin),
                    DateTimeUtils.getNow());
            if (rsDecreaseACoin.failed()) {
                this.alertToTelegram(
                        "Trừ Acoin đối với các công nợ quá hạn:" +
                                " agency_id-" + deptOrderEntity.getAgency_id() +
                                " dept_time-" + deptOrderEntity.getDept_time() +
                                " dept_cycle-" + deptOrderEntity.getDept_cycle() +
                                " transaction_value-" + deptOrderEntity.getTransaction_value() +
                                " dept_order_id-" + deptOrderEntity.getId() +
                                " FAIL",
                        ResponseStatus.FAIL
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
    }

    public ClientResponse getDeptAgencyInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject deptInfo = this.getDeptInfo(request.getId());
            JSONObject data = new JSONObject();
            data.put("deptInfo", deptInfo);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runIncreaseDeptForOrderSchedule() {
        try {
            List<JSONObject> orders = this.orderDB.getListOrderHenGiao(OrderStatus.SHIPPING.getKey());
            for (JSONObject order : orders) {
                /**
                 * Ghi nhận công nợ
                 */
                Date confirm_prepare_date = DateTimeUtils.getDateTime(
                        ConvertUtils.toString(order.get("confirm_prepare_date").toString()), "yyyy-MM-dd");
                Date request_delivery_date = DateTimeUtils.getDateTime(
                        ConvertUtils.toString(order.get("request_delivery_date").toString()), "yyyy-MM-dd");
                if (IncreaseDeptStatus.YES.getValue() !=
                        ConvertUtils.toInt(order.get("increase_dept"))
                        && AppUtils.isHenGiao(
                        confirm_prepare_date,
                        request_delivery_date,
                        this.dataManager.getConfigManager().getNumberDateScheduleDelivery())
                ) {
                    int id = ConvertUtils.toInt(order.get("id"));
                    int agency_id = ConvertUtils.toInt(order.get("agency_id"));
                    String order_code = ConvertUtils.toString(order.get("code"));
                    long total_end_price = ConvertUtils.toLong(order.get("total_end_price"));
                    Integer dept_cycle = order.get("dept_cycle") == null ? null : ConvertUtils.toInt(order.get("dept_cycle"));
                    DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                            this.dataManager.getConfigManager().getHanMucDonHangHenGiao();
                    if (deptTransactionSubTypeEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    JSONObject orderNormalDept = null;
                    int total = ConvertUtils.toInt(order.get("total"));
                    if (total == 0) {
                        orderNormalDept = this.orderDB.getOrderNormalDept(
                                id
                        );
                        if (orderNormalDept == null) {
                            int rsInsertAgencyOrderNormal = this.orderDB.insertAgencyOrderDept(
                                    id,
                                    AgencyOrderDeptType.NORMAL.getId(),
                                    0,
                                    ConvertUtils.toInt(order.get("dept_cycle")),
                                    ConvertUtils.toInt(order.get("total_begin_price")),
                                    ConvertUtils.toInt(order.get("total_promotion_price")),
                                    ConvertUtils.toInt(order.get("total_end_price")),
                                    0,
                                    "[]",
                                    ConvertUtils.toInt(order.get("total_promotion_product_price")),
                                    ConvertUtils.toInt(order.get("total_promotion_order_price")),
                                    ConvertUtils.toInt(order.get("total_promotion_order_price_ctkm")),
                                    ConvertUtils.toInt(order.get("total_refund_price")),
                                    0,
                                    0
                            );
                            if (rsInsertAgencyOrderNormal <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                            orderNormalDept = this.orderDB.getOrderNormalDept(
                                    id
                            );
                        }

                        total = 1;
                        this.orderDB.updateTotalOrder(
                                id,
                                total
                        );
                    }

                    /**
                     * Ghi nhận công nợ thường
                     */
                    if (orderNormalDept != null) {
                        ClientResponse clientResponse = this.ghiNhanCongNoDonHang(
                                id,
                                agency_id,
                                total_end_price,
                                order_code,
                                deptTransactionSubTypeEntity.getId(),
                                0,
                                DateTimeUtils.getNow(),
                                dept_cycle,
                                0,
                                this.appUtils.getAgencyOrderDeptCode(
                                        order_code,
                                        0,
                                        total,
                                        ConvertUtils.toInt(orderNormalDept.get("promo_id")) != 0,
                                        true
                                )
                        );
                        if (clientResponse.failed()) {
                            this.alertToTelegram("runIncreaseDeptForOrderSchedule: " + order_code + " - " + clientResponse.getMessage(),
                                    ResponseStatus.FAIL);
                        }
                    }

                    /**
                     * Ghi nhận công nợ săn sale
                     */
                    List<JSONObject> agencyOrderDeptList = this.orderDB.getAgencyOrderDeptHuntSaleList(
                            id);
                    for (int iDept = 0; iDept < agencyOrderDeptList.size(); iDept++) {
                        JSONObject agencyOrderDept = agencyOrderDeptList.get(iDept);
                        String dept_code = this.appUtils.getAgencyOrderDeptCode(
                                order_code,
                                ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                                total,
                                ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                                orderNormalDept != null
                        );

                        ClientResponse crGhiNhanCongNoHuntSale = this.ghiNhanCongNoDonHang(
                                id,
                                agency_id,
                                ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                                order_code,
                                deptTransactionSubTypeEntity.getId(),
                                0,
                                DateTimeUtils.getNow(),
                                ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")),
                                ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                                dept_code
                        );
                        if (crGhiNhanCongNoHuntSale.failed()) {
                            return crGhiNhanCongNoHuntSale;
                        }
                    }
                }
            }

            this.reportToTelegram(
                    "Ghi nhận công nợ đơn hẹn giao: FINISH",
                    ResponseStatus.SUCCESS
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterDeptAgencyHistory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_AGENCY_HISTORY, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.deptDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
            }


            if (request.getId() != 0) {
                Date now = DateTimeUtils.getNow();
                PromoTimeRequest timeRequest = this.filterUtils.getValueByTime(request.getFilters(), "created_date");
                if (timeRequest == null) {
                    timeRequest = new PromoTimeRequest();
                }

                long dtt = this.deptDB.getTotalDttByAgency(
                        request.getAgency_id(),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getStart_date_millisecond()),
                                "yyyy-MM-dd") + " 00:00:00"),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getEnd_date_millisecond()),
                                "yyyy-MM-dd") + " 23:59:59")
                );
                long sttt = this.deptDB.getTotalTtByAgency(
                        request.getAgency_id(),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getStart_date_millisecond()),
                                "yyyy-MM-dd") + " 00:00:00"),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getEnd_date_millisecond()),
                                "yyyy-MM-dd") + " 23:59:59")
                );
                data.put("dtt", dtt);
                data.put("sttt", sttt);
            }
            int total = this.deptDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runMissCommit() {
        try {
            List<JSONObject> commits = this.deptDB.getListCommitDoing();
            for (JSONObject commit : commits) {
                /**
                 * cập nhật sai cam kết
                 */
                int agency_id = ConvertUtils.toInt(commit.get("agency_id"));

                JSONObject dept_agency_info = this.deptDB.getDeptInfo(agency_id);
                if (dept_agency_info == null) {
                    this.alertToTelegram(
                            "Tính toán dữ liệu sai cam kết: agency - " + agency_id + " FAILED - Đại lý chưa có thông tin công nợ",
                            ResponseStatus.FAIL);
                }

                int miss_commit = ConvertUtils.toInt(dept_agency_info.get("miss_commit"));
                int commit_limit = ConvertUtils.toInt(dept_agency_info.get("commit_limit"));

                if (miss_commit < commit_limit) {
                    boolean rsMissCommit = this.deptDB.increaseMissCommit(agency_id);
                    if (!rsMissCommit) {
                        this.alertToTelegram(
                                "Tính toán dữ liệu sai cam kết: agency - " + agency_id + " FAILED",
                                ResponseStatus.FAIL);
                    }
                }

                this.orderDB.setMissCommit(ConvertUtils.toInt(commit.get("id")));

                /**
                 * Lưu lịch sử
                 * type - 1 sai cam kết
                 */
                this.saveAgencyMissCommitHistory(
                        agency_id,
                        commit_limit,
                        commit_limit < miss_commit ? 0 :
                                (commit_limit - miss_commit),
                        commit_limit < (miss_commit + 1) ? 0 :
                                (commit_limit - (miss_commit + 1)),
                        ConvertUtils.toInt(commit.get("order_id")),
                        ConvertUtils.toString(commit.get("code")),
                        1,
                        0
                );
            }

            this.reportToTelegram(
                    "Tính toán dữ liệu sai cam kết: FINISH",
                    ResponseStatus.SUCCESS);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveAgencyMissCommitHistory(
            int agency_id,
            int commit_limit,
            int before_value,
            int after_value,
            int order_id,
            String code,
            int type,
            int number_day_nqh_miss_commit) {
        try {
            this.agencyDB.saveAgencyMissCommitHistory(
                    agency_id,
                    commit_limit,
                    before_value,
                    after_value,
                    order_id,
                    code,
                    type,
                    number_day_nqh_miss_commit
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
    }

    public ClientResponse getListDeptOrderByAgency(FilterListByIdRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("t_agency.id");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            filterRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_ORDER, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.deptDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                if (ConvertUtils.toInt(jsonObject.get("dept_type_id")) == DeptType.DEPT_DON_HANG.getId()) {
                    jsonObject.put("dept_type_data",
                            ConvertUtils.toString(jsonObject.get("code")));
                } else {
                    jsonObject.put("dept_type_data", "");
                }
            }
            JSONObject data = new JSONObject();
            if (request.getId() != 0) {
                Date now = DateTimeUtils.getNow();
                PromoTimeRequest timeRequest = this.filterUtils.getValueByTime(request.getFilters(), "created_date");
                if (timeRequest == null) {
                    timeRequest = new PromoTimeRequest();
                }

                if (timeRequest.getStart_date_millisecond() == 0 && timeRequest.getEnd_date_millisecond() == 0) {
                    timeRequest.setStart_date_millisecond(
                            this.getFirstDateOfYear().getTime()
                    );

                    timeRequest.setEnd_date_millisecond(
                            now.getTime()
                    );
                }

                long dtt = this.deptDB.getTotalDttByAgency(
                        request.getId(),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getStart_date_millisecond()),
                                "yyyy-MM-dd") + " 00:00:00"),
                        timeRequest.getEnd_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getEnd_date_millisecond()),
                                "yyyy-MM-dd") + " 23:59:59")
                );
                long so_tien_thanh_toan = this.deptDB.getTotalTtByAgency(
                        request.getId(),
                        timeRequest.getStart_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getStart_date_millisecond()),
                                "yyyy-MM-dd") + " 00:00:00"),
                        timeRequest.getEnd_date_millisecond() == 0 ?
                                "" : (DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(timeRequest.getEnd_date_millisecond()),
                                "yyyy-MM-dd") + " 23:59:59")
                );
                data.put("dtt", dtt);
                data.put("so_tien_thanh_toan", so_tien_thanh_toan);
            }
            int total = this.deptDB.getTotal(query);
            data.put("records", records);
            data.put("total", null);
            data.put("total_page", this.appUtils.getTotalPage(total));
            data.put("total_money", null);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterDeptDttHistory(SessionData sessionData, FilterListByIdRequest request) {
        try {
            if (request.getId() != 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setValue(ConvertUtils.toString(request.getId()));
                filterRequest.setType(TypeFilter.SELECTBOX);
                filterRequest.setKey("agency_id");
                request.getFilters().add(filterRequest);
            }

            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_DTT_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.deptDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));

                if (!ConvertUtils.toString(jsonObject.get("code")).isEmpty() &&
                        ConvertUtils.toString(jsonObject.get("code")).split("-").length == 2) {
                    String transaction_type = ConvertUtils.toString(jsonObject.get("code")).split("-")[0];
                    if (TransactionType.DON_HANG.getKey().equals(transaction_type)) {
                        jsonObject.put("code", ConvertUtils.toString(jsonObject.get("code")).split("-")[1]);
                    } else if (TransactionType.CNO.getKey().equals(transaction_type)) {
                        jsonObject.put("code", TransactionType.CNO.getKey() + ConvertUtils.toString(jsonObject.get("code")).split("-")[1]);
                    } else if (TransactionType.HBTL.getKey().equals(transaction_type)) {
                        jsonObject.put("code", TransactionType.HBTL.getKey() + ConvertUtils.toString(jsonObject.get("code")).split("-")[1]);
                    } else if (TransactionType.DIEU_CHINH_DTT.getKey().equals(transaction_type)) {
                        jsonObject.put("code", TransactionType.DIEU_CHINH_DTT.getKey() + ConvertUtils.toString(jsonObject.get("code")).split("-")[1]);
                    }
                }
            }

            int total = this.deptDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editDeptDtt(
            SessionData sessionData,
            EditDeptDttRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
            }

            long transaction_value = (request.getData() *
                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1));
            /**
             * Lưu lệnh điều chỉnh
             */
            int agency_dept_dtt_id = this.deptDB.insertAgencyDeptDtt(
                    request.getAgency_id(),
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    deptAgencyInfoEntity.getTotal_dtt_cycle(),
                    deptAgencyInfoEntity.getTotal_dtt_cycle() +
                            (request.getData() *
                                    (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1)),
                    request.getNote()
            );
            if (agency_dept_dtt_id <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String code = "DTT" + agency_dept_dtt_id;
            this.deptDB.updateCodeForAgencyDeptDtt(
                    agency_dept_dtt_id,
                    code
            );

            boolean rsEditDeptDtt = this.deptDB.editDeptDtt(
                    deptAgencyInfoEntity.getAgency_id(),
                    request.getData() * (request.getType() == ChangeValueType.INCREASE.getId() ? 1 : -1),
                    sessionData.getId(),
                    DateTimeUtils.toString(DateTimeUtils.getNow())
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
                    sessionData.getId(),
                    DateTimeUtils.toString(DateTimeUtils.getNow()),
                    "Điều chỉnh",
                    code
            );

            if (rsSaveDeptDttHistory <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Ghi nhận tích lũy
             */
            this.callGhiNhanTichLuyDTT(
                    request.getAgency_id(),
                    agency_dept_dtt_id,
                    transaction_value
            );

            /**
             * Ghi nhận tích lũy CTXH
             */

            this.callGhiNhanCTXH(
                    CTXHTransactionType.DIEU_CHINH_DTT.getKey(),
                    request.getAgency_id(),
                    code,
                    0,
                    agency_dept_dtt_id,
                    1,
                    0,
                    0,
                    0,
                    (request.getData() *
                            (ChangeValueType.INCREASE.getId() == request.getType() ? 1 : -1))
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void callGhiNhanTichLuyDTT(
            int agency_id, int agency_dept_dtt_id,
            long transaction_value) {
        this.accumulateService.autoAddTransactionOrder(
                CTTLTransactionType.DIEU_CHINH_DTT.getKey(),
                agency_id,
                "DTT" + agency_dept_dtt_id,
                0,
                transaction_value,
                agency_dept_dtt_id,
                CTTLTransactionSource.AUTO.getId(),
                0,
                0, 0
        );
    }

    public ClientResponse estimateDeptInfo(SessionData sessionData) {
        try {
            List<JSONObject> agencyList = this.agencyDB.getAllAgencyActive();
            for (JSONObject js : agencyList) {
                this.updateDeptAgencyInfo(ConvertUtils.toInt(js.get("id")));
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runDeptOrderNQH() {
        try {
            Date today = DateTimeUtils.getNow();
            List<JSONObject> records = this.deptDB.getListDeptOrderNoneNQH();
            for (JSONObject jsDeptOrder : records) {
                DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(jsDeptOrder),
                        DeptOrderEntity.class);
                Date payment_deadline = this.getDeptOrderPaymentDeadline(
                        deptOrderEntity.getDept_time(),
                        deptOrderEntity.getDept_cycle()
                );

                if (payment_deadline.after(today)) {
                    continue;
                }

                this.orderDB.setDeptOrderNQH(deptOrderEntity.getId());
            }

            this.reportToTelegram(
                    "Lưu công nợ quá hạn: FINISH",
                    ResponseStatus.SUCCESS
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse ghiNhanCongNoDonHenGiao(BasicRequest request, SessionData sessionData) {
        try {
            /**
             * Ghi nhận công nợ
             */
            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(
                    request.getId());
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                    this.dataManager.getConfigManager().getHanMucDonHangHenGiao();
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int total = agencyOrderEntity.getTotal();
            if (total == 0) {
                int rsInsertAgencyOrderNormal = this.orderDB.insertAgencyOrderDept(
                        agencyOrderEntity.getId(),
                        AgencyOrderDeptType.NORMAL.getId(),
                        0,
                        agencyOrderEntity.getDept_cycle(),
                        agencyOrderEntity.getTotal_begin_price(),
                        agencyOrderEntity.getTotal_promotion_price(),
                        agencyOrderEntity.getTotal_end_price(),
                        0,
                        "[]",
                        agencyOrderEntity.getTotal_promotion_product_price(),
                        agencyOrderEntity.getTotal_promotion_order_price(),
                        agencyOrderEntity.getTotal_promotion_order_price_ctkm(),
                        agencyOrderEntity.getTotal_refund_price(),
                        0, 0
                );
                if (rsInsertAgencyOrderNormal <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                total = 1;
                this.orderDB.updateTotalOrder(agencyOrderEntity.getId(), total);
            }

            /**
             * Ghi nhận công nợ đơn thường
             */
            JSONObject orderNormalDept = this.orderDB.getOrderNormalDept(
                    agencyOrderEntity.getId()
            );
            if (orderNormalDept != null) {
                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        agencyOrderEntity.getCode(),
                        0,
                        total,
                        false,
                        true
                );
                ClientResponse crGhiNhanCongNo = this.ghiNhanCongNoDonHang(
                        agencyOrderEntity.getId(),
                        agencyOrderEntity.getAgency_id(),
                        ConvertUtils.toInt(orderNormalDept.get("total_end_price")),
                        agencyOrderEntity.getCode(),
                        deptTransactionSubTypeEntity.getId(),
                        sessionData.getId(),
                        DateTimeUtils.getNow(),
                        ConvertUtils.toInt(orderNormalDept.get("dept_cycle")),
                        0,
                        dept_code
                );
                if (crGhiNhanCongNo.failed()) {
                    return crGhiNhanCongNo;
                }
            }

            /**
             * Ghi nhận công nợ săn sale
             */
            List<JSONObject> agencyOrderDeptList = this.orderDB.getAgencyOrderDeptHuntSaleList(agencyOrderEntity.getId());
            for (int iDept = 0; iDept < agencyOrderDeptList.size(); iDept++) {
                JSONObject agencyOrderDept = agencyOrderDeptList.get(iDept);
                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        agencyOrderEntity.getCode(),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        agencyOrderEntity.getTotal(),
                        ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                        orderNormalDept != null
                );

                ClientResponse crGhiNhanCongNoHuntSale = this.ghiNhanCongNoDonHang(
                        agencyOrderEntity.getId(),
                        agencyOrderEntity.getAgency_id(),
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                        agencyOrderEntity.getCode(),
                        deptTransactionSubTypeEntity.getId(),
                        sessionData.getId(),
                        DateTimeUtils.getNow(),
                        ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        dept_code
                );
                if (crGhiNhanCongNoHuntSale.failed()) {
                    return crGhiNhanCongNoHuntSale;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveDeptTransactionByBravo(int id) {
        try {
            ClientResponse crApproveDeptTransaction = this.approveDeptTransactionOne(
                    id,
                    0,
                    null,
                    null
            );
            if (crApproveDeptTransaction.failed()) {
                return crApproveDeptTransaction;
            }

            /**
             * Tích lũy nhiệm vụ
             */
            this.accumulateTichLuyNhiemVu(id);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void accumulateTichLuyNhiemVu(int id) {
        try {
            DeptTransactionEntity deptTransactionEntity = this.deptDB.getDeptTransaction(id);
            if (deptTransactionEntity == null) {
                return;
            }

            if (deptTransactionEntity.getDept_transaction_sub_type_id() == DeptConstants.DEPT_TRANSACTION_THANH_TOAN) {
                this.callTichLuyThanhToan(
                        deptTransactionEntity.getAgency_id(),
                        MissionTransactionType.GIAM_CONG_NO,
                        deptTransactionEntity.getDoc_no(),
                        deptTransactionEntity.getTransaction_value()
                );
            } else if (deptTransactionEntity.getDept_transaction_main_type_id() == DeptTransactionMainType.DECREASE.getId()) {
                if (TransactionEffectValueType.DECREASE.getCode().equals(deptTransactionEntity.getTt_effect_type())) {
                    this.callTichLuyThanhToan(
                            deptTransactionEntity.getAgency_id(),
                            MissionTransactionType.GIAM_CONG_NO,
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getTransaction_value() * -1
                    );
                } else if (TransactionEffectValueType.INCREASE.getCode().equals(deptTransactionEntity.getTt_effect_type())) {
                    this.callTichLuyThanhToan(
                            deptTransactionEntity.getAgency_id(),
                            MissionTransactionType.GIAM_CONG_NO,
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getTransaction_value()
                    );
                }
            } else if (deptTransactionEntity.getDept_transaction_main_type_id() == DeptTransactionMainType.INCREASE.getId()) {
                if (TransactionEffectValueType.DECREASE.getCode().equals(deptTransactionEntity.getTt_effect_type())) {
                    this.callTichLuyThanhToan(
                            deptTransactionEntity.getAgency_id(),
                            MissionTransactionType.TANG_CONG_NO,
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getTransaction_value() * -1
                    );
                } else if (TransactionEffectValueType.INCREASE.getCode().equals(deptTransactionEntity.getTt_effect_type())) {
                    this.callTichLuyThanhToan(
                            deptTransactionEntity.getAgency_id(),
                            MissionTransactionType.TANG_CONG_NO,
                            deptTransactionEntity.getDoc_no(),
                            deptTransactionEntity.getTransaction_value()
                    );
                }
            }

            /* tích lũy chiết khấu sau */
            if (deptTransactionEntity.getDept_transaction_sub_type_id() == DeptConstants.DEPT_TRANSACTION_CHIET_KHAU_SAU) {
                this.callTichLuyChietKhauSau(
                        deptTransactionEntity.getId(),
                        deptTransactionEntity.getAgency_id(),
                        deptTransactionEntity.getDoc_no()
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
    }

    public ClientResponse updateDeptAgencySetting(int agency_id) {
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.initDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runNQHOver() {
        try {
            Date today = DateTimeUtils.getNow();
            List<JSONObject> rsNQHNonePayment =
                    this.deptDB.getListNQHNonePayment();
            for (JSONObject jsNQH : rsNQHNonePayment) {
                int agency_id = ConvertUtils.toInt(jsNQH.get("agency_id"));
                JSONObject agency_info =
                        this.agencyDB.getAgencyInfoById(agency_id);
                if (agency_info == null) {
                    continue;
                }
                int number_day_nqh_miss_commit = ConvertUtils.toInt(agency_info.get("number_day_nqh_miss_commit"));

                if (number_day_nqh_miss_commit == 0) {
                    continue;
                }

                if (jsNQH.get("nqh_date") == null) {
                    continue;
                }

                Date payment_deadline = this.appUtils.getDateAfterDay(
                        AppUtils.convertJsonToDate(jsNQH.get("nqh_date")),
                        number_day_nqh_miss_commit
                );

                if (payment_deadline.after(today)) {
                    continue;
                }

                /**
                 * cập nhật sai cam kết
                 */
                JSONObject dept_agency_info = this.deptDB.getDeptInfo(agency_id);
                if (dept_agency_info == null) {
                    this.alertToTelegram(
                            "Tính toán dữ liệu sai cam kết: agency - " + agency_id + " FAILED - Đại lý chưa có thông tin công nợ",
                            ResponseStatus.FAIL);
                }

                int miss_commit = ConvertUtils.toInt(dept_agency_info.get("miss_commit"));
                int commit_limit = ConvertUtils.toInt(dept_agency_info.get("commit_limit"));
                if (miss_commit < commit_limit) {
                    boolean rsMissCommit = this.deptDB.increaseMissCommit(agency_id);
                    if (!rsMissCommit) {
                        this.alertToTelegram(
                                "Tính toán dữ liệu sai cam kết: agency - " + agency_id + " FAILED",
                                ResponseStatus.FAIL);
                    }
                }

                this.deptDB.updateNQHToOver(
                        ConvertUtils.toInt(jsNQH.get("id"))
                );

                /**
                 * Lưu lịch sử
                 * type - 1 sai cam kết
                 */
                this.saveAgencyMissCommitHistory(
                        agency_id,
                        commit_limit,
                        commit_limit < miss_commit ? 0 :
                                (commit_limit - miss_commit),
                        commit_limit < (miss_commit + 1) ? 0 :
                                (commit_limit - (miss_commit + 1)),
                        ConvertUtils.toInt(jsNQH.get("id")),
                        ConvertUtils.toString(jsNQH.get("code")),
                        2,
                        number_day_nqh_miss_commit
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runCheckDTTHistory() {
        try {
            String year = this.appUtils.getYear(new Date());
            List<JSONObject> agencyList = this.deptDB.checkDTTHistory(
                    year
            );
            if (!agencyList.isEmpty()) {
                this.alertToTelegram(
                        "runCheckDTTHistory: " +
                                JsonUtils.Serialize(agencyList),
                        ResponseStatus.FAIL
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void callGhiNhanCTXH(String type,
                                 int agency_id,
                                 String code,
                                 int order_id,
                                 int transaction_id,
                                 int transaction_source,
                                 int staff_id,
                                 long order_time,
                                 long dept_time,
                                 long value) {
        this.accumulateCTXHService.addTransaction(
                type,
                agency_id,
                code,
                order_id,
                transaction_id,
                transaction_source,
                staff_id,
                order_time,
                dept_time,
                value
        );
    }

    public ClientResponse runNQHTichLuyNhiemVu() {
        try {
            Date now = new Date();
            if (this.checkNgayCuoiMission(DateTimeUtils.getDateTime(
                    DateTimeUtils.toString(now, "yyyyy-MM-dd HH:mm") + ":00",
                    "yyyy-MM-dd HH:mm:ss"))) {
                return ResponseConstants.success;
            }

            List<JSONObject> jsAgencyList = this.deptDB.getListAgencyHasNQH();
            for (JSONObject jsAgency : jsAgencyList) {
                DeptAgencyInfoEntity deptAgencyInfoEntity = DeptAgencyInfoEntity.from(jsAgency);
                if (deptAgencyInfoEntity == null) {
                    this.alertToTelegram("runNQHTichLuyNhiemVu: " + JsonUtils.Serialize(jsAgency), ResponseStatus.EXCEPTION);
                }
                int agency_id = deptAgencyInfoEntity.getAgency_id();
                long no_goi_dau = 0;
                long no_xau = 0;
                long no_qua_han = 0;
                long no_trong_han = 0;
                long no_den_han = 0;
                List<JSONObject> deptOrderPaymentNoneList = this.deptDB.getDeptOrderPaymentNone(agency_id);
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
                            if (!this.checkAccumulateMisssion(ConvertUtils.toInt(deptOrderPaymentNone.get("accumulate_mission_nqh")))) {
                                this.accumulateTichLuyNhiemVuNQH(
                                        ConvertUtils.toInt(deptOrderPaymentNone.get("agency_id")),
                                        ConvertUtils.toString(deptOrderPaymentNone.get("doc_no")),
                                        ConvertUtils.toInt(deptOrderPaymentNone.get("dept_type_id")),
                                        no_con_lai);
                            }
                        } else if (this.isNoDenHan(payment_deadline)) {
                            no_den_han += no_con_lai;
                            no_trong_han += no_con_lai;
                        } else if (this.isNoTrongHan(payment_deadline)) {
                            no_trong_han += no_con_lai;
                        }
                    }
                }
            }

            this.reportToTelegram(
                    "Reset công nợ đầu ngày: FINISH",
                    ResponseStatus.SUCCESS
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkNgayCuoiMission(Date time) {
        try {
            List<Integer> missionPeriodRunningList =
                    this.dataManager.getConfigManager().getListMissionPeriodRunning();
            for (Integer mpr : missionPeriodRunningList) {
                JSONObject jsMissionPeriodRunning = this.dataManager.getConfigManager().getMissionPeriodRunningTime(mpr);
                if (jsMissionPeriodRunning == null) {
                    return false;
                }
                if (DateTimeUtils.getDateTime(time, "yyyy-MM-dd").before(
                        DateTimeUtils.getDateTime(jsMissionPeriodRunning.get("end_date").toString(), "yyyy-MM-dd")
                )) {
                    return false;
                } else if (DateTimeUtils.toString(time, "HH").equals(MissionConstants.TIME_CHECK_NQH)) {
                    return false;
                } else {
                    return true;
                }
            }

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return false;
    }

    private void accumulateTichLuyNhiemVuNQH(int agency_id,
                                             String doc_no,
                                             int dept_type_id,
                                             long value) {
        try {
            this.accumulateMissionService.tichLuyTuDongNoQuaHan(
                    DeptType.DEPT_DON_HANG.getId() == dept_type_id ? MissionTransactionType.DON_HANG : MissionTransactionType.TANG_CONG_NO,
                    agency_id,
                    doc_no,
                    value
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
    }

    private boolean checkAccumulateMisssion(int accumulateMissionNqh) {
        if (accumulateMissionNqh == 1) {
            return true;
        }
        return false;
    }

    protected void callTichLuyThanhToan(
            int agency_id,
            MissionTransactionType missionTransactionType,
            String code,
            long value
    ) {
        try {
            accumulateMissionService.tichLuyTuDongThanhToan(
                    agency_id,
                    missionTransactionType,
                    code,
                    value
            );
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    public void callTichLuyChietKhauSau(
            int dept_transaction_id,
            int agency_id,
            String code

    ) {
        try {
            JSONObject jsDetailData = this.deptDB.getDeptTransactionDetailData(dept_transaction_id);
            if (jsDetailData == null || jsDetailData.get("detail_data") == null) {
                return;
            }
            List<DeptTransactionDetailData> deptTransactionDetailDataList =
                    JsonUtils.DeSerialize(jsDetailData.get("detail_data").toString(),
                            new TypeToken<List<DeptTransactionDetailData>>() {
                            }.getType());
            List<MissionCKSData> data = new ArrayList<>();
            deptTransactionDetailDataList.forEach(d -> {
                MissionCKSData missionCKSData = new MissionCKSData();
                missionCKSData.setOrder_code(d.getDocNoApp_SO());
                JSONObject product = this.dataManager.getProductManager().getProductByCode(d.getItemCode());
                if (product != null) {
                    missionCKSData.setProduct_id(ConvertUtils.toInt(product.get("id")));
                    missionCKSData.setProduct_dtt(d.getAmount());
                    data.add(missionCKSData);
                }
            });
            accumulateMissionService.tichLuyTuDongChietKhauSau(
                    agency_id,
                    code,
                    data
            );
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
    }

    public ClientResponse acceptAccumulateCKS(SessionData sessionData, BasicRequest request) {
        try {
            DeptTransactionEntity deptTransactionEntity = this.deptDB.getDeptTransaction(request.getId());
            if (deptTransactionEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.callTichLuyChietKhauSau(
                    deptTransactionEntity.getId(),
                    deptTransactionEntity.getAgency_id(),
                    deptTransactionEntity.getDoc_no()
            );
        } catch (Exception ex) {
            LogUtil.printDebug("ACCUMULATE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}