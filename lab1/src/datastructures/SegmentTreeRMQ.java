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
        tree = new int[4 * n + 2];
        tree[0] = -1; // Used to indicate invalid index in query results as 1-based indexing is used for tree nodes 

        // memory accounting
        memoryBytes = (long) (4 * n + 2) * Integer.BYTES;

        build(1,0, n - 1);
    }

    
    //Preprocess the segment tree (Build)

    private void build(int node, int left, int right) {
        // leaf
        if (left == right) {
            tree[node] = left;
            return;
        }

        int mid = left + (right - left) / 2;

        int leftChild = node * 2;

        int rightChild = node * 2 + 1;

        build(leftChild, left,mid );

        build(rightChild, mid + 1, right );

        int leftIndex = tree[leftChild];

        int rightIndex = tree[rightChild];

        // leftmost minimum
        if (arr[leftIndex] <= arr[rightIndex]) {
            tree[node] = leftIndex;

        } else {
            tree[node] = rightIndex;
        }
    }

    // Query the segment tree for the minimum index in range [i, j]
    @Override
    public int RMQ(int i, int j) {
        return query(1,0,n - 1,i,j);
    }

    // returns index of minimum in range [left, right] in the segment tree node representing range [tl, tr]
    private int query(int node,int tl,int tr,int left,int right) {

        if (left > right) {
            return 0; // Invalid range, return an index that will not affect the minimum
        }

        if ( tl == left && tr == right) {

            return tree[node];
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

        if (arr[leftIndex] <= arr[rightIndex]) {
            return leftIndex;
        } else {
            return rightIndex;
        }
    }

    @Override
    public long getMemoryBytes() {
        return memoryBytes;
    }
}