package com.app.server.service;

import com.app.server.adapter.TelegramAdapter;
import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.mission.MissionCKSData;
import com.app.server.enums.MissionTransactionType;
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

import java.util.List;

@Service
public class AccumulateMissionService {
    /**
     * Tích lũy tự động đơn hàng
     *
     * @param agency_id
     * @param order_id
     * @param order_dept_id
     * @param order_dept_code
     * @return
     */
    public boolean tichLuyTuDongOrder(
            int agency_id,
            int order_id,
            int order_dept_id,
            String order_dept_code) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_transaction";
                    LogUtil.printDebug("MISION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("type", MissionTransactionType.DON_HANG.getKey());
                    body.add("agency_id", ConvertUtils.toString(agency_id));
                    body.add("order_id", ConvertUtils.toString(order_id));
                    body.add("order_dept_id", ConvertUtils.toString(order_dept_id));
                    body.add("order_dept_code", order_dept_code);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.getBody() == null ||
                            rs.getBody().failed()) {
                        alertToTelegram("tichLuyTuDongOrder: " +
                                JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return false;
    }

    /**
     * Tích lũy tự động HBTL
     *
     * @param agency_id
     * @param agency_hbtl_id
     * @return
     */
    public boolean tichLuyTuDongHBTL(
            int agency_id,
            int agency_hbtl_id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_transaction";
            LogUtil.printDebug("MISSION: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", MissionTransactionType.HBTL.getKey());
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("order_id", ConvertUtils.toString(agency_hbtl_id));
            body.add("order_dept_id", ConvertUtils.toString(0));
            body.add("order_dept_code", "");
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.getBody().success()) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return false;
    }

    /**
     * Tích lũy tự động nợ quá hạn
     *
     * @return
     */
    public boolean tichLuyTuDongNoQuaHan(
            MissionTransactionType transactionType,
            int agency_id,
            String code,
            long value) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_nqh";
                    LogUtil.printDebug("MISSION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("type", transactionType.getKey());
                    body.add("code", code);
                    body.add("value", ConvertUtils.toString(value));
                    body.add("agency_id", ConvertUtils.toString(agency_id));
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return true;
    }

    /**
     * Tích lũy tự động chiết khấu sau
     *
     * @param agency_id
     * @param code:     ma_phieu
     * @param
     * @return
     */
    public ClientResponse tichLuyTuDongChietKhauSau(
            int agency_id,
            String code,
            List<MissionCKSData> data) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_cks";
            LogUtil.printDebug("MISSION: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("code", code);
            body.add("data", JsonUtils.Serialize(data));
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs != null) {
                if (rs.getBody().success()) {
                    return ResponseConstants.success;
                } else {
                    return rs.getBody();
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * tichLuyTuDongThanhToan
     *
     * @param agency_id
     * @param missionTransactionType
     * @param code
     * @param value
     * @return
     */
    public ClientResponse tichLuyTuDongThanhToan(
            int agency_id,
            MissionTransactionType missionTransactionType,
            String code,
            long value) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_payment";
                    LogUtil.printDebug("MISSION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("type", missionTransactionType.getKey());
                    body.add("agency_id", ConvertUtils.toString(agency_id));
                    body.add("code", code);
                    body.add("value", ConvertUtils.toString(value));
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.getBody() == null ||
                            rs.getBody().failed()) {
                        alertToTelegram("tichLuyTuDongThanhToan: " +
                                JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ResponseConstants.success;
    }

    /**
     * tichLuyTuDongGiamTienThu
     *
     * @param agency_id
     * @param missionTransactionType
     * @param code
     * @param value
     * @return
     */
    public ClientResponse tichLuyTuDongGiamTienThu(
            int agency_id,
            MissionTransactionType missionTransactionType,
            String code,
            long value) {
        try {
            if (value <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Giá trị thanh toán: " + value);
            }
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_payment";
            LogUtil.printDebug("MISSION: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", missionTransactionType.getKey());
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("code", code);
            body.add("value", ConvertUtils.toString(value * -1));
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs == null || rs.getBody() == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "rs=null");
            }
            return rs.getBody();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    /**
     * tichLuyTuDongTangTienThu
     *
     * @param agency_id
     * @param missionTransactionType
     * @param code
     * @param value
     * @return
     */
    public ClientResponse tichLuyTuDongTangTienThu(
            int agency_id,
            MissionTransactionType missionTransactionType,
            String code,
            long value) {
        try {
            if (value <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Giá trị thanh toán: " + value);
            }
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_payment";
            LogUtil.printDebug("MISSION: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", missionTransactionType.getKey());
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("code", code);
            body.add("value", ConvertUtils.toString(value));
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs == null || rs.getBody() == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "rs=null");
            }
            return rs.getBody();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    public void alertToTelegram(String message, ResponseStatus responseStatus) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String errorIcon = "\uD83D\uDFE5";
            String successIcon = "✅";
            stringBuilder.append(
                            responseStatus.getValue() == ResponseStatus.FAIL.getValue()
                                    ? errorIcon
                                    : successIcon)
                    .append("[" + ConfigInfo.ENV_LEVEL + "][cms]")
                    .append(" " + message);
            TelegramAdapter.sendMsg(
                    stringBuilder.toString(),
                    ConfigInfo.CHAT_EXCEPTION_ID);
            LogUtil.printDebug(message);
        } catch (Exception ex) {
            LogUtil.printDebug("ALERT", ex);
        }
    }

    public ClientResponse rewardMission(
            String data) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/reward";
                    LogUtil.printDebug("MISSION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    body.add("data", data);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.getBody() == null ||
                            rs.getBody().failed()) {
                        alertToTelegram("rewardMission: " +
                                JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ResponseConstants.success;
    }

    public ClientResponse resetMissionPoint() {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/reset_point";
                    LogUtil.printDebug("MISSION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.getBody() == null ||
                            rs.getBody().failed()) {
                        alertToTelegram("rewardMission: " +
                                JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ResponseConstants.success;
    }

    public void removeOrder(Integer order_id, Integer agency_id) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/remove_order?key=" + ConfigInfo.ACCUMULATE_KEY + "&" +
                            "order_id=" + order_id + "&" +
                            "agency_id=" + agency_id;
                    LogUtil.printDebug("MISSION: " + url);
                    ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.failed()) {
                        alertToTelegram("rewardMission: " + url, ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public ClientResponse getProductDTT(
            int agency_id,
            String code,
            int order_id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/get_product_dtt?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id;
            LogUtil.printDebug("CSDM: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse rejectAccumulateOrder(Integer order_id, Integer agency_id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/delete_order?key=" + ConfigInfo.ACCUMULATE_KEY + "&" +
                    "order_id=" + order_id + "&" +
                    "agency_id=" + agency_id;
            LogUtil.printDebug("MISSION: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs == null ||
                    rs.failed()) {
                alertToTelegram("MISSION: " + url, ResponseStatus.EXCEPTION);
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, JsonUtils.Serialize(rs));
            }
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    public ClientResponse acceptAccumulateOrder(
            int agency_id,
            int order_id,
            int order_dept_id,
            String order_dept_code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/mission/add_order";
            LogUtil.printDebug("MISION: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", MissionTransactionType.DON_HANG.getKey());
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("order_id", ConvertUtils.toString(order_id));
            body.add("order_dept_id", ConvertUtils.toString(order_dept_id));
            body.add("order_dept_code", order_dept_code);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
            if (rs == null ||
                    rs.getBody() == null ||
                    rs.getBody().failed()) {
                alertToTelegram("acceptAccumulateOrder: " +
                        JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, JsonUtils.Serialize(rs));
            }
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    public ClientResponse resetMissionAll() {
        try {
            Thread thread = new Thread() {
                public void run() {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/mission/reset_all_mission";
                    LogUtil.printDebug("MISSION: " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
                    body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(requestEntity));
                    ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
                    LogUtil.printDebug("MISSION: " + JsonUtils.Serialize(rs));
                    if (rs == null ||
                            rs.getBody() == null ||
                            rs.getBody().failed()) {
                        alertToTelegram("rewardMission: " +
                                JsonUtils.Serialize(requestEntity) + " - " + JsonUtils.Serialize(rs), ResponseStatus.EXCEPTION);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ResponseConstants.success;
    }
}