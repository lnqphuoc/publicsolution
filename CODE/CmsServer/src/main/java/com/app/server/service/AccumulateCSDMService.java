package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.bravo.BravoResponse;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
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
public class AccumulateCSDMService {
    /**
     * Tích lũy tự động
     *
     * @param type
     * @param agency_id
     * @param code
     * @param order_id
     * @param transaction_id
     * @param transaction_source
     * @param staff_id
     * @param order_time
     * @param dept_time
     * @return
     */
    public boolean addTransaction(
            String type,
            int agency_id,
            String code,
            int order_id,
            int transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time,
            String data) {
        try {
            /**
             * - /damme/add_transaction
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + order_id (int: mã đơn hàng cha)
             *     + value (int: giá trị dùng cho doanh thu thuần...)
             *     + transaction_source (int)
             *     + staff_id (int)
             *     + order_time
             *     + dept_time
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/add_transaction";
//                    "key=" + ConfigInfo.ACCUMULATE_KEY
//                    + "&type=" + type
//                    + "&agency_id=" + agency_id
//                    + "&code=" + code
//                    + "&order_id=" + order_id
//                    + "&transaction_id=" + transaction_id
//                    + "&transaction_source=" + transaction_source
//                    + "&staff_id=" + staff_id
//                    + "&order_time=" + order_time
//                    + "&dept_time=" + dept_time
//                    + "&data=" + data;
            LogUtil.printDebug("CSDM: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", type);
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("code", code);
            body.add("order_id", ConvertUtils.toString(order_id));
            body.add("transaction_id", ConvertUtils.toString(transaction_id));
            body.add("transaction_source", ConvertUtils.toString(transaction_source));
            body.add("staff_id", ConvertUtils.toString(staff_id));
            body.add("order_time", ConvertUtils.toString(order_time));
            body.add("dept_time", ConvertUtils.toString(dept_time));
            body.add("data", data);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.getBody().success()) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return false;
    }

    /**
     * Bổ sung đơn hàng
     *
     * @param type
     * @param agency_id
     * @param code
     * @param order_id
     * @param value
     * @param transaction_id
     * @param transaction_source
     * @param staff_id
     * @param order_time
     * @param dept_time
     * @param promo_id
     * @param tt
     * @return
     */
    public ClientResponse addOrder(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            int transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time,
            int promo_id,
            long tt,
            String data) {
        try {
            /**
             * - /damme/add_order
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + order_id (int: mã đơn hàng cha)
             *     + value (int: giá trị dùng cho doanh thu thuần...)
             *     + transaction_source (int)
             *     + staff_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/add_order";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
            body.add("key", ConvertUtils.toString(ConfigInfo.ACCUMULATE_KEY));
            body.add("type", type);
            body.add("code", code);
            body.add("agency_id", ConvertUtils.toString(agency_id));
            body.add("order_id", ConvertUtils.toString(order_id));
            body.add("program_id", ConvertUtils.toString(promo_id));
            body.add("transaction_id", ConvertUtils.toString(transaction_id));
            body.add("transaction_source", ConvertUtils.toString(transaction_source));
            body.add("staff_id", ConvertUtils.toString(staff_id));
            body.add("order_time", ConvertUtils.toString(order_time));
            body.add("dept_time", ConvertUtils.toString(dept_time));
            body.add("data", data);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(requestEntity));
            ResponseEntity<ClientResponse> rs = restTemplate.postForEntity(url, requestEntity, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            if (rs != null) {
                return rs.getBody();
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Hủy đơn hàng
     *
     * @param type
     * @param agency_id
     * @param code
     * @param order_id
     * @param promo_id
     * @param transaction_id
     * @param transaction_source
     * @param staff_id
     * @return
     */
    public ClientResponse cancelOrder(
            String type,
            int agency_id,
            String code,
            int order_id,
            int promo_id,
            int transaction_id,
            int transaction_source,
            int staff_id) {
        try {
            /**
             * - /damme/remove_order
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + promo_id (int: mã cttl)
             *     + transaction_id (int)
             *     + transaction_source (int)
             *     + staff_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/remove_order?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id;
            LogUtil.printDebug("CSDM: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            if (rs != null) {
                return rs;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Tạo phiếu điều chỉnh
     *
     * @param type
     * @param agency_id
     * @param code
     * @param order_id
     * @param value
     * @param transaction_id
     * @param transaction_source
     * @param staff_id
     * @param order_time
     * @param dept_time
     * @param promo_id
     * @param tt
     * @return
     */
    public ClientResponse createPhieuDieuChinh(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            long transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time,
            int promo_id,
            long tt) {
        try {
            /**
             * - /damme/add_phieu
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + order_id (int: mã đơn hàng cha)
             *     + value (int: giá trị dùng cho doanh thu thuần...)
             *     + transaction_source (int)
             *     + staff_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/add_phieu?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&value=" + value
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id
                    + "&order_time=" + order_time
                    + "&dept_time=" + dept_time
                    + "&program_id=" + promo_id
                    + "&tt=" + tt;
            LogUtil.printDebug("CSDM: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Giảm giá trị đơn hàng
     *
     * @param type
     * @param agency_id
     * @param code
     * @param order_id
     * @param value
     * @param transaction_id
     * @param transaction_source
     * @param staff_id
     * @return
     */
    public ClientResponse decreaseOrderPrice(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            long transaction_id,
            int transaction_source,
            int staff_id) {
        try {
            /**
             * - /damme/decrease_order_price
             *     + key : reload key
             *     + type (string) 1: order....
             *     + agency_id (int)
             *     + code (string : nếu đơn hàng, lấy mã đơn hàng con)
             *     + order_id (int: mã đơn hàng cha)
             *     + value (int: giá trị dùng cho doanh thu thuần...)
             *     + transaction_source (int)
             *     + staff_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/decrease_order_price?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&value=" + value
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id;
            LogUtil.printDebug("CSDM: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Giảm giá trị sản phẩm của đơn hàng
     *
     * @return
     */
    public ClientResponse decreaseProductPrice(
            String type,
            int agency_id,
            String code,
            int order_id,
            long transaction_id,
            int transaction_source,
            int staff_id,
            int product_id,
            long product_price) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/damme/decrease_product_price?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id
                    + "&product_id=" + product_id
                    + "&product_price=" + product_price;
            LogUtil.printDebug("CSDM: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("CSDM: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lấy thông tin doanh thu thuần
     *
     * @return
     */
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
}