package com.ygame.framework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils
{
  private static final Pattern cacheKeyPattern = Pattern.compile("^[A-Za-z0-9@\\-_\\.\\:]{1,100}$");
  
  public static boolean checkCacheKey(String key)
  {
    if ((key == null) || (key.length() <= 0) || (key.length() > 250)) {
      return false;
    }
    return !key.contains(" ");
  }
  
  private static final Pattern urlPattern = Pattern.compile("(http|https)\\://[A-Za-z0-9\\.\\-]+(/[A-Za-z0-9\\?\\&\\=;\\+!'\\(\\)\\*\\-\\._~%]*)*");
  
  public static boolean isUrl(String data)
  {
    if ((data == null) || (data.length() == 0)) {
      return false;
    }
    Matcher match = urlPattern.matcher(data);
    if (match.find()) {
      return true;
    }
    return false;
  }
  
  public static boolean isNumber(Object data)
  {
    if (data == null) {
      return false;
    }
    if ((data instanceof String)) {
      return isNumber(((String)data).trim());
    }
    return true;
  }
  
  private static final Pattern numberPattern = Pattern.compile("^[-]?\\d+([\\.,]\\d+)?$");
  
  public static boolean isNumber(String data)
  {
    if ((data == null) || (data.length() == 0)) {
      return false;
    }
    return numberPattern.matcher(data).matches();
  }
  
  private static final Pattern emailPattern = Pattern.compile("^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
  
  public static boolean isEmail(String data)
  {
    if ((data == null) || (data.length() == 0)) {
      return false;
    }
    Matcher match = emailPattern.matcher(data);
    if (match.find()) {
      return true;
    }
    return false;
  }
  
  private static final Pattern userNamePattern = Pattern.compile("^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*$");
  
  public static boolean isUserName(String data)
  {
    Matcher match = userNamePattern.matcher(data);
    if (match.find()) {
      return true;
    }
    return false;
  }
  
//  public static boolean haveUnicode(String data)
//  {
//    return StringUtils.haveUnicode(data);
//  }
//  
//  public static boolean haveTag(String data)
//  {
//    return StringUtils.haveTag(data);
//  }
//  
//  public static boolean haveScriptTag(String data)
//  {
//    return StringUtils.haveScriptTag(data);
//  }
//  
//  public static boolean haveBadWord(String data)
//  {
//    return StringUtils.haveBadWord(data);
//  }
}
