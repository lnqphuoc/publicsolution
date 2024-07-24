/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.common;

import com.ygame.framework.utils.DateTimeUtils;
import ga.log4j.GA;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

/**
 *
 * @author Huy Nguyen
 */
public class LogTabUtils {
    private static final String TAG = LogTabUtils.class.getName();
    private static final String DELIMITER_CHAR = "\t";
//    private static final String DELIMITER_CHAR = "<tab>";
    private static final String BLANK = "-";
    private static final String ENTER = "<enter>";
    
    private static Map<String, LogTabUtils> instances = new NonBlockingHashMap();
    
    private static final Lock createLock = new ReentrantLock();
    private String tableLogger;

    private LogTabUtils(final String tableLogger) {
        this.tableLogger = tableLogger;
    }

    public static LogTabUtils getInstance(final String tableLogger) {
        if (!instances.containsKey(tableLogger)) {
            try {
                createLock.lock();
                if (!instances.containsKey(tableLogger)) {
                    instances.put(tableLogger, new LogTabUtils(tableLogger));
                }
            } finally {
                createLock.unlock();
            }
        }
        return instances.get(tableLogger);
    }

    private static StringBuffer toStringBuffer(final Object object) {
        StringBuffer messLog = new StringBuffer();
        try {
            for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
                for (Field field : object.getClass().getDeclaredFields()) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    int index = -1;
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof LogIndex) {
                            index = ((LogIndex) annotation).index();
                        }
                    }
                    if (index == i) {
                        if (field.get(object) instanceof Date) {
                            Date date = (Date) field.get(object);
                            messLog.append(DateTimeUtils.toString(date, "yyyy-MM-dd HH:mm:ss")).append(DELIMITER_CHAR);
                        } else {
                            String value = Objects.toString(field.get(object), BLANK);
                            value = value.replace("\n", ENTER);
                            messLog.append(value).append(DELIMITER_CHAR);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {

        }
        return messLog;
    }
    
    public void writeLog(final Object object) {
        try {
            StringBuffer log = toStringBuffer(object);
//            System.err.println("log: " + log);
            GA.getLogger(tableLogger).info(log);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void writeLog(Logger logger, final Object object) {
        try {
            logger.info(toStringBuffer(object));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
