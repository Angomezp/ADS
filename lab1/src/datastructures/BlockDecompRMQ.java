package lab1.src.datastructures;

public class BlockDecompRMQ implements rmqInterface {

    private final int[] arr;

    // blockMin[b] = índice del mínimo del bloque b
    private final int[] blockMin;

    private final int blockSize;

    private final int numBlocks;

    private final long memoryBytes;

    public BlockDecompRMQ(int[] arr) {

        this.arr = arr;

        int n = this.arr.length;

        // b = ceil(sqrt(n))
        this.blockSize = (int) Math.ceil(Math.sqrt(n));

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;

        this.blockMin = new int[this.numBlocks];

        // memory accounting
        this.memoryBytes = (long) this.numBlocks * Integer.BYTES;

        preprocess();
    }

    private void preprocess() {

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

    @Override
    public int RMQ(int i, int j) {
        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }

        int leftBlock = i / this.blockSize;

        int rightBlock = j / this.blockSize;

        int minIndex = i;

        // Same block cases
        // case 1.1 : both i and j are in the same block

        if(i == j) {
            return i;
        }
        

        // case 1.2 : i and j are in the same block but they are not the limits of the block
        if (leftBlock == rightBlock) {

            for (int k = i + 1; k <= j; k++) {

                if (this.arr[k] < this.arr[minIndex]) {
                    minIndex = k;
                }
            }

            return minIndex;
        }

        // Different block cases
        // case 2 : i and j are in different blocks
        // left partial block
        int leftEnd = (leftBlock + 1) * this.blockSize - 1;

        for (int k = i; k <= leftEnd; k++) {

            if (this.arr[k] < this.arr[minIndex]) {
                minIndex = k;
            }
        }

        // full blocks between leftBlock and rightBlock
        for (int b = leftBlock + 1; b <= rightBlock - 1; b++) {

            int candidate = this.blockMin[b];

            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        // right partial block

        int rightStart = rightBlock * this.blockSize;

        for (int k = rightStart; k <= j; k++) {

            if (this.arr[k] < this.arr[minIndex]) {
                minIndex = k;
            }
        }

        return minIndex;
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }
}