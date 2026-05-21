package lab1.src.datastructures;

public class HybridBRMQ implements rmqInterface {

    private final int[] arr;

    private final int blockSize;

    private final int numBlocks;

    // minimum index per block
    private final int[] blockMin;

    // Top-level sparse table for block minima
    private final int[][] topST;

    private final int[] topLog2;

    // Internal sparse tables for each block
    // blockST[b][k][i]
    private final int[][][] blockST;

    private final int[] blockLog2;

    private final long memoryBytes;

    public HybridBRMQ(int[] arr) {
        this.arr = arr;

        int n = this.arr.length;

        this.blockSize = 32 - Integer.numberOfLeadingZeros(n - 1);

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;

        this.blockMin = new int[this.numBlocks];

        // INTERNAL BLOCK LOGS
        this.blockLog2 = new int[this.blockSize + 1];
        this.blockLog2[1] = 0;
        for (int i = 2; i <= this.blockSize; i++) {
            this.blockLog2[i] = this.blockLog2[i / 2] + 1;
        }

        // BUILD BLOCK MINIMA
        buildBlockMinima();

        // INTERNAL SPARSE TABLES
        int blockK = 32 - Integer.numberOfLeadingZeros(this.blockSize);
        this.blockST = new int[this.numBlocks][blockK][];
        buildInternalSparseTables();

        // TOP SPARSE TABLE
        int topK = 32 - Integer.numberOfLeadingZeros(this.numBlocks);
        this.topST = new int[topK][this.numBlocks];
        this.topLog2 = new int[this.numBlocks + 1];
        buildTopSparseTable();

        // MEMORY ACCOUNTING
        long mem = 0;
        mem += (long) this.numBlocks * Integer.BYTES;
        mem += (long) (this.blockSize + 1) * Integer.BYTES;
        mem += (long) topK * this.numBlocks * Integer.BYTES;
        mem += (long) (this.numBlocks + 1) * Integer.BYTES;

        // internal sparse tables
        for (int b = 0; b < this.numBlocks; b++) {
            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize, n);
            int size = end - start;
            int K = 32 - Integer.numberOfLeadingZeros(size);

            for (int k = 0; k < K; k++) {
                int len = size - (1 << k) + 1;
                mem += (long) len * Integer.BYTES;
            }
        }

        this.memoryBytes = mem;
    }

    private void buildBlockMinima() {
        int n = this.arr.length;

        for (int b = 0; b < this.numBlocks; b++) {
            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize - 1, n - 1);

            int minIndex = start;
            for (int i = start + 1; i <= end; i++) {
                if (this.arr[i] < this.arr[minIndex]) {
                    minIndex = i;
                }
            }

            this.blockMin[b] = minIndex;
        }
    }

    private void buildInternalSparseTables() {
        int n = this.arr.length;

        for (int b = 0; b < this.numBlocks; b++) {
            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize, n);
            int size = end - start;
            int K = 32 - Integer.numberOfLeadingZeros(size);

            // allocate levels
            for (int k = 0; k < K; k++) {
                int len = size - (1 << k) + 1;
                this.blockST[b][k] = new int[len];
            }

            // base level
            for (int i = 0; i < size; i++) {
                this.blockST[b][0][i] = start + i;
            }

            // build
            for (int k = 1; k < K; k++) {
                int len = 1 << k;
                int half = len >> 1;

                for (int i = 0; i + len <= size; i++) {
                    int leftIndex = this.blockST[b][k - 1][i];
                    int rightIndex = this.blockST[b][k - 1][i + half];

                    // leftmost minimum
                    if (this.arr[leftIndex] <= this.arr[rightIndex]) {
                        this.blockST[b][k][i] = leftIndex;
                    } else {
                        this.blockST[b][k][i] = rightIndex;
                    }
                }
            }
        }
    }

    private void buildTopSparseTable() {
        this.topLog2[1] = 0;
        for (int i = 2; i <= this.numBlocks; i++) {
            this.topLog2[i] = this.topLog2[i / 2] + 1;
        }

        // base
        for (int i = 0; i < this.numBlocks; i++) {
            this.topST[0][i] = this.blockMin[i];
        }

        // build
        for (int k = 1; k < this.topST.length; k++) {
            int len = 1 << k;
            int half = len >> 1;

            for (int i = 0; i + len <= this.numBlocks; i++) {
                int leftIndex = this.topST[k - 1][i];
                int rightIndex = this.topST[k - 1][i + half];

                if (this.arr[leftIndex] <= this.arr[rightIndex]) {
                    this.topST[k][i] = leftIndex;
                } else {
                    this.topST[k][i] = rightIndex;
                }
            }
        }
    }

    private int queryInsideBlock(int block, int l, int r) {
        int len = r - l + 1;
        int k = this.blockLog2[len];

        int leftIndex = this.blockST[block][k][l];
        int rightIndex = this.blockST[block][k][r - (1 << k) + 1];

        if (this.arr[leftIndex] <= this.arr[rightIndex]) {
            return leftIndex;
        } else {
            return rightIndex;
        }
    }

    @Override
    public int RMQ(int i, int j) {
        int leftBlock = i / this.blockSize;
        int rightBlock = j / this.blockSize;

        // SAME BLOCK
        if (leftBlock == rightBlock) {
            int localL = i % this.blockSize;
            int localR = j % this.blockSize;
            return queryInsideBlock(leftBlock, localL, localR);
        }

        // LEFT PARTIAL
        int leftLocalL = i % this.blockSize;
        int leftLocalR = Math.min(this.blockSize - 1, this.arr.length - 1);

        int minIndex = queryInsideBlock(leftBlock, leftLocalL, leftLocalR);

        // FULL BLOCKS
        int fullLeft = leftBlock + 1;
        int fullRight = rightBlock - 1;

        if (fullLeft <= fullRight) {
            int len = fullRight - fullLeft + 1;
            int k = this.topLog2[len];

            int leftCandidate = this.topST[k][fullLeft];
            int rightCandidate = this.topST[k][fullRight - (1 << k) + 1];

            int candidate;
            if (this.arr[leftCandidate] <= this.arr[rightCandidate]) {
                candidate = leftCandidate;
            } else {
                candidate = rightCandidate;
            }

            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        // RIGHT PARTIAL
        int rightLocalL = 0;
        int rightLocalR = j % this.blockSize;

        int candidate = queryInsideBlock(rightBlock, rightLocalL, rightLocalR);
        if (this.arr[candidate] < this.arr[minIndex]) {
            minIndex = candidate;
        }

        return minIndex;
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }

}