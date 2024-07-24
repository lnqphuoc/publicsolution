package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.location.City;
import com.app.server.data.request.bravo.BravoResponse;
import com.app.server.enums.MembershipType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BravoService {
    public ClientResponse syncAgencyInfo(JSONObject agency) {
        try {
            JSONObject body = new JSONObject();
            //     * "createdAt": "2023-07-01",
            body.put("createdAt",
                    DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(agency.get("approved_date"))), "yyyy-MM-dd"));
            //     * "customerAppId": "12",
            body.put("customerAppId",
                    ConvertUtils.toString(agency.get("id")));
            //customerLevelAppId
            body.put("customerLevelAppId",
                    ConvertUtils.toInt(agency.get("membership_id")));
            //     * "code": "AnhTin12",
            body.put("code", ConvertUtils.toString(agency.get("code")));
            //     * Tên cửa hàng - "name": "Công ty TNHH Anh Tin12",
            body.put("name", ConvertUtils.toString(agency.get("shop_name")));
            //     * "person": "Nguyễn Văn A12",
            body.put("person", ConvertUtils.toString(agency.get("full_name")));
            //     * "tel": "011 111 1001",
            body.put("tel", ConvertUtils.toString(agency.get("phone")));
            //     * "address": "HCM1",
            body.put("address",
                    ConvertUtils.toString(agency.get("full_address")));
            //     * "provinceAppId": 1,
            body.put("provinceAppId", ConvertUtils.toInt(agency.get("city_id")));
            //     * "districtAppId": 2,
            body.put("districtAppId", ConvertUtils.toInt(agency.get("district_id")));
            //     * "wardAppId": 4,
            body.put("wardAppId", ConvertUtils.toInt(agency.get("ward_id")));
            //     * "callNameCode": "1",
            body.put("callNameCode", ConvertUtils.toString(agency.get("gender")));
            //     * "birthDay": "2008-08-08",
            body.put("birthDay",
                    (agency.get("birthday") == null || agency.get("birthday").toString().isEmpty()) ? "" :
                            this.convertDate(
                                    ConvertUtils.toString(agency.get("birthday")),
                                    "dd/MM/yyyy",
                                    "yyyy-MM-dd"));
            //     * "email": "anhtin@gmail.com"
            body.put("email", ConvertUtils.toString(agency.get("email")));
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.SERVICE_BRAVO_URL + "/anhtin/agency/create_new_agency";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", ConfigInfo.SERVICE_BRAVO_KEY);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(body));
            HttpEntity requestEntity = new HttpEntity<>(JsonUtils.Serialize(body), headers);
            ResponseEntity<BravoResponse> rs = restTemplate.postForEntity(url, requestEntity, BravoResponse.class);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(rs));

            if (rs != null && rs.getBody().success()) {
                return ClientResponse.success(null);
            } else {
                return ClientResponse
                        .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("SYNC", ex);
        }
        return ClientResponse
                .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String convertDate(String data, String format_from, String format_to) {
        try {
            return DateTimeUtils.toString(
                    DateTimeUtils.getDateTime(data, format_from), format_to);
        } catch (Exception ex) {
            LogUtil.printDebug("SYNC", ex);
        }

        return "";
    }

    public ClientResponse syncAgencyMembership(int agency_id,
                                               int new_membership_id,
                                               String new_agency_code) {
        try {
            JSONObject body = new JSONObject();
            /**
             customerAppId;
             */
            body.put("customerAppId",
                    ConvertUtils.toString(agency_id));
            /**
             * appId
             */
            body.put("appId",
                    ConvertUtils.toInt(0));
            /**
             * docDate
             */
            body.put("docDate", DateTimeUtils.getNow("yyyy-MM-dd"));
            /**
             * customerCodeNew
             */
            body.put("customerCodeNew",
                    new_agency_code);
            /**
             * customerLevelAppId
             */
            body.put("customerLevelAppId",
                    new_membership_id);

            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.SERVICE_BRAVO_URL + "/anhtin/agency/update_membership";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", ConfigInfo.SERVICE_BRAVO_KEY);
            HttpEntity requestEntity = new HttpEntity<>(JsonUtils.Serialize(body), headers);
            ResponseEntity<BravoResponse> rs = restTemplate.postForEntity(url, requestEntity, BravoResponse.class);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.getBody().success()) {
                return ClientResponse.success(null);
            } else {
                return ClientResponse
                        .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("SYNC", ex);
        }
        return ClientResponse
                .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse syncAgencyOrder(
            JSONObject body
    ) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.SERVICE_BRAVO_URL + "/anhtin/order/create_order";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", ConfigInfo.SERVICE_BRAVO_KEY);
            HttpEntity requestEntity = new HttpEntity<>(JsonUtils.Serialize(body), headers);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<BravoResponse> rs = restTemplate.postForEntity(url, requestEntity, BravoResponse.class);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(rs));
            if (rs != null) {
                if (rs.getBody().success()) {
                    return ClientResponse.success(null);
                } else {
                    ClientResponse crFail = ClientResponse
                            .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crFail.fail(rs.getBody().getMessage());
                    return crFail;
                }
            } else {
                return ClientResponse
                        .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("SYNC", ex);
        }
        return ClientResponse
                .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse syncProductPrice(JSONObject data) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.SERVICE_BRAVO_URL + "/anhtin/product/create_price_list";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", ConfigInfo.SERVICE_BRAVO_KEY);
            HttpEntity requestEntity = new HttpEntity<>(JsonUtils.Serialize(data), headers);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<BravoResponse> rs = restTemplate.postForEntity(url, requestEntity, BravoResponse.class);
            LogUtil.printDebug("SYNC: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.getBody().success()) {
                return ClientResponse.success(null);
            } else {
                return ClientResponse
                        .fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("SYNC", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}