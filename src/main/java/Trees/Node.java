package Trees;

public interface Node<T extends Comparable<T>> {

    /**
     * Returns an array containing all keys in the node.
     * <p>
     * The keys are ordered in ascending order.
     * The size of the array is equal to the number of keys in the node.
     * The array is a copy of the internal array and can be modified without affecting the node.
     *
     * @return an array containing all keys in the node.
     */
    T[] getKeys();

}

