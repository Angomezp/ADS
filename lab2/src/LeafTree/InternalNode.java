package lab2.src.LeafTree;

public class InternalNode extends Node {

    private int separatorKey;

    private Node left;
    private Node right;

    public InternalNode(int separatorKey) {
        this.separatorKey = separatorKey;
    }

    public int getSeparatorKey() {
        return this.separatorKey;
    }

    public void setSeparatorKey(int separatorKey) {
        this.separatorKey = separatorKey;
    }

    public Node getLeft() {
        return this.left;
    }

    public void setLeft(Node left) {
        this.left = left;

        if (left != null)
            left.setParent(this);
    }

    public Node getRight() {
        return this.right;
    }

    public void setRight(Node right) {
        this.right = right;

        if (right != null)
            right.setParent(this);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

}