package lab1.src.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class FischerHeunRMQ implements rmqInterface {

    private final int[] arr;

    // BLOCKS
    private final int blockSize;
    private final int numBlocks;
    private final int[] blockMin;      // minimum index per block
    private final int[] blockType;     // Cartesian type per block

    // TOP SPARSE TABLE
    private final int[][] topST;
    private final int[] topLog2;

    // CANONICAL BLOCK TABLES (type -> lookup table)
    private final Map<Integer, int[][]> canonicalTables;

    private final long memoryBytes;

    public FischerHeunRMQ(int[] arr) {
        this.arr = arr;

        int n = this.arr.length;

        // block size: approx ceil(log4(n))/2
        this.blockSize = Math.max(1, (32 - Integer.numberOfLeadingZeros(n-1)) / 4);

        this.numBlocks = (n + this.blockSize - 1) / this.blockSize;

        this.blockMin = new int[this.numBlocks];
        this.blockType = new int[this.numBlocks];
        this.canonicalTables = new HashMap<>();

        // build canonical blocks and types
        buildBlocks();

        // top sparse table
        int K = 32 - Integer.numberOfLeadingZeros(Math.max(this.numBlocks, 1));
        this.topST = new int[K][this.numBlocks];
        this.topLog2 = new int[this.numBlocks + 1];
        buildLogTable();
        buildTopSparseTable();

        // memory accounting
        long mem = 0;
        mem += (long) this.numBlocks * Integer.BYTES; // block minima
        mem += (long) this.numBlocks * Integer.BYTES; // block types
        mem += (long) K * this.numBlocks * Integer.BYTES; // top ST

        for (int[][] table : this.canonicalTables.values()) {
            for (int[] row : table) {
                mem += (long) row.length * Integer.BYTES;
            }
        }

        this.memoryBytes = mem;
    }

    private void buildBlocks() {
        int n = this.arr.length;

        for (int b = 0; b < this.numBlocks; b++) {
            int start = b * this.blockSize;
            int end = Math.min(start + this.blockSize, n);

            // block minimum
            int minIndex = start;
            for (int i = start + 1; i < end; i++) {
                if (this.arr[i] < this.arr[minIndex]) {
                    minIndex = i;
                }
            }
            this.blockMin[b] = minIndex;

            // cartesian type
            int type = computeCartesianType(start, end);
            this.blockType[b] = type;

            // canonical table
            if (!this.canonicalTables.containsKey(type)) {
                this.canonicalTables.put(type, buildCanonicalTable(start, end));
            }
        }
    }

    // compute cartesian tree type for block [start, end)
    private int computeCartesianType(int start, int end) {
        Stack<Integer> stack = new Stack<>();
        stack.push(this.arr[start]);
        int type = 1;

        for (int i = start + 1; i < end; i++) {
            int current = this.arr[i];
            while (!stack.isEmpty() && stack.peek() > current) {
                stack.pop();
                type <<= 1;
            }
            stack.push(current);
            type <<= 1;
            type |= 1;
        }
        while (!stack.isEmpty()) {
            stack.pop();
            type <<= 1;
        }
        return type;
    }

    // build RMQ table for canonical block [start, end)
    private int[][] buildCanonicalTable(int start, int end) {
        int size = end - start;
        int[][] table = new int[size][size];

        for (int i = 0; i < size; i++) {
            table[i][i] = i;
            int minIndex = i;
            for (int j = i + 1; j < size; j++) {
                if (this.arr[start + j] < this.arr[start + minIndex]) {
                    minIndex = j;
                }
                table[i][j] = minIndex;
            }
        }

        return table;
    }

    private void buildTopSparseTable() {
        if (this.numBlocks == 0) {
            return;
        }

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
        int type = this.blockType[block];
        int[][] table = this.canonicalTables.get(type);
        int localIndex = table[l][r];
        return block * this.blockSize + localIndex;
    }

    @Override
    public int RMQ(int i, int j) {
        int leftBlock = i / this.blockSize;
        int rightBlock = j / this.blockSize;

        // same block
        if (leftBlock == rightBlock) {
            return queryInsideBlock(leftBlock, i % this.blockSize, j % this.blockSize);
        }

        // left partial (convert global end to local index)
        int leftEndGlobal = Math.min(((leftBlock + 1) * this.blockSize) - 1, this.arr.length - 1);
        int leftEndLocal = leftEndGlobal - (leftBlock * this.blockSize);
        int minIndex = queryInsideBlock(leftBlock, i % this.blockSize, leftEndLocal);

        // full blocks
        int fullLeft = leftBlock + 1;
        int fullRight = rightBlock - 1;
        if (fullLeft <= fullRight) {
            int len = fullRight - fullLeft + 1;
            int k = this.topLog2[len];

            int leftCandidate = this.topST[k][fullLeft];
            int rightCandidate = this.topST[k][fullRight - (1 << k) + 1];

            int candidate = (this.arr[leftCandidate] <= this.arr[rightCandidate]) ? leftCandidate : rightCandidate;
            if (this.arr[candidate] < this.arr[minIndex]) {
                minIndex = candidate;
            }
        }

        // right partial
        int candidate = queryInsideBlock(rightBlock, 0, j % this.blockSize);
        if (this.arr[candidate] < this.arr[minIndex]) {
            minIndex = candidate;
        }

        return minIndex;
    }

    @Override
    public long getMemoryBytes() { 
        return this.memoryBytes; 
    }

    private void buildLogTable() {
        this.topLog2[1] = 0;
        for (int i = 2; i <= this.numBlocks; i++) {
            this.topLog2[i] = this.topLog2[i / 2] + 1;
        }
    }
}