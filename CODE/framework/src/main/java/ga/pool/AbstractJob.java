package ga.pool;

public abstract class AbstractJob {

    public abstract void doJob();

    public String getJobType() {
        return "default";
    }
}
