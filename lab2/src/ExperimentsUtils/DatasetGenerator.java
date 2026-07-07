package lab2.src.ExperimentsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DatasetGenerator {

    private static final long SEED = 42;
    private static final Random random = new Random(SEED);

    /**
     * Genera n enteros distintos uniformemente en [1,100n].
     */
    public static int[] generateDataset(int n) {

        HashSet<Integer> values = new HashSet<>(n);

        while (values.size() < n) {
            values.add(random.nextInt(100 * n) + 1);
        }

        int[] dataset = new int[n];

        int i = 0;
        for (int value : values)
            dataset[i++] = value;

        return dataset;
    }

    /**
     * Genera búsquedas exitosas.
     */
    public static int[] generateExistingSearches(int[] dataset, int amount) {

        int[] searches = new int[amount];

        for (int i = 0; i < amount; i++) {
            searches[i] = dataset[random.nextInt(dataset.length)];
        }

        return searches;
    }

    /**
     * Genera búsquedas fallidas.
     */
    public static int[] generateMissingSearches(int[] dataset, int amount) {

        HashSet<Integer> existing = new HashSet<>();

        for (int value : dataset)
            existing.add(value);

        int max = dataset.length * 100;

        int[] searches = new int[amount];

        for (int i = 0; i < amount; i++) {

            int candidate;

            do {
                candidate = random.nextInt(max) + 1;
            } while (existing.contains(candidate));

            searches[i] = candidate;
        }

        return searches;
    }

    /**
     * Genera claves nuevas para insertar.
     */
    public static int[] generateInsertions(int[] dataset, int amount) {

        HashSet<Integer> used = new HashSet<>();

        for (int value : dataset)
            used.add(value);

        int[] insertions = new int[amount];

        int max = (dataset.length + amount) * 100;

        for (int i = 0; i < amount; i++) {

            int candidate;

            do {
                candidate = random.nextInt(max) + 1;
            } while (used.contains(candidate));

            used.add(candidate);

            insertions[i] = candidate;
        }

        return insertions;
    }

    /**
     * Genera consultas de rango.
     */
    public static List<RangeQuery> generateRangeQueries(
            int n,
            int expectedOutputSize,
            int amount) {

        List<RangeQuery> queries = new ArrayList<>(amount);

        int width = expectedOutputSize * 100;

        int max = 100 * n;

        for (int i = 0; i < amount; i++) {

            int a = random.nextInt(Math.max(1, max - width + 1)) + 1;

            int b = Math.min(max, a + width);

            queries.add(new RangeQuery(a, b));
        }

        return queries;
    }


    public static int[] generateMixedSearches(
            int[] dataset,
            int amount,
            double successfulRatio) {

        if (successfulRatio < 0.0 || successfulRatio > 1.0)
            throw new IllegalArgumentException("successfulRatio must be between 0 and 1.");

        int successful = (int) Math.round(amount * successfulRatio);
        int unsuccessful = amount - successful;

        HashSet<Integer> existing = new HashSet<>();

        for (int value : dataset)
            existing.add(value);

        int max = dataset.length * 100;

        List<Integer> searches = new ArrayList<>(amount);

        // Búsquedas exitosas
        for (int i = 0; i < successful; i++)
            searches.add(dataset[random.nextInt(dataset.length)]);

        // Búsquedas fallidas
        for (int i = 0; i < unsuccessful; i++) {

            int candidate;

            do {
                candidate = random.nextInt(max) + 1;
            } while (existing.contains(candidate));

            searches.add(candidate);
        }

        // Mezclar
        Collections.shuffle(searches, random);

        // Convertir a int[]
        int[] result = new int[amount];

        for (int i = 0; i < amount; i++)
            result[i] = searches.get(i);

        return result;
    }

    /**
     * Reinicia el generador para obtener exactamente los mismos datasets.
     */
    public static void resetSeed() {
        random.setSeed(SEED);
    }

}