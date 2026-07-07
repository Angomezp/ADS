package lab2.src.Utils;

public class Metrics {

    private long nodesVisited;
    private long rotations;
    private long reportedKeys;

    // getters

    public long getNodesVisited() {
        return this.nodesVisited;
    }

    public long getRotations() {
        return this.rotations;
    }

    public long getReportedKeys() {
        return this.reportedKeys;
    }

    // incrementNodesVisited()
    public void incrementNodesVisited() {
        this.nodesVisited++;
    }

    // incrementRotations()
    public void incrementRotations() {
        this.rotations++;
    }

    // incrementReportedKeys()
    public void incrementReportedKeys() {
        this.reportedKeys++;
    }

    // reset()
    public void reset() {
        this.nodesVisited = 0;
        this.rotations = 0;
        this.reportedKeys = 0;
    }
}