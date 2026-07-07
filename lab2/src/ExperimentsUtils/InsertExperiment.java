package lab2.src.ExperimentsUtils;

import lab2.src.Interfaces.AVL_Tree_Interface;
import lab2.src.Utils.Metrics;

public class InsertExperiment {

    public static ExperimentResult execute(
            AVL_Tree_Interface tree,
            int n,
            int[] insertions) {

        long totalTime = 0;
        long totalRotations = 0;

        for (int key : insertions) {

            tree.resetMetrics();

            long start = System.nanoTime();

            tree.insert(key);

            long end = System.nanoTime();

            Metrics metrics = tree.getMetrics();

            totalTime += end - start;
            totalRotations += metrics.getRotations();
        }

        return new ExperimentResult(
                tree.getName(),
                n,
                totalTime,
                0.0,
                0.0,
                totalRotations
        );
    }
}