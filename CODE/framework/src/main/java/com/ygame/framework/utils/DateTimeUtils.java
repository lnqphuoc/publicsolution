/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

/**
 *
 * @author huynxt
 */
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

public class DateTimeUtils {

    public static final long MINMILISECONDS = Long.parseLong("-2208988800000");
    public static final java.util.Date MINDATE = ConvertUtils.toDateTime(new java.util.Date(MINMILISECONDS));

    static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//    private static final String TIMEZONE = "UTC";
    private static final String FORMATDATE = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String formatDate = "dd/MM/yyyy";

    public static int compareDate(java.util.Date date1, java.util.Date date2) {
        return ConvertUtils.toDateSql(date1).compareTo(ConvertUtils.toDateSql(date2));
    }

    public static int compareDateTime(java.util.Date date1, java.util.Date date2) {
        return date1.compareTo(date2);
    }

    public static java.util.Date getNow() {
        return getDateTime(new java.util.Date());
    }

    public static String getNow(String format) {
        return toString(new java.util.Date(), format);
    }

    public static long getNanoSecondNow() {
        return System.nanoTime();
    }

    public static long getMilisecondsNow() {
        return getNow().getTime();
    }

    public static long getTomorrowMilisecond() {
        try {
            Date tomorrow = DateTimeUtils.addDays(1);
            return DateTimeUtils.getMiliseconds(DateTimeUtils.getYear(tomorrow), DateTimeUtils.getMonth(tomorrow), DateTimeUtils.getDay(tomorrow));
        } catch (Exception ex) {
            return System.currentTimeMillis() + 24 * 60 * 60000;
        }
    }

    public static long getMilisecondsMin() {
        return MINMILISECONDS;
    }

    public static long getMiliseconds(java.util.Date date) {
        return ConvertUtils.toDateTime(date).getTime();
    }

    public static long getMiliseconds(int year, int month, int day) {
        return getMiliseconds(year, month, day, 0, 0, 0);
    }

    public static long getMiliseconds(int year, int month, int day, int hour, int minute, int second) {
        try {
            String data = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
            formatter.applyPattern("yyyy-M-d H:m:s");
            java.util.Date utilDate = formatter.parse(data);
            return utilDate.getTime();
        } catch (ParseException ex) {
        }
        return 0L;
    }

    public static Timestamp getTimestampSql(java.util.Date dt) {
        if (dt.getTime() < 0L) {
            return new Timestamp(Long.parseLong("2678400000"));
        }
        return Timestamp.valueOf(toString(dt));
    }

    public static java.sql.Date getDateSql(java.util.Date dt) {
        return new java.sql.Date(dt.getTime());
    }

    public static java.util.Date getDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new java.util.Date(timestamp.getTime());
    }

    public static java.util.Date getDateTime(long miliseconds) {
        return getDateTime(new java.util.Date(miliseconds));
    }

    public static java.util.Date getDateTime(java.util.Date dt) {
        return getDateTime(dt, FORMATDATE);
    }

    public static java.util.Date getDateTime(java.util.Date dt, String format) {
        try {
            formatter.applyPattern(format);
            return formatter.parse(formatter.format(dt));
        } catch (Exception e) {
        }
        return null;
    }

    public static java.util.Date getDateTime(java.sql.Date dt) {
        try {
            formatter.applyPattern("yyyy-MM-dd");
            return formatter.parse(formatter.format(dt));
        } catch (Exception e) {
        }
        return null;
    }

    public static java.util.Date getDateTime(int year, int month, int day) {
        return getDateTime(getMiliseconds(year, month, day));
    }

    public static java.util.Date getDateTime(String data) {
        return getDateTime(data, FORMATDATE);
    }
    
    public static java.util.Date getDateTime(String datetime, String format) {
        try {
            formatter.applyLocalizedPattern(format);
            return formatter.parse(datetime);
        } catch (Exception e) {
        }
        return null;
    }

    public static String toString(java.util.Date dt) {
        return toString(dt, FORMATDATE);
    }

    public static String toString(java.util.Date dt, String format) {
        try {
            formatter.applyPattern(format);
            return formatter.format(dt);
        } catch (Exception e) {
        }
        return null;
    }

    public static java.util.Date addWeeks(int value) {
        return DateUtils.addWeeks(getNow(), value);
    }

    public static java.util.Date addWeeks(java.util.Date d, int value) {
        return DateUtils.addWeeks(d, value);
    }

    public static java.util.Date addYears(int value) {
        return DateUtils.addYears(getNow(), value);
    }

    public static java.util.Date addYears(java.util.Date d, int value) {
        return DateUtils.addYears(d, value);
    }

    public static java.util.Date addMonths(int value) {
        return DateUtils.addMonths(getNow(), value);
    }

    public static java.util.Date addMonths(java.util.Date d, int value) {
        return DateUtils.addMonths(getNow(), value);
    }

    public static java.util.Date addDays(int value) {
        return DateUtils.addDays(getNow(), value);
    }

    public static java.util.Date addDays(java.util.Date d, int value) {
        return DateUtils.addDays(d, value);
    }

    public static java.util.Date addHours(int value) {
        return DateUtils.addHours(getNow(), value);
    }

    public static java.util.Date addHours(java.util.Date d, int value) {
        return DateUtils.addHours(d, value);
    }

    public static java.util.Date addMinutes(int value) {
        return DateUtils.addMinutes(getNow(), value);
    }

    public static java.util.Date addMinutes(java.util.Date d, int value) {
        return DateUtils.addMinutes(d, value);
    }

    public static java.util.Date addSeconds(int value) {
        return DateUtils.addSeconds(getNow(), value);
    }

    public static java.util.Date addSeconds(java.util.Date d, int value) {
        return DateUtils.addSeconds(d, value);
    }

    public static java.util.Date addMilliseconds(int value) {
        return DateUtils.addMilliseconds(getNow(), value);
    }

    public static java.util.Date addMilliseconds(java.util.Date d, int value) {
        return DateUtils.addMilliseconds(d, value);
    }

    public static int getDay(java.util.Date date) {
        String value = toString(date);
        return ConvertUtils.toInt(value.substring(8, 10));
    }

    public static int getMonth(java.util.Date date) {
        String value = toString(date);
        return ConvertUtils.toInt(value.substring(5, 7));
    }

    public static int getYear(java.util.Date date) {
        String value = toString(date);
        return ConvertUtils.toInt(value.substring(0, 4));
    }

    public static int getHour(java.util.Date date) {
        String value = toString(date);//yyyy-MM-dd HH:mm:ss.SSS
        return ConvertUtils.toInt(value.substring(11, 13));
    }
    
    public static int getMin(java.util.Date date) {
        String value = toString(date);//yyyy-MM-dd HH:mm:ss.SSS
        return ConvertUtils.toInt(value.substring(14, 16));
    }

    public static java.util.Date format(String value, String format, java.util.Date defValue) {
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (Exception ex) {
        }
        return defValue;
    }

    public static java.util.Date getBeginDate(java.util.Date d) {
        return format(formatter.format(d), "dd/MM/yyyy", d);
    }
}
