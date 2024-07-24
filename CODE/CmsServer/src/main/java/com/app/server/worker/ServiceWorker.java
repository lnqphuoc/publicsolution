package com.app.server.worker;

import com.app.server.manager.ProductManager;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.gearman.JobEnt;

import org.springframework.beans.factory.annotation.Autowired;


public class ServiceWorker {

    @Autowired
    ProductManager dataManager;

    public void executeFunction(JobEnt job) {
        try {
        } catch (Exception ex) {
            LogUtil.printDebug(this.getClass().getName(), ex);
        }
    }
}