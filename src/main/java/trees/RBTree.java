package trees;

/**
 * A red-black tree. Rules: the root is black, a red node has no red child,
 * and every path from a node down to an empty child has the same number of black nodes.
 */
public class RBTree<T extends Comparable<T>> extends AbstractBinarySearchTree<T, RBNode<T>> {

    @Override
    protected String structure() {
        return "rb";
    }

    @Override
    protected RBNode<T> newNode(T key, int id) {
        return new RBNode<>(key, id, Color.RED);
    }

    @Override
    protected TreeNodeDto toDto(RBNode<T> node, TreeNodeDto left, TreeNodeDto right) {
        return new TreeNodeDto(node.getId(), node.getKey(), null, null, node.getColor().name(), left, right);
    }

    /** The new node is red. Fixes a red node under a red parent by recoloring and rotating. */
    @Override
    protected void afterInsert(RBNode<T> z) {
        while (z != root && z.getParent().isRed()) {
            RBNode<T> parent = z.getParent();
            RBNode<T> grandparent = parent.getParent();
            boolean parentOnLeft = parent == grandparent.getLeft();
            RBNode<T> uncle = parentOnLeft ? grandparent.getRight() : grandparent.getLeft();

            if (uncle != null && uncle.isRed()) {
                parent.setColor(Color.BLACK);
                uncle.setColor(Color.BLACK);
                grandparent.setColor(Color.RED);
                record("recolor", grandparent, "Parent " + parent.getKey() + " and uncle " + uncle.getKey()
                        + " are red: make them black and grandparent " + grandparent.getKey() + " red");
                z = grandparent;
                continue;
            }

            if (parentOnLeft && z == parent.getRight()) {
                z = parent;
                rotateLeft(z);
            } else if (!parentOnLeft && z == parent.getLeft()) {
                z = parent;
                rotateRight(z);
            }
            z.getParent().setColor(Color.BLACK);
            grandparent.setColor(Color.RED);
            record("recolor", z.getParent(), "Uncle is black: make " + z.getParent().getKey()
                    + " black and grandparent " + grandparent.getKey() + " red, then rotate");
            if (parentOnLeft) rotateRight(grandparent);
            else rotateLeft(grandparent);
        }

        if (root.isRed()) {
            root.setColor(Color.BLACK);
            record("recolor", root, "The root is always black");
        }
    }

    @Override
    protected void unlink(RBNode<T> node) {
        if (node.isBlack()) {
            RBNode<T> child = node.hasLeft() ? node.getLeft() : node.getRight();
            if (child != null) {
                // The only child of a black node is red: it takes over the black color.
                child.setColor(Color.BLACK);
            } else if (node != root) {
                fixDoubleBlack(node);
            }
        }
        super.unlink(node);
    }

    /** Called while {@code x} is still in the tree: removing it would leave its path one black node short. */
    private void fixDoubleBlack(RBNode<T> x) {
        if (x == root) return;
        record("double_black", x, x.getKey() + " is double black: its path is missing one black node");

        RBNode<T> sibling = x.getSibling();
        RBNode<T> parent = x.getParent();
        if (sibling == null) {
            fixDoubleBlack(parent);
            return;
        }

        if (sibling.isRed()) {
            parent.setColor(Color.RED);
            sibling.setColor(Color.BLACK);
            record("recolor", sibling, "Sibling " + sibling.getKey() + " is red: swap colors with parent "
                    + parent.getKey() + " and rotate it up");
            if (sibling.isOnLeft()) rotateRight(parent);
            else rotateLeft(parent);
            fixDoubleBlack(x);
            return;
        }

        if (sibling.hasRedChild()) {
            if (sibling.hasLeft() && sibling.getLeft().isRed()) {
                if (sibling.isOnLeft()) {
                    sibling.getLeft().setColor(sibling.getColor());
                    sibling.setColor(parent.getColor());
                    rotateRight(parent);
                } else {
                    sibling.getLeft().setColor(parent.getColor());
                    rotateRight(sibling);
                    rotateLeft(parent);
                }
            } else {
                if (sibling.isOnLeft()) {
                    sibling.getRight().setColor(parent.getColor());
                    rotateLeft(sibling);
                    rotateRight(parent);
                } else {
                    sibling.getRight().setColor(sibling.getColor());
                    sibling.setColor(parent.getColor());
                    rotateLeft(parent);
                }
            }
            parent.setColor(Color.BLACK);
            record("recolor", parent, "Sibling had a red child: after the rotations "
                    + parent.getKey() + " becomes black and the double black is gone");
            return;
        }

        sibling.setColor(Color.RED);
        if (parent.isRed()) {
            parent.setColor(Color.BLACK);
            record("recolor", sibling, "Sibling " + sibling.getKey() + " and its children are black: make it red and parent "
                    + parent.getKey() + " black");
        } else {
            record("recolor", sibling, "Sibling " + sibling.getKey() + " and its children are black: make it red and move "
                    + "the double black up to " + parent.getKey());
            fixDoubleBlack(parent);
        }
    }
}
