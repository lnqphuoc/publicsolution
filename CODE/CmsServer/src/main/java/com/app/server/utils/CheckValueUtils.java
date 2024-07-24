package com.app.server.utils;

import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;
import java.util.List;

public class CheckValueUtils {


    public static boolean isJSONObject(String value) {
        try {
            JSONObject obj = JsonUtils.DeSerialize(value, JSONObject.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJSONArray(String value) {
        try {
            JSONArray obj = JsonUtils.DeSerialize(value, JSONArray.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Object parseJSON(String value) throws ParseException {
        if (isJSONObject(value)) {
            return JsonUtils.DeSerialize(value, JSONObject.class);
        }
        if (isJSONArray(value)) {
            Type type = new TypeToken<List<JSONObject>>() {
            }.getType();
            List<JSONObject> obj = JsonUtils.DeSerialize(value, type);
            return obj;
        }
        return value;
    }

    public static boolean isNumberic(String value) {
        try {
            int temp = Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}