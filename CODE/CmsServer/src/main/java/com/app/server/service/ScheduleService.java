package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.database.PriceDB;
import com.app.server.database.ReloadCacheDB;
import com.app.server.database.WarehouseDB;
import com.app.server.enums.OrderStatus;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Component
public class ScheduleService extends BaseService {
    private PromoService promoService;

    @Autowired
    public void setPromoService(PromoService promoService) {
        this.promoService = promoService;
    }

    private ReloadService reloadService;

    @Autowired
    public void setReloadService(ReloadService reloadService) {
        this.reloadService = reloadService;
    }

    private DeptService deptService;

    @Autowired
    public void setDeptService(DeptService deptService) {
        this.deptService = deptService;
    }

    private WarehouseService warehouseService;

    @Autowired
    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    private AcoinService acoinService;

    @Autowired
    public void setAcoinService(AcoinService acoinService) {
        this.acoinService = acoinService;
    }

    private OrderService orderService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    private ProductService productService;

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private PriceService priceService;

    @Autowired
    public void setPriceService(PriceService priceService) {
        this.priceService = priceService;
    }

    private BannerService bannerService;

    @Autowired
    public void setBannerService(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    private NotifyService notifyService;

    @Autowired
    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    private ProductPriceSettingTimerService productPriceSettingTimerService;

    @Autowired
    public void setProductPriceSettingTimerService(ProductPriceSettingTimerService productPriceSettingTimerService) {
        this.productPriceSettingTimerService = productPriceSettingTimerService;
    }

    private CTTLService cttlService;

    @Autowired
    public void setCttlService(CTTLService cttlService) {
        this.cttlService = cttlService;
    }

    private LockService lockService;

    @Autowired
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private VoucherService voucherService;

    @Autowired
    public void setVoucherService(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    private MissionService missionService;

    @Autowired
    public void setMissionService(MissionService missionService) {
        this.missionService = missionService;
    }

    /**
     * Run PROMO lập lịch
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runPromoSchedule() {
//        LogUtil.printDebug("runPromoSchedule: start-" + DateTimeUtils.getNow());
        promoService.runPromoSchedule();
//        LogUtil.printDebug("runPromoSchedule: end-" + DateTimeUtils.getNow());
    }

    /**
     * Run tồn kho
     */
    @Scheduled(cron = "* * * * * ?")
    public void runInventorySchedule() {

    }

    /**
     * Hủy đơn chờ xác nhận
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runCancelOrderWaitingConfirmSchedule() {
//        LogUtil.printDebug("Hủy đơn chờ xác nhận: start-" + DateTimeUtils.getNow());
        orderService.cancelOrderWaitingConfirmOvertimeProcess();
//        LogUtil.printDebug("Hủy đơn chờ xác nhận: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hủy đơn chờ duyệt
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runCancelOrderWaitingApproveSchedule() {
//        LogUtil.printDebug("Hủy đơn chờ duyệt: start-" + DateTimeUtils.getNow());
        orderService.cancelOrderWaitingApproveOvertimeProcess();
//        LogUtil.printDebug("Hủy đơn chờ duyệt: end-" + DateTimeUtils.getNow());
    }

    /**
     * Gọi lại lệnh thất bại khi reload cache qua APP
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runReloadCache() {
//        LogUtil.printDebug("runReloadCache: start-" + DateTimeUtils.getNow());
        reloadService.runReloadCache();
//        LogUtil.printDebug("runReloadCache: end-" + DateTimeUtils.getNow());
    }

    /**
     * Tính công nợ cuối ngày
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void runDeptAgencyDateByEndDate() {
//        LogUtil.printDebug("runDeptAgencyDateByEndDate: start-" + DateTimeUtils.getNow());
        deptService.runDeptAgencyDateByEndDate();
//        LogUtil.printDebug("runDeptAgencyDateByEndDate: end-" + DateTimeUtils.getNow());
    }

    /**
     * Reset công nợ đầu ngày
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDeptAgencyDateByStartDate() {
//        LogUtil.printDebug("runDeptAgencyDateByStartDate: start-" + DateTimeUtils.getNow());
        deptService.runDeptAgencyDateByStartDate();
//        LogUtil.printDebug("runDeptAgencyDateByStartDate: end-" + DateTimeUtils.getNow());
    }

    /**
     * Cập nhật tồn kho cuối ngày
     */
    @Scheduled(cron = "0 40 23 * * ?")
    public void runWarehouseInfoByEndDate() {
//        LogUtil.printDebug("runWarehouseInfoByEndDate: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.warehouseService.runWarehouseInfoByEndDate();
//        LogUtil.printDebug("runWarehouseInfoByEndDate: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runWarehouseInfoByEndDate: end-" + DateTimeUtils.getNow());
    }

    /**
     * Khởi tạo tồn kho đầu ngày
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runWarehouseInfoByStartDate() {
//        LogUtil.printDebug("runWarehouseInfoByStartDate: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.warehouseService.runWarehouseInfoByStartDate();
//        LogUtil.printDebug("runWarehouseInfoByStartDate: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runWarehouseInfoByStartDate: end-" + DateTimeUtils.getNow());
    }

    /**
     * Trừ Acoin khi phát sinh nợ NQH
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDecreaseACoinForNQH() {
//        LogUtil.printDebug("runDecreaseACoinForNQH: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.acoinService.runDecreaseACoinForNQH();
//        LogUtil.printDebug("runDecreaseACoinForNQH: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runDecreaseACoinForNQH: end-" + DateTimeUtils.getNow());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDeptOrderNQH() {
//        LogUtil.printDebug("runDecreaseACoinForNQH: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.deptService.runDeptOrderNQH();
//        LogUtil.printDebug("runDecreaseACoinForNQH: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runDecreaseACoinForNQH: end-" + DateTimeUtils.getNow());
    }

    /**
     * Ghi nhận công nợ đối với đơn hẹn giao ở trạng thái đang giao hàng
     */
//    @Scheduled(cron = "0 0 23 * * ?")
//    public void runIncreaseDeptForOrderSchedule() {
//        LogUtil.printDebug("runIncreaseDeptForOrderSchedule: start-" + DateTimeUtils.getNow());
//        ClientResponse clientResponse = this.deptService.runIncreaseDeptForOrderSchedule();
//        LogUtil.printDebug("runIncreaseDeptForOrderSchedule: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runIncreaseDeptForOrderSchedule: end-" + DateTimeUtils.getNow());
//    }

    /**
     * Sai cam kết
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void runMissCommit() {
//        LogUtil.printDebug("runMissCommit: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.deptService.runMissCommit();
//        LogUtil.printDebug("runMissCommit: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runMissCommit: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn dừng áp dụng thiết lập ẩn hiện đối tượng
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStopProductVisibilitySettingSchedule() {
//        LogUtil.printDebug("runStopProductVisibilitySettingSchedule: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.productService.runStopProductVisibilitySettingSchedule();
//        LogUtil.printDebug("runStopProductVisibilitySettingSchedule: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runStopProductVisibilitySettingSchedule: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng thiết lập ẩn hiện đối tượng
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartProductVisibilitySettingSchedule() {
//        LogUtil.printDebug("runStartProductVisibilitySettingSchedule: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.productService.runStartProductVisibilitySettingSchedule();
//        LogUtil.printDebug("runStartProductVisibilitySettingSchedule: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("runStartProductVisibilitySettingSchedule: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn dừng áp dụng thiết lập giá đối tượng
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStopProductPriceSettingSchedule() {
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập giá đối tượng: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.priceService.runStopProductPriceSettingSchedule();
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập giá đối tượng: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập giá đối tượng: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng thiết lập giá đối tượng
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartProductPriceSettingSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.priceService.runStartProductPriceSettingSchedule();
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng thiết lập banner
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartBannerSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thiết lập banner: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.bannerService.runStartBannerSchedule();
//        LogUtil.printDebug("Hẹn áp dụng thiết lập banner: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thiết lập banner: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn dừng áp dụng thiết lập banner
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStopBannerSchedule() {
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập banner: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.bannerService.runStopBannerSchedule();
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập banner: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn dừng áp dụng thiết lập banner: end-" + DateTimeUtils.getNow());
    }

    /**
     * Push thông báo chờ push
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void runStartNotifyWaitingPushSchedule() {
//        LogUtil.printDebug("Push thông báo chờ push: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.notifyService.runStartNotifyWaitingPushSchedule();
//        LogUtil.printDebug("Push thông báo chờ push: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Push thông báo chờ push: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn chạy thông báo thiết lập
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartNotifySettingSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.notifyService.runStartNotifySettingSchedule();
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: end-" + DateTimeUtils.getNow());
    }

    /**
     * Thông báo nợ quá hạn
     */
    @Scheduled(cron = "0 0 14 * * ?")
    public void runPushNotifyNQH() {
//        LogUtil.printDebug("Thông báo nợ quá hạn: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.notifyService.runPushNotifyNQH();
//        LogUtil.printDebug("Thông báo nợ quá hạn: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Thông báo nợ quá hạn: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng giá chung
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runProductPriceTimer() {
//        LogUtil.printDebug("Thông báo nợ quá hạn: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.priceService.runProductPriceTimer();
//        LogUtil.printDebug("Thông báo nợ quá hạn: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Thông báo nợ quá hạn: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn dừng áp dụng chi tiết giá thiết lập
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStopProductPriceSettingDetailSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.priceService.runStopProductPriceSettingDetailSchedule();
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng chi tiết giá thiết lập
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartProductPriceSettingDetailSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.priceService.runStartProductPriceSettingDetailSchedule(
        );
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thiết lập giá đối tượng: end-" + DateTimeUtils.getNow());
    }

    /**
     * Hẹn áp dụng giá thiết lập
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runStartProductPriceSettingTimerSchedule() {
        ClientResponse clientResponse = this.productPriceSettingTimerService.runStartProductPriceSettingTimerSchedule();
    }

    /**
     * Chuyển trạng thái CTTL
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runPromoCTTLStatus() {
        ClientResponse clientResponse =
                this.promoService.runPromoCTTLStatus();
    }

    /**
     * Trừ số lần được cam kết đối với NQH quá số ngày cho phép
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runNQHOver() {
        ClientResponse clientResponse = this.deptService.runNQHOver();
    }

    /**
     * Khóa đại lý theo thiết lập
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runLockSchedule() {
        ClientResponse clientResponse = this.lockService.runLockSchedule();
    }

    /**
     * Tạo thông báo sắp đến ngày khóa
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void runPushNotifyWarningLock() {
        ClientResponse clientResponse = this.lockService.runPushNotifyWarningLock();
    }

    /*
     * Hẹn áp dụng phiếu cập nhật khóa
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void runAgencyLockSettingSchedule() {
        ClientResponse clientResponse = this.lockService.runAgencyLockSettingSchedule();
    }

    /*
     * Hẹn áp dụng phiếu cập nhật khóa
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void runCheckCompleteOrderAppointment() {
        ClientResponse clientResponse = this.orderService.runCheckCompleteAgencyOrder();
    }

    /**
     * Kiểm tra lịch sử DTT
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void runCheckDTTHistory() {
        this.deptService.runCheckDTTHistory();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void runExpireVoucher() {
        ClientResponse clientResponse = this.voucherService.runExpireVoucher();
    }

    /**
     * Push thông báo hẹn giờ
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void runStartNotifyWaitingSchedule() {
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: start-" + DateTimeUtils.getNow());
        ClientResponse clientResponse = this.notifyService.runStartNotifyWaitingSchedule();
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: " + JsonUtils.Serialize(clientResponse));
//        LogUtil.printDebug("Hẹn áp dụng thông báo thiết lập: end-" + DateTimeUtils.getNow());
    }
}