package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoCTTLStatus {
    DELETE(-1, "Xóa"),
    DRAFT(0, "Nháp"),
    WAITING(1, "Đã kích hoạt"),
    RUNNING(2, "Đang chạy"),
    STOPPED(3, "Kết thúc"),
    CANCEL(4, "Hủy"),
    WAITING_PAYMENT(5, "Chờ thanh toán"),
    WAITING_REWARD(6, "Chờ trả thưởng");
    private int id;
    private String key;
    private String label;

    PromoCTTLStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static PromoCTTLStatus from(int id) {
        for (PromoCTTLStatus type : PromoCTTLStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static String canSort() {
        return RUNNING.getId() + "," +
                WAITING.getId() + "," +
                RUNNING.getId();
    }
}