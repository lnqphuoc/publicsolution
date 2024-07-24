/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.jobqueue;

import com.ygame.framework.jobqueue.gearman.GearmanWorker;
import com.ygame.framework.common.LogUtil;
import org.apache.log4j.Logger;

/**
 *
 * @author huynxt
 */
public abstract class JobQueueWorker {

    static final Logger _logger = LogUtil.getLogger(JobQueueWorker.class);

    public static JobQueueWorker getInstance(String instanceName) throws Exception {

        return new GearmanWorker(instanceName);

    }

    public abstract void start();

    public abstract boolean stop();

    public abstract boolean status();

    public abstract void setClassName(String className) throws Exception;

}
