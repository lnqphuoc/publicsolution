package com.app.server.controller;

import com.app.server.constants.path.BannerPath;
import com.app.server.constants.path.DeptPath;
import com.app.server.constants.path.NotifyPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.banner.CreateBannerRequest;
import com.app.server.data.request.banner.EditBannerRequest;
import com.app.server.data.request.notify.CreateNotifyRequest;
import com.app.server.data.request.notify.EditNotifyRequest;
import com.app.server.data.request.product.SortBrandRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class NotifyController extends BaseController {
    @RequestMapping(value = NotifyPath.FILTER_NOTIFY_SETTING, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách thông báo thiết lập", notes = "")
    public ClientResponse filterNotifySetting(
            @RequestBody FilterListRequest request) {
        return this.notifyService.filterNotifySetting(request);
    }

    @RequestMapping(value = NotifyPath.CREATE_NOTIFY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo thông báo", notes = "")
    public ClientResponse createNotify(
            @RequestBody CreateNotifyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.createNotifySetting(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.EDIT_NOTIFY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa thông báo", notes = "")
    public ClientResponse editNotify(
            @RequestBody EditNotifyRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.editNotify(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.ACTIVATE_NOTIFY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt thông báo", notes = "")
    public ClientResponse activateNotify(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.activateNotify(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.DEACTIVATE_NOTIFY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Ngừng kích hoạt thông báo", notes = "")
    public ClientResponse cancelNotify(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.cancelNotify(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.DELETE_NOTIFY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa thông báo", notes = "")
    public ClientResponse deleteNotify(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.deleteNotify(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.GET_NOTIFY_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết thông báo", notes = "")
    public ClientResponse getNotifyDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.notifyService.getNotifyDetail(sessionData, request);
    }

    @RequestMapping(value = NotifyPath.FILTER_NOTIFY_AUTO_CONFIG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách nội dung thông báo tự động", notes = "")
    public ClientResponse filterNotifyAutoConfig(
            @RequestBody FilterListRequest request) {
        return this.notifyService.filterNotifyAutoConfig(request);
    }
}