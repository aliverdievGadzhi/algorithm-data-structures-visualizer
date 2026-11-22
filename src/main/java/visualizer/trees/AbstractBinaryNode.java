package visualizer.trees;

/**
 * A node of a binary search tree with a link to its parent.
 * <p>
 * The {@code id} never changes, even when the key is overwritten or the node is rotated,
 * so the visualizer can track the same node across steps and tell apart nodes with equal keys.
 *
 * @param <T> the type of the key.
 * @param <N> the concrete node type.
 */
public abstract class AbstractBinaryNode<T extends Comparable<T>, N extends AbstractBinaryNode<T, N>> {

    private final int id;
    private T key;
    private N left;
    private N right;
    private N parent;

    protected AbstractBinaryNode(T key, int id) {
        this.key = key;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public T getKey() {
        return key;
    }

    protected void setKey(T key) {
        this.key = key;
    }

    public N getLeft() {
        return left;
    }

    public boolean hasLeft() {
        return left != null;
    }

    protected void setLeft(N left) {
        this.left = left;
    }

    public N getRight() {
        return right;
    }

    public boolean hasRight() {
        return right != null;
    }

    protected void setRight(N right) {
        this.right = right;
    }

    public N getParent() {
        return parent;
    }

    protected void setParent(N parent) {
        this.parent = parent;
    }

    /** Returns {@code true} if this node is the left child of its parent. */
    protected boolean isOnLeft() {
        return parent != null && parent.getLeft() == this;
    }

    /** Returns the other child of this node's parent, or {@code null}. */
    protected N getSibling() {
        if (parent == null) return null;
        return isOnLeft() ? parent.getRight() : parent.getLeft();
    }
}
