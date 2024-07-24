package com.app.server.service;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.DeptOrderEntity;
import com.app.server.enums.Module;
import com.app.server.enums.ResponseStatus;
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
public class VoucherService extends BaseService {
    public ClientResponse runExpireVoucher() {
        try {
            this.orderDB.expireVoucher();
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}