package lab1.src.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
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
                    w.write(String.format(Locale.US, "%s,%d,%.7f,%.7f,%.7f,%d,%d\n", impl, n, (double)med, m, sd, min, max));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing preprocess CSV: " + e.getMessage());
        }
    }

    public static void writeQueryCsv(Path path, Map<String, Map<Integer, List<Double>>> perBatchMs, Map<String, Map<Integer, List<Double>>> throughputs) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
            // Report throughput (operations per second) as a column, compute statistics over total batch time (ms)
            w.write("Structure,n,Throughput(ops/s),Median(ms),Mean(ms),StdDev(ms),Min(ms),Max(ms)\n");
            for (String impl : perBatchMs.keySet()) {
                Map<Integer, List<Double>> bySize = perBatchMs.get(impl);
                for (Integer n : bySize.keySet()) {
                    List<Double> vals = bySize.get(n);
                    double med = StatsUtils.medianDouble(vals);
                    double m = StatsUtils.meanDouble(vals);
                    double sd = StatsUtils.stddevDouble(vals);
                    double min = vals.stream().mapToDouble(x -> x).min().orElse(0.0);
                    double max = vals.stream().mapToDouble(x -> x).max().orElse(0.0);

                    double throughputMedian = 0.0;
                    if (throughputs != null && throughputs.containsKey(impl) && throughputs.get(impl).containsKey(n)) {
                        List<Double> thr = throughputs.get(impl).get(n);
                        throughputMedian = StatsUtils.medianDouble(thr);
                    }

                    w.write(String.format(Locale.US, "%s,%d,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f\n", impl, n, throughputMedian, med, m, sd, min, max));
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
                    w.write(String.format(Locale.US, "%s,%d,%d,%.7f\n", impl, n, med, bpe));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing memory CSV: " + e.getMessage());
        }
    }

    private static void ensureHeader(Path path, String header) throws IOException {
        if (!Files.exists(path)) {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
                w.write(header);
            }
        }
    }

    public static void appendPreprocessLine(Path path, String impl, int n, long med, double mean, double sd, long min, long max) {
        String header = "Structure,n,Median(ms),Mean(ms),StdDev(ms),Min(ms),Max(ms)\n";
        try {
            ensureHeader(path, header);
            try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile(), true))) {
                w.write(String.format(Locale.US, "%s,%d,%.7f,%.7f,%.7f,%d,%d\n", impl, n, (double)med, mean, sd, min, max));
            }
        } catch (IOException e) {
            System.err.println("Error appending preprocess CSV: " + e.getMessage());
        }
    }

    public static void appendQueryLine(Path path, String impl, int n, double throughputMedian, double medMs, double meanMs, double sdMs, double minMs, double maxMs) {
        String header = "Structure,n,Throughput(ops/s),Median(ms),Mean(ms),StdDev(ms),Min(ms),Max(ms)\n";
        try {
            ensureHeader(path, header);
            try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile(), true))) {
                w.write(String.format(Locale.US, "%s,%d,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f\n", impl, n, throughputMedian, medMs, meanMs, sdMs, minMs, maxMs));
            }
        } catch (IOException e) {
            System.err.println("Error appending query CSV: " + e.getMessage());
        }
    }

    public static void appendMemoryLine(Path path, String impl, int n, long med, double bpe) {
        String header = "Structure,n,Bytes,BytesPerElement\n";
        try {
            ensureHeader(path, header);
            try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile(), true))) {
                w.write(String.format(Locale.US, "%s,%d,%d,%.7f\n", impl, n, med, bpe));
            }
        } catch (IOException e) {
            System.err.println("Error appending memory CSV: " + e.getMessage());
        }
    }
}
