package lab1.src.datastructures;

public class NaiveRMQ implements rmqInterface {
    private final int arr[];
    private long memory_bytes;

    public NaiveRMQ(int arr[]) {
        this.arr = arr;
        this.memory_bytes = 0;
    }

    @Override
    public int RMQ(int i, int j) {
        if (i > j || i < 0 || j >= this.arr.length) {
            return -1;
        }

        int min = i;
        for (int k = i; k <= j; k++) {
            if (this.arr[k] < this.arr[min]) {
                min = k;
            }
        }
        return min;
    }

    @Override
    public void preprocess() {
        // No preprocessing needed for the naive approach
    }

    @Override
    public void countMemoryBytes() {
        // No memory ussage beyond the input array for the naive approach
        this.memory_bytes = 0;
    }
    @Override
    public long getMemoryBytes() {
        return this.memory_bytes;
    }
}