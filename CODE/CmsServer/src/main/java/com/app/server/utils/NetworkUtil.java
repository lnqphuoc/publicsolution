package com.app.server.utils;

import com.ygame.framework.common.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtil {
    public static String getResponse(String url) {
        try {
            URL _url = new URL(url);
            final URLConnection connection = _url.openConnection();
            connection.setDoOutput(true);
            try (InputStream replyStream = connection.getInputStream()) {
                StringBuilder sb;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(replyStream))) {
                    sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append(System.getProperty("line.separator"));
                    }
                }
                return sb.toString();
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return "";
    }
}