package com.app.server.controller;

import com.app.server.constants.path.BannerPath;
import com.app.server.constants.path.NotifyPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.SortRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.banner.CreateBannerRequest;
import com.app.server.data.request.banner.EditBannerRequest;
import com.app.server.data.request.product.SortBrandRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class BannerController extends BaseController {
    @RequestMapping(value = BannerPath.FILTER_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách banner", notes = "")
    public ClientResponse filterBanner(
            @RequestBody FilterListRequest request) {
        return this.bannerService.filterBanner(request);
    }

    @RequestMapping(value = BannerPath.CREATE_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo banner", notes = "")
    public ClientResponse createBanner(
            @RequestBody CreateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.createBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.EDIT_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa banner", notes = "")
    public ClientResponse editBanner(
            @RequestBody EditBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.editBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.ACTIVATE_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kích hoạt banner", notes = "")
    public ClientResponse activateBanner(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.activateBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.DEACTIVATE_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Ngừng kích hoạt banner", notes = "")
    public ClientResponse deactivateBanner(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.deactivateBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.DELETE_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xóa banner", notes = "")
    public ClientResponse deleteBanner(
            @RequestBody ActivateBannerRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.deleteBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.GET_BANNER_DETAIL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết banner", notes = "")
    public ClientResponse getBannerDetail(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.getBannerDetail(sessionData, request);
    }

    @RequestMapping(value = BannerPath.SORT_BANNER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sắp xếp banner", notes = "")
    public ClientResponse sortBanner(
            @RequestBody SortBrandRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.sortBanner(sessionData, request);
    }

    @RequestMapping(value = BannerPath.FILTER_BANNER_PRIORITY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách banner ưu tiên", notes = "")
    public ClientResponse filterBannerPriority(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.bannerService.filterBannerPriority(sessionData, request);
    }
}