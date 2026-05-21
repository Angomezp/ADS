package lab1.src.datastructures;

import java.util.HashMap;
import java.util.Map;

public class FischerHeunRMQ implements rmqInterface {

    private final int[] arr;

    // =========================================
    // BLOCKS
    // =========================================

    private final int blockSize;

    private final int numBlocks;

    // minimum index per block
    private final int[] blockMin;

    // Cartesian type per block
    private final int[] blockType;

    // =========================================
    // TOP SPARSE TABLE
    // =========================================

    private final int[][] topST;

    private final int[] topLog2;

    // =========================================
    // CANONICAL BLOCK TABLES
    // =========================================

    // type -> lookup table
    private final Map<Integer, int[][]> canonicalTables;

    private final long memoryBytes;

    public FischerHeunRMQ(int[] arr) {

        this.arr = arr;

        int n = arr.length;

        // b = ceil(log4(n))/2 approximation
        // practical version
        blockSize =
            Math.max(
                1,
                (32
                    - Integer.numberOfLeadingZeros(
                        Math.max(n - 1, 1)))
                    / 2
            );

        numBlocks =
            (n + blockSize - 1) / blockSize;

        blockMin = new int[numBlocks];

        blockType = new int[numBlocks];

        canonicalTables =
            new HashMap<>();

        // =====================================
        // BUILD BLOCKS
        // =====================================

        buildBlocks();

        // =====================================
        // TOP SPARSE TABLE
        // =====================================

        int K =
            32 - Integer.numberOfLeadingZeros(
                Math.max(numBlocks, 1)
            );

        topST = new int[K][numBlocks];

        topLog2 = new int[numBlocks + 1];

        buildTopSparseTable();

        // =====================================
        // MEMORY ACCOUNTING
        // =====================================

        long mem = 0;

        mem +=
            (long) numBlocks * Integer.BYTES;

        mem +=
            (long) numBlocks * Integer.BYTES;

        mem +=
            (long) K * numBlocks
            * Integer.BYTES;

        mem +=
            (long) (numBlocks + 1)
            * Integer.BYTES;

        for (int[][] table
            : canonicalTables.values()) {

            for (int[] row : table) {

                mem +=
                    (long) row.length
                    * Integer.BYTES;
            }
        }

        memoryBytes = mem;
    }

    private void buildBlocks() {

        int n = arr.length;

        for (int b = 0; b < numBlocks; b++) {

            int start = b * blockSize;

            int end =
                Math.min(
                    start + blockSize,
                    n
                );

            // =================================
            // BLOCK MINIMUM
            // =================================

            int minIndex = start;

            for (int i = start + 1;
                 i < end;
                 i++) {

                if (arr[i]
                    < arr[minIndex]) {

                    minIndex = i;
                }
            }

            blockMin[b] = minIndex;

            // =================================
            // CARTESIAN TYPE
            // =================================

            int type =
                computeCartesianType(
                    start,
                    end
                );

            blockType[b] = type;

            // =================================
            // CANONICAL TABLE
            // =================================

            if (!canonicalTables.containsKey(type)) {

                canonicalTables.put(
                    type,
                    buildCanonicalTable(
                        start,
                        end
                    )
                );
            }
        }
    }

    // ====================================================
    // CARTESIAN TREE NUMBER
    // ====================================================

    private int computeCartesianType(
        int start,
        int end
    ) {

        int type = 0;

        for (int i = start + 1;
             i < end;
             i++) {

            type <<= 1;

            if (arr[i]
                > arr[i - 1]) {

                type |= 1;
            }
        }

        return type;
    }

    // ====================================================
    // BUILD ALL RMQs INSIDE A CANONICAL BLOCK
    // ====================================================

    private int[][] buildCanonicalTable(
        int start,
        int end
    ) {

        int size = end - start;

        int[][] table =
            new int[size][size];

        for (int i = 0; i < size; i++) {

            table[i][i] = i;

            int minIndex = i;

            for (int j = i + 1;
                 j < size;
                 j++) {

                if (arr[start + j]
                    < arr[start + minIndex]) {

                    minIndex = j;
                }

                table[i][j] = minIndex;
            }
        }

        return table;
    }

    // ====================================================
    // TOP SPARSE TABLE
    // ====================================================

    private void buildTopSparseTable() {

        if (numBlocks == 0) {
            return;
        }

        topLog2[1] = 0;

        for (int i = 2;
             i <= numBlocks;
             i++) {

            topLog2[i] =
                topLog2[i / 2] + 1;
        }

        // base
        for (int i = 0;
             i < numBlocks;
             i++) {

            topST[0][i] =
                blockMin[i];
        }

        // build
        for (int k = 1;
             k < topST.length;
             k++) {

            int len = 1 << k;

            int half = len >> 1;

            for (int i = 0;
                 i + len <= numBlocks;
                 i++) {

                int leftIndex =
                    topST[k - 1][i];

                int rightIndex =
                    topST[k - 1][i + half];

                if (arr[leftIndex]
                    <= arr[rightIndex]) {

                    topST[k][i] =
                        leftIndex;

                } else {

                    topST[k][i] =
                        rightIndex;
                }
            }
        }
    }

    // ====================================================
    // INSIDE BLOCK QUERY
    // ====================================================

    private int queryInsideBlock(
        int block,
        int l,
        int r
    ) {

        int type = blockType[block];

        int[][] table =
            canonicalTables.get(type);

        int localIndex =
            table[l][r];

        return
            block * blockSize
            + localIndex;
    }

    @Override
    public int RMQ(int i, int j) {

        int leftBlock =
            i / blockSize;

        int rightBlock =
            j / blockSize;

        // =====================================
        // SAME BLOCK
        // =====================================

        if (leftBlock == rightBlock) {

            return queryInsideBlock(
                leftBlock,
                i % blockSize,
                j % blockSize
            );
        }

        // =====================================
        // LEFT PARTIAL
        // =====================================

        int leftEnd =
            Math.min(
                blockSize - 1,
                arr.length - 1
            );

        int minIndex =
            queryInsideBlock(
                leftBlock,
                i % blockSize,
                leftEnd
            );

        // =====================================
        // FULL BLOCKS
        // =====================================

        int fullLeft =
            leftBlock + 1;

        int fullRight =
            rightBlock - 1;

        if (fullLeft <= fullRight) {

            int len =
                fullRight - fullLeft + 1;

            int k =
                topLog2[len];

            int leftCandidate =
                topST[k][fullLeft];

            int rightCandidate =
                topST[k][
                    fullRight
                    - (1 << k)
                    + 1
                ];

            int candidate;

            if (arr[leftCandidate]
                <= arr[rightCandidate]) {

                candidate = leftCandidate;

            } else {

                candidate = rightCandidate;
            }

            if (arr[candidate]
                < arr[minIndex]) {

                minIndex = candidate;
            }
        }

        // =====================================
        // RIGHT PARTIAL
        // =====================================

        int candidate =
            queryInsideBlock(
                rightBlock,
                0,
                j % blockSize
            );

        if (arr[candidate]
            < arr[minIndex]) {

            minIndex = candidate;
        }

        return minIndex;
    }

    @Override
    public long getMemoryBytes() {
        return memoryBytes;
    }
}