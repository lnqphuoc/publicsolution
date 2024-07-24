package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.Module;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AssignMissionService {
    public ClientResponse assignMissionForOneAgency(
            int agency_id,
            String period_type,
            long start_time,
            long end_time,
            long end_tl_time) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /mission/generate_one
                     *     + key : reload key
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/generate_one";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("type", period_type);
                    body.add("agency_id", ConvertUtils.toString(agency_id));
                    body.add("start_time", ConvertUtils.toString(start_time));
                    body.add("end_time", ConvertUtils.toString(end_time));
                    body.add("end_tl_time", ConvertUtils.toString(end_tl_time));
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(rs));
                    if (rs == null) {

                    }
                }
            };
            thread.start();
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse assignMissionBySetting(String data) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /mission/generate_by_setting
                     *     + key : reload key
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/generate_by_setting";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("data", data);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(rs));
                    if (rs == null) {

                    }
                }
            };
            thread.start();
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse assignMissionAllAgency(
            String data
    ) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /mission/generate_all
                     *     + key : reload key
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/generate_all";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("data", data);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(rs));
                    if (rs == null) {

                    }
                }
            };
            thread.start();
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}