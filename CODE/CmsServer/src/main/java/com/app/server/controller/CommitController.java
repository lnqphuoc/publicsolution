package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.commit.SettingNumberDayNQHMissCommitRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.CommitService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class CommitController extends BaseController {
    private CommitService commitService;

    @Autowired
    public void setCommitService(CommitService commitService) {
        this.commitService = commitService;
    }

    @RequestMapping(value = AgencyPath.SETTING_NUMBER_DAY_NQH_MISS_COMMIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thiết lập số ngày NQH bị trừ sai cam kết", notes = "")
    @ResponseBody
    public ClientResponse settingNumberDayNQHMissCommit(
            @RequestBody SettingNumberDayNQHMissCommitRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.commitService.settingNumberDayNQHMissCommit(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_NUMBER_DAY_NQH_MISS_COMMIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách thiết lấp số ngày NQH bị trừ sai cam kết", notes = "")
    @ResponseBody
    public ClientResponse filterNumberDayNQHMissCommit(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.commitService.filterNumberDayNQHMissCommit(sessionData, request);
    }

    @RequestMapping(value = AgencyPath.FILTER_AGENCY_MISS_COMMIT_HISTORY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lịch sử sai cam kết", notes = "")
    public ClientResponse filterAgencyMissCommitHistory(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.commitService.filterAgencyMissCommitHistory(sessionData, request);
    }
}