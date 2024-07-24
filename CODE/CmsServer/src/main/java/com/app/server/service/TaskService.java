package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.enums.FunctionList;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.task.TaskStatus;
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
public class TaskService extends BaseService {
    public ClientResponse filterTask(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILER_TASK,
                    request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.staffDB.filter(
                    query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.agencyDB.getTotalAgency(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getTaskInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsTask = this.staffDB.getTaskInfo(request.getId());
            if (jsTask == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            JSONObject data = new JSONObject();
            data.put("task", jsTask);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    public ClientResponse finishTask(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsTask = this.staffDB.getTaskInfo(request.getId());
            if (jsTask == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(jsTask.get("status"));
            if (status == TaskStatus.DONE.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            this.staffDB.finishTask(request.getId());

            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }
}