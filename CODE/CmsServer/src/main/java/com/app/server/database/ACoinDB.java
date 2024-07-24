package com.app.server.database;

import com.app.server.enums.DeptType;
import com.app.server.enums.TransactionEffectValueType;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ACoinDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    public List<JSONObject> getListDeptOrderPaymentNone(int agency_id) {
        return this.masterDB.find(
                "SELECT * FROM dept_order" +
                        " WHERE agency_id = " + agency_id +
                        " AND status = 1" +
                        " AND is_punish_acoin = 0" +
                        " AND (" +
                        " (dept_type_id = " + DeptType.DEPT_DON_HANG.getId() + " AND transaction_value > payment_value)" +
                        " OR (dept_type_id = " + DeptType.DEPT_INCREASE.getId() + " AND transaction_value > payment_value)" +
                        " )"
        );
    }

    public List<JSONObject> getAllDeptOrderPaymentNone() {
        return this.masterDB.find(
                "SELECT * FROM dept_order" +
                        " WHERE status = 1" +
                        " AND is_punish_acoin = 0" +
                        " AND (" +
                        " (dept_type_id = " + DeptType.DEPT_DON_HANG.getId() + " AND transaction_value > payment_value)" +
                        " OR (dept_type_id = " + DeptType.DEPT_INCREASE.getId() + " AND transaction_value > payment_value)" +
                        " )"
        );
    }

    public boolean decreaseACoin(int agency_id, long acoin_value) {
        return this.masterDB.update(
                "UPDATE agency SET current_point = current_point - " + acoin_value +
                        " WHERE id = " + agency_id
        );
    }

    public boolean increaseACoin(int agency_id, long acoin_value) {
        return this.masterDB.update(
                "UPDATE agency SET current_point = current_point + " + acoin_value +
                        " WHERE id = " + agency_id
        );
    }

    public boolean increaseAcoinForDeptTransaction(int id, int acoin_value) {
        return this.masterDB.update(
                "UPDATE dept_transaction SET acoin = acoin + " + acoin_value +
                        " WHERE id = " + id
        );
    }

    public boolean increaseAcoinForAgencyOrder(String code, long acoin_value) {
        return this.masterDB.update(
                "UPDATE agency_order SET acoin = acoin + " + acoin_value +
                        ", modified_date = NOW()" +
                        " WHERE code = '" + code + "'"
        );
    }

    public boolean decreaseAcoinForAgencyOrder(String code, long acoin_value) {
        return this.masterDB.update(
                "UPDATE agency_order SET acoin = acoin - " + acoin_value +
                        ", modified_date = NOW()" +
                        " WHERE code = '" + code + "'"
        );
    }

    public boolean increaseAcoinForDeptOrder(int id, int acoin_value) {
        return this.masterDB.update(
                "UPDATE dept_order SET acoin = acoin + " + acoin_value +
                        " WHERE id = " + id
        );
    }

    public boolean decreaseACoinDeptOrder(Integer id, int acoin_value) {
        return this.masterDB.update(
                "UPDATE dept_order SET acoin = acoin - " + acoin_value +
                        ", is_punish_acoin = 1" +
                        " WHERE id = " + id
        );
    }
}