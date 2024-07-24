package com.app.server.worker;

import com.ygame.framework.common.LogUtil;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.ygame.framework.gearman.JobEnt;

public class ServiceClient extends Thread{
    
    private BlockingQueue<JobEnt> jobQueue = new LinkedBlockingQueue<>();
    private static ServiceClient instance = null;
    private static ServiceClient gI(){
        if(instance == null){
            instance = new ServiceClient();
            instance.start();
        }
        return instance;
    }
    
    public void addJob(JobEnt job){
        jobQueue.add(job);
    }

    public BlockingQueue<JobEnt> getJobQueue() {
        return jobQueue;
    }

    public void setJobQueue(BlockingQueue<JobEnt> jobQueue) {
        this.jobQueue = jobQueue;
    }

    private boolean isRunning = true;
    
    public void endTask() {
        this.isRunning = false;
    }
    
    @Override
    public void run() {
        ServiceWorker worker = new ServiceWorker();
        while (isRunning) {
            try {
                final JobEnt response = jobQueue.take();
                worker.executeFunction(response);
            } catch (InterruptedException ex) {
                LogUtil.printDebug("",ex);
            }
        }
    }
}
