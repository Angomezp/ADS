package lab1;

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
        int numCorrectnessQueries = 1000000;
        int Trials = 30;

        //  Correctness Validation
        
        int[] testArray = RandomGenerator.generateRandomArray(testSize, bound);
        int[][] testQueries = RandomGenerator.generateRandomQueries(numCorrectnessQueries, testArray.length);

        // Example: validate two implementations on the same test set
        rmqInterface[] impls = new rmqInterface[] {
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

        for (rmqInterface impl : impls) {
            boolean ok = Validator.validateDataStructure(impl, testArray, testQueries);
            System.out.println(impl.getClass().getSimpleName() + " valid: " + ok);
        }
    }
    
}
