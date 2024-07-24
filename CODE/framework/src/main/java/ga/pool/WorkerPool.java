package ga.pool;

public class WorkerPool
        extends AbstractPool {

    private static WorkerPool sInstance;

    public WorkerPool() {
        super("work");
    }

    public static synchronized WorkerPool getInstance() {
        if (sInstance == null) {
            sInstance = new WorkerPool();
        }
        return sInstance;
    }
}
