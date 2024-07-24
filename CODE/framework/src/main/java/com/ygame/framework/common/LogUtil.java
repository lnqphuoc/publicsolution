/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.common;

import com.ygame.framework.utils.SlackUtils;
import ga.log4j.GA;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author huynxt
 */
public class LogUtil {

    public static boolean initialized = false;
    public static boolean ENABLE_LOG = false;
    public static String TOKEN = StringUtils.EMPTY;
    public static String APPID = StringUtils.EMPTY;

    static {
        init("");
    }

    public static void init(String prefix) {
        if (initialized) {
            return;
        }
        String apppath = System.getProperty("apppath");
        String APP_ENV = System.getProperty("appenv");

        if (APP_ENV == null) {
            APP_ENV = "";
        }
        if (!"".equals(APP_ENV)) {
            APP_ENV = APP_ENV + ".";
        }

        if (apppath == null) {
            apppath = "";
        } else {
            apppath += File.separator;
        }

        String file = apppath + prefix + "conf" + File.separator + APP_ENV + "log4j.ini";

        // 5 phut check lai 1 lan config
        PropertyConfigurator.configureAndWatch(file, 1000 * 60 * 5);
        initialized = true;
    }

    public static void dumpLog(String content) {
        Logger.getLogger("LogUtil").info(content);
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static Logger getLogger(Class c) {
        return Logger.getLogger(c.getCanonicalName());
    }

    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String out = sw.toString();
        pw.close();
        return out;
    }

    public static String getTimestamp() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:s");
        return df.format(date);
    }

    public static void setLogLevel(String logger, String level) {
        Logger loggerObj = LogManager.getLogger(logger);
        if (null == loggerObj) {
            return;
        }
        level = level.toUpperCase();
        if (level.equals("DEBUG")) {
            loggerObj.setLevel(Level.DEBUG);
        } else if (level.equals("ERROR")) {
            loggerObj.setLevel(Level.ERROR);
        } else if (level.equals("FATAL")) {
            loggerObj.setLevel(Level.FATAL);
        } else if (level.equals("INFO")) {
            loggerObj.setLevel(Level.INFO);
        } else if (level.equals("OFF")) {
            loggerObj.setLevel(Level.OFF);
        } else if (level.equals("WARN")) {
            loggerObj.setLevel(Level.WARN);
        } else {
            loggerObj.setLevel(Level.ALL);
        }
    }

    public static String throwableToString(Throwable e) {
        StringBuilder sbuf = new StringBuilder("");
        String trace = stackTrace(e);
        sbuf.append("Exception was generated at : " + getTimestamp() + " on thread " + Thread.currentThread().getName());

        sbuf.append(System.getProperty("line.separator"));
        String message = e.getMessage();
        if (message != null) {
            sbuf.append(message);
        }
        sbuf.append(System.getProperty("line.separator")).append(trace);

        return sbuf.toString();
    }

    public static String getLogMessage(String message) {
        StringBuilder sbuf = new StringBuilder("Log started at : " + getTimestamp());

        sbuf.append(File.separator).append(message);

        return sbuf.toString();
    }

    public static void printDebug(String str) {
        GA.app.error(str);
//        System.out.println(str);
    }

    public static void printDebug(String tag, Exception ex) {
        GA.app.error("Error", ex);
        ex.printStackTrace();
        SlackUtils.sendMessage(tag, ExceptionUtils.getStackTrace(ex));
    }

    public static void printDebugAll(String str) {
        printDebugAll(StringUtils.EMPTY, str);
    }

    public static void printDebugAll(String tag, String str) {
        GA.app.error(str);
        System.err.println(str);
        SlackUtils.sendMessage(tag, str);
    }

    public static void sendToSlack(String token, String appId, String debug) {
        boolean isDebug = false;
        if (StringUtils.isNotBlank(token) && StringUtils.equalsIgnoreCase(token, TOKEN)) {
            isDebug = true;
        }
        if (StringUtils.isNotBlank(appId) && StringUtils.equalsIgnoreCase(appId, APPID)) {
            isDebug = true;
        }
        if (isDebug) {
            printDebugAll(debug);
        }
    }
    
    public static void main(String[] args){
        System.err.println("aaaaaa");
    }
}
