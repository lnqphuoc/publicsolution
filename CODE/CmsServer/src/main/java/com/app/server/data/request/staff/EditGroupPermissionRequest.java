package com.app.server.data.request.staff;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditGroupPermissionRequest {
    private int id;
    private String name = "";
    private List<MenuRequest> menus = new ArrayList<>();

    public ClientResponse validate() {
        if (name.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }

        return ClientResponse.success(null);
    }
}