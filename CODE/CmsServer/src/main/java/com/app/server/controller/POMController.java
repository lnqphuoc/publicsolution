package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.commit.SettingNumberDayNQHMissCommitRequest;
import com.app.server.data.request.promo.ApprovePromoRequest;
import com.app.server.data.request.promo.CreatePromoRequest;
import com.app.server.data.request.promo.EditPromoRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.CommitService;
import com.app.server.service.POMService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class POMController extends BaseController {
    private POMService pomService;

    @Autowired
    public void setPomService(POMService pomService) {
        this.pomService = pomService;
    }

    @RequestMapping(value = "/pom/filter_pom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "filterPOM", notes = "")
    @ResponseBody
    public ClientResponse filterPOM(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.filterPOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/get_pom_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "getPOMInfo", notes = "")
    @ResponseBody
    public ClientResponse getPOMInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.getPOMInfo(sessionData, request);
    }

    @RequestMapping(value = "/pom/filter_qom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "filterQOM", notes = "")
    @ResponseBody
    public ClientResponse filterQOM(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.filterQOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/get_qom_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "getQOMInfo", notes = "")
    @ResponseBody
    public ClientResponse getQOMInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.getQOMInfo(sessionData, request);
    }

    @RequestMapping(value = "/pom/create_qom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "create_qom", notes = "")
    @ResponseBody
    public ClientResponse createQOM(
            @RequestBody CreatePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.createQOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/approve_qom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "approve_qom", notes = "")
    @ResponseBody
    public ClientResponse approveQOM(
            @RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.approveQOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/stop_qom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "stopQOM", notes = "")
    @ResponseBody
    public ClientResponse stopQOM(
            @RequestBody ApprovePromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.stopQOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/edit_qom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "edit_qom", notes = "")
    @ResponseBody
    public ClientResponse editQOM(
            @RequestBody EditPromoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.pomService.editQOM(sessionData, request);
    }

    @RequestMapping(value = "/pom/get_pom_by_po", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "get_pom_by_po", notes = "")
    @ResponseBody
    public ClientResponse getPOMByPO(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.createPOMFromPO(sessionData, request.getId());
    }
}