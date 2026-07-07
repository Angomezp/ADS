package lab2.src.LeafTree;

public class LeafNode extends Node {

    private int key;

    private LeafNode prev;
    private LeafNode next;

    public LeafNode(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public LeafNode getPrev() {
        return this.prev;
    }

    public void setPrev(LeafNode prev) {
        this.prev = prev;
    }

    public LeafNode getNext() {
        return this.next;
    }

    public void setNext(LeafNode next) {
        this.next = next;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public void linkNext(LeafNode node) {

        this.next = node;

        if (node != null)
            node.prev = this;
    }

    public void unlink() {

        if (this.prev != null)
            this.prev.next = this.next;

        if (this.next != null)
            this.next.prev = this.prev;

        this.prev = null;
        this.next = null;
    }
}
