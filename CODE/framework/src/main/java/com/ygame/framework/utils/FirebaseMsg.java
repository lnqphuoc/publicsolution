/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;

/**
 *
 * @author huynxt
 *
 */
public class FirebaseMsg {

    private String message;
    private String title;

    public FirebaseMsg() {
    }

    public JsonObject getDataObj() {
        JsonObject objData = new JsonObject();
        JsonObject obj = new JsonObject();
        obj.add("data", objData);
        return obj;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement createNotificationObj(String title, String body) {
        JsonObject obj = new JsonObject();

        obj.addProperty("body", body);
        obj.addProperty("title", title);
        obj.addProperty("badge", "1");

        return obj;
    }

    public JsonObject createJsonPushTopicObj(String topic) {
        JsonObject obj = new JsonObject();

        obj.addProperty("to", "/topics/" + topic);
        obj.add("data", this.getDataObj());
        obj.add("notification", this.createNotificationObj(this.getTitle(), this.getMessage()));

        return obj;
    }

    public static JsonObject createJsonSubscribeTopicObj(String[] lstKeypush, String topic) {
        JsonObject obj = new JsonObject();

        JsonArray jsonArrayId = JSONUtil.DeSerialize(JSONUtil.Serialize(lstKeypush), JsonArray.class);
        obj.addProperty("to", "/topics/" + topic);
        obj.add("registration_tokens", jsonArrayId);

        return obj;
    }

    public JsonObject createJsonPushDeviceObj(String[] lstKeypush) {
        JsonObject obj = new JsonObject();

        JsonArray arrId = JSONUtil.DeSerialize(JSONUtil.Serialize(lstKeypush), JsonArray.class);
        obj.add("registration_ids", arrId);
        obj.add("data", this.getDataObj());
        obj.add("notification", this.createNotificationObj(this.getTitle(), this.getMessage()));

        return obj;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
