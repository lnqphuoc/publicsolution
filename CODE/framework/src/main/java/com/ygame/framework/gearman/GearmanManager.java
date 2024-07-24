/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.gearman;

import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author huynxt
 */
public class GearmanManager implements GearmanManagerMBean {

    private static Logger logger = LogUtil.getLogger(GearmanManager.class);
    private static final Integer _msleep_idle = 1000; // 1 second
    private List<GWorkerRunner2> lstWorker;

    public GearmanManager() {
        lstWorker = new ArrayList<>();
    }

    public void start(String[] args) {

        if (args.length == 0) {
            return;
        }

        String serviceName = args[0];        

        // MBean stuff        
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(this, new ObjectName("com.nct.gearman:type=" + serviceName));
        } catch (Exception e) {
            logger.error(LogUtil.stackTrace(e));
        }

        logger = LogUtil.getLogger("worker-" + serviceName);

        String host = Config.getParam(serviceName, "host");
        int port = ConvertUtils.toInt(Config.getParam(serviceName, "port"));
        int workerNumber = ConvertUtils.toInt(Config.getParam(serviceName, "worker"));

        String strFunction = Config.getParam(serviceName, "function");
        String strClassName = Config.getParam(serviceName, "classname");

        Map<String, String> mapFuncs = new HashMap<String, String>();
        try {
            // StartService
            // neu khong config classname thi dung kieu cu
            String[] arrFunction = strFunction.split(":");
            for (int i = 0; i < arrFunction.length; i++) {
                if (!StringUtils.isEmpty(arrFunction[i])) {
                    if (StringUtils.isEmpty(strClassName)) {
                        mapFuncs.put(arrFunction[i], arrFunction[i]);
                    } else {
                        mapFuncs.put(arrFunction[i], strClassName);
                    }

                }
            }

            logger.info("Starting service " + serviceName + " with " + workerNumber + " worker...");
            for (int i = 0; i < workerNumber; i++) {
                logger.info("Starting worker " + i + "...");
                GWorkerRunner2 worker = new GWorkerRunner2(host, port, mapFuncs);
                lstWorker.add(worker);
                new Thread(worker).start();
            }

        } catch (Exception ex) {
            logger.error("Having exception when start service " + serviceName);
            logger.error(LogUtil.stackTrace(ex));
        }

    }

    @Override
    public boolean stop() {
        for (GWorkerRunner2 worker : lstWorker) {
            worker.stop();
        }

        boolean isRunning = true;
        while (isRunning) {
            isRunning = false;
            for (GWorkerRunner2 worker : lstWorker) {
                if (worker.isRunning()) {
                    isRunning = true;
                    break; // khong can kiem tra cac worker sau.
                }
            }
            // sleep
            try {
                Thread.sleep(_msleep_idle);
            } catch (InterruptedException ex) {
                logger.error(LogUtil.stackTrace(ex));
            }
        }
        logger.error("Worker is stopped.");

        return true;
    }

    @Override
    public boolean status() {
        for (GWorkerRunner2 worker : lstWorker) {
            if (worker.isRunning()) {
                return true;
            }
        }
        return false;
    }
}