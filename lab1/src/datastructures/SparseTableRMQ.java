package lab1.src.datastructures;

public class SparseTableRMQ implements rmqInterface {

    private final int[] arr;

    // sparseTable[k][i] = índice del mínimo en el intervalo
    // [i, i + 2^k - 1]
    private final int[][] sparseTable;

    // log2[x] = floor(log2(x))
    private final int[] log2;

    private final long memoryBytes;

    public SparseTableRMQ(int[] arr) {
        

        this.arr = arr;

        int n = this.arr.length;

        int K = 32 - Integer.numberOfLeadingZeros(Math.max(n, 1));

        this.sparseTable = new int[K][n];
        this.log2 = new int[n + 1];

        // sparse table
        this.memoryBytes = (long) K * n * Integer.BYTES;

        // We are not counting the memory usage of the log table as it should be an operation
        // but java does not have explicit log2 function and precomputing it 
        // is more efficient than computing it on the fly
        BuildLogTable();

        Preprocess();
    }

    private void Preprocess() {

        int n = this.arr.length;

        // initialize level 0 with indices (sparseTable stores indices of minima)
        for (int i = 0; i < n; i++) {
            this.sparseTable[0][i] = i;
        }

        // build sparse table
        for (int k = 1; k < this.sparseTable.length; k++) {

            int len = 1 << k;
            int half = len >> 1;

            for (int i = 0; i + len <= n; i++) {

                int leftIndex = this.sparseTable[k - 1][i];
                int rightIndex = this.sparseTable[k - 1][i + half];

                // leftmost minimum
                if (this.arr[leftIndex] <= this.arr[rightIndex]) {
                    this.sparseTable[k][i] = leftIndex;
                } else {
                    this.sparseTable[k][i] = rightIndex;
                }
            }
        }
    }


    @Override
    public int RMQ(int i, int j) {

        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }

        int length = j - i + 1;

        int k = this.log2[length];

        int leftIndex = this.sparseTable[k][i];
        int rightIndex = this.sparseTable[k][j - (1 << k) + 1];

        // leftmost minimum
        if (this.arr[leftIndex] <= this.arr[rightIndex]) {
            return leftIndex;
        } 
        
        return rightIndex;
        
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }

    private void BuildLogTable() {
        this.log2[1] = 0;
        for (int i = 2; i < this.log2.length; i++) {
            this.log2[i] = this.log2[i / 2] + 1;
        }
    }
}
