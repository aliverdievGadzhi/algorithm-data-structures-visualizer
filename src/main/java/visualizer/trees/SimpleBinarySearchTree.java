package visualizer.trees;

/** A plain binary search tree without balancing. */
public class SimpleBinarySearchTree<T extends Comparable<T>> extends AbstractBinarySearchTree<T, BSTNode<T>> {

    @Override
    protected String structure() {
        return "bst";
    }

    @Override
    protected BSTNode<T> newNode(T key, int id) {
        return new BSTNode<>(key, id);
    }

    @Override
    protected TreeNodeDto toDto(BSTNode<T> node, TreeNodeDto left, TreeNodeDto right) {
        return new TreeNodeDto(node.getId(), node.getKey(), null, null, null, left, right);
    }
}
