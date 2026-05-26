package lab1.src.datastructures;

public class SegmentTreeRMQ implements rmqInterface {

    private final int[] arr;

    // tree[node] stores index of minimum
    private final int[] tree;

    private final int n;
    private long memoryBytes;

    public SegmentTreeRMQ(int[] arr) {

        this.arr = arr;

        this.n = arr.length;

        // safe size
        this.tree = new int[4 * this.n + 2];
    }

    @Override
    public void preprocess() {
        build(1, 0, this.n - 1);
    }
    // Build the segment tree. Nodes are stored in 1-based node indices.
    private void build(int node, int left, int right) {
        // leaf
        if (left == right) {
            this.tree[node] = left;
            return;
        }

        int mid = left + (right - left) / 2;

        int leftChild = node * 2;
        int rightChild = node * 2 + 1;

        build(leftChild, left, mid);
        build(rightChild, mid + 1, right);

        int leftIndex = this.tree[leftChild];
        int rightIndex = this.tree[rightChild];

        // choose leftmost minimum on ties
        if (this.arr[leftIndex] <= this.arr[rightIndex]) {
            this.tree[node] = leftIndex;
        } else {
            this.tree[node] = rightIndex;
        }
    }
    @Override
    public int RMQ(int i, int j) {
        return query(1, 0, this.n - 1, i, j);
    }
    // returns index of minimum in range [left, right] within node representing [tl, tr]
    private int query(int node, int tl, int tr, int left, int right) {
        if (left > right) {
            return -1; // invalid
        }

        if (tl == left && tr == right) {
            return this.tree[node];
        }

        int tm = tl + (tr - tl) / 2;

        int leftIndex = query(2 * node, tl, tm, left, Math.min(right, tm));
        int rightIndex = query(node * 2 + 1, tm + 1, tr, Math.max(left, tm + 1), right);

        if (leftIndex == -1) return rightIndex;
        if (rightIndex == -1) return leftIndex;

        return (this.arr[leftIndex] <= this.arr[rightIndex]) ? leftIndex : rightIndex;
    }

    @Override
    public void countMemoryBytes() {
        this.memoryBytes = (long) (4 * this.n + 2) * Integer.BYTES;
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }
}