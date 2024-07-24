/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.jobqueue.gearman;

import com.ygame.framework.jobqueue.JobQueueWorker;
import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author huynxt
 */
public class GearmanWorker extends JobQueueWorker {

    static final Logger _logger = LogUtil.getLogger(GearmanWorker.class);
    static final Integer _msleep_idle = 1000; // 1 second

    final String instanceName;
    final List<GearmanWorkerRunner> lstWorker;
    String strClassName;
    

    public GearmanWorker(String instanceName) {
        this.instanceName = instanceName;
        lstWorker = new ArrayList<>();
        strClassName = Config.getParam(instanceName, "classname");
    }

    @Override
    public void start() {
        String host = Config.getParam(instanceName, "host");
        int port = ConvertUtils.toInt(Config.getParam(instanceName, "port"));
        int workerNumber = ConvertUtils.toInt(Config.getParam(instanceName, "worker"), 1);

        String strFunction = Config.getParam(instanceName, "function");

        Map<String, String> mapFuncs = new HashMap<>();
        try {
            // StartService
            // neu khong config classname thi dung kieu cu
            String[] arrFunction = strFunction.split(":");
            for (String arrFunction1 : arrFunction) {
                if (!StringUtils.isEmpty(arrFunction1)) {
                    if (StringUtils.isEmpty(strClassName)) {
                        mapFuncs.put(arrFunction1, arrFunction1);
                    } else {
                        mapFuncs.put(arrFunction1, strClassName);
                    }
                }
            }

            _logger.info("Starting service " + instanceName + " with " + workerNumber + " worker...");
            for (int i = 0; i < workerNumber; i++) {
                _logger.info("Starting worker " + i + "...");
                GearmanWorkerRunner worker = new GearmanWorkerRunner(host, port, mapFuncs);
                lstWorker.add(worker);
                new Thread(worker).start();
            }

        } catch (Exception ex) {
            _logger.error(LogUtil.stackTrace(ex));
        }
    }

    @Override
    public boolean stop() {
        for (GearmanWorkerRunner worker : lstWorker) {
            worker.stop();
        }

        boolean isRunning = true;
        while (isRunning) {
            isRunning = false;
            for (GearmanWorkerRunner worker : lstWorker) {
                if (worker.isRunning()) {
                    isRunning = true;
                    break; // khong can kiem tra cac worker sau.
                }
            }
            // sleep
            try {
                Thread.sleep(_msleep_idle);
            } catch (InterruptedException ex) {
                _logger.error(LogUtil.stackTrace(ex));
            }
        }
        _logger.info("Worker is stopped.");

        return true;
    }

    @Override
    public boolean status() {
        for (GearmanWorkerRunner worker : lstWorker) {
            if (worker.isRunning()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void setClassName(String className) throws Exception {
        this.strClassName = className;
    }
}
