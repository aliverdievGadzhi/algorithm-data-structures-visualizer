package visualizer.trees;

import visualizer.steps.OperationResponse;
import visualizer.steps.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for binary search trees that record every operation as steps for the visualizer.
 * <p>
 * It implements the plain BST insert and delete. Self-balancing trees plug in through
 * {@link #afterInsert}, {@link #unlink} and the rotations. Duplicate keys are rejected.
 *
 * @param <T> the type of the keys.
 * @param <N> the node type.
 */
public abstract class AbstractBinarySearchTree<T extends Comparable<T>, N extends AbstractBinaryNode<T, N>> {

    protected N root;
    private List<Step<TreeNodeDto>> steps = new ArrayList<>();
    private int nextId;

    /** Name of the structure in the JSON contract, e.g. {@code "avl"}. */
    protected abstract String structure();

    protected abstract N newNode(T key, int id);

    /** Builds the snapshot of one node. Its children are already converted. */
    protected abstract TreeNodeDto toDto(N node, TreeNodeDto left, TreeNodeDto right);

    /** Called after a new node is linked into the tree. */
    protected void afterInsert(N node) {
    }

    public N getRoot() {
        return root;
    }

    /** Finds the node with the given key without recording steps. */
    public N search(T value) {
        N x = root;
        while (x != null) {
            int cmp = value.compareTo(x.getKey());
            if (cmp == 0) return x;
            x = cmp < 0 ? x.getLeft() : x.getRight();
        }
        return null;
    }

    /** Returns all keys in ascending order. */
    public List<T> inOrder() {
        List<T> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(N node, List<T> result) {
        if (node == null) return;
        inOrder(node.getLeft(), result);
        result.add(node.getKey());
        inOrder(node.getRight(), result);
    }

    public OperationResponse<TreeNodeDto> insert(T value) {
        Objects.requireNonNull(value, "value");
        steps = new ArrayList<>();

        N parent = null;
        N x = root;
        while (x != null) {
            int cmp = value.compareTo(x.getKey());
            if (cmp == 0) {
                record("duplicate", x, value + " is already in the tree");
                return finish("insert", value, "Nothing to insert, the tree is unchanged");
            }
            recordCompare(value, x, cmp);
            parent = x;
            x = cmp < 0 ? x.getLeft() : x.getRight();
        }

        N node = newNode(value, nextId++);
        if (parent == null) {
            root = node;
            record("insert", node, "The tree is empty, " + value + " becomes the root");
        } else {
            boolean left = value.compareTo(parent.getKey()) < 0;
            link(parent, node, left);
            record("insert", node, "Insert " + value + " as the " + side(left) + " child of " + parent.getKey());
        }
        afterInsert(node);
        return finish("insert", value, "Insert " + value + " finished");
    }

    public OperationResponse<TreeNodeDto> remove(T value) {
        Objects.requireNonNull(value, "value");
        steps = new ArrayList<>();

        N x = root;
        while (x != null) {
            int cmp = value.compareTo(x.getKey());
            if (cmp == 0) break;
            recordCompare(value, x, cmp);
            x = cmp < 0 ? x.getLeft() : x.getRight();
        }
        if (x == null) {
            record("not_found", null, value + " is not in the tree");
            return finish("delete", value, "Nothing to delete, the tree is unchanged");
        }
        record("found", x, "Found " + value);

        if (x.hasLeft() && x.hasRight()) {
            N successor = x.getRight();
            while (successor.hasLeft()) successor = successor.getLeft();
            record("find_replacement", successor, value + " has two children, so take its in-order successor "
                    + successor.getKey() + " (the minimum of the right subtree)");
            x.setKey(successor.getKey());
            record("replace", x, "Copy " + successor.getKey() + " into the node, then remove the old "
                    + successor.getKey() + " node");
            x = successor;
        }
        unlink(x);
        return finish("delete", value, "Delete " + value + " finished");
    }

    /** Removes a node with at most one child by putting that child in its place. */
    protected void unlink(N node) {
        N child = node.hasLeft() ? node.getLeft() : node.getRight();
        replaceInParent(node, child);
        record("delete", node, child == null
                ? "Remove leaf " + node.getKey()
                : "Replace " + node.getKey() + " with its only child " + child.getKey());
    }

    /** Rotates left around {@code x} and returns the new root of this subtree. */
    protected N rotateLeft(N x) {
        N y = x.getRight();
        link(x, y.getLeft(), false);
        replaceInParent(x, y);
        link(y, x, true);
        record("rotate_left", x, "Rotate left around " + x.getKey() + ", " + y.getKey() + " moves up");
        return y;
    }

    /** Rotates right around {@code x} and returns the new root of this subtree. */
    protected N rotateRight(N x) {
        N y = x.getLeft();
        link(x, y.getRight(), true);
        replaceInParent(x, y);
        link(y, x, false);
        record("rotate_right", x, "Rotate right around " + x.getKey() + ", " + y.getKey() + " moves up");
        return y;
    }

    /** Adds a step with a snapshot of the whole tree as it is now. */
    protected void record(String action, N node, String comment) {
        Integer at = node == null ? null : node.getId();
        steps.add(new Step<>(steps.size(), action, at, comment, snapshot(root)));
    }

    private void recordCompare(T value, N node, int cmp) {
        boolean left = cmp < 0;
        record("compare", node, value + (left ? " < " : " > ") + node.getKey() + ", go " + side(left));
    }

    private OperationResponse<TreeNodeDto> finish(String operation, T value, String comment) {
        record("done", null, comment);
        return new OperationResponse<>(structure(), operation, value, List.copyOf(steps));
    }

    private TreeNodeDto snapshot(N node) {
        if (node == null) return null;
        return toDto(node, snapshot(node.getLeft()), snapshot(node.getRight()));
    }

    private void link(N parent, N child, boolean left) {
        if (left) parent.setLeft(child);
        else parent.setRight(child);
        if (child != null) child.setParent(parent);
    }

    /** Puts {@code replacement} where {@code node} hangs from its parent (or makes it the root). */
    private void replaceInParent(N node, N replacement) {
        N parent = node.getParent();
        if (replacement != null) replacement.setParent(parent);
        if (parent == null) root = replacement;
        else link(parent, replacement, parent.getLeft() == node);
    }

    private static String side(boolean left) {
        return left ? "left" : "right";
    }
}
