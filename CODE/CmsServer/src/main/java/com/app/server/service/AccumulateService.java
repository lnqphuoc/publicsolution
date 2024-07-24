package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AccumulateService {
    public boolean autoAddTransactionOrder(
            String type,
            int agency_id,
            String code,
            int order_id,
            long value,
            int transaction_id,
            int transaction_source,
            int staff_id,
            long order_time,
            long dept_time) {
        try {
            /**
             * - /tl/add_transaction
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/add_transaction?" +
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
                    + "&dept_time=" + dept_time;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.success()) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return false;
    }

    public ClientResponse adminAddTransaction(
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
            long tt) {
        try {
            /**
             * - /tl/add_order
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/add_order?" +
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
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse adminRemoveTransaction(
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
             * - /tl/remove_transaction
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/remove_transaction?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&program_id=" + promo_id
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public boolean autoRemoveTransaction(
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
             * - /tl/remove_order
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/remove_order?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&type=" + type
                    + "&agency_id=" + agency_id
                    + "&code=" + code
                    + "&order_id=" + order_id
                    + "&transaction_id=" + transaction_id
                    + "&transaction_source=" + transaction_source
                    + "&staff_id=" + staff_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.success()) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return false;
    }

    public ClientResponse getReward(int promo_id, List<Integer> agencyList) {
        try {
            /**
             * - /tl/get_reward
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/get_reward?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&agency=" + JsonUtils.Serialize(agencyList)
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.success()) {
                return rs;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void calculateProgressCTTL(int promo_id) {
        try {
            /**
             * - /tl/estimate_reward
             *     + key : reload key
             *     + program_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/estimate_reward?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public void calculateRewardCTTL(int promo_id) {
        try {
            /**
             * - /tl/reward_program
             *     + key : reload key
             *     + program_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/reward_program?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public ClientResponse callGetDataLienKet(int promo_id, int agency_id) {
        try {
            /**
             * - /tl/get_data_lk
             *     + key : reload key
             *     + program_id (int)
             *     + agency_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/get_data_lk?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&agency_id=" + agency_id
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            return rs;
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Thống kê tích lũy
     *
     * @param promo_id
     * @return
     */
    public ClientResponse getStatistic(int promo_id) {
        try {
            /**
             * - /tl/get_reward
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
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/get_statistic?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            if (rs != null && rs.success()) {
                return rs;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}