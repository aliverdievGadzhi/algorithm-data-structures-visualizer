package visualizer.trees;

/** A node of an {@link RBTree}. */
public class RBNode<T extends Comparable<T>> extends AbstractBinaryNode<T, RBNode<T>> {

    private Color color;

    public RBNode(T key, int id, Color color) {
        super(key, id);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean isRed() {
        return color == Color.RED;
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }

    protected void setColor(Color color) {
        this.color = color;
    }

    protected boolean hasRedChild() {
        return (hasLeft() && getLeft().isRed()) || (hasRight() && getRight().isRed());
    }
}
