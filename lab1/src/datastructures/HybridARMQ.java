package lab1.src.datastructures;

public class HybridARMQ implements rmqInterface {

    private final int[] arr;

    private final int blockSize;

    private final int numBlocks;

    // blockMin[b] = index of minimum of block b
    private final int[] blockMin;


    // sparseBlockTable[k][i] = minimum index among blocks
    // in interval [i, i + 2^k - 1]
    private final int[][] sparseBlockTable;

    private final int[] log2;

    private final long memoryBytes;

    public HybridARMQ(int[] arr) {

        this.arr = arr;

        int n = this.arr.length;

        // b = ceil(log2(n))
        this.blockSize = 32 - Integer.numberOfLeadingZeros(n - 1);

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;
        

        this.log2 = new int[this.numBlocks + 1];
        buildLogTable();

        // table for RMQ on each block minimum
        this.blockMin = new int[this.numBlocks];
        buildBlockMinima();

        
        // sparse table for block minima
        int K = 32 - Integer.numberOfLeadingZeros(this.numBlocks);

        this.sparseBlockTable = new int[K][this.numBlocks];

        buildSparseTable();

        // calculate memory usage: block minima + sparse table for block minima (not log table as it should be an operation)
        this.memoryBytes = (long) this.numBlocks * Integer.BYTES  // Block minima
            + (long) K * this.numBlocks * Integer.BYTES;
    }

    private void buildBlockMinima() {

        int n = this.arr.length;

        for (int b = 0; b < this.numBlocks; b++) {

            int start = b * this.blockSize;

            int end = Math.min(start + this.blockSize - 1, n - 1);

            int minIndex = start;

            for (int i = start + 1; i <= end; i++) {

                // leftmost minimum
                if (this.arr[i] < this.arr[minIndex]) {
                    minIndex = i;
                }
            }

            this.blockMin[b] = minIndex;
        }
    }

    private void buildSparseTable() {

        System.arraycopy(this.blockMin, 0, this.sparseBlockTable[0], 0, this.numBlocks);


        for (int k = 1; k < this.sparseBlockTable.length; k++) {

            int len = 1 << k;

            int half = len >> 1;

            for (int i = 0;
                 i + len <= this.numBlocks;
                 i++) {

                int leftIndex =
                    this.sparseBlockTable[k - 1][i];

                int rightIndex =
                    this.sparseBlockTable[k - 1][i + half];

                // leftmost minimum
                if (this.arr[leftIndex] <= this.arr[rightIndex]) {
                    this.sparseBlockTable[k][i] = leftIndex;
                } else {
                    this.sparseBlockTable[k][i] = rightIndex;
                }
            }
        }
    }

    @Override
    public int RMQ(int i, int j) {
        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }

        int leftBlock = i / this.blockSize;

        int rightBlock = j / this.blockSize;

        int minIndex = i;

        // case 1: i and j are in the same block

        if (i == j) {
            return i;
        }

        if (i == leftBlock * this.blockSize && j == (rightBlock + 1) * this.blockSize - 1) {
            return this.blockMin[leftBlock];
        }

        if (leftBlock == rightBlock) {

            for (int k = i + 1; k <= j; k++) {

                if (this.arr[k] < this.arr[minIndex]) {
                    minIndex = k;
                }
            }

            return minIndex;
        }

        // partial block on the left

        int leftEnd = (leftBlock + 1) * this.blockSize - 1;

        for (int k = i; k <= leftEnd; k++) {

            if (this.arr[k] < this.arr[minIndex]) {
                minIndex = k;
            }
        }

        // full blocks between leftBlock and rightBlock

        int fullLeft = leftBlock + 1;

        int fullRight = rightBlock - 1;

        if (fullLeft <= fullRight) {

            int len = fullRight - fullLeft + 1;

            int k = this.log2[len];

            int leftCandidate = this.sparseBlockTable[k][fullLeft];

            int rightCandidate = this.sparseBlockTable[k][fullRight - (1 << k) + 1];

            int candidate;

            // leftmost minimum
            if (this.arr[leftCandidate] <= this.arr[rightCandidate]) {
                
                candidate = leftCandidate;
            } else {

                candidate = rightCandidate;
            }

            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        //right partial block

        int rightStart = rightBlock * this.blockSize;

        for (int k = rightStart; k <= j; k++) {

            if (this.arr[k] < this.arr[minIndex]) {
                minIndex = k;
            }
        }

        return minIndex;
    }

    private void buildLogTable() {

        this.log2[1] = 0;

        for (int i = 2; i <= this.numBlocks; i++) {
            this.log2[i] = this.log2[i / 2] + 1;
        }
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }
}