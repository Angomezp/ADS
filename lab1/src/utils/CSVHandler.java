package lab1.src.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CSVHandler {

    public static void ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) Files.createDirectories(dir);
    }

    public static void writePreprocessCsv(Path path, Map<String, Map<Integer, List<Long>>> preprocessTimes) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
            w.write("Structure,n,Median(ms),Mean(ms),StdDev(ms),Min(ms),Max(ms)\n");
            for (String impl : preprocessTimes.keySet()) {
                Map<Integer, List<Long>> bySize = preprocessTimes.get(impl);
                for (Integer n : bySize.keySet()) {
                    List<Long> vals = bySize.get(n);
                    long med = StatsUtils.medianLong(vals);
                    double m = StatsUtils.meanLong(vals);
                    double sd = StatsUtils.stddevLong(vals);
                    long min = vals.stream().mapToLong(x->x).min().orElse(0L);
                    long max = vals.stream().mapToLong(x->x).max().orElse(0L);
                    w.write(String.format("%s,%d,%.3f,%.3f,%.3f,%d,%d\n", impl, n, (double)med, m, sd, min, max));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing preprocess CSV: " + e.getMessage());
        }
    }

    public static void writeQueryCsv(Path path, Map<String, Map<Integer, List<Double>>> perQueryMs) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
            w.write("Structure,n,Query Type,Median(ms),Mean(ms),StdDev(ms),Min(ms),Max(ms)\n");
            for (String impl : perQueryMs.keySet()) {
                Map<Integer, List<Double>> bySize = perQueryMs.get(impl);
                for (Integer n : bySize.keySet()) {
                    List<Double> vals = bySize.get(n);
                    double med = StatsUtils.medianDouble(vals);
                    double m = StatsUtils.meanDouble(vals);
                    double sd = StatsUtils.stddevDouble(vals);
                    double min = vals.stream().mapToDouble(x->x).min().orElse(0.0);
                    double max = vals.stream().mapToDouble(x->x).max().orElse(0.0);
                    w.write(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f,%.6f\n", impl, n, "any", med, m, sd, min, max));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing query CSV: " + e.getMessage());
        }
    }

    public static void writeMemoryCsv(Path path, Map<String, Map<Integer, List<Long>>> memoryBytes) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
            w.write("Structure,n,Bytes,BytesPerElement\n");
            for (String impl : memoryBytes.keySet()) {
                Map<Integer, List<Long>> bySize = memoryBytes.get(impl);
                for (Integer n : bySize.keySet()) {
                    List<Long> vals = bySize.get(n);
                    long med = StatsUtils.medianLong(vals);
                    double bpe = (double) med / (double) n;
                    w.write(String.format("%s,%d,%d,%.6f\n", impl, n, med, bpe));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing memory CSV: " + e.getMessage());
        }
    }
}
