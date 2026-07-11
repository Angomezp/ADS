package lab2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lab2.src.ExperimentsUtils.DatasetGenerator;
import lab2.src.ExperimentsUtils.ExperimentResult;
import lab2.src.ExperimentsUtils.InsertExperiment;
import lab2.src.ExperimentsUtils.RangeExperiment;
import lab2.src.ExperimentsUtils.RangeQuery;
import lab2.src.ExperimentsUtils.SearchExperiment;
import lab2.src.ExperimentsUtils.TraversalExperiment;
import lab2.src.LeafTree.AVL_LeafTree;
import lab2.src.NodeTree.AVL_NodeTree;
import lab2.src.Utils.CsvWriter;

public class Main {

    private static final int[] SIZES = {
            10_000,
            50_000,
            100_000,
            500_000
    };

    private static final int SEARCHES = 10_000;
    private static final int INSERTIONS = 10_000;
    private static final int RANGE_QUERIES = 1_000;

    private static final int REPETITIONS = 50;

    private static ExperimentResult medianOf( Supplier<ExperimentResult> experiment) {

        // Warm-up
        for (int i = 0; i < 10; i++) {
                experiment.get();
        }

        List<Long> executionTimes = new ArrayList<>();
        List<Double> avgVisited = new ArrayList<>();
        List<Double> avgReported = new ArrayList<>();
        List<Long> rotations = new ArrayList<>();

        String treeName = "";
        int n = 0;

        for (int i = 0; i < REPETITIONS; i++) {

                ExperimentResult r = experiment.get();

                treeName = r.getTreeName();
                n = r.getN();

                executionTimes.add(r.getExecutionTimeNs());
                avgVisited.add(r.getAverageNodesVisited());
                avgReported.add(r.getAverageReportedKeys());
                rotations.add(r.getTotalRotations());
        }

        executionTimes.sort(Long::compare);
        avgVisited.sort(Double::compare);
        avgReported.sort(Double::compare);
        rotations.sort(Long::compare);

        int mid = REPETITIONS / 2;

        return new ExperimentResult(
                treeName,
                n,
                executionTimes.get(mid),
                avgVisited.get(mid),
                avgReported.get(mid),
                rotations.get(mid));
        }

    public static void main(String[] args) throws IOException {

        List<ExperimentResult> searchResults = new ArrayList<>();
        List<ExperimentResult> insertResults = new ArrayList<>();
        List<ExperimentResult> range10Results = new ArrayList<>();
        List<ExperimentResult> range100Results = new ArrayList<>();
        List<ExperimentResult> range1000Results = new ArrayList<>();
        List<ExperimentResult> range5000Results = new ArrayList<>();
        List<ExperimentResult> traversalResults = new ArrayList<>();

        for (int n : SIZES) {

            System.out.println("========================================");
            System.out.println("Running experiments for n = " + n);

            DatasetGenerator.resetSeed();

            int[] dataset = DatasetGenerator.generateDataset(n);

            int[] searches = DatasetGenerator.generateMixedSearches( dataset, SEARCHES, 0.5);

            int[] insertions = DatasetGenerator.generateInsertions( dataset, INSERTIONS);

            List<RangeQuery> range10 = DatasetGenerator.generateRangeQueries( n, 10, RANGE_QUERIES);

            List<RangeQuery> range100 = DatasetGenerator.generateRangeQueries( n, 100, RANGE_QUERIES);

            List<RangeQuery> range1000 = DatasetGenerator.generateRangeQueries( n, 1000, RANGE_QUERIES);

            List<RangeQuery> range5000 = DatasetGenerator.generateRangeQueries( n, 5000, RANGE_QUERIES);

            //-------------------------
            // Node AVL
            //-------------------------

            AVL_NodeTree nodeTree = new AVL_NodeTree();

            for (int key : dataset)
                nodeTree.insert(key);

            searchResults.add( medianOf(() -> 
                            SearchExperiment.execute(
                                    nodeTree,
                                    n,
                                    searches)));

            insertResults.add( medianOf(() -> {
                        AVL_NodeTree tree = new AVL_NodeTree();
                        for (int key : dataset)
                            tree.insert(key);
                        return InsertExperiment.execute(
                                tree,
                                n,
                                insertions);
                    }));

            range10Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    nodeTree,
                                    n,
                                    range10)));

            range100Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    nodeTree,
                                    n,
                                    range100)));

            range1000Results.add(
                    medianOf(() -> RangeExperiment.execute(
                                    nodeTree,
                                    n,
                                    range1000)));

            range5000Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    nodeTree,
                                    n,
                                    range5000)));

            traversalResults.add( medianOf(() ->
                            TraversalExperiment.execute(
                                    nodeTree,
                                    n)));

            //-------------------------
            // Leaf AVL
            //-------------------------

            AVL_LeafTree leafTree = new AVL_LeafTree();

            for (int key : dataset)
                leafTree.insert(key);

            searchResults.add( medianOf(() ->
                            SearchExperiment.execute(
                                    leafTree,
                                    n,
                                    searches)));

            insertResults.add(
                    medianOf(() -> {
                        AVL_LeafTree tree = new AVL_LeafTree();
                        for (int key : dataset)
                            tree.insert(key);
                        return InsertExperiment.execute(
                                tree,
                                n,
                                insertions);
                    }));

            range10Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    leafTree,
                                    n,
                                    range10)));

            range100Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    leafTree,
                                    n,
                                    range100)));

            range1000Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    leafTree,
                                    n,
                                    range1000)));

            range5000Results.add( medianOf(() ->
                            RangeExperiment.execute(
                                    leafTree,
                                    n,
                                    range5000)));

            traversalResults.add( medianOf(() ->
                            TraversalExperiment.execute(
                                    leafTree,
                                    n)));
        }

        CsvWriter.write("csv/search.csv", searchResults);
        CsvWriter.write("csv/insert.csv", insertResults);
        CsvWriter.write("csv/range10.csv", range10Results);
        CsvWriter.write("csv/range100.csv", range100Results);
        CsvWriter.write("csv/range1000.csv", range1000Results);
        CsvWriter.write("csv/range5000.csv", range5000Results);
        CsvWriter.write("csv/traversal.csv", traversalResults);

        System.out.println();
        System.out.println("All experiments completed successfully.");
    }
}