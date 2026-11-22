package visualizer.trees;

/** A node of an {@link AVLTree}. It caches the height of its subtree (a leaf has height 0). */
public class AVLNode<T extends Comparable<T>> extends AbstractBinaryNode<T, AVLNode<T>> {

    private int height;

    public AVLNode(T key, int id) {
        super(key, id);
    }

    public int getHeight() {
        return height;
    }

    void setHeight(int height) {
        this.height = height;
    }
}
