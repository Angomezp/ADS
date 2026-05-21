package lab1.src.datastructures;

public class HybridCRMQ implements rmqInterface {

    private final int[] arr;

    // =========================================
    // TOP LEVEL
    // =========================================

    private final int blockSize;

    private final int numBlocks;

    private final int[] blockMin;

    // sparse table over block minima
    private final int[][] topST;

    private final int[] topLog2;

    // =========================================
    // INTERNAL HYBRID A STRUCTURES
    // =========================================

    private final HybridAInsideBlock[] blocks;

    private final long memoryBytes;

    public HybridCRMQ(int[] arr) {

        this.arr = arr;

        int n = arr.length;

        // top block size = ceil(log2(n))
        blockSize =
            32 - Integer.numberOfLeadingZeros(n - 1);

        numBlocks =
            (n + blockSize - 1) / blockSize;

        blockMin = new int[numBlocks];

        blocks =
            new HybridAInsideBlock[numBlocks];

        // =====================================
        // BUILD INTERNAL BLOCK STRUCTURES
        // =====================================

        buildBlocks();

        // =====================================
        // TOP SPARSE TABLE
        // =====================================

        int K =
            32 - Integer.numberOfLeadingZeros(numBlocks);

        topST = new int[K][numBlocks];

        topLog2 = new int[numBlocks + 1];

        buildTopSparseTable();

        // =====================================
        // MEMORY ACCOUNTING
        // =====================================

        long mem = 0;

        mem += (long) numBlocks * Integer.BYTES;

        mem += (long) K * numBlocks
            * Integer.BYTES;

        mem += (long) (numBlocks + 1)
            * Integer.BYTES;

        for (HybridAInsideBlock block : blocks) {
            mem += block.memoryBytes;
        }

        memoryBytes = mem;
    }

    private void buildBlocks() {

        int n = arr.length;

        for (int b = 0; b < numBlocks; b++) {

            int start = b * blockSize;

            int end =
                Math.min(start + blockSize, n);

            blocks[b] =
                new HybridAInsideBlock(
                    arr,
                    start,
                    end
                );

            blockMin[b] =
                blocks[b].blockMinimumIndex;
        }
    }

    private void buildTopSparseTable() {

        topLog2[1] = 0;

        for (int i = 2; i <= numBlocks; i++) {
            topLog2[i] = topLog2[i / 2] + 1;
        }

        // base
        for (int i = 0; i < numBlocks; i++) {
            topST[0][i] = blockMin[i];
        }

        // build
        for (int k = 1; k < topST.length; k++) {

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

            return blocks[leftBlock]
                .query(i, j);
        }

        // =====================================
        // LEFT PARTIAL BLOCK
        // =====================================

        int leftEnd =
            Math.min(
                ((leftBlock + 1) * blockSize) - 1,
                arr.length - 1
            );

        int minIndex =
            blocks[leftBlock]
                .query(i, leftEnd);

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
                    fullRight - (1 << k) + 1
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
        // RIGHT PARTIAL BLOCK
        // =====================================

        int rightStart =
            rightBlock * blockSize;

        int candidate =
            blocks[rightBlock]
                .query(rightStart, j);

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

    // ====================================================
    // INTERNAL HYBRID A FOR EACH BLOCK
    // ====================================================

    private static class HybridAInsideBlock {

        private final int[] arr;

        private final int start;

        private final int end;

        private final int size;

        private final int subBlockSize;

        private final int numSubBlocks;

        private final int[] subBlockMin;

        private final int[][] st;

        private final int[] log2;

        private final int blockMinimumIndex;

        private final long memoryBytes;

        public HybridAInsideBlock(
            int[] arr,
            int start,
            int end
        ) {

            this.arr = arr;

            this.start = start;

            this.end = end;

            this.size = end - start;

            // recursive block size
            subBlockSize =
                Math.max(
                    1,
                    32 - Integer.numberOfLeadingZeros(
                        Math.max(size - 1, 1)
                    )
                );

            numSubBlocks =
                (size + subBlockSize - 1)
                / subBlockSize;

            subBlockMin =
                new int[numSubBlocks];

            buildSubBlockMinima();

            blockMinimumIndex =
                subBlockMin[0];

            int K =
                32 - Integer.numberOfLeadingZeros(
                    Math.max(numSubBlocks, 1)
                );

            st = new int[K][numSubBlocks];

            log2 = new int[numSubBlocks + 1];

            buildSparseTable();

            long mem = 0;

            mem += (long) numSubBlocks
                * Integer.BYTES;

            mem += (long) K * numSubBlocks
                * Integer.BYTES;

            mem += (long) (numSubBlocks + 1)
                * Integer.BYTES;

            memoryBytes = mem;
        }

        private void buildSubBlockMinima() {

            int globalMin = start;

            for (int b = 0; b < numSubBlocks; b++) {

                int s =
                    start + b * subBlockSize;

                int e =
                    Math.min(
                        s + subBlockSize - 1,
                        end - 1
                    );

                int minIndex = s;

                for (int i = s + 1; i <= e; i++) {

                    if (arr[i] < arr[minIndex]) {
                        minIndex = i;
                    }
                }

                subBlockMin[b] = minIndex;

                if (arr[minIndex]
                    < arr[globalMin]) {

                    globalMin = minIndex;
                }
            }

            subBlockMin[0] = globalMin;
        }

        private void buildSparseTable() {

            if (numSubBlocks == 0) {
                return;
            }

            log2[1] = 0;

            for (int i = 2;
                 i <= numSubBlocks;
                 i++) {

                log2[i] =
                    log2[i / 2] + 1;
            }

            for (int i = 0;
                 i < numSubBlocks;
                 i++) {

                st[0][i] =
                    subBlockMin[i];
            }

            for (int k = 1;
                 k < st.length;
                 k++) {

                int len = 1 << k;

                int half = len >> 1;

                for (int i = 0;
                     i + len <= numSubBlocks;
                     i++) {

                    int leftIndex =
                        st[k - 1][i];

                    int rightIndex =
                        st[k - 1][i + half];

                    if (arr[leftIndex]
                        <= arr[rightIndex]) {

                        st[k][i] =
                            leftIndex;

                    } else {

                        st[k][i] =
                            rightIndex;
                    }
                }
            }
        }

        public int query(int i, int j) {

            int minIndex = i;

            for (int k = i + 1; k <= j; k++) {

                if (arr[k]
                    < arr[minIndex]) {

                    minIndex = k;
                }
            }

            return minIndex;
        }
    }
}