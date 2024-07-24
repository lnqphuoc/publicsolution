/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.gearman;

import com.ygame.framework.common.LogUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.gearman.common.GearmanNIOJobServerConnection;
import org.gearman.worker.GearmanFunction;
import org.gearman.worker.GearmanWorker;
import org.gearman.worker.GearmanWorkerImpl;

/**
 *
 * @author huynxt
 */
public class GWorkerRunner implements Runnable {

    private static Logger logger = LogUtil.getLogger(GWorkerRunner.class);
    
    GearmanNIOJobServerConnection conn;
    List<Class<GearmanFunction>> functions;

    public GWorkerRunner(String host, int port, List<Class<GearmanFunction>> funs) {
        this.conn = new GearmanNIOJobServerConnection(host, port);
        this.functions = new ArrayList();
        this.functions.addAll(funs);
    }

    @Override
    public void run() {
        try {
            GearmanWorker worker = new GearmanWorkerImpl();
            worker.addServer(this.conn);
            for (Class fun : this.functions) {
                worker.registerFunction(fun);
            }
            worker.work();
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            //ex.printStackTrace();
        }
    }
}