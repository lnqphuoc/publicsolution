package com.app.server.securities;

import com.app.server.response.ClientResponse;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.ygame.framework.common.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {
    private AuthenticationUtil authenticationUtil;

    @Autowired
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        try {
            ClientResponse clientResponse = new ClientResponse();
            clientResponse.setStatus(ResponseStatus.FAIL);
            clientResponse.setMessage(ResponseMessage.USER_FORBIDDEN.getValue());
            authenticationUtil.sendResponse(response, clientResponse);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }
}