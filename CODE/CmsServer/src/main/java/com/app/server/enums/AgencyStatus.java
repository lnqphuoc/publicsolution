package com.app.server.enums;

import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Setter;

import java.util.List;

public enum AgencyStatus {
    WAITING_APPROVE(0, "Chờ duyệt"),
    APPROVED(1, "Đang hoạt động"),
    INACTIVE(2, "Ngưng hoạt động"),
    LOCK(3, "Khóa App"),
    REJECT(4, "Từ chối");

    private int value;
    private String name;

    AgencyStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getAllToString() {
        String result = "";
        for (AgencyStatus agencyStatus : AgencyStatus.values()) {
            if (!result.isEmpty()) {
                result += ",";
            }
            result += agencyStatus.getValue();
        }
        return result;
    }

    public static AgencyStatus from(int status) {
        try {
            for (AgencyStatus agencyStatus : AgencyStatus.values()) {
                if (agencyStatus.getValue() == status) {
                    return agencyStatus;
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug(Module.AGENCY.name(), e);
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static String getNameFrom(int value) {
        for (AgencyStatus agencyStatus : AgencyStatus.values()) {
            if (agencyStatus.getValue() == value) {
                return agencyStatus.name;
            }
        }

        return "";
    }
}