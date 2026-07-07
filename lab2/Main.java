package lab2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

            int[] searches = DatasetGenerator.generateMixedSearches(
                    dataset,
                    SEARCHES,
                    0.5);

            int[] insertions = DatasetGenerator.generateInsertions(
                    dataset,
                    INSERTIONS);

            List<RangeQuery> range10 =
                    DatasetGenerator.generateRangeQueries(n, 10, RANGE_QUERIES);

            List<RangeQuery> range100 =
                    DatasetGenerator.generateRangeQueries(n, 100, RANGE_QUERIES);

            List<RangeQuery> range1000 =
                    DatasetGenerator.generateRangeQueries(n, 1000, RANGE_QUERIES);

            List<RangeQuery> range5000 =
                    DatasetGenerator.generateRangeQueries(n, 5000, RANGE_QUERIES);

            //-------------------------
            // Node AVL
            //-------------------------

            AVL_NodeTree nodeTree = new AVL_NodeTree();

            for (int key : dataset)
                nodeTree.insert(key);

            searchResults.add(
                    SearchExperiment.execute(nodeTree, n, searches));

            insertResults.add(
                    InsertExperiment.execute(nodeTree, n, insertions));

            range10Results.add(
                    RangeExperiment.execute(nodeTree, n, range10));

            range100Results.add(
                    RangeExperiment.execute(nodeTree, n, range100));

            range1000Results.add(
                    RangeExperiment.execute(nodeTree, n, range1000));

            range5000Results.add(
                    RangeExperiment.execute(nodeTree, n, range5000));

            traversalResults.add(
                    TraversalExperiment.execute(nodeTree, n));

            //-------------------------
            // Leaf AVL
            //-------------------------

            AVL_LeafTree leafTree = new AVL_LeafTree();

            for (int key : dataset)
                leafTree.insert(key);

            searchResults.add(
                    SearchExperiment.execute(leafTree, n, searches));

            insertResults.add(
                    InsertExperiment.execute(leafTree, n, insertions));

            range10Results.add(
                    RangeExperiment.execute(leafTree, n, range10));

            range100Results.add(
                    RangeExperiment.execute(leafTree, n, range100));

            range1000Results.add(
                    RangeExperiment.execute(leafTree, n, range1000));

            range5000Results.add(
                    RangeExperiment.execute(leafTree, n, range5000));

            traversalResults.add(
                    TraversalExperiment.execute(leafTree, n));
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