package lab1.src.datastructures;

public interface rmqInterface {
    //returns the index of the minimum element in the range [i, j]
    public int RMQ(int i, int j); 
    //returns the memory usage of the data structure in bytes
    public long getMemoryBytes();

    public void preprocess();

    public void countMemoryBytes();
    
}
