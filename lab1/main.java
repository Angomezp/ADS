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
        rmqInterface[] implementations;
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

        // Correctness Validation
        System.out.println("Validating implementations with random test cases...");

        testArray = RandomGenerator.generateRandomArray(testSize, bound, seed);
        testQueries = RandomGenerator.generateRandomQueries(numCorrectnessQueries, testArray.length, seed);

        implementations = new rmqInterface[] {
            new NaiveRMQ(testArray),
            new FullPreprocessingRMQ(testArray),
            new BlockDecompRMQ(testArray),
            new SparseTableRMQ(testArray),
            new HybridARMQ(testArray),
            new HybridBRMQ(testArray),
            new HybridCRMQ(testArray),
            new FischerHeunRMQ(testArray),
            new SegmentTreeRMQ(testArray)
        };

        for (rmqInterface impl : implementations) {
            impl.preprocess();
            impl.countMemoryBytes();
            boolean ok = Validator.validateDataStructure(impl, testArray, testQueries);
            System.out.println(" - " + impl.getClass().getSimpleName() + " valid: " + ok);
        }

        // Performance Experiments
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

        Map<String, Map<Integer, List<Long>>> preprocessTimes = new HashMap<>();
        Map<String, Map<Integer, List<Long>>> memoryBytes = new HashMap<>();
        Map<String, Map<Integer, List<Double>>> perQueryMs = new HashMap<>();
        Map<String, Map<Integer, List<Double>>> throughputs = new HashMap<>();

        for (String implName : implNames) {
            System.out.println("Running experiments for: " + implName);
            for (int size : sizes) {

                if (size <= 1 << 14) {
                    numQueries = 5_000_000;
                } else {
                    numQueries = 1_000_000;
                    if (implName.equals("FullPreprocessingRMQ")) {
                        break;
                    }
                }

                // accumulate per-size lists for this impl
                List<Long> preprocessListLocal = new ArrayList<>();
                List<Long> memoryListLocal = new ArrayList<>();
                List<Double> perQueryListLocal = new ArrayList<>();
                List<Double> throughputListLocal = new ArrayList<>();

                for (int trial = 0; trial < numTrials; trial++) {
                    testArray = RandomGenerator.generateRandomArray(size, bound, seed + trial);
                    testQueries = RandomGenerator.generateRandomQueries(numQueries, size, seed + trial);

                    rmqInterface impl = createInstance(implName, testArray);
                    experiment = new ExperimentDataStructure(testArray, testQueries, impl);
                    experiment.Experiment();

                    preprocessTimes.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(experiment.getPreprocessTimeMs());
                    preprocessListLocal.add(experiment.getPreprocessTimeMs());

                    memoryBytes.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(experiment.getMemoryBytes());
                    memoryListLocal.add(experiment.getMemoryBytes());

                    double perQuery = (double) experiment.getQueryTimeMs() / (double) numQueries;
                    perQueryMs.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(perQuery);
                    perQueryListLocal.add(perQuery);

                    double throughput = experiment.getThroughputOpsPerSec();
                    throughputs.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(throughput);
                    throughputListLocal.add(throughput);

                    System.out.println("Completado intento " + (trial + 1) + " de " + numTrials);
                }

                // After finishing trials for this impl and size, compute stats and append to CSVs
                long medPre = StatsUtils.medianLong(preprocessListLocal);
                double meanPre = StatsUtils.meanLong(preprocessListLocal);
                double sdPre = StatsUtils.stddevLong(preprocessListLocal);
                long minPre = preprocessListLocal.stream().mapToLong(x -> x).min().orElse(0L);
                long maxPre = preprocessListLocal.stream().mapToLong(x -> x).max().orElse(0L);
                CSVHandler.appendPreprocessLine(csvDir.resolve("preprocessing_time.csv"), implName, size, medPre, meanPre, sdPre, minPre, maxPre);

                double medQuery = StatsUtils.medianDouble(perQueryListLocal);
                double meanQuery = StatsUtils.meanDouble(perQueryListLocal);
                double sdQuery = StatsUtils.stddevDouble(perQueryListLocal);
                double minQuery = perQueryListLocal.stream().mapToDouble(x -> x).min().orElse(0.0);
                double maxQuery = perQueryListLocal.stream().mapToDouble(x -> x).max().orElse(0.0);
                double medThroughput = StatsUtils.medianDouble(throughputListLocal);
                CSVHandler.appendQueryLine(csvDir.resolve("query_time.csv"), implName, size, medThroughput, medQuery, meanQuery, sdQuery, minQuery, maxQuery);

                long medMem = StatsUtils.medianLong(memoryListLocal);
                double bpe = (double) medMem / (double) size;
                CSVHandler.appendMemoryLine(csvDir.resolve("memory_usage.csv"), implName, size, medMem, bpe);

                System.out.println("Terminado " + implName + " size " + size + " — stats guardados.");
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
                        
