package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.DeptOrderEntity;
import com.app.server.enums.Module;
import com.app.server.enums.ResponseStatus;
import com.app.server.manager.ConfigManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.mysql.cj.log.Log;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AcoinService extends BaseService {
    public ClientResponse runDecreaseACoinForNQH() {
        try {
            int acoin_rate_default = this.dataManager.getConfigManager().getAcoinRateDefault();
            Date today = DateTimeUtils.getNow();
            List<JSONObject> deptOrderList = this.aCoinDB.getAllDeptOrderPaymentNone();
            for (JSONObject jsDeptOrder : deptOrderList) {
                int agency_id = ConvertUtils.toInt(jsDeptOrder.get("agency_id"));
                AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(agency_id);
                if (agencyEntity == null) {
                    /**
                     * Thông báo qua tele
                     */
                    continue;
                }

                DeptOrderEntity deptOrderEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(jsDeptOrder), DeptOrderEntity.class);
                Date payment_deadline = this.getDeptOrderPaymentDeadline(
                        deptOrderEntity.getDept_time(),
                        deptOrderEntity.getDept_cycle()
                );

                if (payment_deadline.after(today)) {
                    continue;
                }

                /**
                 * Tính toán acoin giảm
                 */
                long total_value_dept_acoin = ConvertUtils.toLong(jsDeptOrder.get("transaction_value"))
                        - ConvertUtils.toLong(jsDeptOrder.get("payment_value"));
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
                        agency_id,
                        acoin_punish_value,
                        "Nợ quá hạn: " + ConvertUtils.toString(jsDeptOrder.get("note")) + "-" + this.appUtils.priceFormat(total_value_dept_acoin),
                        DateTimeUtils.getNow());
                if (rsDecreaseACoin.failed()) {
                    this.alertToTelegram(
                            "Trừ Acoin đối với các công nợ quá hạn:" +
                                    " agency_id-" + agency_id +
                                    " dept_time-" + deptOrderEntity.getDept_time() +
                                    " dept_cycle-" + deptOrderEntity.getDept_cycle() +
                                    " transaction_value-" + deptOrderEntity.getTransaction_value() +
                                    " dept_order_id-" + deptOrderEntity.getId() +
                                    " FAIL",
                            ResponseStatus.FAIL
                    );
                }
            }

            this.reportToTelegram(
                    "Trừ Acoin đối với các công nợ quá hạn: FINISH",
                    ResponseStatus.SUCCESS
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}