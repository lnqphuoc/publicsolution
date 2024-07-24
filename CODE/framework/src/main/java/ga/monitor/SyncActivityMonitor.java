package ga.monitor;

import java.util.Date;

public abstract interface SyncActivityMonitor {

    public abstract Date getLastActivity();

    public abstract boolean getWaitMode();
}
