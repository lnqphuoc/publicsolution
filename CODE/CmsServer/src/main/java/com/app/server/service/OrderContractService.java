package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.request.FilterListRequest;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
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
public class OrderContractService extends BaseService {
    /**
     * Danh sách phiếu hẹn
     *
     * @param request
     * @return
     */
    public ClientResponse filterOrderContract(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_APPOINTMENT, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filterPurchaseOrder(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotalPurchaseOrder(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                int source = ConvertUtils.toInt(js.get("source"));
                js.put("creator_info",
                        SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                this.dataManager.getStaffManager().getStaff(
                                        ConvertUtils.toInt(js.get("creator_id"))
                                ));
                int locked = ConvertUtils.toInt(js.get("locked"));
                int order_status = ConvertUtils.toInt(js.get("status"));
                Date update_status_date = DateTimeUtils.getDateTime(ConvertUtils.toString(
                        js.get("update_status_date")));
                if (ConvertUtils.toInt(js.get("source")) == SourceOrderType.APP.getValue()
                        && (order_status == OrderStatus.WAITING_CONFIRM.getKey() ||
                        order_status == OrderStatus.PREPARE.getKey())
                        && locked == 0) {
                    Date time_lock = this.appUtils.getTimeLock(
                            update_status_date, this.dataManager.getConfigManager().getTimeLock());
                    js.put("time_lock", time_lock.getTime());
                } else {
                    js.put("time_lock", update_status_date.getTime());
                }
            }
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}