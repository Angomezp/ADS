package lab1.src.utils;

import lab1.src.datastructures.rmqInterface;

public class ExperimentDataStructure {
    private final int[] array;
    private final int[][] queries;
    private final rmqInterface rmq;

    private long queryTimeNs;
    private double throughputOpsPerSec;

    private long preprocessTimeMs;
    private long memoryBytes;


    public ExperimentDataStructure(int[] array, int[][] queries, rmqInterface rmq) {
        this.array = array;
        this.queries = queries;
        this.rmq = rmq;
    }

    public void Experiment (){
        int[] indexResults = new int[this.queries.length];
        int L ;
        int R ;

        // Preprocess time
        long startPreprocess = System.currentTimeMillis();
        this.rmq.preprocess();
        long endPreprocess = System.currentTimeMillis();
        this.preprocessTimeMs = endPreprocess - startPreprocess ;


        // Query time (high-resolution)
        long startQuery = System.nanoTime();
        int idx = 0;
        for (int[] query : this.queries) {
            L = query[0];
            R = query[1];
            indexResults[idx] = this.rmq.RMQ(L, R);
            idx++;
        }
        long endQuery = System.nanoTime();
        this.queryTimeNs = endQuery - startQuery;

        // Throughput: queries per second for the batch (using nanoseconds)
        int numQueries = this.queries.length;
        if (this.queryTimeNs > 0) {
            this.throughputOpsPerSec = (double) numQueries * 1e9 / (double) this.queryTimeNs;
        } else {
            this.throughputOpsPerSec = Double.POSITIVE_INFINITY;
        }

        // Memory usage
        this.rmq.countMemoryBytes();
        this.memoryBytes = this.rmq.getMemoryBytes();

        // final check to ensure all queries are executed and correct
        boolean valid = Validator.confirmQueries(this.array, indexResults, this.queries, this.rmq);
        if (!valid) {
            System.out.println("Warning: Query results did not match expected values. Check implementation correctness.");
        }
    }

    public double getQueryTimeMsDouble() {
        return (double) this.queryTimeNs / 1_000_000.0;
    }

    public double getThroughputOpsPerSec() {
        return this.throughputOpsPerSec;
    }

    public long getPreprocessTimeMs() {
        return this.preprocessTimeMs;
    }

    public long getMemoryBytes() {
        return this.memoryBytes;
    }

}
