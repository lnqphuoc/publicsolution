package com.app.server.service;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.ProductEntity;
import com.app.server.data.request.config.SettingConfigDataRequest;
import com.app.server.enums.Module;
import com.app.server.enums.ResponseStatus;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MasterService {
    private DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    public ClientResponse updateConfigData(SettingConfigDataRequest request) {
        try {
            if (StringUtils.isBlank(request.getType()) ||
                    StringUtils.isBlank(request.getValue())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DATA_INVALID);
            }

            if (request.getValue().equals(
                    this.dataManager.getConfigManager().getMPConfigData().get(request.getType()))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DATA_INVALID);
            }

            boolean rs = this.dataManager.getConfigManager().updateConfigData(
                    request.getType(), request.getValue()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.dataManager.callReloadConfig();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}