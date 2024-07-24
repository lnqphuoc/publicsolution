package com.app.server.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ygame.framework.common.LogUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingService {
    private Gson gson = new Gson();

    /**
     * Lưu log request
     */
    public void logRequest(HttpServletRequest request) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Map<String, String> parameters = buildParametersMap(request);
            stringBuilder.append("method=[").append(request.getMethod()).append("] ");
            stringBuilder.append("path=[").append(request.getRequestURI()).append("] ");
            stringBuilder.append("headers=[").append(buildHeadersMap(request)).append("] ");
            stringBuilder.append("Query=[").append("" + request.getQueryString() + "] ");
            if (!parameters.isEmpty())
                stringBuilder.append("parameters=[").append(parameters).append("] ");
            stringBuilder.append("body=[" + gson.toJson((request.getParameterMap())) + "]");
            LogUtil.getLogger("data").error(stringBuilder.toString());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    /**
     * Lưu log response
     */
    public void logResponse(HttpServletRequest request, HttpServletResponse response, Object body) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            HttpSession session = request.getSession();
            stringBuilder.append("[SERVICE] RESPONSE " + session.getId() + " ");
            stringBuilder.append("method=[").append(request.getMethod()).append("] ");
            stringBuilder.append("path=[").append(request.getRequestURI()).append("] ");
            stringBuilder.append("responseHeaders=[").append(buildHeadersMap(response)).append("] ");
            stringBuilder.append("responseBody=[").append(gson.toJson(body)).append("] ");
            LogUtil.getLogger("data").info(stringBuilder.toString());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> map = new HashMap<>();
        try {
            Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String key = parameterNames.nextElement();
                String value = httpServletRequest.getParameter(key);
                map.put(key, value);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        try {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();
        try {
            Collection<String> headerNames = response.getHeaderNames();
            for (String header : headerNames) {
                map.put(header, response.getHeader(header));
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return map;
    }
}