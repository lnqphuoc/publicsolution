
package com.ygame.framework.queue;

public abstract interface Queue {
	
	public abstract QueueCommand take();

	public abstract boolean put(QueueCommand paramQueueCommand);

	public abstract void process();

	public abstract int size();
	
	public abstract int remaining();

	public abstract int getWorkerNum();

	public abstract int getMaxLength();
	
}
