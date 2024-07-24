package com.app.server.response;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;

public class ClientResponse {
    private ResponseStatus status;
    private String message;
    private Object data;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static ClientResponse success(Object data) {
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setStatus(ResponseStatus.SUCCESS);
        clientResponse.setMessage(ResponseMessage.SUCCESS.getValue());
        clientResponse.setData(data);
        return clientResponse;
    }

    public static ClientResponse fail(ResponseStatus status, ResponseMessage message) {
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setStatus(status);
        clientResponse.setMessage(message.getKey());
        return clientResponse;
    }

    public static ClientResponse error(ResponseStatus status, ResponseMessage message, Object error) {
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setStatus(status);
        clientResponse.setMessage("[ERROR] " + error + " (INVALID)");
        return clientResponse;
    }

    public boolean failed() {
        return this.status == ResponseStatus.FAIL;
    }

    public boolean success() {
        return this.status == ResponseStatus.SUCCESS;
    }

    public ClientResponse fail(String message) {
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setStatus(ResponseStatus.FAIL);
        clientResponse.setMessage(message);
        return clientResponse;
    }
}