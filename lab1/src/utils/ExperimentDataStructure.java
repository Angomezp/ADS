package lab1.src.utils;

import lab1.src.datastructures.rmqInterface;

public class ExperimentDataStructure {
    public int[] array;
    public int[][] queries;
    public rmqInterface rmq;

    private String name = rmq.getClass().getSimpleName();

    // Performance metrics
    // Preprocess metrics
    private double AvgPreprocessingTimeMs;
    private double MaxPreprocessingTimeMs;
    private double MinPreprocessingTimeMs;
    private double StddevPreprocessingTimeMs;
    private double MedianPreprocessingTimeMs;



    // Query metrics
    private double maxQueryTimeMs;
    private double minQueryTimeMs;
    private double averageQueryTimeMs;
    private double stddevQueryTimeMs;
    private double medianQueryTimeMs;
    private double throughputQueriesPerSec;

    // Space metrics
    private long memoryBytes;


    public ExperimentDataStructure(int[] array, int[][] queries, rmqInterface rmq) {
        this.array = array;
        this.queries = queries;
        this.rmq = rmq;
    }

}
