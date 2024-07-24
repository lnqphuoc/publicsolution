package com.app.server.data.request.staff;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class EditStaffRequest {
    private int id;
    private String full_name;
    private String phone;
    private String username;
    private String email;
    private String address;
    private String password;
    private int staff_group_permission_id;
    private StaffAgencyData agencyData = new StaffAgencyData();
    private StaffOrderData orderData = new StaffOrderData();

    public ClientResponse validate() {
        if (id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STAFF_NOT_FOUND);
        }
        if (StringUtils.isBlank(full_name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
        }
        if (StringUtils.isBlank(username) || username.contains(" ")) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USERNAME_INVALID);
        }

        if (password != null && !password.isEmpty() && (StringUtils.isBlank(password) ||
                password.contains(" ") ||
                password.length() < 6 ||
                password.length() > 50)
        ) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PASSWORD_INVALID);
        }

        if (staff_group_permission_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GROUP_PERMISSION_NOT_FOUND);
        }

        if (orderData == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_DATA_MANAGE_NOT_EMPTY);
        }

        if (agencyData == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_DATA_MANAGE_NOT_EMPTY);
        }

        if (agencyData.getType().equals("LIST")) {
            if (agencyData.getAgency_ids().isEmpty()) {
                if (agencyData.getBusiness_department_ids().isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BUSINESS_DEPARTMENT_NOT_EMPTY);
                }
                if (agencyData.getRegion_ids().isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REGION_INVALID);
                }
                if (agencyData.getCity_ids().isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CITY_INVALID);
                }
            } else {
                if (!(agencyData.getBusiness_department_ids().isEmpty()
                        && agencyData.getRegion_ids().isEmpty()
                        && agencyData.getCity_ids().isEmpty())) {
                    if (agencyData.getBusiness_department_ids().isEmpty()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BUSINESS_DEPARTMENT_NOT_EMPTY);
                    }
                    if (agencyData.getRegion_ids().isEmpty()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REGION_INVALID);
                    }
                    if (agencyData.getCity_ids().isEmpty()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CITY_INVALID);
                    }
                }
            }
        }

        if (this.agencyData.validate().failed()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }

        return ClientResponse.success(null);
    }
}