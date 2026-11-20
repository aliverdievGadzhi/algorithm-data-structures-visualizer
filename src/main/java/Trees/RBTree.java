package Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An implementation of a red-black tree.
 * <p>
 * A red-black tree is a self-balancing binary search tree. It guarantees that the searching and inserting operation
 * have a logarithmic time complexity.
 *
 * @param <T> The type of the keys in the tree.
 * @see AbstractBinarySearchTree
 * @see RBNode
 */
public class RBTree<T extends Comparable<T>> extends AbstractBinarySearchTree<T, RBNode<T>> {

    /**
     * The sentinel node of the tree.
     * <p>
     * The sentinel node is a special node that is used to simplify the implementation of the tree. It is a black
     * node that is used as the parent of the root node and is its own child. It is not considered part of the tree.
     */
    protected final RBNode<T> sentinel = new RBNode<>(null, Color.BLACK);

    /**
     * Creates a new, empty red-black tree.
     */
    public RBTree() {
        sentinel.setParent(sentinel);
        sentinel.setLeft(sentinel);
        sentinel.setRight(sentinel);
    }

    @Override
    public void insert(T value) {
        RBNode<T> z = createNode(value);
        insert(z, sentinel);
        fixColorsAfterInsertion(z);
    }

    @Override
    public String toJsonString() {
        return "{}"; // заглушка, пока не дойдете до реализации этих деревьев
    }

    /**
     * Ensures that the red-black tree properties are maintained after inserting a new node, which might have
     * added a red node as a child of another red node.
     *
     * @param z The node that was inserted.
     */
    protected void fixColorsAfterInsertion(RBNode<T> z) {
        while (z.getParent().isRed()) {
            if (z.getParent() == z.getParent().getParent().getLeft()) {
                RBNode<T> y = z.getParent().getParent().getRight();
                if (y != null && y.isRed()) {
                    z.getParent().setColor(Color.BLACK);
                    y.setColor(Color.BLACK);
                    z.getParent().getParent().setColor(Color.RED);
                    z = z.getParent().getParent();
                } else {
                    if (z == z.getParent().getRight()) {
                        z = z.getParent();
                        rotateLeft(z);
                    }
                    z.getParent().setColor(Color.BLACK);
                    z.getParent().getParent().setColor(Color.RED);
                    rotateRight(z.getParent().getParent());
                }
            } else {
                RBNode<T> y = z.getParent().getParent().getLeft();
                if (y != null && y.isRed()) {
                    z.getParent().setColor(Color.BLACK);
                    y.setColor(Color.BLACK);
                    z.getParent().getParent().setColor(Color.RED);
                    z = z.getParent().getParent();
                } else {
                    if (z == z.getParent().getLeft()) {
                        z = z.getParent();
                        rotateRight(z);
                    }
                    z.getParent().setColor(Color.BLACK);
                    z.getParent().getParent().setColor(Color.RED);
                    rotateLeft(z.getParent().getParent());
                }
            }
        }

        root.setColor(Color.BLACK);
    }

    /**
     * Rotates the given node to the left by making it the left child of its previous right child.
     * <p>
     * The method assumes that the right child of the given node is not {@code null}.
     *
     * @param x The node to rotate.
     */
    protected void rotateLeft(RBNode<T> x) {

        RBNode<T> y = x.getRight();
        x.setRight(y.getLeft());

        if (y.hasLeft()) {
            y.getLeft().setParent(x);
        }

        y.setParent(x.getParent());

        if (x.getParent() == sentinel) {
            root = y;
        } else if (x == x.getParent().getLeft()) {
            x.getParent().setLeft(y);
        } else {
            x.getParent().setRight(y);
        }

        y.setLeft(x);
        x.setParent(y);
    }

    /**
     * Rotates the given node to the right by making it the right child of its previous left child.
     * <p>
     * The method assumes that the left child of the given node is not {@code null}.
     *
     * @param x The node to rotate.
     */
    protected void rotateRight(RBNode<T> x) {

        RBNode<T> y = x.getLeft();
        x.setLeft(y.getRight());

        if (y.hasRight()) {
            y.getRight().setParent(x);
        }

        y.setParent(x.getParent());

        if (x.getParent() == sentinel) {
            root = y;
        } else if (x == x.getParent().getRight()) {
            x.getParent().setRight(y);
        } else {
            x.getParent().setLeft(y);
        }

        y.setRight(x);
        x.setParent(y);
    }

    @Override
    public void inOrder(Node<T> node, List<? super T> result, Predicate<? super T> predicate) {
        if (node instanceof RBNode<T> rbNode) {
            super.inOrder(rbNode, result, predicate);
            return;
        }

        if (node != null) throw new IllegalArgumentException("Node must be of type RBNode");
    }


    @Override
    public void inOrder(Node<T> node, List<? super T> result) {
        if (node instanceof RBNode<T> rbNode) {
            super.inOrder(rbNode, result);
            return;
        }

        if (node != null) throw new IllegalArgumentException("Node must be of type RBNode");
    }

    @Override
    public void preOrder(Node<T> node, List<? super T> result) {
        if (node instanceof RBNode<T> rbNode)    {
            super.preOrder(rbNode, result);
            return;
        }

        if (node != null) throw new IllegalArgumentException("Node must be of type RBNode");
    }

    /**
     * Performs an in-order traversal of the given Red-Black Tree node and
     * adds each visited key to the provided list.
     *
     * <p>This method traverses the binary search tree in in-order sequence:
     * it first recursively traverses the left subtree, then processes the current node
     * by adding its key to the list, and finally recursively traverses the right subtree.
     * As a result, the list will contain the tree's keys in ascending order.
     *
     * @param node  the current node to start the traversal from; if {@code null},
     *               the traversal ends.
     * @param list  the list to which the keys of the visited nodes are added.
     *               Elements are appended in in-order sequence.
     */

    void inOrder(RBNode<T> node, List<? super T> list) {
        if (node == null) return;
        inOrder(node.getLeft(), list);
        list.add(node.getKey());
        inOrder(node.getRight(), list);
    }

    @Override
    public void findNext(Node<T> node, List<? super T> result, int max, Predicate<? super T> predicate) {
        if (node instanceof RBNode<T> rbNode) {
            super.findNext(rbNode, result, max, predicate);
            return;
        }
        if (node != null) throw new IllegalArgumentException("Node must be of type RBNode");
    }


    /**
     * Separates this red-black tree (which contains at least a root) into two subtrees at the specified {@code joinKey}.
     * @param joinKey The key of the node where the tree should be split.
     * @return A list containing two {@code RBTree} instances:
     * The former left tree containing all nodes with keys strictly less than {@code joinKey}.
     * The former right tree ({@code this}), modified to contain only nodes with keys strictly greater than {@code joinKey}.
     * @throws IllegalArgumentException if the {@code joinKey} is not found in the tree.
     */

    public List<RBTree<T>> separate(T joinKey) {

        //Initialization

        List<RBTree<T>> rbTreeList = new ArrayList<>();
        RBNode<T> joinNode = getRoot();
        RBTree<T> leftRBT = new RBTree<>();

        //Searching joinNode

        while (joinNode.getKey().compareTo(joinKey) != 0) {
            if (joinNode.getKey().compareTo(joinKey) > 0) {
                joinNode = joinNode.getLeft();
            } else {
                joinNode = joinNode.getRight();
            }
            if (joinNode == null) {
                throw new IllegalArgumentException("Join key not found in tree.");
            }
        }
        RBNode<T> joinParent = joinNode.getParent();
        if (joinParent.getLeft() == joinNode) {
            RBNode<T> joinRight = joinNode.getRight();
            joinParent.setLeft(joinRight);
            if (joinNode.hasRight()) {
                joinNode.getRight().setParent(joinParent);
                joinNode.setRight(null);
            }
            leftRBT.root = joinNode.getLeft();
            if (joinNode.hasLeft()) {
                joinNode.getLeft().setParent(leftRBT.sentinel);
                joinNode.setLeft(null);
            }
            joinNode.setParent(null);
            rbTreeList.add(leftRBT);
            rbTreeList.add(this);
            return rbTreeList;
        }
        else {
            joinParent.setRight(null);
            joinNode.setParent(null);
            rbTreeList.add(this);
            return rbTreeList;
        }
    }



    /**
     * Joins this red-black tree with another red-black tree using a separating key.
     * <p>
     * The method assumes the following preconditions:
     * <ul>
     *   <li>both trees are non-empty,</li>
     *   <li>every key in this tree is strictly less than {@code joinKey},</li>
     *   <li>every key in {@code treeToJoinWith} is strictly greater than {@code joinKey}.</li>
     * </ul>
     * <p>
     * After the call, this tree contains every key of both original trees as well as {@code joinKey},
     * organised as a single valid red-black tree. The operation is performed in place;
     * {@code treeToJoinWith} should be considered consumed and is no longer guaranteed to be a valid
     * red-black tree.
     *
     * @param treeToJoinWith the other red-black tree to be joined into this one.
     * @param joinKey        the separating key, lying strictly between the keys of the two trees.
     */
    public void join(RBTree<T> treeToJoinWith, T joinKey) {
        int bh1 = this.blackHeight();
        int bh2 = treeToJoinWith.blackHeight();
        RBNode<T> joinNode = createNode(joinKey);
        if (bh1>=bh2){
            RBNode<T> targetNode = this.findBlackNodeWithBlackHeight(bh2, bh1, false);
            RBNode<T> targetParent = targetNode.getParent();
            joinNode.setLeft(targetNode);
            targetNode.setParent(joinNode);
            joinNode.setRight(treeToJoinWith.getRoot());
            treeToJoinWith.getRoot().setParent(joinNode);
            joinNode.setParent(targetParent);
            if (targetParent==sentinel){
                this.root = joinNode;
                joinNode.setColor(Color.BLACK);
            }
            else if (targetParent.getLeft() == targetNode){
                targetParent.setLeft(joinNode);
            }
            else {
                targetParent.setRight(joinNode);
            }
            joinNode.setColor(Color.RED);
            this.fixColorsAfterInsertion(joinNode);
        }
        else {
            RBNode<T> targetNode = treeToJoinWith.findBlackNodeWithBlackHeight(bh1, bh2, true);
            RBNode<T> targetParent = targetNode.getParent();
            joinNode.setRight(targetNode);
            targetNode.setParent(joinNode);
            joinNode.setLeft(this.getRoot());
            this.getRoot().setParent(joinNode);
            joinNode.setParent(targetParent);
            if (targetParent.getLeft() == targetNode){
                targetParent.setLeft(joinNode);
            }
            else {
                targetParent.setRight(joinNode);
            }
            joinNode.setColor(Color.RED);
            treeToJoinWith.fixColorsAfterInsertion(joinNode);
            this.setRoot(treeToJoinWith.getRoot());
        }
    }
    /**
     * Returns the black height of the tree, i.e. the number of black nodes on a path from the root to a leaf.
     *
     * @return the black height of the tree.
     */
    public int blackHeight() {
        return blackHeight(root);
    }

    private int blackHeight(RBNode<T> node) {
        if (node == null) return 0;

        return blackHeight(node.getLeft()) + (node.isBlack() ? 1 : 0);
    }

    /**
     * Finds a black node with the given black height in the tree.
     * <p>
     * Depending on the value of the {@code findSmallest} parameter, the method finds the smallest or largest node with the
     * target black height.
     * <p>
     * It assumes that the tree is non-empty and that there is a node with the target black height.
     *
     * @param targetBlackHeight The target black height to find a node with.
     * @param totalBlackHeight  The total black height of the tree.
     * @param findSmallest      Whether to find the smallest or largest node with the target black height.
     * @return A black node with the target black height.
     */
    public RBNode<T> findBlackNodeWithBlackHeight(int targetBlackHeight, int totalBlackHeight, boolean findSmallest) {

        int currentBlackHeight = totalBlackHeight;
        RBNode<T> currentNode = root;

        while (currentNode.isRed() || currentBlackHeight > targetBlackHeight) {

            if (currentNode.isBlack()) currentBlackHeight--;

            if (findSmallest) currentNode = currentNode.getLeft();
            else currentNode = currentNode.getRight();
        }

        return currentNode;
    }




    @Override
    protected RBNode<T> createNode(T key) {
        return new RBNode<>(key, Color.RED);
    }


    /**
     * Swaps the key values of two Red-Black Tree nodes.
     *
     * <p>This method is typically used during deletion when a node with two children
     * is replaced by its in-order successor. Instead of moving the actual nodes,
     * their key values are exchanged to simplify pointer changes and preserve tree structure.
     *
     * @param u the first node
     * @param v the second node
     */

    void swapValues(RBNode<T> u, RBNode<T> v) {
        T temp = u.getKey();
        u.setKey(v.getKey());
        v.setKey(temp);
    }

    /**
     * Finds the in-order successor of a given node in its subtree.
     *
     * <p>This method returns the node with the smallest key greater than the given node,
     * which is the leftmost node in the right subtree. It is used in deletion when replacing
     * a node that has two children.
     *
     * @param x the node whose successor is to be found
     * @return the in-order successor node (leftmost node in x's right subtree)
     */

    RBNode<T> successor(RBNode<T> x) {
        RBNode<T> temp = x.getRight();
        while (temp.getLeft() != null)
            temp = temp.getLeft();
        return temp;
    }

    /**
     * Determines the node that should replace a given node during deletion in a BST.
     *
     * <p>This method follows standard binary search tree deletion logic:
     * <ul>
     *   <li>If the node has two children, it returns the in-order successor (smallest node in right subtree).</li>
     *   <li>If the node has only one child, it returns that child.</li>
     *   <li>If the node is a leaf (no children), it returns {@code null}.</li>
     * </ul>
     *
     * @param x the node to be deleted
     * @return the node that should replace {@code x} in the tree
     */

    public RBNode<T> replace(RBNode<T> x) {
        // when node have 2 children
        if (x.getLeft() != null && x.getRight() != null)
            return successor(x);

        // when leaf
        if (x.getLeft() == null && x.getRight() == null)
            return null;

        // when single child
        if (x.getLeft() != null)
            return x.getLeft();
        else
            return x.getRight();
    }

    /**
     * Deletes a node from the Red-Black Tree while preserving tree balance and color properties.
     *
     * <p>This method handles all three primary cases when deleting a node:
     * <ul>
     *   <li>If the node has no children (leaf)</li>
     *   <li>If the node has one child</li>
     *   <li>If the node has two children</li>
     * </ul>
     *
     * <p>In the case of a leaf node, it is removed directly. If it causes a double black violation,
     * {@code fixDoubleBlack} is called to restore Red-Black properties.
     *
     * <p>If the node has one child, that child is moved up to replace the node.
     * If needed, color adjustments are made based on whether the deleted node and its child were black.
     *
     * <p>If the node has two children, the in-order successor (determined by {@code replace}) is
     * swapped with the node, and the deletion continues recursively.
     *
     * @param v the {@link RBNode} to delete from the tree
     */

    public void deleteNode(RBNode<T> v) {
        RBNode<T> u = replace(v);

        boolean uvBlack = ((u == null || u.getColor() == Color.BLACK) && v.getColor() == Color.BLACK);
        RBNode<T> parent = v.getParent();

        if (u == null) {
            // v is a leaf
            if (v == root) {
                root = null;
            } else {
                if (uvBlack) {
                    fixDoubleBlack(v);
                } else {
                    if (v.getSibling() != null) {
                        v.getSibling().setColor(Color.RED);
                    }
                }

                if (v.isOnLeft()) {
                    parent.setLeft(null);
                } else {
                    parent.setRight(null);
                }
            }
            return;
        }

        if (v.getLeft() == null || v.getRight() == null) {
            // v has 1 child
            if (v == root) {
                v.setKey(u.getKey());
                v.setLeft(null);
                v.setRight(null);
            } else {
                if (v.isOnLeft()) {
                    parent.setLeft(u);
                } else {
                    parent.setRight(u);
                }
                u.setParent(parent);

                if (uvBlack) {
                    fixDoubleBlack(u);
                } else {
                    u.setColor(Color.BLACK);
                }
            }
            return;
        }

        // v has 2 children
        swapValues(u, v);
        deleteNode(u);
    }

    /**
     * Fixes a double-black violation in the Red-Black Tree after a deletion.
     *
     * <p>A double-black situation occurs when a black node is deleted or a black child replaces
     * a black node, causing a temporary imbalance in the number of black nodes on paths from the root
     * to leaves. This method restores the Red-Black properties by performing recoloring and/or
     * rotations as needed, depending on the color of the sibling and its children.
     *
     * <p>The method considers the following scenarios:
     * <ul>
     *   <li><b>No sibling:</b> Propagate double black up to the parent.</li>
     *   <li><b>Red sibling:</b> Recolor and rotate to convert to a black-sibling case.</li>
     *   <li><b>Black sibling with at least one red child:</b> Perform appropriate rotations and recoloring to fix the tree.</li>
     *   <li><b>Black sibling with two black children:</b> Recolor sibling red and push double black up if necessary.</li>
     * </ul>
     *
     * <p>This method is recursive and terminates when the root is reached or the tree is rebalanced.
     *
     * @param x the node where the double-black violation occurs
     */


    public void fixDoubleBlack(RBNode<T> x) {
        if (x == root) return;

        RBNode<T> sibling = x.getSibling();
        RBNode<T> parent = x.getParent();

        if (sibling == null) {
            fixDoubleBlack(parent);
        } else {
            if (sibling.getColor() == Color.RED) {
                parent.setColor(Color.RED);
                sibling.setColor(Color.BLACK);

                if (sibling.isOnLeft()) {
                    rotateRight(parent);
                } else {
                    rotateLeft(parent);
                }

                fixDoubleBlack(x);
            } else {
                if (sibling.hasRedChild()) {
                    // at least one red child
                    if (sibling.getLeft() != null && sibling.getLeft().getColor() == Color.RED) {
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
                } else {
                    sibling.setColor(Color.RED);
                    if (parent.getColor() == Color.BLACK) {
                        fixDoubleBlack(parent);
                    } else {
                        parent.setColor(Color.BLACK);
                    }
                }
            }
        }
    }

}

