package com.app.server.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PREPARE(0, "PREPARE"),
    COMPLETE(1, "COMPLETE"),
    WAITING_CONFIRM(2, "WAITING CONFIRM"),
    RETURN_AGENCY(3, "RETURN AGENCY"),
    CANCEL(4, "CANCEL"),
    SHIPPING(5, "SHIPPING"),
    WAITING_APPROVE(6, "WAITING APPROVE"),
    DRAFT(7, "DRAFT"),
    REJECT(8, "REJECT"),
    RESPONSE(9, "RESPONSE"),
    ;
    private int key;
    private String label;

    OrderStatus(int key, String label) {
        this.key = key;
        this.label = label;
    }

    public static OrderStatus from(int key) {
        for (OrderStatus type : OrderStatus.values()) {
            if (type.key == key) {
                return type;
            }
        }
        return null;
    }

    public static String getStatusWaitingShip() {
        return OrderStatus.SHIPPING.getKey()
                + "," + OrderStatus.PREPARE.getKey()
                + "," + OrderStatus.RESPONSE.getKey()
                + "," + OrderStatus.SHIPPING.getKey();
    }

    public static String getStatusWaitingApprove() {
        return OrderStatus.WAITING_CONFIRM.getKey()
                + "," + OrderStatus.WAITING_APPROVE.getKey()
                ;
    }

    public static String getStatusDoing() {
        return OrderStatus.PREPARE.getKey()
                + "," + OrderStatus.WAITING_APPROVE.getKey()
                + "," + OrderStatus.WAITING_CONFIRM.getKey()
                + "," + OrderStatus.SHIPPING.getKey()
                + "," + OrderStatus.RESPONSE.getKey()
                ;
    }

    public static boolean isOrderWaitingShip(int status) {
        if (OrderStatus.SHIPPING.getKey() == status ||
                OrderStatus.PREPARE.getKey() == status ||
                OrderStatus.RESPONSE.getKey() == status) {
            return true;
        }
        return false;
    }

    public static boolean isOrderWaitingApprove(int status) {
        if (OrderStatus.WAITING_CONFIRM.getKey() == status ||
                OrderStatus.WAITING_APPROVE.getKey() == status) {
            return true;
        }
        return false;
    }

    public static String getStatusDeptDoing() {
        return OrderStatus.PREPARE.getKey()
                + "," + OrderStatus.SHIPPING.getKey()
                + "," + OrderStatus.RESPONSE.getKey()
                ;
    }

    public static boolean isStatusDeptDoing(int status) {
        if (OrderStatus.SHIPPING.getKey() == status ||
                OrderStatus.PREPARE.getKey() == status ||
                OrderStatus.RESPONSE.getKey() == status) {
            return true;
        }
        return false;
    }

    public static boolean canPrepareOrder(int status) {
        if (OrderStatus.WAITING_CONFIRM.getKey() == status ||
                OrderStatus.DRAFT.getKey() == status ||
                OrderStatus.RESPONSE.getKey() == status) {
            return true;
        }
        return false;
    }
}