package com.app.server.securities;

import com.app.server.data.SessionData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygame.framework.common.LogUtil;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@Component
public class AuthenticationUtil {
    public void setAuthentication(HttpServletRequest request, String username, String token, SessionData sessionData) {
        CustomUserDetails userDetails = new CustomUserDetails(username, token, new ArrayList<>(), sessionData);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities());
        authenticationToken.setDetails(new
                WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    // send response to client
    public void sendResponse(HttpServletResponse response, Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getWriter(), data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }
}