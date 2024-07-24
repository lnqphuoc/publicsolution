/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.cliffc.high_scale_lib.NonBlockingHashtable;

/**
 *
 * @author ChuongVD
 */
public class StringUtils {

    private static NonBlockingHashtable _hashTableBadWord = null;
    private static String _patternBadWord = "";
    private static final String GEN_KEY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    static {
        //loadBadWord(ConfigUtils.getAppSetting("badWordFile", "bad-word.txt"));
        //loadBadWord("bad-word.txt");
    }

    public static String removeNonPrintableCharactor(String data) {
        return data.replaceAll("\\p{C}", "");
    }

    public static String urlEncode(String url) {
        if (isEmpty(url)) {
            return "";
        }
        String result = "";
        try {
            result = java.net.URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return result;
    }

    public static String urlDecode(String url) {
        if (isEmpty(url)) {
            return "";
        }
        String result = "";
        try {
            result = java.net.URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return result;
    }

    public static String htmlEncode(String html) {
        if (isEmpty(html)) {
            return "";
        }
        String result = "";
        try {
            result = StringEscapeUtils.escapeHtml(html);
        } catch (Exception ex) {
        }
        return result;
    }

    public static String htmlDecode(String html) {
        if (isEmpty(html)) {
            return "";
        }
        String result = "";
        try {
            result = StringEscapeUtils.unescapeHtml(html);
        } catch (Exception ex) {
        }
        return result;
    }

    private static char getRandomChar() {
        Random rd = new Random();
        Integer number = rd.nextInt(GEN_KEY.length());
        return GEN_KEY.charAt(number);
    }

    public static String getRandomString(Integer length) {
        String result = "";

        for (int i = 0; i < length; i++) {
            result += getRandomChar();
        }

        return result;
    }

    public static String removeScriptTag(String data) {
        if (data != null && data.length() > 0) {
            return data.replaceAll("<\\s*script[^>]*>.*?</\\s*script[^>]*>", "");
        }
        return "";
    }

    public static boolean haveScriptTag(String data) {
        if (data == null) {
            return false;
        }
        return data.length() != removeScriptTag(data).length();
    }

    public static String removeAllTag(String data) {
        if (data != null && data.length() > 0) {
            return data.replaceAll("<(.|\n)*?>", "");
        }
        return "";
    }

    public static boolean haveTag(String data) {
        if (data == null) {
            return false;
        }
        return data.length() != removeAllTag(data).length();
    }

    /*
     * Remove unicode Ex: Hôm nay -> Hm nay @data
     */
    public static String removeUnicode(String data) {
        if (data != null && data.length() > 0) {
            return data.replaceAll("[^\\p{ASCII}]", "");
        }
        return "";
    }

    /*
     * Kill unicode Convert unicode to non-unicode Ex: Hôm nay -> Hom nay @data
     */
    public static String killUnicode(String data) {
        if (data != null && data.length() > 0) {
            data = data.replaceAll("Đ", "D");
            data = data.replaceAll("đ", "d");
            return java.text.Normalizer.normalize(data, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        }
        return "";
    }

    public static boolean haveUnicode(String data) {
        if (data == null) {
            return false;
        }
        return data.length() != removeUnicode(data).length();
    }

    public static String normalize(String data) {
        if (data != null && data.length() > 0) {
            return Normalizer.normalize(data, Normalizer.Form.NFKC);
        }
        return "";
    }

    public static String removeBadWord(String data) {
        if (data != null && data.length() > 0
                && _patternBadWord.length() > 0
                && _hashTableBadWord != null
                && _hashTableBadWord.size() > 0) {
            Matcher matcher = Pattern.compile(_patternBadWord).matcher(data);
            int i = 0;
            while (matcher.find()) {
                //if(matcher.find(i)){
                if (_hashTableBadWord.containsKey(matcher.group(i))) {
                    data = data.replaceAll(matcher.group(i), (String) _hashTableBadWord.get(matcher.group(i)));
                }
                i++;
                //}
            }
            return data;
        }
        return "";
    }

    public static boolean haveBadWord(String data) {
        if (data != null && data.length() > 0) {
            Matcher matcher = Pattern.compile(_patternBadWord).matcher(data);
            return matcher.find();
        }
        return false;
    }

    private static boolean loadBadWord(String filePath) {
        try {
            if (_hashTableBadWord == null) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream inputData = classLoader.getResourceAsStream(filePath);
                if (inputData == null) {
                    return false;
                }
                _hashTableBadWord = new NonBlockingHashtable();
                String streamLine;
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputData, "UTF-8"))) {
                    _patternBadWord = "(";
                    while ((streamLine = bufferedReader.readLine()) != null) {
                        String[] strArr = streamLine.split(",");
                        if (strArr.length < 2) {
                            continue;
                        }
                        String key = strArr[0].trim().toLowerCase();

                        if (!_hashTableBadWord.containsKey(key)) {
                            _hashTableBadWord.put(key, strArr[1]);
                            _patternBadWord += key + "|";
                        }
                    }
                    _patternBadWord = _patternBadWord.replaceAll(".$", "");
                    _patternBadWord += ")";
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String slug(String data) {
        data = killUnicode(data).trim();
        if (data.length() > 0) {
            data = data.replaceAll("[^a-zA-Z0-9- ]*", "");
            data = data.replaceAll("[ ]{1,}", "-");
        }
        return data;
    }

    public static String slug(String data, int length) {
        String slugString = slug(data);
        return slug(data).substring(0, slugString.length() > length ? length : slugString.length());
    }

    public static String digitFormat(long value) {
        NumberFormat formatter = new DecimalFormat("#,##0");
        return formatter.format(value);
    }

    public static String doMD5(String source) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.update(source.getBytes());
        byte[] hash = digest.digest();
        return byteArrayToHexString(hash);
    }

    public static String doSHA256(String source) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.update(source.getBytes());
        byte[] hash = digest.digest();
        return byteArrayToHexString(hash);
    }

    public static String byteToHexString(byte aByte) {
        String hex = Integer.toHexString(0xFF & aByte);
        return new StringBuilder().append(hex.length() == 1 ? "0" : "").append(hex).toString();
    }

    public static String byteArrayToHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            hexString.append(byteToHexString(hash[i]));
        }
        return hexString.toString();
    }

    /**
     *
     * @param str
     * @param stripChars
     * @return Returns a copy of the string, with leading
     * <i>stripChars</i>omitted.
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if ((str == null) || ((strLen = str.length()) == 0)) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while ((start != strLen) && (Character.isWhitespace(str.charAt(start)))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }

        return str.substring(start);
    }

    /**
     *
     * @param str
     * @param stripChars
     * @return Returns a copy of the string, with trailing <i>stripChars</i>
     * omitted.
     */
    public static String stripEnd(String str, String stripChars) {
        int end;
        if ((str == null) || ((end = str.length()) == 0)) {
            return str;
        }
        if (stripChars == null) {
            while ((end != 0) && (Character.isWhitespace(str.charAt(end - 1)))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }

        return str.substring(0, end);
    }

    public static boolean isEmpty(String str) {
        return (str == null) || (str.length() == 0);
    }

    /**
     *
     * @param str
     * @param stripChars
     * @return Returns a copy of the string, with leading and trailing
     * <i>stripChars</i> omitted.
     */
    public static String strip(String str, String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     *
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     *
     * @param array
     * @param separator
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        int bufSize = endIndex - startIndex;
        if (bufSize <= 0) {
            return "";
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                if (array[i].toString().contains(separator)) {
                    buf.append("\"");
                    buf.append(array[i]);
                    buf.append("\"");
                    continue;
                }
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     *
     * @param iterator
     * @param separator
     * @return
     */
    public static String join(Iterator iterator, String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first == null ? "" : first.toString();
        }

        StringBuilder buf = new StringBuilder();
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     *
     * @param collection
     * @param separator
     * @return
     */
    public static String join(Collection collection, String separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * Splits this string around matches of the given regular expression
     *
     * @param str
     * @param regex : The regular expression is used to split the string
     * @return The list of strings computed by splitting this string around
     * matches of the given regular expression
     */
    public static List<String> toList(String str, String regex) {
        ArrayList<String> list = new ArrayList<>();

        if (str != null) {
            boolean quoted = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '"') {
                    quoted = !quoted;
                    sb.append(str.charAt(i));
                } else if ((!quoted) && (regex.indexOf(str.charAt(i)) >= 0)) {
                    list.add(sb.toString().trim().replaceAll("\"", ""));
                    sb.setLength(0);
                } else {
                    sb.append(str.charAt(i));
                }
            }
            if (sb.length() > 0) {
                list.add(sb.toString().trim().replaceAll("\"", ""));
            }
        }
        return list;
    }

    /**
     * Splits this string around matches of the given regular expression
     *
     * @param <T> The subclass of Number (support: Long, Integer, Float, Double)
     * @param str The String to be used to create the list of the specified type
     * @param regex The regular expression is used to split the string
     * @param clazz Must be a subclass of Number. Defines the type of the new
     * List. Only support: Long, Integer, Float, Double
     * @return The list of specified type is computed by splitting this string
     * around matches of the given regular expression <br><b>Note: </b> items
     * which are not valid number format will be skip(not add to return data
     * list)
     */
    public static <T extends Number> List<T> toList(String str, String regex, final Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (str != null) {
            boolean quoted = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '"') {
                    quoted = !quoted;
                    sb.append(str.charAt(i));
                } else if ((!quoted) && (regex.indexOf(str.charAt(i)) >= 0)) {
                    try {
                        if (clazz.equals(Integer.class)) {
                            list.add((T) Integer.valueOf(sb.toString().trim().replaceAll("\"", "")));
                        } else if (clazz.equals(Long.class)) {
                            list.add((T) Long.valueOf(sb.toString().trim().replaceAll("\"", "")));
                        } else if (clazz.equals(Float.class)) {
                            list.add((T) Float.valueOf(sb.toString().trim().replaceAll("\"", "")));
                        } else if (clazz.equals(Double.class)) {
                            list.add((T) Double.valueOf(sb.toString().trim().replaceAll("\"", "")));
                        } else if (clazz.equals(Short.class)) {
                            list.add((T) Short.valueOf(sb.toString().trim().replaceAll("\"", "")));
                        } else {
//                        String.format("Type %s is not supported (yet)", clazz.getName());
                        }
                    } catch (NumberFormatException e) {
                    }
                    sb.setLength(0);
                } else {
                    sb.append(str.charAt(i));
                }
            }
            if (sb.length() > 0) {
                try {
                    if (clazz.equals(Integer.class)) {
                        list.add((T) Integer.valueOf(sb.toString().trim().replaceAll("\"", "")));
                    } else if (clazz.equals(Long.class)) {
                        list.add((T) Long.valueOf(sb.toString().trim().replaceAll("\"", "")));
                    } else if (clazz.equals(Float.class)) {
                        list.add((T) Float.valueOf(sb.toString().trim().replaceAll("\"", "")));
                    } else if (clazz.equals(Double.class)) {
                        list.add((T) Double.valueOf(sb.toString().trim().replaceAll("\"", "")));
                    } else if (clazz.equals(Short.class)) {
                        list.add((T) Short.valueOf(sb.toString().trim().replaceAll("\"", "")));
                    } else {
//                    String.format("Type %s is not supported (yet)", clazz.getName());
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return list;
    }

    public static String normalizeSpecialChar(String input) {
        String str = org.apache.commons.lang3.StringUtils.removePattern(input.trim(), "[^A-Za-z0-9 ]");
        return org.apache.commons.lang3.StringUtils.replacePattern(str, "( +)", ".");
    }
}
