package fr.miage.filestore.index;

public class IndexStoreJob {

    private String id;
    private String type;
    private String node;
    private long startDate;
    private long stopDate;
    private int failures;
    private Status status;
    private String output;

    public IndexStoreJob() {
        this.failures = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStartDate() {
        return startDate;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getStopDate() {
        return stopDate;
    }

    public void setStopDate(long stopDate) {
        this.stopDate = stopDate;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "IndexStoreJob{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", node='" + node + '\'' +
                ", startDate=" + startDate +
                ", stopDate=" + stopDate +
                ", failures=" + failures +
                ", status=" + status +
                ", output='" + output + '\'' +
                '}';
    }

    public enum Status {
        PENDING,
        RUNNING,
        DONE,
        FAILED
    }


}
