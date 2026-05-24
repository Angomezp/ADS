package lab1.src.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsUtils {

    public static double meanLong(List<Long> data) {
        if (data.isEmpty()) return 0.0;
        double s = 0.0;
        for (Long v : data) s += v;
        return s / data.size();
    }

    public static double meanDouble(List<Double> data) {
        if (data.isEmpty()) return 0.0;
        double s = 0.0;
        for (Double v : data) s += v;
        return s / data.size();
    }

    public static double stddevLong(List<Long> data) {
        if (data.size() <= 1) return 0.0;
        double m = meanLong(data);
        double s = 0.0;
        for (Long v : data) s += (v - m) * (v - m);
        return Math.sqrt(s / (data.size() - 1));
    }

    public static double stddevDouble(List<Double> data) {
        if (data.size() <= 1) return 0.0;
        double m = meanDouble(data);
        double s = 0.0;
        for (Double v : data) s += (v - m) * (v - m);
        return Math.sqrt(s / (data.size() - 1));
    }

    public static long medianLong(List<Long> data) {
        if (data.isEmpty()) return 0L;
        List<Long> copy = new ArrayList<>(data);
        Collections.sort(copy);
        int m = copy.size() / 2;
        if (copy.size() % 2 == 1) return copy.get(m);
        return (copy.get(m-1) + copy.get(m)) / 2;
    }

    public static double medianDouble(List<Double> data) {
        if (data.isEmpty()) return 0.0;
        List<Double> copy = new ArrayList<>(data);
        Collections.sort(copy);
        int m = copy.size() / 2;
        if (copy.size() % 2 == 1) return copy.get(m);
        return (copy.get(m-1) + copy.get(m)) / 2.0;
    }
}
