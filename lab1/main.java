package lab1;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lab1.src.datastructures.BlockDecompRMQ;
import lab1.src.datastructures.FischerHeunRMQ;
import lab1.src.datastructures.FullPreprocessingRMQ;
import lab1.src.datastructures.HybridARMQ;
import lab1.src.datastructures.HybridBRMQ;
import lab1.src.datastructures.HybridCRMQ;
import lab1.src.datastructures.NaiveRMQ;
import lab1.src.datastructures.SegmentTreeRMQ;
import lab1.src.datastructures.SparseTableRMQ;
import lab1.src.datastructures.rmqInterface;
import lab1.src.utils.CSVHandler;
import lab1.src.utils.ExperimentDataStructure;
import lab1.src.utils.RandomGenerator;
import lab1.src.utils.StatsUtils;
import lab1.src.utils.Validator;

public class Main {
    public static void main(String[] args) {

        // Configurations for experiments
        int testSize = 5_000;
        int[] sizes = {1 << 10, 1 << 12, 1 << 14, 1 << 16, 1 << 18, 1 << 20};
        int bound = 1_000_000_000;
        long seed = 123456789L;
        int numCorrectnessQueries = 10_000;
        int numTrials = 30;
        int numQueries;

        // Variables
        int[] testArray;
        int[][] testQueries;
        ExperimentDataStructure experiment;

        // Ensure CSV directory exists before running experiments
        Path csvDir = Paths.get("csv");
        try {
            CSVHandler.ensureDir(csvDir);
        } catch (IOException e) {
            System.err.println("Failed to create csv directory: " + e.getMessage());
        }

        String[] implNames = new String[] {
            "NaiveRMQ",
            "FullPreprocessingRMQ",
            "BlockDecompRMQ",
            "SparseTableRMQ",
            "HybridARMQ",
            "HybridBRMQ",
            "HybridCRMQ",
            "FischerHeunRMQ",
            "SegmentTreeRMQ"
        };

        // Correctness Validation
        System.out.println("Validating implementations with random test cases...");

        testArray = RandomGenerator.generateRandomArray(testSize, bound, seed);
        testQueries = RandomGenerator.generateRandomQueries(numCorrectnessQueries, testArray.length, seed);

        for (String implName : implNames) {
            rmqInterface impl = createInstance(implName, testArray);
            impl.preprocess();
            impl.countMemoryBytes();
            boolean ok = Validator.validateDataStructure(impl, testArray, testQueries);
            System.out.println(" - " + impl.getClass().getSimpleName() + " valid: " + ok);
        }

        // Performance Experiments

        Map<String, Map<Integer, List<Double>>> batchQueryMsByImpl = new HashMap<>();
        Map<String, Map<Integer, List<Double>>> throughputs = new HashMap<>();

        for (String implName : implNames) {
            for (int size : sizes) {

                System.out.println("Running experiments for: " + implName + " with size " + size);

                if (size <= 1 << 14) {
                    numQueries = 5_000_000;
                } else {
                    numQueries = 1_000_000;
                    if (implName.equals("FullPreprocessingRMQ")) {
                        break;
                    }
                }

                // accumulate per-size lists for this impl
                List<Long> preprocessList = new ArrayList<>();
                List<Long> memoryList = new ArrayList<>();
                List<Double> throughputList = new ArrayList<>();

                for (int trial = 0; trial < numTrials; trial++) {
                    testArray = RandomGenerator.generateRandomArray(size, bound, seed + trial);
                    testQueries = RandomGenerator.generateRandomQueries(numQueries, size, seed + trial);

                    rmqInterface impl = createInstance(implName, testArray);
                    experiment = new ExperimentDataStructure(testArray, testQueries, impl);
                    experiment.Experiment();

                    preprocessList.add(experiment.getPreprocessTimeMs());
                    memoryList.add(experiment.getMemoryBytes());

                    double batchQueryMs = experiment.getQueryTimeMsDouble();
                    batchQueryMsByImpl.computeIfAbsent(implName, k -> new HashMap<>())
                        .computeIfAbsent(size, k -> new ArrayList<>()).add(batchQueryMs);

                    double throughput = experiment.getThroughputOpsPerSec();
                    throughputs.computeIfAbsent(implName, k -> new HashMap<>())
                        .computeIfAbsent(size, k -> new ArrayList<>()).add(throughput);
                    throughputList.add(throughput);

                    if ((trial + 1) % 5 == 0) {
                        System.out.println(" - Completed trial " + (trial + 1) + " of " + numTrials);
                    }
                }

                // After finishing trials for this impl and size, compute stats and append to CSVs
                long medPre = StatsUtils.medianLong(preprocessList);
                double meanPre = StatsUtils.meanLong(preprocessList);
                double sdPre = StatsUtils.stddevLong(preprocessList);
                long minPre = preprocessList.stream().mapToLong(x -> x).min().orElse(0L);
                long maxPre = preprocessList.stream().mapToLong(x -> x).max().orElse(0L);
                CSVHandler.appendPreprocessLine(csvDir.resolve("preprocessing_time.csv"), implName, size, medPre, meanPre, sdPre, minPre, maxPre);

                double medQuery = StatsUtils.medianDouble(batchQueryMsByImpl.getOrDefault(implName, new HashMap<>()).getOrDefault(size, new ArrayList<>()));
                double meanQuery = StatsUtils.meanDouble(batchQueryMsByImpl.getOrDefault(implName, new HashMap<>()).getOrDefault(size, new ArrayList<>()));
                double sdQuery = StatsUtils.stddevDouble(batchQueryMsByImpl.getOrDefault(implName, new HashMap<>()).getOrDefault(size, new ArrayList<>()));
                double minQuery = batchQueryMsByImpl.getOrDefault(implName, new HashMap<>()).getOrDefault(size, new ArrayList<>()).stream().mapToDouble(x -> x).min().orElse(0.0);
                double maxQuery = batchQueryMsByImpl.getOrDefault(implName, new HashMap<>()).getOrDefault(size, new ArrayList<>()).stream().mapToDouble(x -> x).max().orElse(0.0);
                double medThroughput = StatsUtils.medianDouble(throughputList);
                CSVHandler.appendQueryLine(csvDir.resolve("query_time.csv"), implName, size, medThroughput, medQuery, meanQuery, sdQuery, minQuery, maxQuery);

                long medMem = StatsUtils.medianLong(memoryList);
                double bpe = (double) medMem / (double) size;
                CSVHandler.appendMemoryLine(csvDir.resolve("memory_usage.csv"), implName, size, medMem, bpe);

                System.out.println("Finished " + implName + " size " + size + "  stats saved.");
            }
        }

        System.out.println("CSV files updated in: " + csvDir.toAbsolutePath());
    }

    private static rmqInterface createInstance(String name, int[] arr) {
        return switch (name) {
            case "NaiveRMQ" -> new NaiveRMQ(arr);
            case "FullPreprocessingRMQ" -> new FullPreprocessingRMQ(arr);
            case "BlockDecompRMQ" -> new BlockDecompRMQ(arr);
            case "SparseTableRMQ" -> new SparseTableRMQ(arr);
            case "HybridARMQ" -> new HybridARMQ(arr);
            case "HybridBRMQ" -> new HybridBRMQ(arr);
            case "HybridCRMQ" -> new HybridCRMQ(arr);
            case "FischerHeunRMQ" -> new FischerHeunRMQ(arr);
            case "SegmentTreeRMQ" -> new SegmentTreeRMQ(arr);
            default -> throw new IllegalArgumentException("Unknown impl: " + name);
        };
    }
}
                        
