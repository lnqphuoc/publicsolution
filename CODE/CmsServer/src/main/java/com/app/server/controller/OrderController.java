package com.app.server.controller;

import com.app.server.constants.path.OrderPath;
import com.app.server.data.SessionData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.order.*;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class OrderController extends BaseController {
    @RequestMapping(value = OrderPath.CREATE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tạo đơn hàng giao ngay", notes = "")
    public ClientResponse createOrder(
            @RequestBody CreateOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.createOrderInstantly(sessionData, request);
    }

    @RequestMapping(value = OrderPath.EDIT_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa đơn hàng", notes = "")
    public ClientResponse editOrder(
            @RequestBody EditOrderProductRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.editOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_PURCHASE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách đơn đặt hàng", notes = "")
    public ClientResponse filterPurchaseOrder(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.filterPurchaseOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_CONFIRM, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu xác nhận đặt hàng", notes = "")
    public ClientResponse filterOrderConfirmation(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.filterOrderConfirmation(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_CONFIRM_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu xác nhận đặt hàng", notes = "")
    public ClientResponse filterOrderConfirmationProduct(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.filterOrderConfirmationProduct(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_DELIVERY_PLAN_PRODUCT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách kế hoạch giao hàng", notes = "")
    public ClientResponse filterOrderDeliveryPlanProduct(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.filterOrderDeliveryPlanProduct(sessionData, request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy thông tin đơn hàng", notes = "")
    public ClientResponse getOrderInfo(
            BasicRequest request) {
        return this.orderService.getOrderInfo(this.getSessionData(), request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_PREPARE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận đơn đặt hàng chuyển qua soạn hàng", notes = "")
    public ClientResponse confirmPrepareOrder(
            @RequestBody ConfirmPrepareOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmPrepareOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_COMMIT_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận cam kết", notes = "")
    public ClientResponse confirmCommitOrder(
            @RequestBody ConfirmPrepareOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmPrepareOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_DELIVERY_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận đơn đặt hàng đã được giao hàng", notes = "")
    public ClientResponse confirmDeliveryOrder(
            @RequestBody ConfirmDeliveryOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmDeliveryOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_DELIVERY_OC, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận oc đặt hàng đã được giao hàng", notes = "")
    public ClientResponse confirmDeliveryOC(
            @RequestBody ConfirmDeliveryOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmDeliveryOC(sessionData, request);
    }

    @RequestMapping(value = OrderPath.ESTIMATE_COST_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tính toán đơn đặt hàng", notes = "")
    public ClientResponse estimateCostOrder(
            @RequestBody EstimateCostOrderRequest request) {
        return this.orderService.estimateCostOrder(request);
    }

    @RequestMapping(value = OrderPath.CANCEL_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Hủy đơn hàng", notes = "")
    public ClientResponse cancelOrder(
            @RequestBody CancelOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.cancelOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.LOCK_TIME_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Khóa thời gian đại lý điều chỉnh đơn hàng", notes = "")
    public ClientResponse lockTimeOrder(
            @RequestBody LockTimeOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.lockTimeOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.REFUSE_ORDER_TO_AGENCY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Trả đơn hàng về lại đại lý", notes = "")
    public ClientResponse refuseOrderToAgency(
            @RequestBody RefuseOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.refuseOrderToAgency(sessionData, request);
    }

    @RequestMapping(value = OrderPath.REJECT_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Từ chối duyệt", notes = "")
    public ClientResponse rejectOrder(
            @RequestBody RefuseOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.rejectOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_SHIPPING_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận đang giao hàng", notes = "")
    public ClientResponse confirmShippingOrder(
            @RequestBody ConfirmShippingOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmShippingOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CONFIRM_SHIPPING_OC, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Xác nhận đang giao hàng", notes = "")
    public ClientResponse confirmShippingOC(
            @RequestBody ConfirmShippingOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.confirmShippingOC(sessionData, request);
    }

    @RequestMapping(value = OrderPath.RESPONSE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Phản hồi đơn soạn hàng", notes = "")
    public ClientResponse responseOrder(
            @RequestBody RefuseOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.responseOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.CREATE_REQUEST_APPROVE_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Gửi yêu cầu duyệt đơn hàng", notes = "")
    public ClientResponse createRequestApproveOrder(
            @RequestBody CreateRequestApproveOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.createRequestApproveOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.APPROVE_REQUEST_ORDER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duyệt đơn hàng", notes = "")
    public ClientResponse approveRequestOrder(
            @RequestBody ApproveRequestOrderRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.approveRequestOrder(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_TEMP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách đơn hàng tạm", notes = "")
    public ClientResponse filterOrderTemp(
            @RequestBody FilterListRequest request) {
        return this.orderService.filterOrderTemp(request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_TEMP_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy thông tin đơn hàng tạm", notes = "")
    public ClientResponse getOrderTempInfo(
            BasicRequest request) {
        return this.orderService.getOrderTempInfo(this.getSessionData(), request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_DELIVERY_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy thông tin đơn hàng", notes = "")
    public ClientResponse getOrderDeliveryInfo(
            BasicRequest request) {
        return this.orderService.getAgencyOrderConfirm(this.getSessionData(), request);
    }

    @RequestMapping(value = OrderPath.EDIT_REQUEST_DELIVERY_DATE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh ngày hẹn giao", notes = "")
    public ClientResponse editRequestDeliveryDate(
            @RequestBody EditRequestDeliveryDateRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.editRequestDeliveryDate(sessionData, request);
    }

    @RequestMapping(value = "/order/check_san_sale", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Kiểm tra kết quả tính toán săn sale", notes = "")
    public ClientResponse checkSanSale(
            @RequestBody EstimateCostOrderRequest request) {
        return this.orderService.checkSanSale(request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_APPOINTMENT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu hẹn giao", notes = "")
    public ClientResponse filterOrderAppointment(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderAppointmentService.filterOrderAppointment(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_DELIVERY_BILL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách phiếu giao hàng", notes = "")
    public ClientResponse filterOrderDelivery(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderDeliveryService.filterOrderDelivery(sessionData, request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_DELIVERY_BILL_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết đơn giao hàng", notes = "")
    public ClientResponse getOrderDeliveryBillInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderDeliveryService.getOrderDeliveryBillInfo(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_ORDER_CONTRACT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách đơn hợp đồng", notes = "")
    public ClientResponse filterOrderContract(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderContractService.filterOrderContract(sessionData, request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_CONTRACT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết đơn hợp đồng", notes = "")
    public ClientResponse getOrderContractInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.getOrderInfo(sessionData, request);
    }

    @RequestMapping(value = OrderPath.GET_ORDER_APPOINTMENT_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết phiếu hẹn giao", notes = "")
    public ClientResponse getOrderAppointmentInfo(
            BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.getOrderInfo(sessionData, request);
    }

    @RequestMapping(value = OrderPath.SYNC_ORDER_TO_BRAVO,
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Đồng bộ đơn hàng qua Bravo", notes = "")
    public ClientResponse syncOrderToBravo(
            @RequestBody BasicRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.syncOrderToBravo(sessionData, request);
    }

    @RequestMapping(value = OrderPath.FILTER_HBTL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Danh sách hàng bán trả lại", notes = "")
    public ClientResponse filterHBTL(
            @RequestBody FilterListRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.filterHBTL(sessionData, request);
    }

    @RequestMapping(value = OrderPath.GET_HBTL_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết hàng bán trả lại", notes = "")
    public ClientResponse getHBTLInfo(
            BasicRequest request) {
        return this.orderService.getHBTLInfo(this.getSessionData(), request);
    }

    @RequestMapping(value = "/order/check_complete", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chi tiết hàng bán trả lại", notes = "")
    public ClientResponse checkComplete(
            BasicRequest request) {
        return this.orderService.runCheckCompleteAgencyOrder();
    }

    @RequestMapping(value = OrderPath.SEARCH_VOUCHER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Tìm kiếm voucher", notes = "")
    public ClientResponse searchVoucher(
            @RequestBody FilterListByIdRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.searchVoucher(sessionData, request);
    }

    @RequestMapping(value = OrderPath.EDIT_PLAN_DELIVERY_DATE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Điều chỉnh ngày giao hàng dự kiên", notes = "")
    public ClientResponse editPlanDeliveryDate(
            @RequestBody EditRequestDeliveryDateRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.editPlanDeliveryDate(sessionData, request);
    }

    @RequestMapping(value = OrderPath.EDIT_OC_NO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "EDIT_OC_NO", notes = "")
    public ClientResponse editOCNo(
            @RequestBody EditOCNoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.editOCNo(sessionData, request);
    }

    @RequestMapping(value = OrderPath.EDIT_PO_NO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "EDIT_PO_NO", notes = "")
    public ClientResponse editPONo(
            @RequestBody EditOCNoRequest request) {
        SessionData sessionData = this.getSessionData();
        return this.orderService.editPONo(sessionData, request);
    }
}