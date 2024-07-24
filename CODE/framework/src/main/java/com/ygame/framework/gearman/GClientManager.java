/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.gearman;

import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.JSONUtil;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.gearman.client.GearmanClient;
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
public class GClientManager {

    private static Logger logger = LogUtil.getLogger(GClientManager.class);
    private static Map<String, GClientManager> instances = new NonBlockingHashMap();
    private final AtomicLong totalQueueErrors = new AtomicLong();
    private ArrayBlockingQueue<GearmanClient> queue;
    private Integer maxQueue;
    private Integer minQueue;
    private String host;
    private Integer port;
    private String function;

    public static GClientManager getInstance(String name) {
        GClientManager instance = (GClientManager) instances.get(name);
        if (instance == null) {
            synchronized (GClientManager.class) {
                instance = (GClientManager) instances.get(name);
                if (instance == null) {
                    instance = new GClientManager(name);
                    instances.put(name, instance);
                }
            }
        }
        return instance;
    }

    public GClientManager(String name) {

        host = Config.getParam(name, "host");
        port = ConvertUtils.toInt(Config.getParam(name, "port"), 4730);

        /*
         if (host == null || host.isEmpty()){
         throw new Exception("Please config this instance : " + name);
         }
         * */

        maxQueue = ConvertUtils.toInt(Config.getParam(name, "maxqueue"), 1024);
        minQueue = ConvertUtils.toInt(Config.getParam(name, "minqueue"), 256);

        function = Config.getParam(name, "function");

        queue = new ArrayBlockingQueue(maxQueue);
    }

    private GearmanClient borrowClient() {
        GearmanClient client = null;
        if (queue.size() > 0) {
            synchronized (GClientManager.class) {
                try {
                    client = (GearmanClient) queue.take();
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

    private void returnClient(GearmanClient client) {
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

    private void destroyClient(GearmanClient client) {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }

    public boolean push(JobEnt jobIn) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data);
    }

    public boolean push(JobEnt jobIn, String function) {
        // to json
        String jsonData = JSONUtil.Serialize(jobIn);
        // to birary
        byte[] data = ConvertUtils.encodeString(jsonData);
        // push to gearman
        return push(data, function);
    }

    private boolean push(byte[] data) {
        return push(data, this.function);
    }

    private boolean push(byte[] data, String function) {
        boolean result = false;
        GearmanClient client = this.borrowClient();
        try {
            GearmanJob job = GearmanJobImpl.createBackgroundJob(function, data, "");
            client.submit(job);
            GearmanJobResult res = job.get();
            result = res.jobSucceeded();
            this.returnClient(client);
        } catch (Exception ex) {
            totalQueueErrors.incrementAndGet();
            logger.error(ex);
            result = false;
            destroyClient(client);
        }
        return result;
    }

    /*
     * Get stats error
     */
    public Long getTotalErrors() {
        return totalQueueErrors.get();
    }
}
