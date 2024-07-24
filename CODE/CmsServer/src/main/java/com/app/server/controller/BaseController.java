package com.app.server.controller;

import com.app.server.data.SessionData;
import com.app.server.securities.CustomUserDetails;
import com.app.server.service.*;
import com.app.server.utils.JwtUtil;
import com.ygame.framework.common.LogUtil;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {
    protected AgencyService agencyService;

    @Autowired
    public void setAgencyService(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    protected ProductService productService;

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    protected OrderService orderService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    protected PromoService promoService;

    @Autowired
    public void setPromoService(PromoService promoService) {
        this.promoService = promoService;
    }

    protected JwtUtil jwtUtil;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    protected SessionData getSessionData() {
        SessionData sessionData = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                sessionData = customUserDetails.getSessionData();
            }
        } catch (Exception ex) {
            sessionData = null;
            LogUtil.printDebug("", ex);
        }
        return sessionData;
    }

    protected SessionData getSessionDataByToken(String token) {
        SessionData sessionData = null;
        try {
            sessionData = jwtUtil.getData(token);
        } catch (Exception ex) {
            sessionData = null;
            LogUtil.printDebug("", ex);
        }
        return sessionData;
    }

    protected DeptService deptService;

    @Autowired
    public void setDeptService(DeptService deptService) {
        this.deptService = deptService;
    }

    protected WarehouseService warehouseService;

    @Autowired
    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    protected PriceService priceService;

    @Autowired
    public void setPriceService(PriceService priceService) {
        this.priceService = priceService;
    }

    protected NotifyService notifyService;

    @Autowired
    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    protected BannerService bannerService;

    @Autowired
    public void setBannerService(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    protected StaffService staffService;

    @Autowired
    public void setStaffService(StaffService staffService) {
        this.staffService = staffService;
    }

    protected VisibilityService visibilityService;

    @Autowired
    public void setVisibilityService(VisibilityService visibilityService) {
        this.visibilityService = visibilityService;
    }

    protected ExportService exportService;

    @Autowired
    public void setExportService(ExportService exportService) {
        this.exportService = exportService;
    }

    protected DealService dealService;

    @Autowired
    public void setDealService(DealService dealService) {
        this.dealService = dealService;
    }

    protected ReportService reportService;

    @Autowired
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    protected OrderAppointmentService orderAppointmentService;

    @Autowired
    public void setOrderAppointmentService(OrderAppointmentService orderAppointmentService) {
        this.orderAppointmentService = orderAppointmentService;
    }

    protected OrderDeliveryService orderDeliveryService;

    @Autowired
    public void setOrderDeliveryService(OrderDeliveryService orderDeliveryService) {
        this.orderDeliveryService = orderDeliveryService;
    }

    protected OrderContractService orderContractService;

    @Autowired
    public void setOrderContractService(OrderContractService orderContractService) {
        this.orderContractService = orderContractService;
    }

    protected SyncService syncService;

    @Autowired
    public void setSyncService(SyncService syncService) {
        this.syncService = syncService;
    }

    protected ResetService resetService;

    @Autowired
    public void setResetService(ResetService resetService) {
        this.resetService = resetService;
    }

    protected MasterService masterService;

    @Autowired
    public void setMasterService(MasterService masterService) {
        this.masterService = masterService;
    }

    protected MissionService missionService;

    @Autowired
    public void setMissionService(MissionService missionService) {
        this.missionService = missionService;
    }

    protected TaskService taskService;

    @Autowired
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}