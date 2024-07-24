/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ygame.framework.jobqueue.gearman;

import com.ygame.framework.jobqueue.JobQueueClient;
import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.gearman.JobEnt;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.JSONUtil;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.gearman.client.GearmanClientImpl;
import org.gearman.client.GearmanJob;
import org.gearman.client.GearmanJobImpl;
import org.gearman.client.GearmanJobResult;
import org.gearman.common.GearmanJobServerConnection;
import org.gearman.common.GearmanNIOJobServerConnection;

/**
 *
 * @author huynxt
 */
public class GearmanClient extends JobQueueClient {
    Logger logger = LogUtil.getLogger(GearmanClient.class);
    
    final ArrayBlockingQueue<org.gearman.client.GearmanClient> queue;
    final AtomicLong totalQueueErrors = new AtomicLong();
    final Integer maxQueue;
    final Integer minQueue;
    final String host;
    final Integer port;
    final String function;
    
    public GearmanClient(String instanceName) {

        host = Config.getParam(instanceName, "host");
        port = ConvertUtils.toInt(Config.getParam(instanceName, "port"), 4730);

        /*
         if (host == null || host.isEmpty()){
         throw new Exception("Please config this instance : " + name);
         }
         * */

        maxQueue = ConvertUtils.toInt(Config.getParam(instanceName, "maxqueue"), 1024*1024);
        minQueue = ConvertUtils.toInt(Config.getParam(instanceName, "minqueue"), 256);

        function = Config.getParam(instanceName, "function");

        queue = new ArrayBlockingQueue(maxQueue);
    }
    
    private org.gearman.client.GearmanClient borrowClient() {
        org.gearman.client.GearmanClient client = null;
        if (queue.size() > 0) {
            synchronized (GearmanClient.class) {
                try {
                    client = (org.gearman.client.GearmanClient) queue.take();
                } catch (InterruptedException ex) {
                }
            }
        }
        if (client == null) {
            client = new GearmanClientImpl();
            GearmanJobServerConnection connection = new GearmanNIOJobServerConnection(host, port);
            client.addJobServer(connection);
        }

        return client;
    }
    
    void returnClient(org.gearman.client.GearmanClient client) {
        try {
            if (client != null && this.queue.size() <= maxQueue) {
                this.queue.put(client);
            } else {
                destroyClient(client);
            }
        } catch (InterruptedException e) {
            logger.error("Exception in put", e);
        }
    }
    
    void destroyClient(org.gearman.client.GearmanClient client) {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }
    
    boolean push(byte[] data) {
        return push(data, this.function);
    }
    
    boolean push(byte[] data, GearmanJob.JobPriority priority) {
        return push(data, this.function, priority);
    }

    boolean push(byte[] data, String function) {
        boolean result = false;
        org.gearman.client.GearmanClient client = this.borrowClient();
        try {
            GearmanJob job = GearmanJobImpl.createBackgroundJob(function, data, "");
            client.submit(job);
            GearmanJobResult res = job.get();
            result = res.jobSucceeded();
            this.returnClient(client);
        } catch (Exception ex) {
            totalQueueErrors.incrementAndGet();
            logger.error(ex);
            destroyClient(client);
        }
        return result;
    }
    
    boolean push(byte[] data, String function, GearmanJob.JobPriority priority) {
        boolean result = false;
        org.gearman.client.GearmanClient client = this.borrowClient();
        try {
            GearmanJob job = GearmanJobImpl.createBackgroundJob(function, data, priority, "");
            client.submit(job);
            GearmanJobResult res = job.get();
            result = res.jobSucceeded();
            this.returnClient(client);
        } catch (Exception ex) {
            totalQueueErrors.incrementAndGet();
            logger.error(ex);
            destroyClient(client);
        }
        return result;
    }
    
    @Override
    public boolean push(JobEnt jobIn, String function) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data, function);
    }
    
    @Override
    public boolean push(JobEnt jobIn) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data);
    }
    
    public boolean push(JobEnt jobIn, String function, GearmanJob.JobPriority priority) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data, function, priority);
    }
    
    public boolean push(JobEnt jobIn, GearmanJob.JobPriority priority) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data, priority);
    }

    public Long getTotalErrors() {
        return totalQueueErrors.get();
    }
}
