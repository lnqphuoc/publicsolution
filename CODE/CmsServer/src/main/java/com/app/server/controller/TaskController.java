package com.app.server.controller;

import com.app.server.constants.path.StaffPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.staff.CreateGroupPermissionRequest;
import com.app.server.data.request.staff.CreateStaffRequest;
import com.app.server.data.request.staff.EditGroupPermissionRequest;
import com.app.server.data.request.staff.EditStaffRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.StaffService;
import com.app.server.service.TaskService;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class TaskController extends BaseController {

    @RequestMapping(value = StaffPath.FILTER_TASK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse filterTask(
            @RequestBody FilterListRequest request) {
        return this.taskService.filterTask(this.getSessionData(), request);
    }

    @RequestMapping(value = StaffPath.GET_TASK_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse getTaskInfo(BasicRequest request) {
        return this.taskService.getTaskInfo(this.getSessionData(), request);
    }

    @RequestMapping(value = StaffPath.FINISH_TASK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse finishTask(
            @RequestBody BasicRequest request) {
        return this.taskService.finishTask(this.getSessionData(), request);
    }
}