/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.gearman;

import com.ygame.framework.common.LogUtil;
import java.util.Map;
import org.apache.log4j.Logger;
import org.gearman.common.GearmanNIOJobServerConnection;
import org.gearman.worker.DefaultGearmanFunctionFactory;
import org.gearman.worker.GearmanWorker;
import org.gearman.worker.GearmanWorkerImpl;

/**
 *
 * @author huynxt
 */
public class GWorkerRunner2 implements Runnable {

    private static Logger logger = LogUtil.getLogger(GWorkerRunner.class);
    
    GearmanNIOJobServerConnection conn;
    Map<String, String> mapFunctions;
    GearmanWorker worker;

    public GWorkerRunner2(String host, int port, Map<String, String> funs) {
        this.conn = new GearmanNIOJobServerConnection(host, port);
        this.mapFunctions = funs;
        worker = new GearmanWorkerImpl();
    }

    @Override
    public void run() {
        try {            
            worker.addServer(this.conn);

            for (Map.Entry<String, String> entry : mapFunctions.entrySet()) {
                DefaultGearmanFunctionFactory gearFunc = new DefaultGearmanFunctionFactory(entry.getKey(), entry.getValue());
                worker.registerFunctionFactory(gearFunc);
            }
            worker.work();

        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
        }
    }

    public  void stop() {
        worker.stop();
    }

    public boolean isRunning() {        
        return worker.isRunning();
    }
}