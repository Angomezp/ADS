package lab2.src.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import lab2.src.ExperimentsUtils.ExperimentResult;

public class CsvWriter {

    public static void write(String fileName,
                             List<ExperimentResult> results) throws IOException {

        File file = new File(fileName);

        File parent = file.getParentFile();

        if (parent != null && !parent.exists())
            parent.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("tree,n,executionTimeNs,averageNodesVisited,averageReportedKeys,rotations");

            for (ExperimentResult result : results) {

                writer.printf(
                        Locale.US,
                        "%s,%d,%d,%.4f,%.4f,%d%n",
                        result.getTreeName(),
                        result.getN(),
                        result.getExecutionTimeNs(),
                        result.getAverageNodesVisited(),
                        result.getAverageReportedKeys(),
                        result.getTotalRotations()
                );
            }
        }
    }

}