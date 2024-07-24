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
public class ResetMissionService {

    public ClientResponse resetMissionPoint(
    ) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /mission/reset_point
                     *     + key : reload key
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/reset_point";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug(Module.MISSION + ": " + JsonUtils.Serialize(rs));
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