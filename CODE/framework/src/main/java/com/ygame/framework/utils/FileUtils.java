/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author huynxt
 */
public class FileUtils {
    public static String HOME_PATH;
    static {
        HOME_PATH = System.getProperty("apppath");

        if (HOME_PATH == null) {
            HOME_PATH = "";
        } else {
            HOME_PATH += File.separator;
        }
    }
    
    public static void createFolder(String folder) {
        try {
            new File(folder).mkdirs();
        } catch(Exception ex) {
            
        }
    }

    public static String readAllString(String filePath) {
        try {
            File f = new File(filePath);
            return readAllString(f);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String readAllString(File f) {
        try {
            java.io.BufferedReader bi = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = bi.readLine()) != null) {
                sb.append("\r\n" + str);
            }
            String result = sb.toString();
            if (result.length() > 0) {
                result = result.substring(2);
            }
            return result;
        } catch (Exception ex) {
            return "";
        }
    }
    
    public static boolean writeAllString(String filePath, String data) {
        try {
            File f = new File(filePath);
            return writeAllString(f, data);
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean writeAllString(File f, String data) {
        try {
            try (java.io.FileWriter wr = new java.io.FileWriter(f)) {
                wr.write(data);
            }
            return true;
        } catch (Exception ex) {}
        return false;
    }
    
    public static boolean appendString(String filePath, String data) {
        try {
            File f = new File(filePath);
            return appendString(f, data);
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean appendString(File f, String data) {
        try {
            try (java.io.FileWriter wr = new java.io.FileWriter(f, true)) {
                wr.append(data);
                wr.flush();
            }
            return true;
        } catch (Exception ex) {}
        return false;
    }
    
    public static boolean deleteFile(String filePath) {
        try {
            File f = new File(filePath);
            return f.delete();
        } catch (Exception ex) {}
        return false;
    }
}
