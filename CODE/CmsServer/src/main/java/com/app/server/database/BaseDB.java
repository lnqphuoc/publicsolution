package com.app.server.database;

import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;

import java.util.Date;

public class BaseDB {
    protected String parseIntegerToSql(Integer data) {

        return data == null ? null : ("'" + data + "'");
    }

    protected String parseStringToSql(String data) {
        return data == null ? null : ("'" + data + "'");
    }

    protected String parseDateToSql(Date data) {
        return data == null ?
                null :
                ("'" + DateTimeUtils.toString(data, "yyyy-MM-dd HH:mm:ss") + "'");
    }

    protected String parseLongToSql(Long data) {
        return data == null ? null : ("'" + data + "'");
    }

    protected String parseDoubleToSql(Double data) {
        return data == null ? null : ("'" + data + "'");
    }
}