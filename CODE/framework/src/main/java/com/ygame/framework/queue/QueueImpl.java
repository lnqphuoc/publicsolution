
package com.ygame.framework.queue;

import java.util.concurrent.ArrayBlockingQueue;
import org.apache.log4j.Logger;

public class QueueImpl implements Queue {

	private static Logger logger = Logger.getLogger(QueueImpl.class);
	private ArrayBlockingQueue<QueueCommand> queue;
	private int workerNum;
	private int maxLength;
	private long msleepIdle;

	public QueueImpl(int workerNum, int maxLength) {
		this.workerNum = workerNum;
		this.maxLength = maxLength;
		this.msleepIdle = msleepIdle;
		this.queue = new ArrayBlockingQueue(maxLength);
	}

	public int size() {
		return this.queue.size();
	}

	public boolean put(QueueCommand i) {
		if (this.queue.remainingCapacity() < this.workerNum) {
			logger.error("Queue exceed max length!!!!!!");
			return false;
		}
		boolean ret = false;
		try {
			this.queue.put(i);
			ret = true;
		} catch (InterruptedException e) {
			logger.error("Exception in put", e);
		}
		return ret;
	}

	public QueueCommand take() {
		QueueCommand cmd = null;
		try {
			cmd = (QueueCommand) this.queue.take();
		} catch (InterruptedException e) {
			logger.error("Exception in take", e);
		}
		return cmd;
	}

	public void process() {
		for (int i = 0; i < this.workerNum; i++) {
			QueueWorker qw = new QueueWorker(this);
			new Thread(qw).start();
		}
	}

	public int getWorkerNum() {
		return this.workerNum;
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public int remaining() {
		return this.queue.remainingCapacity();
	}

}
