/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import com.google.gson.JsonObject;
import com.ygame.framework.common.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 *
 * @author huynxt
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getName();

//    private static final String FIREBASE_SERVER_KEY = Config.getParam("firebase", "server_key");

    private static final String FCM_SUBSCRIBE = "https://iid.googleapis.com/iid/v1:batchAdd";
    private static final String FCM_UNSUBSCRIBE = "https://iid.googleapis.com/iid/v1:batchRemove";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String CONTENT_TYPE = "application/json";
//    private static final String AUTHORIZE = String.format("key=%s", FIREBASE_SERVER_KEY);

    public static boolean isKeyValid(String keyNotify) {
        if (keyNotify.length() < 100) {
            return false;
        }
        return true;
    }

    public enum TopicAction {
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    public static String pushToTopic(String serverKey, String topic, FirebaseMsg message) {
        return sendRequestToFCM(serverKey, FCM_URL, message.createJsonPushTopicObj(topic));
    }

    public static String pushToDevices(String serverKey, FirebaseMsg message, List<String> lstKeyPush) {
        return pushToDevices(serverKey, message, lstKeyPush.toArray(new String[lstKeyPush.size()]));
    }
    
    public static String pushToDevices(String serverKey, FirebaseMsg message, String... lstKeyPush) {
        if (lstKeyPush.length == 0) {
            return StringUtils.EMPTY;
        }

        return sendRequestToFCM(serverKey, FCM_URL, message.createJsonPushDeviceObj(lstKeyPush));
    }

    /**
     *
     * @param action
     * @param serverKey
     * @param topicName
     * @throws Exception
     */
    public static boolean subscribeOrUnSubscribeTopic(String serverKey, TopicAction action, String topicName, String... lstKeyIos) throws Exception {
        if (lstKeyIos.length == 0) {
            return true;
        }
        try {
            String apiURL = FCM_SUBSCRIBE;
            if (action == TopicAction.UNSUBSCRIBE) {
                apiURL = FCM_UNSUBSCRIBE;
            }

            int maxSize = 900;
            int pageCount = lstKeyIos.length / maxSize;
            if (pageCount * maxSize < lstKeyIos.length) {
                pageCount++;
            }

            for (int i = 0; i < pageCount; i++) {
                String[] childList;
                if (i != pageCount - 1) {
                    childList = (String[])ArrayUtils.subarray(lstKeyIos, i * maxSize, (i * maxSize) + maxSize);
                } else {
                    childList = (String[])ArrayUtils.subarray(lstKeyIos, i * maxSize, lstKeyIos.length);
                }
                sendRequestToFCM(serverKey, apiURL, FirebaseMsg.createJsonSubscribeTopicObj(childList, topicName));
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(TAG, ex);
            return false;
        }
    }

    private static String sendRequestToFCM(String serverKey, String fcmUrl, JsonObject jsonDataPost) {
        try {
            URL url = new URL(fcmUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", String.format("key=%s", serverKey));
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);

//            System.err.println("-----------------------");
//            System.err.println("SERVER KEY >> " + serverKey);

            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());
            wr.write(jsonDataPost.toString());
            wr.flush();

//            System.err.println("REQUEST >> " + jsonDataPost.toString());
            
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            StringBuilder sbResponse = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sbResponse.append(output);
            }
            String response = sbResponse.toString();

            return response;
        } catch (Exception ex) {
            LogUtil.printDebug(TAG, ex);
            LogUtil.printDebugAll("ERROR POST FCM >> " + fcmUrl + " >> " + jsonDataPost.toString());
            return StringUtils.EMPTY;
        }
    }

}
