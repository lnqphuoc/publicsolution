/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;

/**
 *
 * @author huynxt
 */
public class TServerManager {

    private static final Logger logger = Logger.getLogger(TServerManager.class);

    public void start(String serviceName, TProcessor processor) {
        String host = Config.getParam(serviceName, "host");
        Integer port = ConvertUtils.toInt(Config.getParam(serviceName, "port"));        
        Integer minThread = ConvertUtils.toInt(Config.getParam(serviceName, "minthread"), 16);
        Integer maxThread = ConvertUtils.toInt(Config.getParam(serviceName, "maxthread"), 1024);
        
        String serverMode = ConvertUtils.toString(Config.getParam(serviceName, "servermode"), "threadpool");
        
        TServerThread serverThread = new TServerThread(serverMode ,host, port, minThread, maxThread, processor);
        serverThread.start();
        
        logger.info(serviceName + " service start with host : " + host + ":" + port);
        
    }
}
