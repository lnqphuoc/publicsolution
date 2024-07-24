package com.app.server.controller;

import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class VisibilityController extends BaseController {
    @RequestMapping(value = "/visibility/run_setting_all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Ẩn hiện sản phẩm theo đại lý", notes = "")
    public ClientResponse filterProductVisibilityByAgency() {
        return this.visibilityService.filterProductVisibilityByAgency();
    }
}