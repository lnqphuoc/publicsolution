package ga.pool;

import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;
import ga.log4j.GA;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractPool {

    private static final int INIT = 0;
    private static final int RUNNING = 1;
    private static final int SHUTTING_DOWN = 2;
    private static final int SHUTDOWN = 3;
    private ConcurrentHashMap mWorkers;
    private String mPoolName;
    private int mShutdown;
    private int mPoolSize;
    private BlockingQueue<AbstractJob> mListJob;

    public AbstractPool(String aName) {
        GA.pool.info(aName + " Created");
        this.mPoolName = aName;
        this.mWorkers = new ConcurrentHashMap();
        this.mListJob = new LinkedBlockingQueue();
        configReloaded();
        this.mShutdown = 0;
    }

    public void configReloaded() {
        GA.pool.info("Reloading pool " + getPoolName());
        this.mPoolSize = ConvertUtils.toInt(Config.getParam(getPoolName(), "size"), 5);
        adjustPoolSize();
    }

    public void start() {
        synchronized (this) {
            if (this.mShutdown != 0) {
                return;
            }
            this.mShutdown = 1;
        }
        adjustPoolSize();
    }

    public void shutdown() {
        synchronized (this) {
            if ((this.mShutdown != 0) && (this.mShutdown != 1)) {
                return;
            }
            this.mShutdown = 2;
            notifyAll();
        }
        GA.pool.info("Shutting down pool " + getPoolName());
        synchronized (this) {
            if (this.mWorkers.isEmpty()) {
                GA.pool.warn("No workers running, emptying job queue");
                synchronized (this.mListJob) {
                    this.mListJob.clear();
                }
            }
        }
        testShutdownComplete();
    }

    public void removeWorker(String workerName) {
        this.mWorkers.remove(workerName);
        GA.pool.info("removed " + workerName + " from " + this.mPoolName);
    }

    public void addWorker(String workerName, Object objectWorker) {
        this.mWorkers.putIfAbsent(workerName, objectWorker);
        GA.pool.info("added " + workerName + " to " + this.mPoolName + " pool");
    }

    private WorkerThread createWorkerThread() {
        return new WorkerThread(this);
    }

    public String getPoolName() {
        return this.mPoolName;
    }

    public int getConfiguredPoolSize() {
        return this.mPoolSize;
    }

    protected void adjustPoolSize() {
        adjustPoolSize(getConfiguredPoolSize());
    }

    protected void adjustPoolSize(int newCount) {
        int currentCount;
        synchronized (this) {
            currentCount = this.mWorkers.size();
        }
        boolean resized = currentCount != newCount;
        if ((this.mShutdown == 3) || (this.mShutdown == 0)) {
            return;
        }
        GA.pool.info("adjust pool size from " + currentCount + " to " + newCount);
        while (currentCount < newCount) {
            WorkerThread wt = createWorkerThread();
            wt.activeWorker = true;
            synchronized (this) {
                this.mWorkers.putIfAbsent(wt.getName(), wt);
            }
            wt.start();
            currentCount++;
        }
        while (currentCount > newCount) {
            synchronized (this.mWorkers) {
                WorkerThread wt = (WorkerThread) this.mWorkers.get(Integer.valueOf(currentCount - 1));
                if (!wt.waitingForDoJob) {
                    wt.activeWorker = false;
                    currentCount--;
                    wt.interrupt();
                }
            }
        }
        if (resized) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public void deployJob(AbstractJob aJob) {
        if (this.mShutdown == 0) {
            start();
        }
        if (this.mShutdown != 1) {
            GA.pool.fatal(getPoolName() + " pool has shutdown");
            return;
        }
        try {
            GA.pool.debug("deploying job");
            this.mListJob.put(aJob);
        } catch (InterruptedException e) {
            GA.pool.error("deploying job fail", e);
        }
    }

    public AbstractJob getNextJob() {
        AbstractJob aJob = null;

        Thread t = Thread.currentThread();
        if (!(t instanceof WorkerThread)) {
            return null;
        }
        WorkerThread wt = (WorkerThread) t;
        if (wt.activeWorker) {
            try {
                aJob = (AbstractJob) this.mListJob.take();
            } catch (Exception e) {
                GA.pool.error("getNextJob fail", e);
            }
        }
        if (aJob == null) {
            if (!wt.waitingForDoJob) {
                wt.activeWorker = false;
                wt.interrupt();
            }
            testShutdownComplete();
            return null;
        }
        if ((aJob instanceof TransactionJob)) {
            GA.setTrans(((TransactionJob) aJob).getTransID());
        } else {
            GA.clearTrans();
        }
        GA.setType(aJob.getJobType());
        GA.pool.debug("got next job");

        return aJob;
    }

    private int jobCount() {
        return this.mListJob.size();
    }

    private void testShutdownComplete() {
        if (this.mShutdown != 2) {
            return;
        }
        synchronized (this) {
            if (jobCount() > 0) {
                return;
            }
        }
        synchronized (this) {
            this.mShutdown = 3;
            notifyAll();
        }
        GA.pool.info(getPoolName() + " Pool shutdown complete");
    }
}
