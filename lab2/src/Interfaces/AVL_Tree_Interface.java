package lab2.src.Interfaces;

import java.util.List;
import lab2.src.Utils.Metrics;

public interface AVL_Tree_Interface {

    boolean search(int key);

    void insert(int key);

    void delete(int key);

    List<Integer> rangeSearch(int a, int b);

    List<Integer> inOrder();

    void resetMetrics();
    
    Metrics getMetrics();
    
    String getName();

}