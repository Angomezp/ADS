package lab2.src.NodeTree;

public class Node {
    private int key;
    private int height;
    private Node left;
    private Node right;

    public Node(int key) {
        this.key = key;
        this.height = 0;
        this.left = null;
        this.right = null;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Node getLeft() {
        return this.left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return this.right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

}
