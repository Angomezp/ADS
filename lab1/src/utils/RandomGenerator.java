package lab1.src.utils;

import java.util.Random;

public class RandomGenerator {
    
    public static int[] generateRandomArray(int size, int bound) {
        return generateRandomArray(size, bound, System.currentTimeMillis());
    }

    public static int[] generateRandomArray(int size, int bound, long seed) {
        int[] arr = new int[size];
        Random rnd = new Random(seed);
        for (int i = 0; i < size; i++) {
            arr[i] = rnd.nextInt(bound);
        }
        return arr;
    }

    public static int[][] generateRandomQueries(int numQueries, int arraySize, long seed) {
        int[][] queries = new int[numQueries][2];
        Random rnd = new Random(seed);
        for (int i = 0; i < numQueries; i++) {
            int start = rnd.nextInt(arraySize);
            int end = rnd.nextInt(arraySize);
            if (start > end) {
                // swap to ensure start <= end
                int temp = start;
                start = end;
                end = temp;
            }
            queries[i][0] = start;
            queries[i][1] = end;
        }
        return queries;
    }

    public static int[][] generateRandomQueries(int numQueries, int arraySize) {
        return generateRandomQueries(numQueries, arraySize, System.currentTimeMillis());
    }

}
