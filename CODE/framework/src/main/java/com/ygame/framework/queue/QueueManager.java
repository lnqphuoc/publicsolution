
package com.ygame.framework.queue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;


public class QueueManager {

	private static Map<String, QueueManager> instances = new NonBlockingHashMap();
	private Queue queue;
        //AtomicInteger jobs = new AtomicInteger();
	

	public static QueueManager getInstance(String name) {
		QueueManager instance = (QueueManager) instances.get(name);
		if (instance == null) {
			synchronized (QueueManager.class) {
				if (instance == null) {
					instance = new QueueManager();
					instances.put(name, instance);
				}
			}
		}
		return instance;
	}

	public void init(int workerNum, int maxLength) {
		this.queue = new QueueImpl(workerNum, maxLength);
	}

	public void process() {
		this.queue.process();
	}

	public int size() {
		return this.queue.size();
	}
	
	public int remaining() {
		return this.queue.remaining();
	}

	public void put(QueueCommand cmd) {
		this.queue.put(cmd);
                //jobs.incrementAndGet();
	}
	
        public boolean hasJobRunning() {
            return (this.queue.size() > 0 || QueueWorker.jobRunning.get() > 0);
            //return jobs.intValue() > 0;
        }
}
