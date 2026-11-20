package trees;

/** A node of a {@link SimpleBinarySearchTree}. */
public class BSTNode<T extends Comparable<T>> extends AbstractBinaryNode<T, BSTNode<T>> {

    public BSTNode(T key, int id) {
        super(key, id);
    }
}
