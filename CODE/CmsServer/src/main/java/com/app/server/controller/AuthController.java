package com.app.server.controller;

import com.app.server.constants.path.AgencyPath;
import com.app.server.constants.path.AuthPath;
import com.app.server.data.request.LoginRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class AuthController extends BaseController {
    /**
     * Login
     */
    @RequestMapping(value = AuthPath.LOGIN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ClientResponse login(@RequestBody LoginRequest request) {
        return this.staffService.login(request.getUsername(), request.getPassword());
    }
}