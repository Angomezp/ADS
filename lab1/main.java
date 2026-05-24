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
import lab1.src.utils.Validator;


public class Main {
    public static void main(String[] args) {
        
        //  Configurations for experiments
        //  sizes: 1024, 4096, 16384, 65536, 262144, 1048576
        int testSize = 5_000;
        int[] sizes = {1<<10, 1<<12, 1<<14, 1<<16, 1<<18, 1<<20}; 
        int bound = 1_000_000_000; 
        long seed = 123456789L;
        int numCorrectnessQueries = 10000;
        int numTrials = 30;
        int numQueries;

        // Variables
        rmqInterface[] implementations;
        int[] testArray;
        int[][] testQueries;
        ExperimentDataStructure experiment;

        //  Correctness Validation

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

        for (String implName : implNames) {
            System.out.println("Running experiments for: " + implName);
            for (int size : sizes) {


                if (size <= 1<<14){
                    numQueries = 5_000_000;
                    
                }else{
                    numQueries = 1_000_000;
                    if (implName.equals("FullPreprocessingRMQ")){
                        break;
                    }
                }

                for (int trial = 0; trial < numTrials; trial++) {
                    testArray = RandomGenerator.generateRandomArray(size, bound, seed + trial);
                    testQueries = RandomGenerator.generateRandomQueries(numQueries, size, seed + trial);

                    rmqInterface impl = createInstance(implName, testArray);
                    experiment = new ExperimentDataStructure(testArray, testQueries, impl);
                    experiment.Experiment();

                    preprocessTimes.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(experiment.getPreprocessTimeMs());

                    memoryBytes.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(experiment.getMemoryBytes());

                    double perQuery = (double) experiment.getQueryTimeMs() / (double) numQueries;
                    perQueryMs.computeIfAbsent(implName, k -> new HashMap<>())
                            .computeIfAbsent(size, k -> new ArrayList<>()).add(perQuery);

                }
            }
        }

        Path csvDir = Paths.get("csv");
        try {
            CSVHandler.ensureDir(csvDir);
        } catch (IOException e) {
            System.err.println("Failed to create csv directory: " + e.getMessage());
        }

        CSVHandler.writePreprocessCsv(csvDir.resolve("preprocessing_time.csv"), preprocessTimes);
        CSVHandler.writeQueryCsv(csvDir.resolve("query_time_per_query.csv"), perQueryMs);
        CSVHandler.writeMemoryCsv(csvDir.resolve("memory_usage.csv"), memoryBytes);

        System.out.println("CSV files written to: " + csvDir.toAbsolutePath());
    }





    private static rmqInterface createInstance(String name, int[] arr) {
        switch (name) {
            case "NaiveRMQ" -> {
                return new NaiveRMQ(arr);
            }
            case "FullPreprocessingRMQ" -> {
                return new FullPreprocessingRMQ(arr);
            }
            case "BlockDecompRMQ" -> {
                return new BlockDecompRMQ(arr);
            }
            case "SparseTableRMQ" -> {
                return new SparseTableRMQ(arr);
            }
            case "HybridARMQ" -> {
                return new HybridARMQ(arr);
            }
            case "HybridBRMQ" -> {
                return new HybridBRMQ(arr);
            }
            case "HybridCRMQ" -> {
                return new HybridCRMQ(arr);
            }
            case "FischerHeunRMQ" -> {
                return new FischerHeunRMQ(arr);
            }
            case "SegmentTreeRMQ" -> {
                return new SegmentTreeRMQ(arr);
            }
            default -> throw new IllegalArgumentException("Unknown impl: " + name);
        }
    }
}
