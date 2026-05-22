package lab1.src.datastructures;

public class SegmentTreeRMQ implements rmqInterface {

    private final int[] arr;

    // tree[node] stores index of minimum
    private final int[] tree;

    private final int n;

    private final long memoryBytes;

    public SegmentTreeRMQ(int[] arr) {

        this.arr = arr;

        this.n = arr.length;

        // safe size
        this.tree = new int[4 * this.n + 2];
        this.tree[0] = -1; // Used to indicate invalid index in query results as 1-based indexing is used for tree nodes 

        // memory accounting
        this.memoryBytes = (long) (4 * this.n + 2) * Integer.BYTES;

        build(1,0, this.n - 1);
    }

    
    //Preprocess the segment tree (Build)

    private void build(int node, int left, int right) {
        // leaf
        if (left == right) {
            this.tree[node] = left;
            return;
        }

        int mid = left + (right - left) / 2;

        int leftChild = node * 2;

        int rightChild = node * 2 + 1;

        build(leftChild, left,mid );

        build(rightChild, mid + 1, right );

        int leftIndex = this.tree[leftChild];

        int rightIndex = this.tree[rightChild];

        // leftmost minimum
        if (this.arr[leftIndex] <= this.arr[rightIndex]) {
            this.tree[node] = leftIndex;

        } else {
            this.tree[node] = rightIndex;
        }
    }

    // Query the segment tree for the minimum index in range [i, j]
    @Override
    public int RMQ(int i, int j) {
        return query(1,0,this.n - 1,i,j);
    }

    // returns index of minimum in range [left, right] in the segment tree node representing range [tl, tr]
    private int query(int node,int tl,int tr,int left,int right) {

        if (left > right) {
            return 0; // Invalid range, return an index that will not affect the minimum
        }

        if ( tl == left && tr == right) {

            return this.tree[node];
        }

        int tm  = tl + (tr - tl) / 2;

        int leftIndex = query(2 * node, tl, tm, left, Math.min(right,tm));

        int rightIndex = query(node * 2 + 1, tm + 1, tr, Math.max(left, tm + 1), right);

        if (leftIndex == 0) {
            return rightIndex;
        }

        if (rightIndex == 0) {
            return leftIndex;
        }

        if (this.arr[leftIndex] <= this.arr[rightIndex]) {
            return leftIndex;
        } else {
            return rightIndex;
        }
    }

    @Override
    public long getMemoryBytes() {
        return this.memoryBytes;
    }
}