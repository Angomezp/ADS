package lab1.src.datastructures;

public class HybridCRMQ implements rmqInterface {

    private final int[] arr;

    private final int blockSize;

    private final int numBlocks;

    // minimum index per block
    private final int[] blockMin;

    // Top-level sparse table for block minima
    private final int[][] topST;

    // log table for top-level sparse table
    private final int[] topLog2;

    // per-block RMQ instances (Hybrid A / ARMQ)
    private final HybridARMQ[] blocks;

    private long memoryBytes;

    public HybridCRMQ(int[] arr) {

        this.arr = arr;

        int n = this.arr.length;

        this.blockSize = 32 - Integer.numberOfLeadingZeros(n - 1);

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;

        this.blockMin = new int[this.numBlocks];

        this.blocks = new HybridARMQ[this.numBlocks];

        // TOP SPARSE TABLE
        int K = 32 - Integer.numberOfLeadingZeros(this.numBlocks);
        this.topST = new int[K][this.numBlocks];
        this.topLog2 = new int[this.numBlocks + 1];
        buildTopLogTable();

        this.memoryBytes = 0;
    }

    @Override
    public void preprocess() {
        // BUILD BLOCK MINIMA
        buildBlocks();

        // TOP SPARSE TABLE
        buildTopSparseTable();
    }

    private void buildBlocks() {

        int n = this.arr.length;

        for (int b = 0; b < this.numBlocks; b++) {

            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize, n);

            int[] blockArr = new int[end - start];
            System.arraycopy(this.arr, start, blockArr, 0, end - start);

            this.blocks[b] = new HybridARMQ(blockArr);

            // ensure internal block structures are preprocessed
            this.blocks[b].preprocess();

            this.blockMin[b] = this.blocks[b].RMQ(0, blockArr.length - 1) + start;
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

    private void buildTopLogTable() {
        this.topLog2[1] = 0;
        for (int i = 2; i <= this.numBlocks; i++) {
            this.topLog2[i] = this.topLog2[i / 2] + 1;
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
            return this.blocks[leftBlock].RMQ(localL, localR) + leftBlock * this.blockSize;
        }

        // LEFT PARTIAL BLOCK
        int leftLocalL = i % this.blockSize;
        int leftGlobalEnd = Math.min((leftBlock + 1) * this.blockSize, this.arr.length) - 1;
        int leftLocalR = leftGlobalEnd % this.blockSize;
        int minIndex = this.blocks[leftBlock].RMQ(leftLocalL, leftLocalR) + leftBlock * this.blockSize;

        // FULL BLOCKS IN THE MIDDLE
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

            // preserve leftmost minimum
            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        // RIGHT PARTIAL BLOCK
        int rightLocalL = 0;
        int rightLocalR = j % this.blockSize;
        int candidate = this.blocks[rightBlock].RMQ(rightLocalL, rightLocalR) + rightBlock * this.blockSize;

        // preserve leftmost minimum
        if (this.arr[candidate] < this.arr[minIndex]) {
            minIndex = candidate;
        }

        return minIndex;
    }

    @Override
    public void countMemoryBytes() {
        int K = 32 - Integer.numberOfLeadingZeros(this.numBlocks);
        long mem = 0;
        mem += (long) this.numBlocks * Integer.BYTES; // block minima
        mem += (long) K * this.numBlocks * Integer.BYTES; // top sparse table

        for (int b = 0; b < this.numBlocks; b++) {
            mem += this.blocks[b].getMemoryBytes(); // internal block memory
        }

        this.memoryBytes = mem;
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }

}