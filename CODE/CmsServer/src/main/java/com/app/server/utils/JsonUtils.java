package com.app.server.utils;

import java.lang.reflect.Type;
import java.sql.ResultSet;

import com.ygame.framework.common.LogUtil;
import org.apache.log4j.Logger;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

public class JsonUtils {
    public static final Gson json = new Gson();
    private static final Logger logger = LogUtil.getLogger(JsonUtils.class);

    public static String Serialize(Object value) {
        return json.toJson(value);
    }

    public static <T> T DeSerialize(String value, Type typeOfT) {
        T result = json.fromJson(value, typeOfT);

        return result;
    }

    public static JSONObject convertToJSON(ResultSet resultSet) {
        try {
            JSONObject js = new JSONObject();
            int total_rows = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < total_rows; i++) {
                js.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), resultSet.getObject(i + 1));
            }
            return js;
        } catch (Exception ex) {
            logger.error(ex.getStackTrace());
        }
        return null;
    }
}