package lab1.src.datastructures;

public class HybridBRMQ implements rmqInterface {

    private final int[] arr;

    private final int blockSize;

    private final int numBlocks;

    // minimum index per block
    private final int[] blockMin;

    // Top-level sparse table for block minima
    private final int[][] topST;

    // Internal sparse tables for each block
    // blockST[b][k][i]
    private final int[][][] blockST;

    private final int[] Log2;

    private long memoryBytes;

    public HybridBRMQ(int[] arr) {
        this.arr = arr;

        int n = this.arr.length;

        this.blockSize = 32 - Integer.numberOfLeadingZeros(n - 1);

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;

        this.blockMin = new int[this.numBlocks];

        // INTERNAL BLOCK LOGS
        this.Log2 = BuildLog(Math.max(this.blockSize, this.numBlocks));

        // INTERNAL SPARSE TABLES
        int blockK = 32 - Integer.numberOfLeadingZeros(this.blockSize);
        this.blockST = new int[this.numBlocks][blockK][];
        

        // TOP SPARSE TABLE
        int topK = 32 - Integer.numberOfLeadingZeros(this.numBlocks);
        this.topST = new int[topK][this.numBlocks];

        this.memoryBytes = 0; 
    }

    @Override
    public void preprocess() {
        // BUILD BLOCK MINIMA
        buildBlockMinima();

        // INTERNAL SPARSE TABLES
        buildInternalSparseTables();

        // TOP SPARSE TABLE
        buildTopSparseTable();
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
        // base
        System.arraycopy(this.blockMin, 0, this.topST[0], 0, this.numBlocks);

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
        int k = this.Log2[len];

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

        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }

        if (i == j) {
            return i;
        }

        int leftBlock = i / this.blockSize;
        int rightBlock = j / this.blockSize;

        // SAME BLOCK
        if (leftBlock == rightBlock) {

            int localL = i % this.blockSize;
            int localR = j % this.blockSize;

            return queryInsideBlock(leftBlock, localL, localR);
        }

        // LEFT PARTIAL BLOCK
        int leftLocalL = i % this.blockSize;

        int leftGlobalEnd = Math.min((leftBlock + 1) * this.blockSize, this.arr.length) - 1;

        int leftLocalR = leftGlobalEnd % this.blockSize;

        int minIndex = queryInsideBlock(leftBlock, leftLocalL, leftLocalR);

        // FULL BLOCKS IN THE MIDDLE
        int fullLeft = leftBlock + 1;
        int fullRight = rightBlock - 1;

        if (fullLeft <= fullRight) {

            int len = fullRight - fullLeft + 1;

            int k = this.Log2[len];

            int leftCandidate = this.topST[k][fullLeft];

            int rightCandidate = this.topST[k][fullRight - (1 << k) + 1];

            int candidate;

            if (this.arr[leftCandidate] <= this.arr[rightCandidate]) {
                candidate = leftCandidate;
            } else {
                candidate = rightCandidate;
            }

            // preserve leftmost minimum
            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        // RIGHT PARTIAL BLOCK
        int rightLocalL = 0;
        int rightLocalR = j % this.blockSize;

        int candidate = queryInsideBlock(rightBlock, rightLocalL, rightLocalR);

        // preserve leftmost minimum
        if (this.arr[candidate] < this.arr[minIndex]) {
            minIndex = candidate;
        }

        return minIndex;
    }


    private int[] BuildLog(int limit) {
        int[] logs = new int[limit + 1]; 
        logs[1] = 0;
        for (int i = 2; i <= limit; i++) {
            logs[i] = logs[i / 2] + 1;
        }
        return logs;
    }

    @Override
    public void countMemoryBytes() {
        // MEMORY ACCOUNTING

        int topK = 32 - Integer.numberOfLeadingZeros(this.numBlocks);

        long mem = 0;
        mem += (long) this.numBlocks * Integer.BYTES; // block minima
        mem += (long) topK * this.numBlocks * Integer.BYTES; // top sparse table

        // internal sparse tables
        for (int b = 0; b < this.numBlocks; b++) {
            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize, this.arr.length);
            int size = end - start;
            int K = 32 - Integer.numberOfLeadingZeros(size);

            mem += (long) K * size * Integer.BYTES; // block b sparse table
        }

        this.memoryBytes = mem;
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }

}