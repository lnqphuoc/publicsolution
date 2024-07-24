package ga.pool;

import ga.log4j.GA;
import ga.monitor.MonitoredThread;

public class WorkerThread
        extends MonitoredThread {

    AbstractPool mPool;
    private static int sID = 0;
    public boolean waitingForDoJob;
    public boolean activeWorker;

    public WorkerThread(AbstractPool aPool) {
        super(aPool.getPoolName() + " Worker #" + sNextID());
        this.mPool = aPool;
    }

    public static synchronized int sNextID() {
        return ++sID;
    }

    public boolean isActiveWorker() {
        return this.activeWorker;
    }

    public void run() {
        GA.pool.info("started");
        this.activeWorker = true;
        while (!interrupted()) {
            setWaitMode(true);
            AbstractJob aJob = this.mPool.getNextJob();
            if (aJob == null) {
                break;
            }
            setWaitMode(false);
            this.waitingForDoJob = false;

            aJob.doJob();

            this.waitingForDoJob = false;
            GA.pool.info("Finished Job");
            GA.clearTrans();
        }
        this.mPool.removeWorker(getName());
        GA.pool.info("stopped");
    }
}
