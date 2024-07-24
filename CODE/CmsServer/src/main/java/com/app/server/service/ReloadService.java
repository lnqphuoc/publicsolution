package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseConstants;
import com.app.server.data.dto.notify.NotifyPopupData;
import com.app.server.database.ReloadCacheDB;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReloadService {
    private ReloadCacheDB reloadCacheDB;

    @Autowired
    public void setReloadCacheDB(ReloadCacheDB reloadCacheDB) {
        this.reloadCacheDB = reloadCacheDB;
    }

    public void callAppServerReload(String type, int cache_id) {
        this.callAppServerReload(type, cache_id, reloadCacheDB, 0);
    }

    public void callAppServerReloadFailed(String type, int cache_id, int id) {
        this.callAppServerReload(type, cache_id, reloadCacheDB, id);
    }

    public void callAppServerReload(String type, int cache_id, ReloadCacheDB reloadCacheDB, int id) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.RELOAD_CACHE_URL + "?key=" + ConfigInfo.RELOAD_CACHE_KEY + "&type=" + type + "&id=" + cache_id;
                    ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
                    LogUtil.printDebug("CACHE: " + JsonUtils.Serialize(rs));
                    boolean isSucess = false;
                    if (rs != null && rs.success()) {
                        isSucess = true;
                    } else {
                        rs = restTemplate.getForObject(url, ClientResponse.class);
                        if (rs != null && rs.success()) {
                            isSucess = true;
                        } else {
                            rs = restTemplate.getForObject(url, ClientResponse.class);
                            if (rs != null &&
                                    rs.success()) {
                                isSucess = true;
                            }
                        }
                    }

                    if (id == 0) {
                        reloadCacheDB.insertReloadCache(type, cache_id, isSucess == true ? 1 : 0);
                    } else {
                        reloadCacheDB.updateReloadCache(id, type, cache_id, isSucess == true ? 1 : 0);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public void runReloadCache() {
        try {
            List<JSONObject> cacheList = this.reloadCacheDB.getListCacheFailed(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject cache : cacheList) {
                try {
                    this.callAppServerReloadFailed(
                            ConvertUtils.toString(cache.get("type")),
                            ConvertUtils.toInt(cache.get("cache_id")),
                            ConvertUtils.toInt(cache.get("id")));
                } catch (Exception ex) {
                    LogUtil.printDebug("CACHE", ex);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public void pushNotify(
            String firebase_token_data,
            String type,
            String title,
            String body,
            String image) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.PUSH_NOTIFY_URL + "?" +
                            "key=" + ConfigInfo.PUSH_NOTIFY_KEY
                            + "&firebase_token_data=" + firebase_token_data
                            + "&type=" + type
                            + "&title=" + title
                            + "&body=" + body
                            + "&image=" + image
                            + "&env=" + ConfigInfo.ENV_LEVEL
                            + "&id=" + DateTimeUtils.getMilisecondsNow();
                    ClientResponse rs = restTemplate.postForObject(url, null, ClientResponse.class);
                    LogUtil.printDebug("PUSH: " + JsonUtils.Serialize(rs));
                    boolean isSucess = false;
                    if (rs != null && rs.success()) {
                        isSucess = true;
                    } else {
                        rs = restTemplate.postForObject(url, null, ClientResponse.class);
                        if (rs != null && rs.success()) {
                            isSucess = true;
                        } else {
                            rs = restTemplate.postForObject(url, null, ClientResponse.class);
                            if (rs != null &&
                                    rs.success()) {
                                isSucess = true;
                            }
                        }
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public ClientResponse pushPopup(
            String firebase_token_data,
            String type,
            String title,
            String body,
            String image,
            String data,
            long time) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.PUSH_NOTIFY_CMS_URL;
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    JsonObject mission_data = JsonUtils.DeSerialize(data, JsonObject.class);
                    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
                    JsonArray jaFB = JsonUtils.DeSerialize(firebase_token_data, JsonArray.class);
                    requestBody.add("key", ConfigInfo.PUSH_NOTIFY_KEY);
                    requestBody.add("firebase_token_data", jaFB.toString());
                    requestBody.add("type", type);
                    requestBody.add("title", title);
                    requestBody.add("body", body);
                    requestBody.add("data", mission_data.toString());
                    requestBody.add("image", image);
                    requestBody.add("env", ConfigInfo.ENV_LEVEL);
                    requestBody.add("id", ConvertUtils.toString(time));
                    LogUtil.printDebug("PUSH: " + url);
                    LogUtil.printDebug("PUSH: " + requestBody.toString());
                    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, httpEntity, ClientResponse.class);
                    LogUtil.printDebug("PUSH: " + JsonUtils.Serialize(rs));
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ResponseConstants.success;
    }
}