package lab2.src.ExperimentsUtils;

import lab2.src.Interfaces.*;
import lab2.src.Utils.Metrics;

public class SearchExperiment {

    public static ExperimentResult execute(
            AVL_Tree_Interface tree,
            int n,
            int[] searches) {

        long totalTime = 0;
        long totalNodesVisited = 0;

        for (int key : searches) {

            tree.resetMetrics();

            long start = System.nanoTime();

            tree.search(key);

            long end = System.nanoTime();

            Metrics metrics = tree.getMetrics();

            totalTime += (end - start);
            totalNodesVisited += metrics.getNodesVisited();
        }

        return new ExperimentResult(
                tree.getName(),
                n,
                totalTime,
                (double) totalNodesVisited / searches.length,
                0.0,
                0
        );
    }

}
