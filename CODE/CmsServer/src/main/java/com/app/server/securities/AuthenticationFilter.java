package com.app.server.securities;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.database.AgencyDB;
import com.app.server.database.StaffDB;
import com.app.server.enums.PermissionStatus;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.YesNoStatus;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.service.LoggingService;
import com.app.server.utils.JwtUtil;
import com.mysql.cj.log.Log;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationFilter
        extends OncePerRequestFilter {
    private JwtUtil jwtUtil;
    private AgencyDB agencyDB;
    private StaffDB staffDB;
    private AuthenticationUtil authenticationUtil;

    private DataManager dataManager;

    private LoggingService loggingService;

    @Autowired
    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setJWTUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationUtil authenticationService) {
        this.authenticationUtil = authenticationService;
    }

    @Autowired
    public void setStaffDB(StaffDB staffDB) {
        this.staffDB = staffDB;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (this.dataManager.getConfigManager().isActiveSaveRequestLog()) { // kiểm tra lưu log request
                loggingService.logRequest(request);
            }

            String token = getTokenFromRequest(request);
            if (StringUtils.isNotBlank(token)) {
                SessionData sessionData = jwtUtil.getData(token);
                if (sessionData == null) {
                    authenticationUtil.sendResponse(response, ClientResponse.fail(ResponseStatus.LOGIN_EXPIRED, ResponseMessage.LOGIN_EXPIRED));
                    return;
                }
                ClientResponse crPermission = this.checkPermission(sessionData, request.getServletPath());
                if (crPermission.success()) {
                    authenticationUtil.setAuthentication(request, sessionData.getName(), token, sessionData);
                } else {
                    authenticationUtil.sendResponse(response, crPermission);
                    return;
                }
            } else {
                if (request.getServletPath().contains("/utility/upload_file_base64")) {
                    if (getSecretKeyFromRequest(request) == null || !getSecretKeyFromRequest(request).equals(ConfigInfo.RELOAD_CACHE_KEY)) {
                        authenticationUtil.sendResponse(response, ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN));
                        return;
                    } else {
                        authenticationUtil.setAuthentication(request, "APP", ConfigInfo.RELOAD_CACHE_KEY, new SessionData());
                    }
                } else if (request.getServletPath().contains("/app/")) {
                    if (getSecretKeyFromRequest(request) == null || !getSecretKeyFromRequest(request).equals(ConfigInfo.RELOAD_CACHE_KEY)) {
                        authenticationUtil.sendResponse(response, ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN));
                        return;
                    } else {
                        authenticationUtil.setAuthentication(request, "APP", ConfigInfo.RELOAD_CACHE_KEY, new SessionData());
                    }
                } else if (request.getServletPath().contains("/bravo/")) {
                    if (getSecretKeyFromRequest(request) == null || !getSecretKeyFromRequest(request).equals(ConfigInfo.RELOAD_CACHE_KEY)) {
                        authenticationUtil.sendResponse(response, ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN));
                        return;
                    } else {
                        authenticationUtil.setAuthentication(request, "APP", ConfigInfo.RELOAD_CACHE_KEY, new SessionData());
                    }
                } else if (!request.getServletPath().contains("/auth/login")
                        && !request.getServletPath().contains("swagger")
                        && !request.getServletPath().contains("v2/api")
                        && !request.getServletPath().contains("/export/download")
                ) {
                    authenticationUtil.sendResponse(response, ClientResponse.fail(ResponseStatus.LOGIN_EXPIRED, ResponseMessage.USER_FORBIDDEN));
                    return;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("Auth", ex);
        }
        filterChain.doFilter(request, response);
    }

    // get token from request
    private String getTokenFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(headerAuth) && headerAuth.startsWith("Bearer "))
            return headerAuth.substring(7, headerAuth.length());
        return null;
    }

    private String getSecretKeyFromRequest(HttpServletRequest request) {
        String key = request.getHeader("key");
        return key;
    }

    private Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();

        @SuppressWarnings("rawtypes")
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            map.put(header, response.getHeader(header));
        }

        return map;
    }

    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }

    private ClientResponse checkPermission(SessionData sessionData, String path) {
        JSONObject staff = this.staffDB.getStaffInfo(sessionData.getId());
        if (staff == null) {
            return ClientResponse.fail(ResponseStatus.LOGIN_EXPIRED, ResponseMessage.USER_FORBIDDEN);
        }

        if (ConvertUtils.toInt(staff.get("force_update_status")) == 1) {
            return ClientResponse.fail(ResponseStatus.LOGIN_EXPIRED, ResponseMessage.USER_FORBIDDEN);
        }

        JSONObject cms_api = this.staffDB.getCmsApi(path);
        if (cms_api == null) {
            return ClientResponse.success(null);
        }

        JSONObject staff_group_permission = this.staffDB.getGroupPermissionInfo(
                ConvertUtils.toInt(staff.get("staff_group_permission_id"))
        );
        if (staff_group_permission == null ||
                ConvertUtils.toInt(staff_group_permission.get("status")) != PermissionStatus.ACTIVATED.getId()) {
            return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
        }

        if (ConvertUtils.toInt(staff_group_permission.get("full_permission")) == YesNoStatus.YES.getValue()) {
            return ClientResponse.success(null);
        }

        JSONObject permission = this.staffDB.getActionDetailByActionIdAndGroupId(
                ConvertUtils.toInt(cms_api.get("cms_action_id")),
                ConvertUtils.toInt(staff.get("staff_group_permission_id"))
        );
        if (permission == null) {
            return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
        }

        if (ConvertUtils.toInt(permission.get("allow")) == YesNoStatus.YES.getValue()) {
            return ClientResponse.success(null);
        }

        return ClientResponse.fail(
                "BUTTON".equals(ConvertUtils.toString(permission.get("type"))) ?
                        ResponseStatus.NOT_PERMISSION : ResponseStatus.NOT_PERMISSION,
                ResponseMessage.USER_FORBIDDEN);
    }
}