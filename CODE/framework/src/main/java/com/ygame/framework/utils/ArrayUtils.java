/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

/**
 *
 * @author huynxt
 */
public class ArrayUtils extends org.apache.commons.lang.ArrayUtils {
    public static short[] convertString2Short(String[] arr) {
        short[] result = new short[arr.length];
        for(int i=0; i<arr.length; ++i) {
            result[i] = Short.parseShort(arr[i]);
        }
        return result;
    }
    public static String[] convertShort2String(short[] arr) {
        String[] result = new String[arr.length];
        for(int i=0; i<arr.length; ++i) {
            result[i] = Short.toString(arr[i]);
        }
        return result;
    }
    
    public static int[] convertString2Int(String[] arr) {
        int[] result = new int[arr.length];
        for(int i=0; i<arr.length; ++i) {
            result[i] = Integer.parseInt(arr[i]);
        }
        return result;
    }
    public static String[] convertInt2String(int[] arr) {
        String[] result = new String[arr.length];
        for(int i=0; i<arr.length; ++i) {
            result[i] = Integer.toString(arr[i]);
        }
        return result;
    }
    
    public static long[] convertString2Long(String[] arr) {
        long[] result = new long[arr.length];
        for(int i=0; i<arr.length; ++i) {
            try {
                result[i] = Long.parseLong(arr[i]);
            } catch (Exception ex) {}
        }
        return result;
    }
    public static String[] convertLong2String(long[] arr) {
        String[] result = new String[arr.length];
        for(int i=0; i<arr.length; ++i) {
            result[i] = Long.toString(arr[i]);
        }
        return result;
    }    
}
