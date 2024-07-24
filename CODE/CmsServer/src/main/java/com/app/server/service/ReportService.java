package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterListRequest;
import com.app.server.database.LogDB;
import com.app.server.enums.FunctionList;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.Convert;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReportService extends BaseService {

    private LogDB logDB;

    @Autowired
    public void setLogDB(LogDB logDB) {
        this.logDB = logDB;
    }

    public ClientResponse filterAgencyAccessApp(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(
                    FunctionList.FILTER_AGENCY_ACCESS_APP_REPORT,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.logDB.filter(
                    query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                ));
            }

            JSONObject data = new JSONObject();
            int total = this.logDB.getTotal(query);
            int sum = this.logDB.sum(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_quantity", sum);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("REPORT", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}