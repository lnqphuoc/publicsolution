package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterListRequest;
import com.app.server.database.LogDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.FilterUtils;
import com.ygame.framework.common.LogUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {
    protected AppUtils appUtils;

    @Autowired
    public void setAppUtil(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    protected FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    private LogDB logDB;

    @Autowired
    public void setLogDB(LogDB logDB) {
        this.logDB = logDB;
    }

    public ClientResponse filterSyncHistory(FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            String query = this.filterUtils.getQuery(FunctionList.LIST_SYNC_HISTORY, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.logDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.logDB.getTotal(query);

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