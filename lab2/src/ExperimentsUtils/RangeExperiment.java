package lab2.src.ExperimentsUtils;

import java.util.List;
import lab2.src.Interfaces.AVL_Tree_Interface;
import lab2.src.Utils.Metrics;

public class RangeExperiment {

    public static ExperimentResult execute(
            AVL_Tree_Interface tree,
            int n,
            List<RangeQuery> queries) {

        long totalTime = 0;
        long totalNodesVisited = 0;
        long totalReportedKeys = 0;

        for (RangeQuery query : queries) {

            tree.resetMetrics();

            long start = System.nanoTime();

            tree.rangeSearch(query.getA(), query.getB());

            long end = System.nanoTime();

            Metrics metrics = tree.getMetrics();

            totalTime += end - start;
            totalNodesVisited += metrics.getNodesVisited();
            totalReportedKeys += metrics.getReportedKeys();
        }

        return new ExperimentResult(
                tree.getName(),
                n,
                totalTime,
                (double) totalNodesVisited / queries.size(),
                (double) totalReportedKeys / queries.size(),
                0
        );
    }
}
