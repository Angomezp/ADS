package lab1.src.datastructures;

public class FullPreprocessingRMQ implements rmqInterface {
    private final int arr[];
    private final long memoryBytes;
    private final int[][] lookupTable;

    public FullPreprocessingRMQ(int arr[]) {
        this.arr = arr;
        this.lookupTable = new int[this.arr.length][this.arr.length];
        // The extra memory usage is for the lookup table, 
        // we need to allocate a full nxn matrix.
        this.memoryBytes = (long) this.arr.length * this.arr.length * Integer.BYTES;
        preprocess();
    }

    @Override
    public int RMQ(int i, int j) {
        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }
        return this.lookupTable[i][j];
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }

    private void preprocess() {
        int n = this.arr.length;
        for (int i = 0; i < n; i++) {
            this.lookupTable[i][i] = i;
        }
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                // We compare the minimum of the previous range [i, j-1] with the new element at index j.
                // If the new element is smaller, we update the minimum index to j, 
                // otherwise we keep the previous minimum index as is less than the new element.
                this.lookupTable[i][j] = (this.arr[this.lookupTable[i][j - 1]] <= this.arr[j]) ? this.lookupTable[i][j - 1] : j;
            }
        }  
    }

}