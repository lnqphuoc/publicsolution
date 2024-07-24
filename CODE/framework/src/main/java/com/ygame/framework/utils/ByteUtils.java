/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author huynxt
 */
public class ByteUtils {

    public synchronized static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public synchronized static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }

    public static List<byte[]> ListLong2ListByte(List<Long> list) {
        List<byte[]> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Long longValue : list) {
                result.add(longToBytes(longValue));
            }
        }
        return result;
    }

    public static List<byte[]> ListString2ListByte(List<String> list) {
        List<byte[]> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (String stringValue : list) {
                result.add(stringValue.getBytes());
            }
        }
        return result;
    }

    public static byte[][] ListLong2ArrayByte(List<Long> list) {

        if (list == null || list.isEmpty()) {
            return null;
        }
        byte[][] result = new byte[list.size()][];
        int i = 0;
        for (Long longValue : list) {
            result[i] = longToBytes(longValue);
            ++i;
        }
        return result;
    }

    public static byte[][] ListString2ArrayByte(List<String> list) {

        if (list == null || list.isEmpty()) {
            return null;
        }
        byte[][] result = new byte[list.size()][];
        int i = 0;
        for (String stringValue : list) {
            result[i] = stringValue.getBytes();
            ++i;
        }
        return result;
    }

    public static List<Long> ListByte2ListLong(List<byte[]> list) {
        List<Long> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (byte[] _byte : list) {
                result.add(bytesToLong(_byte));
            }
        }
        return result;
    }

    public static List<String> ListByte2ListString(List<byte[]> list) {
        List<String> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (byte[] _byte : list) {
                result.add(new String(_byte));
            }
        }
        return result;
    }

}
