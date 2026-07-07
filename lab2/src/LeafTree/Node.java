package lab2.src.LeafTree;

public abstract class Node {

    protected int height;
    private InternalNode parent;

    public Node() {
        this.height = 0;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public abstract boolean isLeaf();

    public InternalNode getParent() {
        return this.parent;
    }

    public void setParent(InternalNode parent) {
        this.parent = parent;
    }
}
