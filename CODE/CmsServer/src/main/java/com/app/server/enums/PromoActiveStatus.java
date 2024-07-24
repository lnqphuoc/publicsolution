package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoActiveStatus {
    DELETE(-1, "Xóa"),
    DRAFT(0, "Nháp"),
    WAITING(1, "Đã kích hoạt"),
    RUNNING(2, "Đang chạy"),
    STOPPED(3, "Kết thúc"),
    CANCEL(4, "Hủy");
    private int id;
    private String key;
    private String label;

    PromoActiveStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static PromoActiveStatus from(int id) {
        for (PromoActiveStatus type : PromoActiveStatus.values()) {
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