package com.app.server.controller;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.order.CancelOrderByAppRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class SyncController extends BaseController {
    @RequestMapping(value = "/sync/reload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse reload(String type, int id) {
        switch (type) {
            case "DEPT": {
                return this.deptService.updateDeptAgencyInfo(id);
            }
            default:
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
    }

    @RequestMapping(value = "/sync/filter_sync_history", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterSyncHistory(
            @RequestBody FilterListRequest request
    ) {
        return this.syncService.filterSyncHistory(request);
    }
}