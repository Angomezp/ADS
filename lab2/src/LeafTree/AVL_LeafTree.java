package lab2.src.LeafTree;

import java.util.ArrayList;
import java.util.List;
import lab2.src.Utils.Metrics;
import lab2.src.Interfaces.AVL_Tree_Interface;

public class AVL_LeafTree implements AVL_Tree_Interface {

    private Node root;
    private final Metrics metrics = new Metrics();

    public AVL_LeafTree() {
        this.root = null;
    }
    // SEARCH
    @Override
    public boolean search(int key) {

        if (this.root == null)
            return false;

        Node current = this.root;

        while (!current.isLeaf()) {
            metrics.incrementNodesVisited();

            InternalNode internal = (InternalNode) current;

            if (key < internal.getSeparatorKey())
                current = internal.getLeft();
            else
                current = internal.getRight();
        }

        LeafNode leaf = (LeafNode) current;
        metrics.incrementNodesVisited();

        return leaf.getKey() == key;
    }
    // INSERT
    @Override
    public void insert(int key) {

        if (root == null) {
            root = new LeafNode(key);
            return;
        }

        // Buscar la hoja correspondiente
        Node current = root;

        while (!current.isLeaf()) {
            metrics.incrementNodesVisited();
            InternalNode node = (InternalNode) current;

            if (key < node.getSeparatorKey())
                current = node.getLeft();
            else
                current = node.getRight();
        }

        LeafNode leaf = (LeafNode) current;
        metrics.incrementNodesVisited();

        // No insertar duplicados
        if (leaf.getKey() == key)
            return;

        InternalNode parent = leaf.getParent();

        LeafNode newLeaf = new LeafNode(key);

        InternalNode newInternal;

        if (key < leaf.getKey()) {

            insertBefore(leaf, newLeaf);

            newInternal = new InternalNode(leaf.getKey());
            newInternal.setLeft(newLeaf);
            newInternal.setRight(leaf);

        } else {

            insertAfter(leaf, newLeaf);

            newInternal = new InternalNode(key);
            newInternal.setLeft(leaf);
            newInternal.setRight(newLeaf);
        }

        // Sustituir la hoja por el nuevo nodo interno
        if (parent == null) {

            root = newInternal;
            newInternal.setParent(null);

        } else {

            replaceChild(parent, leaf, newInternal);
        }

        // Primer nodo que puede cambiar de altura
        InternalNode currentNode = newInternal;

        while (currentNode != null) {

            InternalNode parentNode = currentNode.getParent();

            InternalNode balanced = rebalance(currentNode);

            if (parentNode == null) {

                root = balanced;
                balanced.setParent(null);

            } else {

                replaceChild(parentNode, currentNode, balanced);
            }

            currentNode = parentNode;
        }
    }

    // DELETE
    @Override
    public void delete(int key) {

        LeafNode leaf = findLeaf(key);

        if (leaf == null)
            return;

        // quitar de la lista enlazada
        leaf.unlink();

        InternalNode start = removeInternalNode(leaf);

        rebalanceUp(start);
    }
    //RANGE SEARCH
    @Override
    public List<Integer> rangeSearch(int a, int b) {

        List<Integer> result = new ArrayList<>();

        if (root == null || a > b)
            return result;

        Node current = root;

        // Buscar la primera hoja donde terminaría la búsqueda de a
        while (!current.isLeaf()) {
            metrics.incrementNodesVisited();
            InternalNode internal = (InternalNode) current;

            if (a < internal.getSeparatorKey())
                current = internal.getLeft();
            else
                current = internal.getRight();
        }

        LeafNode leaf = (LeafNode) current;

        /*
        * Si la hoja encontrada es menor que a,
        * avanzar hasta encontrar la primera >= a.
        */
        while (leaf != null && leaf.getKey() < a){
            leaf = leaf.getNext();
            metrics.incrementNodesVisited();
        }

        /*
        * Recorrer la lista enlazada hasta superar b.
        */
        while (leaf != null && leaf.getKey() <= b) {
            metrics.incrementNodesVisited();

            result.add(leaf.getKey());
            metrics.incrementReportedKeys();

            leaf = leaf.getNext();
            
        }

        return result;
    }

    // IN-ORDER TRAVERSAL

    @Override
    public List<Integer> inOrder() {

        List<Integer> result = new ArrayList<>();

        if (root == null)
            return result;

        LeafNode current = findMinimumLeaf(root);

        while (current != null) {

            result.add(current.getKey());

            current = current.getNext();
        }

        return result;
    }

    @Override
    public Metrics getMetrics(){
        return this.metrics;
    }

    @Override
    public void resetMetrics(){
        this.metrics.reset();
    }

    // AUXILIARY METHODS
    private void rebalanceUp(InternalNode node) {

        while (node != null) {

            updateSeparator(node);
            updateHeight(node);

            InternalNode parent = node.getParent();

            InternalNode balanced = rebalance(node);

            if (parent == null) {

                root = balanced;
                balanced.setParent(null);

            } else {

                replaceChild(parent, node, balanced);
            }

            node = parent;
        }
    }

    private InternalNode removeInternalNode(LeafNode leaf) {

        InternalNode parent = leaf.getParent();

        if (parent == null) {

            root = null;
            return null;
        }

        InternalNode grandParent = parent.getParent();

        Node sibling;

        if (parent.getLeft() == leaf)
            sibling = parent.getRight();
        else
            sibling = parent.getLeft();

        if (grandParent == null) {

            root = sibling;
            sibling.setParent(null);

        } else {

            replaceChild(grandParent, parent, sibling);
        }

        return grandParent;
    }


    private LeafNode findLeaf(int key) {

        if (root == null)
            return null;

        Node current = root;

        while (!current.isLeaf()) {

            InternalNode internal = (InternalNode) current;

            if (key < internal.getSeparatorKey())
                current = internal.getLeft();
            else
                current = internal.getRight();
        }

        LeafNode leaf = (LeafNode) current;

        return leaf.getKey() == key ? leaf : null;
    }
    

    
    private int height(Node node) {

        if (node == null)
            return -1;

        return node.getHeight();
    }

    private void updateHeight(Node node) {

        if (node == null)
            return;

        if (node.isLeaf()) {
            node.setHeight(0);
            return;
        }

        InternalNode internal = (InternalNode) node;

        int leftHeight = height(internal.getLeft());
        int rightHeight = height(internal.getRight());

        node.setHeight(Math.max(leftHeight, rightHeight) + 1);
    }

    private int balanceFactor(InternalNode node) {
        return height(node.getLeft()) - height(node.getRight());
    }

    private LeafNode findMinimumLeaf(Node node) {

        Node current = node;

        while (!current.isLeaf()) {
            current = ((InternalNode) current).getLeft();
        }

        return (LeafNode) current;
    }

    private void updateSeparator(InternalNode node) {
        if (node == null || node.getRight() == null)
            return;

        LeafNode minimum = findMinimumLeaf(node.getRight());

        node.setSeparatorKey(minimum.getKey());

    }

    private InternalNode rotateLeft(InternalNode x) {
        metrics.incrementRotations();

        if (x.getRight().isLeaf())
            return x;

        InternalNode y = (InternalNode) x.getRight();
        Node beta = y.getLeft();

        // Padre original de x
        InternalNode parent = x.getParent();

        // Rotación
        y.setParent(parent);
        y.setLeft(x);
        x.setRight(beta);

        // Actualizar información
        updateSeparator(x);
        updateHeight(x);

        updateSeparator(y);
        updateHeight(y);

        return y;
    }

    private InternalNode rotateRight(InternalNode y) {
        metrics.incrementRotations();

        if (y.getLeft().isLeaf())
            return y;

        InternalNode x = (InternalNode) y.getLeft();
        Node beta = x.getRight();

        // Padre original de y
        InternalNode parent = y.getParent();

        // Rotación
        x.setParent(parent);
        x.setRight(y);
        y.setLeft(beta);

        // Actualizar información
        updateSeparator(y);
        updateHeight(y);

        updateSeparator(x);
        updateHeight(x);

        return x;
    }

    private InternalNode rebalance(InternalNode node) {

        updateHeight(node);
        updateSeparator(node);

        int balance = balanceFactor(node);

        // LEFT HEAVY
        if (balance > 1) {

            if (!node.getLeft().isLeaf()) {

                InternalNode left = (InternalNode) node.getLeft();

                if (balanceFactor(left) < 0)
                    node.setLeft(rotateLeft(left));
            }

            return rotateRight(node);
        }

        // RIGHT HEAVY
        if (balance < -1) {

            if (!node.getRight().isLeaf()) {

                InternalNode right = (InternalNode) node.getRight();

                if (balanceFactor(right) > 0)
                    node.setRight(rotateRight(right));
            }

            return rotateLeft(node);
        }

        return node;
    }

    private void replaceChild( InternalNode parent, Node oldChild, Node newChild) {
        if (parent.getLeft() == oldChild)
            parent.setLeft(newChild);
        else
            parent.setRight(newChild);

    }

    private void insertBefore(LeafNode current, LeafNode newLeaf) {

        newLeaf.setNext(current);

        newLeaf.setPrev(current.getPrev());

        if (current.getPrev() != null)
            current.getPrev().setNext(newLeaf);

        current.setPrev(newLeaf);

    }

    private void insertAfter(LeafNode current, LeafNode newLeaf) {

        newLeaf.setPrev(current);

        newLeaf.setNext(current.getNext());

        if (current.getNext() != null)
            current.getNext().setPrev(newLeaf);

        current.setNext(newLeaf);

    }

    @Override
    public String getName() {
        return "AVL_LeafTree";
    }
}