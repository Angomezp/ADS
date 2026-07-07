package lab2.src.ExperimentsUtils;

public class ExperimentResult {

    private final String treeName;

    private int n;

    private long executionTimeNs;

    private double averageNodesVisited;

    private double averageReportedKeys;

    private long totalRotations;

    // Constructor

    public ExperimentResult() {
        this.treeName = "";
        this.n = 0;
        this.executionTimeNs = 0;
        this.averageNodesVisited = 0.0;
        this.averageReportedKeys = 0.0;
        this.totalRotations = 0;
    }

    public ExperimentResult(String treeName, int n, long executionTimeNs, double averageNodesVisited, double averageReportedKeys, long totalRotations) {
        this.treeName = treeName;
        this.n = n;
        this.executionTimeNs = executionTimeNs;
        this.averageNodesVisited = averageNodesVisited;
        this.averageReportedKeys = averageReportedKeys;
        this.totalRotations = totalRotations;
    }
    // Getters

    public String getTreeName() {
        return this.treeName;
    }

    public int getN() {
        return this.n;
    }

    public long getExecutionTimeNs() {
        return this.executionTimeNs;
    }

    public double getAverageNodesVisited() {
        return this.averageNodesVisited;
    }

    public double getAverageReportedKeys() {
        return this.averageReportedKeys;
    }

    public long getTotalRotations() {
        return this.totalRotations;
    }

    // Setters

    public void setN(int n) {
        this.n = n;
    }

    public void setExecutionTimeNs(long executionTimeNs) {
        this.executionTimeNs = executionTimeNs;
    }

    public void setAverageNodesVisited(double averageNodesVisited) {
        this.averageNodesVisited = averageNodesVisited;
    }

    public void setAverageReportedKeys(double averageReportedKeys) {
        this.averageReportedKeys = averageReportedKeys;
    }

    public void setTotalRotations(long totalRotations) {
        this.totalRotations = totalRotations;
    }
}
