package lab2.src.ExperimentsUtils;

import lab2.src.Interfaces.AVL_Tree_Interface;

public class TraversalExperiment {

    public static ExperimentResult execute(
            AVL_Tree_Interface tree,
            int n) {

        long start = System.nanoTime();

        tree.inOrder();

        long end = System.nanoTime();

        return new ExperimentResult(
                tree.getName(),
                n,
                end - start,
                0.0,
                0.0,
                0
        );
    }

}
