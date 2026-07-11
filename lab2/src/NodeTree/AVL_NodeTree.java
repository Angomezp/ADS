package lab2.src.NodeTree;

import java.util.ArrayList;
import java.util.List;
import lab2.src.Interfaces.AVL_Tree_Interface;
import lab2.src.Utils.Metrics;

public class AVL_NodeTree implements AVL_Tree_Interface {
    private Node root;
    private final Metrics metrics = new Metrics();

    // SEARCH
    @Override
    public boolean search(int key) {
        return search(this.root, key);
    }

    private boolean search(Node node, int key) {
        if (node == null)
            return false;

        metrics.incrementNodesVisited();

        if (key == node.getKey())
            return true;

        if (key < node.getKey())
            return search(node.getLeft(), key);

        return search(node.getRight(), key);
    }

    // INSERT
    @Override
    public void insert(int key) {
        this.root = insert(this.root, key);
    }

    private Node insert(Node node, int key) {
        
        if (node == null)
            return new Node(key);
        metrics.incrementNodesVisited();

        if (key < node.getKey())
            node.setLeft(insert(node.getLeft(), key));
        else if (key > node.getKey())
            node.setRight(insert(node.getRight(), key));
        else
            return node; // No duplicados

        return rebalance(node);
    }

    // DELETE
    @Override
    public void delete(int key) {
        this.root = delete(this.root, key);
    }

    private Node delete(Node node, int key) {

        if (node == null)
            return null;

        metrics.incrementNodesVisited();

        if (key < node.getKey()) {
            node.setLeft(delete(node.getLeft(), key));
        } else if (key > node.getKey()) {
            node.setRight(delete(node.getRight(), key));
        } else {

            // Caso 1 o 2 hijos

            if (node.getLeft() == null || node.getRight() == null) {

                Node temp;

                if (node.getLeft() != null)
                    temp = node.getLeft();
                else
                    temp = node.getRight();

                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }

            } else {

                Node successor = minValueNode(node.getRight());

                node.setKey(successor.getKey());

                node.setRight(delete(node.getRight(), successor.getKey()));
            }
        }

        if (node == null)
            return null;

        return rebalance(node);
    }

    // RANGE SEARCH
    @Override
    public List<Integer> rangeSearch(int a, int b) {

        List<Integer> result = new ArrayList<>();

        rangeSearch(this.root, a, b, result);

        return result;
    }

    private void rangeSearch(Node node, int a, int b, List<Integer> result) {
        if (node == null)
            return;

        metrics.incrementNodesVisited();
        if (node.getKey() > a)
            rangeSearch(node.getLeft(), a, b, result);

        if (node.getKey() >= a && node.getKey() <= b) {
            metrics.incrementReportedKeys();
            result.add(node.getKey());
        }

        if (node.getKey() < b)
            rangeSearch(node.getRight(), a, b, result);
    }

    // IN-ORDER TRAVERSAL
    @Override
    public List<Integer> inOrder() {

        List<Integer> result = new ArrayList<>();

        inOrder(this.root, result);

        return result;
    }

    private void inOrder(Node node, List<Integer> result) {

        if (node == null)
            return;

        inOrder(node.getLeft(), result);

        result.add(node.getKey());

        inOrder(node.getRight(), result);
    }

    // AVL AUXILIARY METHODS
    private int height(Node node) {
        if (node == null)
            return 0;
        return node.getHeight();
    }

    private void updateHeight(Node node) {
        node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
    }

    private int balance(Node node) {
        if (node == null)
            return 0;
        return height(node.getLeft()) - height(node.getRight());
    }

    private Node rebalance(Node node) {
        updateHeight(node);

        int balance = balance(node);

        // Left Left
        if (balance > 1 && balance(node.getLeft()) >= 0)
            return rotateRight(node);

        // Left Right
        if (balance > 1 && balance(node.getLeft()) < 0) {
            node.setLeft(rotateLeft(node.getLeft()));
            return rotateRight(node);
        }

        // Right Right
        if (balance < -1 && balance(node.getRight()) <= 0)
            return rotateLeft(node);

        // Right Left
        if (balance < -1 && balance(node.getRight()) > 0) {
            node.setRight(rotateRight(node.getRight()));
            return rotateLeft(node);
        }

        return node;
    }

    private Node rotateLeft(Node x) {
        this.metrics.incrementRotations();

        Node y = x.getRight();
        Node T2 = y.getLeft();

        y.setLeft(x);
        x.setRight(T2);

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    private Node rotateRight(Node y) {
        this.metrics.incrementRotations();

        Node x = y.getLeft();
        Node T2 = x.getRight();

        x.setRight(y);
        y.setLeft(T2);

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private Node minValueNode(Node node) {
        Node current = node;

        while (current.getLeft() != null)
            current = current.getLeft();

        return current;
    }

    @Override
    public Metrics getMetrics() {
        return this.metrics;
    }

    @Override
    public void resetMetrics() {
        this.metrics.reset();
    }

    @Override
    public String getName() {
        return "AVL_NodeTree";
    }
}