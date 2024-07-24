package ga.monitor;

import java.util.Date;

public abstract class MonitoredThread
        extends Thread
        implements SyncActivityMonitor {

    private Date mLastActivity;
    private boolean mWaitMode;

    public MonitoredThread() {
    }

    public MonitoredThread(Runnable a) {
        super(a);
    }

    public MonitoredThread(Runnable a, String b) {
        super(a, b);
    }

    public MonitoredThread(String a) {
        super(a);
    }

    public MonitoredThread(ThreadGroup a, Runnable b) {
        super(a, b);
    }

    public MonitoredThread(ThreadGroup a, Runnable b, String c) {
        super(a, b, c);
    }

    public MonitoredThread(ThreadGroup a, String b) {
        super(a, b);
    }

    public synchronized boolean getWaitMode() {
        return this.mWaitMode;
    }

    public synchronized Date getLastActivity() {
        return this.mLastActivity;
    }

    public synchronized void setWaitMode(boolean aFlag) {
        this.mWaitMode = aFlag;
        this.mLastActivity = new Date();
    }

    public synchronized void setLastActivity() {
        this.mLastActivity = new Date();
    }
}
