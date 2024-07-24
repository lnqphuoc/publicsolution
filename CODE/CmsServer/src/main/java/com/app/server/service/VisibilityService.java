package com.app.server.service;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VisibilityService extends BaseService {
    public ClientResponse filterProductVisibilityByAgency() {
        try {
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.getAllProduct();
            List<JSONObject> agencyList = this.agencyDB.getAllAgency();
            for (JSONObject agency : agencyList) {
                int agency_id = ConvertUtils.toInt(agency.get("id"));
                for (JSONObject js : records) {
                    int product_id = ConvertUtils.toInt(js.get("id"));
                    int visibility = this.getProductVisibilityByAgency(
                            agency_id,
                            product_id);
                    this.visibilityDB.insertVisibity(product_id, agency_id, visibility);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}