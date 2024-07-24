package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.constants.path.ReportPath;
import com.app.server.data.request.FilterListRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class ReportController extends BaseController {
    @RequestMapping(value = ReportPath.FILTER_AGENCY_ACCESS_APP_REPORT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "SL truy cập App", notes = "")
    public ClientResponse filterAgencyAccessApp(
            @RequestBody FilterListRequest request) {
        return this.reportService.filterAgencyAccessApp(request);
    }
}