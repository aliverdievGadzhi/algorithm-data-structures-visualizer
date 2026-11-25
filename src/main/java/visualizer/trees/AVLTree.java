package visualizer.trees;

/**
 * An AVL tree: for every node the heights of the two subtrees differ by at most one.
 * After each insert or delete the path up to the root is rebalanced with single or double rotations.
 */
public class AVLTree<T extends Comparable<T>> extends AbstractBinarySearchTree<T, AVLNode<T>> {

    @Override
    protected String structure() {
        return "avl";
    }

    @Override
    protected AVLNode<T> newNode(T key, int id) {
        return new AVLNode<>(key, id);
    }

    /**
     * Height and balance are computed from the snapshot itself, not read from the cached field,
     * so a step taken in the middle of rebalancing shows the real imbalance (for example -2).
     */
    @Override
    protected TreeNodeDto toDto(AVLNode<T> node, TreeNodeDto left, TreeNodeDto right) {
        int leftHeight = height(left);
        int rightHeight = height(right);
        return new TreeNodeDto(node.getId(), node.getKey(), 1 + Math.max(leftHeight, rightHeight),
                leftHeight - rightHeight, null, left, right);
    }

    @Override
    protected void afterInsert(AVLNode<T> node) {
        rebalanceUp(node.getParent());
    }

    @Override
    protected void unlink(AVLNode<T> node) {
        AVLNode<T> parent = node.getParent();
        super.unlink(node);
        rebalanceUp(parent);
    }

    @Override
    protected AVLNode<T> rotateLeft(AVLNode<T> x) {
        AVLNode<T> y = super.rotateLeft(x);
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    @Override
    protected AVLNode<T> rotateRight(AVLNode<T> x) {
        AVLNode<T> y = super.rotateRight(x);
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private void rebalanceUp(AVLNode<T> node) {
        while (node != null) {
            node = rebalance(node).getParent();
        }
    }

    /** Fixes the node if it is out of balance and returns the new root of its subtree. */
    private AVLNode<T> rebalance(AVLNode<T> node) {
        updateHeight(node);
        int balance = balance(node);

        if (balance > 1) {
            boolean leftRight = balance(node.getLeft()) < 0;
            record("unbalanced", node, "Balance of " + node.getKey() + " is " + balance + (leftRight
                    ? ": left-right case, rotate left around the left child, then right"
                    : ": left-left case, rotate right"));
            if (leftRight) rotateLeft(node.getLeft());
            return rotateRight(node);
        }
        if (balance < -1) {
            boolean rightLeft = balance(node.getRight()) > 0;
            record("unbalanced", node, "Balance of " + node.getKey() + " is " + balance + (rightLeft
                    ? ": right-left case, rotate right around the right child, then left"
                    : ": right-right case, rotate left"));
            if (rightLeft) rotateRight(node.getRight());
            return rotateLeft(node);
        }
        return node;
    }

    private void updateHeight(AVLNode<T> node) {
        node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
    }

    private int balance(AVLNode<T> node) {
        return height(node.getLeft()) - height(node.getRight());
    }

    private int height(AVLNode<T> node) {
        return node == null ? -1 : node.getHeight();
    }

    private static int height(TreeNodeDto node) {
        return node == null ? -1 : node.height();
    }
}
