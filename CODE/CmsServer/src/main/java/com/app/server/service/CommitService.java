package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.constants.path.AgencyPath;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.commit.SettingNumberDayNQHMissCommitRequest;
import com.app.server.database.AgencyDB;
import com.app.server.enums.FunctionList;
import com.app.server.enums.Module;
import com.app.server.enums.ResponseStatus;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.FilterUtils;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CommitService {
    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    public DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    protected AppUtils appUtils;

    @Autowired
    public void setAppUtil(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    public ClientResponse settingNumberDayNQHMissCommit(SessionData sessionData, SettingNumberDayNQHMissCommitRequest request) {
        try {
            List<String> agencyResultList = this.getListAgency(request);
            if (!this.verifyAgencyList(request.getAgency_setting_data(), agencyResultList)) {
                ClientResponse crVerify = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crVerify.setMessage("Danh sách đại lý theo đối tượng đã thay đổi, vui lòng kiểm tra lại một lần nữa");
                return crVerify;
            }

            List<AgencyBasicData> agencyErrorList = new ArrayList<>();
            for (String strAgencyId : agencyResultList) {
                int rsInsert = this.agencyDB.insertDeptCommitSetting(
                        ConvertUtils.toInt(strAgencyId),
                        request.getNumber_day_nqh_miss_commit(),
                        sessionData.getId()
                );
                if (rsInsert <= 0) {
                    agencyErrorList.add(this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(strAgencyId)));
                }

                this.agencyDB.updateDeptCommitSetting(
                        ConvertUtils.toInt(strAgencyId),
                        request.getNumber_day_nqh_miss_commit());
            }
            JSONObject data = new JSONObject();
            data.put("agency_error_list", agencyErrorList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean verifyAgencyList(List<String> agency_setting_data, List<String> agencyResultList) {
        return true;
    }

    private List<String> getListAgency(SettingNumberDayNQHMissCommitRequest request) {
        int hasFilter = 0;
        if (!request.getCity_data().isEmpty() ||
                !request.getRegion_data().isEmpty() ||
                !request.getMembership_data().isEmpty()) {
            hasFilter = 1;
        }
        List<JSONObject> agencyList = this.agencyDB.filterAgency(
                JsonUtils.Serialize(request.getAgency_include_data()),
                JsonUtils.Serialize(request.getAgency_ignore_data()),
                JsonUtils.Serialize(request.getCity_data()),
                JsonUtils.Serialize(request.getRegion_data()),
                JsonUtils.Serialize(request.getMembership_data()),
                hasFilter
        );
        return agencyList.stream().map(
                e -> e.get("id").toString()
        ).collect(Collectors.toList());
    }

    public ClientResponse filterNumberDayNQHMissCommit(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEPT_COMMIT_SETTING, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));
            }

            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMissCommitHistory(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_AGENCY_MISS_COMMIT_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("agency", this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsonObject.get("agency_id"))));
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));
            }

            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEPT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}