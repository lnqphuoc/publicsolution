package com.ygame.framework.queue;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class QueueWorker implements Runnable {

    private static Logger logger = Logger.getLogger(QueueWorker.class);
    private Queue queue;
    private long _msleep_idle = 1000; //default sleep 1ms 
    
    public static AtomicInteger jobRunning = new AtomicInteger();

    public QueueWorker(Queue queue) {
        this.queue = queue;
    }

    public void run() {
        while (true) {
            try {
                QueueCommand command = this.queue.take();
                if (command != null) {
                    try {
                        jobRunning.incrementAndGet();
                        
                        command.execute();
                    } catch (Exception ex) {
                        throw ex;
                    }
                    finally {
                        jobRunning.decrementAndGet();
                        //QueueManager.jobs.decrementAndGet();
                    }                    
                } else {
                    //idle
                    if (this._msleep_idle > 0) {
                        Thread.sleep(_msleep_idle);
                    }
                }
                continue;
            } catch (Exception ex) {
                logger.info("Error in exec QueueWorker", ex);
            }
        }
    }
    
//    public int getJobsRunning() {
//        return jobRunning.get();
//    }
}