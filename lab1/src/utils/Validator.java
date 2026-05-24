package lab1.src.utils;

import lab1.src.datastructures.rmqInterface;

public class Validator {

    private static int getMinIndex(int[] arr, int start, int end) {
        int minIndex = start;
        for (int i = start + 1; i <= end; i++) {
            if (arr[i] < arr[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static boolean validateDataStructure(rmqInterface rmq, int[] arr, int[][] queries) {
        for (int[] query : queries) {

            int expectedIndex = getMinIndex(arr, query[0], query[1]);
            int rmqIndex = rmq.RMQ(query[0], query[1]);

            if (expectedIndex != rmqIndex) {
                System.out.printf("Validation failed for query [%d, %d]. Expected index: %d, RMQ index: %d%n",
                        query[0], query[1], expectedIndex, rmqIndex);
                return false;
            }
        }
        return true;
    }

    public static boolean confirmQueries(int[] arr, int[] rmqResults, int[][] queries, rmqInterface rmq) {
        int i = 0;
        for (int[] query : queries) {
            int expectedIndex = getMinIndex(arr, query[0], query[1]);
            if (expectedIndex != rmqResults[i]) {
                System.out.printf("Validation failed for query [%d, %d]. Expected index: %d, RMQ index: %d%n",
                        query[0], query[1], expectedIndex, rmqResults[i]);
                return false;
            }
            i++;
        }
        return true;
    }

}
