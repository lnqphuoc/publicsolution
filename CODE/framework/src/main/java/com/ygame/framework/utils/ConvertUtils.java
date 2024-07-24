package com.ygame.framework.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ConvertUtils
{
  private static final String DEFAULT_CHARSET = "UTF-8";
  
  public static float toFloat(Object value)
  {
    return toFloat(value, 0.0F);
  }
  
  public static float toFloat(Object value, float def)
  {
    if (value == null) {
      return def;
    }
    if ((value instanceof Number)) {
      return ((Number)value).floatValue();
    }
    try
    {
      return Float.valueOf(value.toString()).floatValue();
    }
    catch (Exception e) {}
    return def;
  }
  
  public static double toDouble(Object value)
  {
    return toDouble(value, 0.0D);
  }
  
  public static double toDouble(Object value, double def)
  {
    if (value == null) {
      return def;
    }
    if ((value instanceof Number)) {
      return ((Number)value).doubleValue();
    }
    try
    {
      return Double.valueOf(value.toString()).doubleValue();
    }
    catch (Exception e) {}
    return def;
  }
  
  public static byte toByte(String value)
  {
    return toByte(value, Byte.parseByte("0"));
  }
  
  public static byte toByte(String value, byte defaultValue)
  {
    if (ValidationUtils.isNumber(value)) {
      try
      {
        return Byte.parseByte(value);
      }
      catch (Exception e)
      {
        return defaultValue;
      }
    }
    return defaultValue;
  }
  
  public static short toShort(String value)
  {
    return toShort(value, Short.parseShort("0"));
  }
  
  public static short toShort(Object value)
  {
    if (value != null) {
      return toShort(value.toString(), Short.parseShort("0"));
    }
    return 0;
  }
  
  public static short toShort(String value, short defaultValue)
  {
    if (ValidationUtils.isNumber(value)) {
      try
      {
        return Short.parseShort(value);
      }
      catch (Exception e)
      {
        return defaultValue;
      }
    }
    return defaultValue;
  }
  
  public static int toInt(Object value)
  {
    return toInt(value, 0);
  }
  
  public static int toInt(Object value, int def)
  {
    if (value == null) {
      return def;
    }
    if ((value instanceof Number)) {
      return ((Number)value).intValue();
    }
    try
    {
      return Integer.valueOf(value.toString()).intValue();
    }
    catch (Exception e) {}
    return def;
  }
  
  public static long toLong(Object value)
  {
    return toLong(value, 0L);
  }
  
  public static long toLong(Object value, long def)
  {
    if (value == null) {
      return def;
    }
    if ((value instanceof Number)) {
      return ((Number)value).longValue();
    }
    try
    {
      return Long.valueOf(value.toString()).longValue();
    }
    catch (Exception e) {}
    return def;
  }
  
  public static String toString(Object obj)
  {
    return toString(obj, "");
  }
  
  public static String toString(Object obj, String defaultValue)
  {
    try
    {
      if (obj == null) {
        return defaultValue;
      }
      if ((obj instanceof java.util.Date)) {
        return toString((java.util.Date)obj);
      }
      return obj.toString();
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public static boolean toBoolean(Object obj)
  {
    return toBoolean(obj, false);
  }
  
  public static boolean toBoolean(Object obj, boolean defaultValue)
  {
    try
    {
      if (obj != null)
      {
        if ((obj instanceof Boolean)) {
          return ((Boolean)obj).booleanValue();
        }
        if ((obj instanceof String)) {
          return Boolean.parseBoolean((String)obj);
        }
        if (ValidationUtils.isNumber(obj))
        {
          if (obj.equals(Integer.valueOf(1))) {
            return true;
          }
          return false;
        }
      }
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public static String toString(java.util.Date dt)
  {
    return DateTimeUtils.toString(dt);
  }
  
  public static String toString(java.util.Date dt, String format)
  {
    return DateTimeUtils.toString(dt, format);
  }
  
//  public static String toString(java.util.Date dt, String format, String timeZone)
//  {
//    return DateTimeUtils.toString(dt, format, timeZone);
//  }
  
  public static java.util.Date getDateTime(int year, int month, int day)
  {
    return DateTimeUtils.getDateTime(year, month, day);
  }
  
  public static java.util.Date toDateTime(long timestamp)
  {
    return DateTimeUtils.getDateTime(timestamp);
  }
  
  public static java.util.Date toDateTime(java.util.Date dt)
  {
    return DateTimeUtils.getDateTime(dt);
  }
  
  public static java.util.Date toDateTime(java.util.Date date, java.util.Date defaultValue)
  {
    java.util.Date dt = DateTimeUtils.getDateTime(date);
    if (dt != null) {
      return dt;
    }
    return defaultValue;
  }
  
  public static java.util.Date toDateTime(java.util.Date dt, String format)
  {
    return toDateTime(dt, null, format);
  }
  
  public static java.util.Date toDateTime(java.util.Date date, java.util.Date defaultValue, String format)
  {
    java.util.Date dt = DateTimeUtils.getDateTime(date, format);
    if (dt != null) {
      return dt;
    }
    return defaultValue;
  }
  
//  public static java.util.Date toDateTime(java.util.Date date, java.util.Date defaultValue, String format, String timeZone)
//  {
//    java.util.Date dt = DateTimeUtils.getDateTime(date, format, timeZone);
//    if (dt != null) {
//      return dt;
//    }
//    return defaultValue;
//  }
  
  public static java.util.Date toDateTime(String data)
  {
    return DateTimeUtils.getDateTime(data);
  }
  
  public static java.util.Date toDateTime(String data, String format)
  {
    return toDateTime(data, null, format);
  }
  
  public static java.util.Date toDateTime(String data, java.util.Date defaultValue)
  {
    java.util.Date dt = DateTimeUtils.getDateTime(data);
    if (dt != null) {
      return dt;
    }
    return defaultValue;
  }
  
  public static java.util.Date toDateTime(String data, java.util.Date defaultValue, String format)
  {
    java.util.Date dt = DateTimeUtils.getDateTime(data, format);
    if (dt != null) {
      return dt;
    }
    return defaultValue;
  }
  
//  public static java.util.Date toDateTime(String data, java.util.Date defaultValue, String format, String timeZone)
//  {
//    java.util.Date dt = DateTimeUtils.getDateTime(data, format, timeZone);
//    if (dt != null) {
//      return dt;
//    }
//    return defaultValue;
//  }
  
  public static java.util.Date toDateTime(java.sql.Date date)
  {
    return DateTimeUtils.getDateTime(date);
  }
  
  public static java.util.Date toDateTime(java.sql.Date date, java.util.Date defaultValue)
  {
    java.util.Date dt = toDateTime(date);
    if (dt != null) {
      return dt;
    }
    return defaultValue;
  }
  
  public static java.sql.Date toDateSql(java.util.Date dt)
  {
    return DateTimeUtils.getDateSql(dt);
  }
  
  public static List<String> convertListLong(List<Long> values)
  {
    List<String> result = new ArrayList();
    if ((values == null) || (values.isEmpty())) {
      return result;
    }
    for (int i = 0; i < values.size(); i++) {
      result.add(String.valueOf(values.get(i)));
    }
    return result;
  }
  
  public static String[] List2Array(List<Long> values)
  {
    if ((values == null) || (values.isEmpty())) {
      return new String[0];
    }
    String[] result = new String[values.size()];
    for (int i = 0; i < values.size(); i++) {
      result[i] = String.valueOf(values.get(i));
    }
    return result;
  }
  
  public static List<Long> convertListString(List<String> values)
  {
    List<Long> result = new ArrayList();
    if ((values == null) || (values.isEmpty())) {
      return result;
    }
    for (int i = 0; i < values.size(); i++)
    {
      String tmp = ((String)values.get(i)).trim();
      result.add(Long.valueOf(toLong(tmp, 0L)));
    }
    return result;
  }
  
  public static String decodeString(byte[] data)
  {
    String rv = null;
    try
    {
      if (data != null) {
        rv = new String(data, "UTF-8");
      }
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    return rv;
  }
  
  public static byte[] encodeString(String in)
  {
    byte[] rv = null;
    try
    {
      rv = in.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    return rv;
  }
  
  public static byte[] long2Bytes(long value)
  {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(value);
    return buffer.array();
  }
  
  public static long bytesToLong(byte[] bytes, long defaultValue)
  {
    try
    {
      if (bytes == null) {
        return defaultValue;
      }
      ByteBuffer buffer = ByteBuffer.allocate(8);
      buffer.put(bytes);
      buffer.flip();
      return buffer.getLong();
    }
    catch (Exception ex) {}
    return defaultValue;
  }
}
