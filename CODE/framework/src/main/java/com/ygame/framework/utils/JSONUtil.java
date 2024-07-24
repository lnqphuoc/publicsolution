/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author huynxt
 */
public class JSONUtil {

    public static final Gson json = new Gson();

    public static String Serialize(Object value) {
        return json.toJson(value);
    }

    public static <T extends Object> T DeSerialize(String value, Type typeOfT) {

        T result = json.fromJson(value, typeOfT);

        return result;
    }

    public static boolean getBoolean(JsonObject obj, String member) {
        JsonElement elem = obj.get(member);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsBoolean();
        }
        return false;
    }

    public static String getString(JsonObject obj, String member) {
        JsonElement elem = obj.get(member);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsString();
        }
        return StringUtils.EMPTY;
    }

    public static int getInt(JsonObject obj, String member) {
        JsonElement elem = obj.get(member);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsInt();
        }
        return 0;
    }

    public static float getFloat(JsonObject obj, String member) {
        JsonElement elem = obj.get(member);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsFloat();
        }
        return 0;
    }

    public static JsonObject toJson(Object obj) {
        Gson gson = new Gson();
        JsonObject elem = gson.toJsonTree(obj).getAsJsonObject();
        return elem;
    }
}
