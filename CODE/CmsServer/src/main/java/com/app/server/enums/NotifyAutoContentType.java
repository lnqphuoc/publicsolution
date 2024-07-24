package com.app.server.enums;

import lombok.Getter;

@Getter
public enum NotifyAutoContentType {
    CANCEL_ORDER_WAITING_CONFIRM(1, "CANCEL_ORDER_WAITING_CONFIRM", "DON_HANG", "Đơn đặt hàng <Mã ĐH> của Quý khách đã hủy vì không thanh toán đúng hạn."),
    APPROVE_AGENCY(2, "APPROVE_AGENCY", "DUYET_TAI_KHOAN", "Tài khoản đã được kích hoạt thành công, Quý khách vui lòng truy cập ứng dụng để sử dụng dịch vụ"),
    CANCEL_ORDER(3, "CANCEL_ORDER", "DON_HANG", "Đơn đặt hàng <Mã ĐH> của Quý khách đã hủy vì <Lý do từ chối>."),
    REFUSE_ORDER_COMMIT(4, "REFUSE_ORDER_COMMIT", "DON_HANG", "Đơn đặt hàng <Mã ĐH> của Quý khách đã bị từ chối vì cam kết không phù hợp."),
    REFUSE_ORDER(5, "REFUSE_ORDER", "DON_HANG",
            "Đơn đặt hàng <Mã ĐH> đã được trả về cho Quý khách điều chỉnh, vui lòng hoàn tất đơn hàng"),
    APPROVE_ORDER_COMMIT(6, "APPROVE_ORDER_COMMIT", "DON_HANG",
            "Cam kết thanh toán của Quý Khách đã được chấp nhận, Anh Tin sẽ giao hàng trong thời gian sớm nhất."),
    DELIVERY_ORDER(7, "DELIVERY_ORDER", "DON_HANG", "DELIVERY_ORDER"),
    CREATE_REQUEST_APPROVE_ORDER(8, "CREATE_REQUEST_APPROVE_ORDER", "DON_HANG", "Anh Tin đã tạo giúp Quý khách đơn đặt hàng <mã đơn hàng>. Vui lòng kiểm tra đơn hàng."),
    APPROVE_TRANSACTION_THANH_THANH(9, "APPROVE_TRANSACTION_THANH_THANH", "CONG_NO_THANH_TOAN",
            "Anh Tin đã nhận được khoản thanh toán <số tiền> của Quý khách."),
    CONFIRM_PREPARE_ORDER(10, "CONFIRM_PREPARE_ORDER", "DON_HANG",
            "Đơn đặt hàng <Mã ĐH> của Quý khách đã được tiếp nhận và đang xử lý."),
    APPROVE_TRANSACTION_INCREASE(11, "APPROVE_TRANSACTION_INCREASE", "CONG_NO_TANG_GIAM", ""),
    APPROVE_TRANSACTION_DECREASE(12, "APPROVE_TRANSACTION_DECREASE", "CONG_NO_TANG_GIAM", ""),
    DEPT_AGENCY_NQH(13, "DEPT_AGENCY_NQH", "CONG_NO_NO_QUA_HAN", ""),
    CONG_NO_CHI_TIET(14, "CONG_NO_CHI_TIET", "CONG_NO_NO_QUA_HAN", ""),
    THACH_GIA_CHI_TIET(15, "THACH_GIA_CHI_TIET", "THACH_GIA_CHI_TIET", ""),
    MEMBERSHIP(15, "THACH_GIA_CHI_TIET", "THACH_GIA_CHI_TIET", "");;

    private int id;
    private String code;
    private String type;
    private String label;

    NotifyAutoContentType(int id, String code, String type, String label) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.label = label;
    }
}