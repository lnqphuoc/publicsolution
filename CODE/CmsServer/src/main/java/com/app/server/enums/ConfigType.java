package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ConfigType {
    GTTT("GTTT", "Giá trị tối thiểu", "5000000"),
    NO_XAU_NUMBER_DATE("NO_XAU_NUMBER_DATE", "Số ngày nợ quá hạn bị tính nợ xấu", "45"),
    ACOIN_RATE_DEFAULT("ACOIN_RATE_DEFAULT", "Tỉ lệ tính acoin mặc định", "100000"),
    COMMIT_LIMIT("COMMIT_LIMIT", "Số lần được sai cam kết", "3"),
    HOT_COMMON("HOT_COMMON", "Số lượng bán chạy trên trang chủ", "30"),
    HOT_AGENCY("HOT_AGENCY", "Số lượng bán chạy trên gợi ý cho bạn", "50"),
    APP_ACTIVE_REQUEST_LOG("APP_ACTIVE_REQUEST_LOG", "Kích hoạt lưu log request trên server app", "1"),
    CMS_ACTIVE_REQUEST_LOG("CMS_ACTIVE_REQUEST_LOG", "Kích hoạt lưu log request trên server cms", "1"),
    ORDER_SCHEDULE("ORDER_SCHEDULE", "Hạn mục hẹn giao", "17"),
    NUMBER_DATE_SCHEDULE_DELIVERY("NUMBER_DATE_SCHEDULE_DELIVERY", "Số ngày được tính là đơn hẹn giao", "2"),
    PUSH_NOTIFY_NQH("PUSH_NOTIFY_NQH", "Thông báo nợ quá hạn", "1"),
    PUSH_NOTIFY_LOCK("PUSH_NOTIFY_LOCK", "Thời hạn thông báo khóa đại lý", "0"),
    CATALOG_N_REGISTER("CATALOG_N_REGISTER", "Giới hạn số lượng catalog được chọn khi đăng ký", "5"),
    CATALOG_N_ADD("CATALOG_N_ADD", "Giới hạn số lượng catalog được chọn khi mở thêm", "3"),
    ;
    private String code;
    private String name;
    private String value;

    ConfigType(String code, String name, String value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }
}