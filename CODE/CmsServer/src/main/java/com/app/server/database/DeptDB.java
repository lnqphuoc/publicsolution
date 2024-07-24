package com.app.server.database;

import com.app.server.data.entity.*;
import com.app.server.database.repository.*;
import com.app.server.enums.*;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeptDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    public List<JSONObject> filterDeptTransaction(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalDeptTransaction(String query) {

        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> filterDeptOrder(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalDeptOrder(String query) {
        return this.masterDB.getTotal(query);
    }

    public JSONObject getDeptInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_agency_info WHERE agency_id=" + id);
    }

    public DeptTransactionSubTypeEntity getHanMuc(Integer id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_transaction_sub_type WHERE id=" + id);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptTransactionSubTypeEntity.class);
        }
        return null;
    }

    public int createDeptTransaction(
            DeptTransactionEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_transaction(" +
                        "dept_transaction_sub_type_id" + "," +
                        "dept_transaction_main_type_id" + "," +
                        "dept_type_id" + "," +
                        "cn_effect_type" + "," +
                        "dtt_effect_type" + "," +
                        "tt_effect_type" + "," +
                        "acoin_effect_type" + "," +
                        "transaction_value" + "," +
                        "agency_id" + "," +
                        "dept_cycle_end" + "," +
                        "dept_type_data" + "," +
                        "note" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "dept_function_type" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "dept_time" + "," +
                        "confirmed_time" + "," +
                        "description" + "," +
                        "acoin" + "," +
                        "transaction_used_value" +
                        ")" +
                        " VALUES(" +
                        parseIntegerToSql(entity.getDept_transaction_sub_type_id()) + "," +
                        parseIntegerToSql(entity.getDept_transaction_main_type_id()) + "," +
                        parseIntegerToSql(entity.getDept_type_id()) + "," +
                        parseStringToSql(entity.getCn_effect_type()) + "," +
                        parseStringToSql(entity.getDtt_effect_type()) + "," +
                        parseStringToSql(entity.getTt_effect_type()) + "," +
                        parseStringToSql(entity.getAcoin_effect_type()) + "," +
                        parseLongToSql(entity.getTransaction_value()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseLongToSql(entity.getDept_cycle_end()) + "," +
                        parseStringToSql(entity.getDept_type_data()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseStringToSql(entity.getDept_function_type()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseDateToSql(entity.getDept_time()) + "," +
                        parseDateToSql(entity.getConfirmed_time()) + "," +
                        parseStringToSql(entity.getDescription()) + "," +
                        parseLongToSql(entity.getAcoin()) + "," +
                        parseLongToSql(entity.getTransaction_used_value()) + "" +
                        ")"
        );
    }

    public boolean updateDeptTransaction(DeptTransactionEntity entity) {
        return this.masterDB.update(
                "UPDATE dept_transaction SET" +
                        " dept_transaction_sub_type_id = " + parseIntegerToSql(entity.getDept_transaction_sub_type_id()) + "," +
                        " dept_transaction_main_type_id = " + parseIntegerToSql(entity.getDept_transaction_main_type_id()) + "," +
                        " dept_type_id = " + parseIntegerToSql(entity.getDept_type_id()) + "," +
                        " cn_effect_type = " + parseStringToSql(entity.getCn_effect_type()) + "," +
                        " dtt_effect_type = " + parseStringToSql(entity.getDtt_effect_type()) + "," +
                        " tt_effect_type = " + parseStringToSql(entity.getTt_effect_type()) + "," +
                        " acoin_effect_type = " + parseStringToSql(entity.getAcoin_effect_type()) + "," +
                        " transaction_value = " + parseLongToSql(entity.getTransaction_value()) + "," +
                        " agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        " dept_cycle_end = " + parseLongToSql(entity.getDept_cycle_end()) + "," +
                        " dept_type_data = " + parseStringToSql(entity.getDept_type_data()) + "," +
                        " note = " + parseStringToSql(entity.getNote()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " dept_function_type = " + parseStringToSql(entity.getDept_function_type()) + "," +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " dept_time = " + parseDateToSql(entity.getDept_time()) + "," +
                        " confirmed_time = " + parseDateToSql(entity.getConfirmed_time()) + "," +
                        " description = " + parseStringToSql(entity.getDescription()) + "," +
                        " acoin = " + parseLongToSql(entity.getAcoin()) + "," +
                        " transaction_used_value = " + parseLongToSql(entity.getTransaction_used_value()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public DeptTransactionEntity getDeptTransaction(Integer id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_transaction WHERE id=" + id);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptTransactionEntity.class);
        }
        return null;
    }

    public JSONObject getDeptTransactionJs(Integer id) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_transaction WHERE id=" + id);
    }

    public int createDeptOrder(DeptOrderEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_order(" +
                        "agency_id" + "," +
                        "dept_transaction_sub_type_id" + "," +
                        "dept_transaction_main_type_id" + "," +
                        "dept_type_id" + "," +
                        "cn_effect_type" + "," +
                        "dtt_effect_type" + "," +
                        "tt_effect_type" + "," +
                        "acoin_effect_type" + "," +
                        "transaction_value" + "," +
                        "dept_cycle" + "," +
                        "dept_cycle_end" + "," +
                        "dept_type_data" + "," +
                        "note" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "dept_time" + "," +
                        "payment_value" + "," +
                        "payment_date" + "," +
                        "dept_transaction_id" + "," +
                        "acoin" + "," +
                        "order_data_index" + "," +
                        "code," +
                        "doc_no" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getDept_transaction_sub_type_id()) + "," +
                        parseIntegerToSql(entity.getDept_transaction_main_type_id()) + "," +
                        parseIntegerToSql(entity.getDept_type_id()) + "," +
                        parseStringToSql(entity.getCn_effect_type()) + "," +
                        parseStringToSql(entity.getDtt_effect_type()) + "," +
                        parseStringToSql(entity.getTt_effect_type()) + "," +
                        parseStringToSql(entity.getAcoin_effect_type()) + "," +
                        parseLongToSql(entity.getTransaction_value()) + "," +
                        parseIntegerToSql(entity.getDept_cycle()) + "," +
                        parseLongToSql(entity.getDept_cycle_end()) + "," +
                        parseStringToSql(entity.getDept_type_data()) + "," +
                        parseStringToSql(entity.getNote()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseDateToSql(entity.getDept_time()) + "," +
                        parseLongToSql(entity.getPayment_value()) + "," +
                        parseDateToSql(entity.getPayment_date()) + "," +
                        parseIntegerToSql(entity.getDept_transaction_id()) + "," +
                        parseLongToSql(entity.getAcoin()) + "," +
                        parseIntegerToSql(entity.getOrder_data_index()) + "," +
                        parseStringToSql(entity.getCode()) + "," +
                        parseStringToSql(entity.getDoc_no()) +
                        ")"
        );
    }

    public boolean approveDeptTransaction(
            Integer id,
            String confirmed_time,
            int modifier_id) {
        return this.masterDB.update(
                "UPDATE dept_transaction SET status = " + DeptTransactionStatus.CONFIRMED.getId()
                        + ", confirmed_time = '" + confirmed_time + "'"
                        + ", modifier_id = '" + modifier_id + "'"
                        + " WHERE id =" + id
        );
    }

    public boolean rejectDeptTransaction(Integer id, String note) {
        return this.masterDB.update(
                "UPDATE dept_transaction SET status = " + DeptTransactionStatus.REJECT.getId()
                        + ", note ='" + note + "'"
                        + " WHERE id =" + id
        );
    }

    public DeptTransactionSubTypeEntity getDeptTransactionSubType(Integer id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_transaction_sub_type WHERE id=" + id);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptTransactionSubTypeEntity.class);
        }
        return null;
    }

    public boolean increaseCNO(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET current_dept = current_dept + " + transaction_value +
                        ", dept_cycle_end = dept_cycle_end + " + transaction_value +
                        ", modified_date = NOW()" +
                        " WHERE agency_id=" + agency_id
        );
    }

    public boolean decreaseCNO(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET current_dept = current_dept - " + transaction_value +
                        ", dept_cycle_end = dept_cycle_end - " + transaction_value +
                        ", modified_date = NOW()" +
                        " WHERE agency_id=" + agency_id
        );
    }

    public List<JSONObject> getDeptOrderPaymentNone(Integer agency_id) {
        return this.masterDB.find(
                "SELECT * FROM dept_order" +
                        " WHERE agency_id =" + agency_id +
                        " AND status = 1" +
                        " AND dept_type_id != " + DeptType.DEPT_DECREASE.getId()
        );
    }

    public boolean excutePaymentDeptOrder(Integer id, Long payment_value, Integer status, Date payment_date) {
        return this.masterDB.update(
                "UPDATE dept_order SET payment_value = payment_value + " + payment_value + ""
                        + ", status = " + status
                        + ", payment_date='" + DateTimeUtils.toString(payment_date) + "'"
                        + " WHERE id =" + id
        );
    }

    public int insertDeptClearing(DeptClearingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_clearing(" +
                        "dept_order_id" + "," +
                        "dept_transaction_id" + "," +
                        "dept_clearing_value" + "," +
                        "dept_clearing_data" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "note" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getDept_order_id()) + "," +
                        parseIntegerToSql(entity.getDept_transaction_id()) + "," +
                        parseLongToSql(entity.getDept_clearing_value()) + "," +
                        parseStringToSql(entity.getDept_clearing_data()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseStringToSql(entity.getNote()) +
                        ")"
        );
    }

    public boolean increaseDTT(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET dtt = dtt + " + transaction_value +
                        ", total_dtt_cycle = total_dtt_cycle + " + transaction_value +
                        ", modified_date = NOW()" +
                        " WHERE agency_id=" + agency_id
        );
    }

    public boolean decreaseDTT(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET dtt = dtt - " + transaction_value +
                        ", total_dtt_cycle = total_dtt_cycle - " + transaction_value +
                        ", modified_date = NOW()" +
                        " WHERE agency_id=" + agency_id
        );
    }

    public boolean increaseTT(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET tt = tt + " + transaction_value +
                        ", total_tt_cycle = total_tt_cycle + " + transaction_value +
                        ", modified_date = NOW()" +
                        " WHERE agency_id=" + agency_id
        );
    }

    public boolean decreaseTT(int agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET tt = tt - " + transaction_value
                        + ", total_tt_cycle = total_tt_cycle - " + transaction_value +
                        ", modified_date = NOW()"
                        + " WHERE agency_id = " + agency_id
        );
    }

    public int insertDeptAgencyHistory(
            DeptAgencyHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_agency_history(" +
                        "data" + "," +
                        "type" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "agency_id" + "," +
                        "dept_cycle" + "," +
                        "dept_cycle_end" + "," +
                        "dept_limit" + "," +
                        "ngd_limit" + "," +
                        "note" +
                        ") VALUES(" +
                        parseLongToSql(entity.getData()) + "," +
                        parseStringToSql(entity.getType()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getDept_cycle()) + "," +
                        parseLongToSql(entity.getDept_cycle_end()) + "," +
                        parseLongToSql(entity.getDept_limit()) + "," +
                        parseLongToSql(entity.getNgd_limit()) + "," +
                        parseStringToSql(entity.getNote()) +
                        ")"
        );
    }

    public int insertDeptSetting(DeptSettingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_setting(" +
                        "status," +
                        "created_date," +
                        "modifier_id," +
                        "creator_id," +
                        "modified_date," +
                        "dept_limit," +
                        "ngd_limit," +
                        "dept_cycle," +
                        "start_date," +
                        "end_date," +
                        "confirmed_date," +
                        "agency_include," +
                        "agency_ignore," +
                        "agency_id," +
                        "note" +
                        ")" +
                        " VALUES(" +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseLongToSql(entity.getDept_limit()) + "," +
                        parseLongToSql(entity.getNgd_limit()) + "," +
                        parseIntegerToSql(entity.getDept_cycle()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseDateToSql(entity.getConfirmed_date()) + "," +
                        parseStringToSql(entity.getAgency_include()) + "," +
                        parseStringToSql(entity.getAgency_ignore()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseStringToSql(entity.getNote()) + "" +
                        ")"
        );
    }

    public boolean updateDeptSetting(DeptSettingEntity entity) {
        return this.masterDB.update(
                "UPDATE dept_setting SET " +
                        "status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        "created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        "modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        "creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        "modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        "dept_limit = " + parseLongToSql(entity.getDept_limit()) + "," +
                        "ngd_limit = " + parseLongToSql(entity.getNgd_limit()) + "," +
                        "dept_cycle = " + parseIntegerToSql(entity.getDept_cycle()) + "," +
                        "start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        "end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        "confirmed_date = " + parseDateToSql(entity.getConfirmed_date()) + "," +
                        "agency_include = " + parseStringToSql(entity.getAgency_include()) + "," +
                        "agency_ignore = " + parseStringToSql(entity.getAgency_ignore()) + "," +
                        "agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        "note = " + parseStringToSql(entity.getNote()) + "" +
                        " WHERE id = " + entity.getId()
        );
    }

    public DeptSettingEntity getDeptSetting(Integer dept_setting_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_setting WHERE id = " + dept_setting_id
        );
        if (rs != null) {
            return DeptSettingEntity.from(rs);
        }
        return null;
    }

    public boolean approveDeptSetting(Integer dept_setting_id, Date start_date, Date confirmed_date) {
        return this.masterDB.update(
                "UPDATE dept_setting SET status = " + DeptTransactionStatus.CONFIRMED.getId() +
                        ", start_date = '" + DateTimeUtils.toString(start_date) + "'" +
                        ", confirmed_date = '" + DateTimeUtils.toString(confirmed_date) + "'"
                        + " WHERE id = " + dept_setting_id
        );
    }

    /**
     * Kỳ hạn nợ thiết lập mới nhất
     *
     * @param agency_id
     * @return
     */
    public DeptSettingEntity getDeptCycleSettingForAgency(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_setting " +
                        " WHERE dept_cycle IS NOT NULL"
                        + " AND status = " + DeptSettingStatus.CONFIRMED.getId()
                        + " AND agency_include = '[\"" + agency_id + "\"]'"
                        + " AND (start_date <= NOW() AND (end_date IS NULL OR end_date >= NOW()))"
                        + " ORDER BY confirmed_date DESC, id desc"
                        + " LIMIT 1");
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptSettingEntity.class);
        }
        return null;
    }

    /**
     * Hạn mức nợ thiết lập mới nhất
     *
     * @param agency_id
     * @return
     */
    public DeptSettingEntity getDeptLimitSettingForAgency(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_setting " +
                        " WHERE dept_limit IS NOT NULL"
                        + " AND status = " + DeptSettingStatus.CONFIRMED.getId()
                        + " AND agency_include = '[\"" + agency_id + "\"]'"
                        + " AND (start_date <= NOW() AND (end_date IS NULL OR end_date >= NOW()))"
                        + " ORDER BY confirmed_date DESC, id desc"
                        + " LIMIT 1");
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptSettingEntity.class);
        }
        return null;
    }

    /**
     * Hạn mức gối đầu thiết lập mới nhất
     *
     * @param agency_id
     * @return
     */
    public DeptSettingEntity getNGDLimitSettingForAgency(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_setting " +
                        " WHERE ngd_limit IS NOT NULL"
                        + " AND status = " + DeptSettingStatus.CONFIRMED.getId()
                        + " AND agency_include = '[\"" + agency_id + "\"]'"
                        + " AND (start_date <= NOW() AND (end_date IS NULL OR end_date >= NOW()))"
                        + " ORDER BY confirmed_date DESC, id desc"
                        + " LIMIT 1");
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptSettingEntity.class);
        }
        return null;
    }

    public DeptAgencyInfoEntity getDeptAgencyInfo(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_agency_info " +
                        " WHERE agency_id = " + agency_id);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptAgencyInfoEntity.class);
        }
        return null;
    }

    public boolean updateDeptLimitAgencyInfo(Integer agency_id, Long dept_limit) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET dept_limit = " + dept_limit +
                        ", modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public boolean updateDeptCycleAgencyInfo(Integer agency_id, Integer dept_cycle) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET dept_cycle = " + dept_cycle +
                        ", modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public boolean updateDeptNGDLimitAgencyInfo(Integer agency_id, Long ngd_limit) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET ngd_limit = " + ngd_limit +
                        ", modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public DeptAgencyDateEntity getDeptAgencyDate(int agency_id, Date date) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_agency_date" +
                        " WHERE agency_id=" + agency_id +
                        " AND dept_date = " + DateTimeUtils.toString(date, "yyyy-MM-dd")
        );
        if (rs != null) {
            return DeptAgencyDateEntity.from(rs);
        }
        return null;
    }

    public boolean saveDeptAgencyDateForEndDate(int agency_id,
                                                Integer dept_cycle_end, long ngd_limit, long ngd, long nqh, long nth, long nx, long ndh, long tt, long total_tt_cycle, long dtt, long total_dtt_cycle, Date dept_date) {
        return this.masterDB.update(
                "UPDATE dept_agency_date SET dept_cycle_end = " + dept_cycle_end +
                        " WHERE agency_id = " + agency_id +
                        " AND dept_date = '" + DateTimeUtils.toString(dept_date, "yyyy-MM-dd")
        );
    }

    public int createDeptAgencyInfo(DeptAgencyInfoEntity deptAgencyInfoEntity) {
        return this.masterDB.insert(
                "INSERT INTO dept_agency_info"
        );
    }

    public int saveDeptAgencyDate(DeptAgencyDateEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_agency_date(" +
                        "dept_cycle_start" + "," +
                        "dept_cycle_end" + "," +
                        "status" + "," +
                        "created_date" + "," +
                        "modifier_id" + "," +
                        "creator_id" + "," +
                        "modified_date" + "," +
                        "agency_id" + "," +
                        "ngd" + "," +
                        "nqh" + "," +
                        "nx" + "," +
                        "nth" + "," +
                        "ndh" + "," +
                        "total_price_order" + "," +
                        "total_price_payment" + "," +
                        "dept_date" + "," +
                        "total_dtt_cycle" + "," +
                        "total_tt_cycle" + "," +
                        "dtt" + "," +
                        "tt" +
                        ") VALUES(" +
                        parseLongToSql(entity.getDept_cycle_start()) + "," +
                        parseLongToSql(entity.getDept_cycle_end()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseLongToSql(entity.getNgd()) + "," +
                        parseLongToSql(entity.getNqh()) + "," +
                        parseLongToSql(entity.getNx()) + "," +
                        parseLongToSql(entity.getNth()) + "," +
                        parseLongToSql(entity.getNdh()) + "," +
                        parseLongToSql(entity.getTotal_price_order()) + "," +
                        parseLongToSql(entity.getTotal_price_payment()) + "," +
                        "'" + DateTimeUtils.toString(entity.getDept_date(), "yyyy-MM-dd") + "'" + "," +
                        parseLongToSql(entity.getTotal_dtt_cycle()) + "," +
                        parseLongToSql(entity.getTotal_tt_cycle()) + "," +
                        parseLongToSql(entity.getDtt()) + "," +
                        parseLongToSql(entity.getTt()) +
                        ")"
        );
    }

    public boolean rejectDeptSetting(Integer id, String note) {
        return this.masterDB.update(
                "UPDATE dept_setting SET status = " + DeptTransactionStatus.REJECT.getId()
                        + ", note ='" + note + "'"
                        + " WHERE id =" + id
        );
    }

    public DeptOrderEntity getDeptOrder(int dept_order_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dept_order " +
                        " WHERE id = " + dept_order_id);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), DeptOrderEntity.class);
        }
        return null;
    }

    public boolean editDeptCycleOfDeptOrder(
            int id,
            int dept_cycle,
            int modifier_id,
            Date time
    ) {
        return this.masterDB.update(
                "UPDATE dept_order SET" +
                        " dept_cycle = " + dept_cycle + "," +
                        " modifier_id = " + modifier_id + "," +
                        " modified_date = " + parseDateToSql(time) +
                        " WHERE id = " + id

        );
    }

    public List<JSONObject> filterDeptSetting(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalDeptSetting(String query) {
        return this.masterDB.getTotal(query);
    }

    public int insertDeptAgencyInfo(DeptAgencyInfoEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO dept_agency_info(" +
                        "created_date" + "," +
                        "status" + "," +
                        "agency_id" + "," +
                        "current_dept" + "," +
                        "dept_cycle" + "," +
                        "dept_limit" + "," +
                        "ngd" + "," +
                        "ngd_limit" + "," +
                        "dt" + "," +
                        "commit_limit" + "," +
                        "dept_cycle_end" + "," +
                        "dept_order_waiting" + "," +
                        "total_price_sales" + "," +
                        "dtt" + "," +
                        "tt" + "," +
                        "nx" + "," +
                        "nth" + "," +
                        "ndh" + "," +
                        "nqh" + "," +
                        "dept_cycle_start" + "," +
                        "total_price_order" + "," +
                        "total_price_payment" + "," +
                        "total_dtt_cycle" + "," +
                        "total_tt_cycle" + "," +
                        "miss_commit" +
                        ") VALUES(" +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseLongToSql(entity.getCurrent_dept()) + "," +
                        parseIntegerToSql(entity.getDept_cycle()) + "," +
                        parseLongToSql(entity.getDept_limit()) + "," +
                        parseLongToSql(entity.getNgd()) + "," +
                        parseLongToSql(entity.getNgd_limit()) + "," +
                        parseLongToSql(entity.getDt()) + "," +
                        parseIntegerToSql(entity.getCommit_limit()) + "," +
                        parseLongToSql(entity.getDept_cycle_end()) + "," +
                        parseLongToSql(entity.getDept_order_waiting()) + "," +
                        parseLongToSql(entity.getTotal_price_sales()) + "," +
                        parseLongToSql(entity.getDtt()) + "," +
                        parseLongToSql(entity.getTt()) + "," +
                        parseLongToSql(entity.getNx()) + "," +
                        parseLongToSql(entity.getNth()) + "," +
                        parseLongToSql(entity.getNdh()) + "," +
                        parseLongToSql(entity.getNqh()) + "," +
                        parseLongToSql(entity.getDept_cycle_start()) + "," +
                        parseLongToSql(entity.getTotal_price_order()) + "," +
                        parseLongToSql(entity.getTotal_price_payment()) + "," +
                        parseLongToSql(entity.getTotal_dtt_cycle()) + "," +
                        parseLongToSql(entity.getTotal_tt_cycle()) + "," +
                        parseIntegerToSql(entity.getMiss_commit()) +
                        ")"
        );
    }

    public boolean updateDeptAgencyInfo(DeptAgencyInfoEntity entity) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET" +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        " current_dept = " + parseLongToSql(entity.getCurrent_dept()) + "," +
                        " dept_cycle = " + parseIntegerToSql(entity.getDept_cycle()) + "," +
                        " dept_limit = " + parseLongToSql(entity.getDept_limit()) + "," +
                        " ngd = " + parseLongToSql(entity.getNgd()) + "," +
                        " ngd_limit = " + parseLongToSql(entity.getNgd_limit()) + "," +
                        " dt = " + parseLongToSql(entity.getDt()) + "," +
                        " commit_limit = " + parseIntegerToSql(entity.getCommit_limit()) + "," +
                        " dept_cycle_end = " + parseLongToSql(entity.getDept_cycle_end()) + "," +
                        " dept_order_waiting = " + parseLongToSql(entity.getDept_order_waiting()) + "," +
                        " total_price_sales = " + parseLongToSql(entity.getTotal_price_sales()) + "," +
                        " dtt = " + parseLongToSql(entity.getDtt()) + "," +
                        " tt = " + parseLongToSql(entity.getTt()) + "," +
                        " nx = " + parseLongToSql(entity.getNx()) + "," +
                        " nth = " + parseLongToSql(entity.getNth()) + "," +
                        " ndh = " + parseLongToSql(entity.getNdh()) + "," +
                        " nqh = " + parseLongToSql(entity.getNqh()) + "," +
                        " dept_cycle_start = " + parseLongToSql(entity.getDept_cycle_start()) + "," +
                        " total_price_order = " + parseLongToSql(entity.getTotal_price_order()) + "," +
                        " total_price_payment = " + parseLongToSql(entity.getTotal_price_payment()) + "," +
                        " total_dtt_cycle = " + parseLongToSql(entity.getTotal_dtt_cycle()) + "," +
                        " total_tt_cycle = " + parseLongToSql(entity.getTotal_tt_cycle()) + "," +
                        " miss_commit = " + parseIntegerToSql(entity.getMiss_commit()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public Long getTotalTtAgencyByDate(Integer agency_id, String effect_value_type, String start_date, String end_date) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT sum(transaction_value) as total" +
                        " FROM dept_transaction WHERE" +
                        " agency_id = " + agency_id +
                        " AND status = " + DeptTransactionStatus.CONFIRMED.getId() +
                        " AND confirmed_time >= '" + start_date + "'" +
                        " AND confirmed_time <= '" + end_date + "'" +
                        " AND tt_effect_type = '" + effect_value_type + "'"
        );

        if (rsTotal != null) {
            return ConvertUtils.toLong(rsTotal.get("total"));
        }

        return 0L;
    }

    public Long getTotalDttAgencyByDate(Integer agency_id, String effect_value_type, String start_date, String end_date) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT sum(transaction_value) as total" +
                        " FROM dept_order WHERE" +
                        " dept_time >= '" + start_date + "'" +
                        " AND dept_time <= '" + end_date + "'" +
                        " AND agency_id = " + agency_id +
                        " AND dtt_effect_type = '" + effect_value_type + "'"
        );

        if (rsTotal != null) {
            return ConvertUtils.toLong(rsTotal.get("total"));
        }

        return 0L;
    }

    public List<JSONObject> getListDeptTransactionRemain(Integer agency_id) {
        return this.masterDB.find(
                "SELECT * FROM dept_transaction WHERE status = 2" +
                        " AND agency_id = " + agency_id +
                        " AND cn_effect_type = '" + TransactionEffectValueType.DECREASE.getCode() + "'" +
                        " AND transaction_value - transaction_used_value > 0"
        );
    }

    public boolean updateTransactionUsedValue(Integer dept_transaction_id, long transaction_used_value) {
        return this.masterDB.update(
                "UPDATE dept_transaction" +
                        " SET transaction_used_value = " + transaction_used_value +
                        " WHERE id= " + dept_transaction_id
        );
    }

    public Long getCurrentDept(int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT *" +
                        " FROM dept_agency_info WHERE" +
                        " agency_id = " + agency_id
        );

        if (rs != null) {
            return ConvertUtils.toLong(rs.get("current_dept"));
        }

        return 0L;
    }

    public Long getTotalPaymentToday(Integer agency_id) {
        JSONObject rsTotal = this.masterDB.getOne(
                "SELECT sum(transaction_value) as total" +
                        " FROM dept_transaction" +
                        " WHERE agency_id = " + agency_id +
                        " AND dept_transaction_sub_type_id = 2 " +
                        " AND status = " + DeptTransactionStatus.CONFIRMED.getId() +
                        " AND DATE(confirmed_time) = DATE(NOW())"
        );

        if (rsTotal != null) {
            return ConvertUtils.toLong(rsTotal.get("total"));
        }

        return 0L;
    }

    public boolean increaseTotalPricePayment(Integer agency_id, Long transaction_value) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET total_price_payment = total_price_payment +" + transaction_value +
                        " WHERE agency_id = " + agency_id
        );
    }

    public List<JSONObject> getListAgencyHasNQH() {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM dept_agency_info" +
                        " WHERE nqh > 0"
        );
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> getListCommitDoing() {
        return this.masterDB.find(
                "SELECT t.*, t1.code FROM agency_order_commit t" +
                        " LEFT JOIN agency_order t1 ON t1.id = t.order_id" +
                        " WHERE t.status = " + CommitStatus.WAITING.getId() +
                        " AND t1.commit_approve_status != " + CommitApproveStatus.REJECT.getId() +
                        " AND t.committed_date = '" + DateTimeUtils.getNow("yyyy-MM-dd") + "'" +
                        " AND t.committed_money > t.completed_money"
        );
    }

    public List<JSONObject> getListDeptOrderByAgency(int agency_id, int status, String start_date, String end_date, int offset, int pageSize) {
        return this.masterDB.find(
                "SELECT * " +
                        " FROM dept_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND (" + status + " = 0 OR status = " + status + ")" +
                        " AND created_date >= '" + start_date + "'" +
                        " AND created_date <= '" + end_date + "'" +
                        " LIMIT " + offset + "," + pageSize
        );
    }

    public int getTotalDeptOrderByAgency(int agency_id, int status, String start_date, String end_date) {
        return this.masterDB.getTotal(
                "SELECT * " +
                        " FROM dept_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND (" + status + " = 0 OR status = " + status + ")" +
                        " AND created_date >= '" + start_date + "'" +
                        " AND created_date <= '" + end_date + "'"
        );
    }

    public JSONObject sumDeptOrderByAgency(int agency_id, int status, String start_date, String end_date) {
        return this.masterDB.getOne(
                "SELECT SUM(transaction_value) AS total_transaction_value," +
                        " SUM(payment_value) AS total_payment_value" +
                        " FROM dept_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND (" + status + " = 0 OR status = " + status + ")" +
                        " AND created_date >= '" + start_date + "'" +
                        " AND created_date <= '" + end_date + "'"
        );
    }

    public boolean increaseMissCommit(int agency_id) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET miss_commit = miss_commit + 1" +
                        ", modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public boolean decreaseMissCommit(int agency_id) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET miss_commit = miss_commit - 1" +
                        ", modified_date = NOW()" +
                        " WHERE miss_commit > 0 AND agency_id = " + agency_id
        );
    }

    public JSONObject getCommitDoingByAgency(Integer agency_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_order_commit" +
                        " WHERE agency_id = " + agency_id +
                        " AND status = 1" +
                        " AND committed_date >= '" + DateTimeUtils.getNow("yyyy-MM-dd") + "'" +
                        " LIMIT 1"
        );
    }

    public JSONObject getCommitNeedFinishByAgency(Integer agency_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_order_commit" +
                        " WHERE agency_id = " + agency_id +
                        " AND status = 1" +
                        " LIMIT 1"
        );
    }

    public boolean finishCommit(int agency_order_commit_id) {
        return this.masterDB.update(
                "UPDATE agency_order_commit SET status = 2," +
                        " completed_money = committed_money," +
                        " completed_date = NOW()" +
                        " WHERE id = " + agency_order_commit_id
        );
    }

    public boolean increaseCompleteMoneyCommit(int agency_order_commit_id, long money) {
        return this.masterDB.update(
                "UPDATE agency_order_commit SET " +
                        " completed_money = completed_money + " + money + "," +
                        " completed_date = NOW()" +
                        " WHERE id = " + agency_order_commit_id
        );
    }

    public boolean saveAcoinDeptTransaction(Integer id, long acoin) {
        return this.masterDB.update(
                "UPDATE dept_transaction SET acoin = " + acoin +
                        " WHERE id = " + id
        );
    }

    public boolean saveAcoinDeptOrder(Integer id, long acoin) {
        return this.masterDB.update(
                "UPDATE dept_order SET acoin = " + acoin +
                        " WHERE id = " + id
        );
    }

    public boolean punishAcoinDeptOrder(Integer id, long acoin) {
        return this.masterDB.update(
                "UPDATE dept_order SET acoin = acoin - " + acoin + "," +
                        "is_punish_acoin = 1" +
                        " WHERE id = " + id
        );
    }

    public boolean increaseTotalPriceOrder(Integer agency_id, long total_end_price) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET" +
                        " total_price_order = total_price_order + " + total_end_price + "," +
                        " modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public JSONObject getDeptOrderInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_order WHERE id = " + id
        );
    }

    public List<JSONObject> getDeptOrderInfoByOrderCode(String dept_type_data, int dept_type_id) {
        return this.masterDB.find(
                "SELECT * FROM dept_order WHERE " +
                        "dept_type_id = " + dept_type_id +
                        " AND dept_type_data = '" + dept_type_data + "'"
        );
    }

    public boolean editDeptDtt(Integer agency_id, long data, int staff_id, String time) {
        return this.masterDB.update(
                "UPDATE dept_agency_info SET dtt = dtt + " + data +
                        ", total_dtt_cycle = total_dtt_cycle + " + data +
                        ", modifier_id = " + staff_id +
                        ", modified_date = '" + time + "'" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public int saveDeptDttHistory(
            int agency_id,
            long data,
            long before_value,
            long after_value,
            String note,
            int staff_id,
            String time,
            String type,
            String code) {
        return this.masterDB.insert(
                "INSERT INTO dept_dtt_history(" +
                        "agency_id," +
                        "data," +
                        "before_value," +
                        "after_value," +
                        "note," +
                        "creator_id," +
                        "created_date," +
                        "type," +
                        "code)" +
                        " VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + data + "'," +
                        "'" + before_value + "'," +
                        "'" + after_value + "'," +
                        "'" + note + "'," +
                        "'" + staff_id + "'," +
                        "'" + time + "'," +
                        "'" + type + "'," +
                        "'" + code + "'" +
                        ")"
        );
    }

    public int saveDeptTtHistory(
            int agency_id,
            long data,
            long before_value,
            long after_value,
            String note,
            int staff_id,
            String time) {
        return this.masterDB.insert(
                "INSERT INTO dept_tt_history(" +
                        "agency_id," +
                        "data," +
                        "before_value," +
                        "after_value," +
                        "note," +
                        "creator_id," +
                        "created_date)" +
                        " VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + data + "'," +
                        "'" + before_value + "'," +
                        "'" + after_value + "'," +
                        "'" + note + "'," +
                        "'" + staff_id + "'," +
                        "'" + time + "'" +
                        ")"
        );
    }

    public boolean updateCommitLimit(
            Integer agency_id,
            Integer commit_limit,
            int miss_commit) {
        return this.masterDB.update(
                "UPDATE dept_agency_info" +
                        " SET commit_limit = " + commit_limit + "," +
                        " miss_commit = " + miss_commit +
                        " WHERE agency_id = " + agency_id
        );
    }

    public List<JSONObject> getListDeptOrderNoneNQH() {
        return this.masterDB.find(
                "SELECT * FROM dept_order WHERE" +
                        " dept_type_id != " + DeptType.DEPT_DECREASE.getId() +
                        " AND transaction_value > payment_value" +
                        " AND is_nqh = 0"
        );
    }

    public int insertAgencyDeptDtt(
            int agency_id,
            long data,
            long before_value,
            long after_value,
            String note) {
        return this.masterDB.insert(
                "INSERT INTO agency_dept_dtt(" +
                        "agency_id," +
                        "data," +
                        "before_value," +
                        "after_value," +
                        "note)" +
                        " VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + data + "'," +
                        "'" + before_value + "'," +
                        "'" + after_value + "'," +
                        "'" + note + "'" +
                        ")"
        );
    }

    public JSONObject getAgencyDeptDtt(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_dept_dtt" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getDeptOrderInfoByCode(String code) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_order WHERE code = '" + code + "'"
        );
    }

    public JSONObject getDeptOrderInfoByTransactionId(int dept_transaction_id) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_order WHERE dept_transaction_id = " + dept_transaction_id
        );
    }

    public boolean updateCodeForAgencyDeptDtt(int agency_dept_dtt_id, String code) {
        return this.masterDB.update(
                "UPDATE agency_dept_dtt SET code = '" + code + "'" +
                        " WHERE id = " + agency_dept_dtt_id
        );
    }

    public boolean updateCodeForDeptTransaction(int id, String code) {
        return this.masterDB.update(
                "UPDATE dept_transaction" +
                        " SET code = '" + code + "'," +
                        " doc_no = '" + code + "'" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getListNQHNonePayment() {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM dept_order" +
                        " WHERE is_nqh = 1" +
                        " AND is_nqh_over = 0" +
                        " AND transaction_value > payment_value"
        );
    }

    public boolean updateNQHToOver(int id) {
        return this.masterDB.update(
                "UPDATE dept_order SET is_nqh_over = 1" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getLastDeptOrderDate(
            int agency_id) {
        return this.masterDB.getOne(
                "SELECT * FROM dept_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND dept_transaction_sub_type_id IN (1,16,17)" +
                        " ORDER BY id DESC" +
                        " LIMIT 1"
        );
    }

    /**
     * Reset doanh thu thuần
     *
     * @param agency_id
     * @return
     */
    public boolean resetDTT(int agency_id) {
        return this.masterDB.update(
                "UPDATE dept_agency_info" +
                        " SET total_dtt_cycle = 0, dtt = 0 WHERE agency_id = " + agency_id
        );
    }


    /**
     * Reset tiền thu
     *
     * @param agency_id
     * @return
     */
    public boolean resetTT(int agency_id) {
        return this.masterDB.update(
                "UPDATE dept_agency_info" +
                        " SET total_tt_cycle = 0, tt = 0 WHERE agency_id = " + agency_id
        );
    }

    public long getTotalDttByAgency(int agencyId, String start_date, String end_date) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT SUM(data) AS total" +
                        " FROM dept_dtt_history" +
                        " WHERE agency_id=" + agencyId +
                        " AND ('" + start_date + "' = '' OR ('" + start_date + "' != '' AND created_date >= '" + start_date + "'))" +
                        " AND ('" + end_date + "' = '' OR ('" + end_date + "' != '' AND created_date <= '" + end_date + "'))");
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }
        return 0;
    }

    public long getTotalTtByAgency(int agencyId, String start_date, String end_date) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT SUM(data) AS total" +
                        " FROM dept_tt_history" +
                        " WHERE agency_id=" + agencyId +
                        " AND ('" + start_date + "' = '' OR ('" + start_date + "' != '' AND created_date >= '" + start_date + "'))" +
                        " AND ('" + end_date + "' = '' OR ('" + end_date + "' != '' AND created_date <= '" + end_date + "'))");
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }
        return 0;
    }

    public List<JSONObject> checkDTTHistory(String year) {
        return this.masterDB.find(
                "select t.total_dtt_cycle, t1.total_dtt\n" +
                        "from \n" +
                        "dept_agency_info t left join \n" +
                        "(SELECT agency_id, SUM(data) AS total_dtt\n" +
                        "                    FROM dept_dtt_history\n" +
                        "                    WHERE created_date BETWEEN '" + year + "-01-01 00:00:00' AND '" + year + "-12-28 23:59:59'\n" +
                        " GROUP BY agency_id) as t1 ON t1.agency_id = t.agency_id\n" +
                        "where t.total_dtt_cycle != t1.total_dtt"
        );
    }

    public JSONObject getDeptTransactionDetailData(int dept_transaction_id) {
        return this.masterDB.getOne("SELECT * FROM dept_transaction_detail_data WHERE dept_transaction_id = " + dept_transaction_id);
    }
}