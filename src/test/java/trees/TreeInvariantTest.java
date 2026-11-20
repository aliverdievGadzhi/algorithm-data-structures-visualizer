package trees;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class TreeInvariantTest {

    // ---------- random operations against java.util.TreeSet ----------

    @Test
    void bstStaysValidUnderRandomOperations() {
        randomOperations(SimpleBinarySearchTree::new, root -> { });
    }

    @Test
    void avlStaysBalancedUnderRandomOperations() {
        randomOperations(AVLTree::new, TreeInvariantTest::checkAvl);
    }

    @Test
    void rbKeepsColorRulesUnderRandomOperations() {
        randomOperations(RBTree::new, root -> {
            if (root != null) assertTrue(root.isBlack(), "root must be black");
            checkRb(root);
        });
    }

    private static <N extends AbstractBinaryNode<Integer, N>> void randomOperations(
            java.util.function.Supplier<AbstractBinarySearchTree<Integer, N>> factory, Consumer<N> invariant) {
        Random random = new Random(42);
        for (int run = 0; run < 300; run++) {
            AbstractBinarySearchTree<Integer, N> tree = factory.get();
            TreeSet<Integer> expected = new TreeSet<>();
            for (int op = 0; op < 60; op++) {
                int value = random.nextInt(40);
                TreeOperationResponse response;
                if (random.nextInt(3) > 0) {
                    response = tree.insert(value);
                    expected.add(value);
                } else {
                    response = tree.remove(value);
                    expected.remove(value);
                }
                assertEquals(new ArrayList<>(expected), tree.inOrder());
                checkParents(tree.getRoot(), null);
                invariant.accept(tree.getRoot());
                checkSteps(response);
            }
        }
    }

    // ---------- edge cases ----------

    @Test
    void removeFromEmptyTreeIsNotFound() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            assertEquals(List.of("not_found", "done"), actions(tree.remove(1)));
        }
    }

    @Test
    void duplicateInsertChangesNothing() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            tree.insert(2);
            tree.insert(1);
            List<String> actions = actions(tree.insert(2));
            assertTrue(actions.contains("duplicate"));
            assertFalse(actions.contains("insert"));
            assertEquals(List.of(1, 2), tree.inOrder());
        }
    }

    @Test
    void removeMissingValueIsNotFound() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            tree.insert(5);
            assertEquals(List.of("compare", "not_found", "done"), actions(tree.remove(7)));
        }
    }

    @Test
    void removeLastNodeLeavesEmptyTree() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            tree.insert(5);
            TreeOperationResponse response = tree.remove(5);
            assertNull(tree.getRoot());
            assertNull(response.steps().getLast().state());
        }
    }

    @Test
    void removeRootWithTwoChildren() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            tree.insert(2);
            tree.insert(1);
            tree.insert(3);
            assertTrue(actions(tree.remove(2)).contains("replace"));
            assertEquals(3, tree.getRoot().getKey());
            assertEquals(List.of(1, 3), tree.inOrder());
        }
    }

    @Test
    void nullValueIsRejected() {
        assertThrows(NullPointerException.class, () -> new AVLTree<Integer>().insert(null));
    }

    @Test
    void avlRotationCases() {
        assertEquals(List.of("rotate_right"), rotations(3, 2, 1));
        assertEquals(List.of("rotate_left"), rotations(1, 2, 3));
        assertEquals(List.of("rotate_left", "rotate_right"), rotations(3, 1, 2));
        assertEquals(List.of("rotate_right", "rotate_left"), rotations(1, 3, 2));
    }

    @Test
    void avlCascadingRotationsOnDelete() {
        // Deleting 1 unbalances one node, and fixing it shortens the subtree and unbalances an ancestor.
        AVLTree<Integer> tree = new AVLTree<>();
        for (int v : new int[]{1, 10, 5, 8, 12, 2, 4, 9, 6, 3, 7, 11}) tree.insert(v);
        List<String> actions = actions(tree.remove(1));
        assertEquals(2, actions.stream().filter(a -> a.equals("unbalanced")).count(), actions.toString());
        checkAvl(tree.getRoot());
    }

    @Test
    void avlSnapshotShowsRealImbalanceBeforeRotation() {
        AVLTree<Integer> tree = new AVLTree<>();
        tree.insert(1);
        tree.insert(2);
        TreeStep unbalanced = tree.insert(3).steps().stream()
                .filter(s -> s.action().equals("unbalanced")).findFirst().orElseThrow();
        assertEquals(-2, unbalanced.state().balance());
        assertEquals(2, unbalanced.state().height());
    }

    @Test
    void nodeIdsStayUniqueWhenKeyIsCopied() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int v : new int[]{2, 1, 3}) tree.insert(v);
        TreeStep replace = tree.remove(2).steps().stream()
                .filter(s -> s.action().equals("replace")).findFirst().orElseThrow();
        Set<Integer> ids = new HashSet<>();
        List<Object> values = new ArrayList<>();
        collect(replace.state(), ids, values);
        assertEquals(3, ids.size());
        assertEquals(List.of(1, 3, 3), values.stream().sorted().toList());
    }

    @Test
    void jsonHasOnlyTheFieldsOfEachTree() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AVLTree<Integer> avl = new AVLTree<>();
        JsonNode avlState = mapper.readTree(avl.insert(1).toJson()).get("steps").get(0).get("state");
        assertTrue(avlState.has("height") && avlState.has("balance") && !avlState.has("color"));

        RBTree<Integer> rb = new RBTree<>();
        JsonNode rbJson = mapper.readTree(rb.insert(1).toJson());
        JsonNode rbState = rbJson.get("steps").get(0).get("state");
        assertTrue(rbState.has("color") && !rbState.has("height"));
        assertEquals("rb", rbJson.get("structure").asText());
    }

    // ---------- helpers ----------

    private static List<AbstractBinarySearchTree<Integer, ?>> allTrees() {
        return List.of(new SimpleBinarySearchTree<>(), new AVLTree<>(), new RBTree<>());
    }

    private static List<String> actions(TreeOperationResponse response) {
        return response.steps().stream().map(TreeStep::action).toList();
    }

    private static List<String> rotations(int... values) {
        AVLTree<Integer> tree = new AVLTree<>();
        TreeOperationResponse last = null;
        for (int v : values) last = tree.insert(v);
        return actions(last).stream().filter(a -> a.startsWith("rotate")).toList();
    }

    private static void checkSteps(TreeOperationResponse response) {
        List<TreeStep> steps = response.steps();
        assertFalse(steps.isEmpty());
        assertEquals("done", steps.getLast().action());
        for (int i = 0; i < steps.size(); i++) {
            assertEquals(i, steps.get(i).index());
            assertTrue(steps.get(i).comment().matches("\\p{ASCII}+"), steps.get(i).comment());
        }
    }

    private static <N extends AbstractBinaryNode<Integer, N>> void checkParents(N node, N parent) {
        if (node == null) return;
        assertSame(parent, node.getParent(), "wrong parent of " + node.getKey());
        checkParents(node.getLeft(), node);
        checkParents(node.getRight(), node);
    }

    /** Returns the height and checks the AVL rule and the cached height. */
    private static int checkAvl(AVLNode<Integer> node) {
        if (node == null) return -1;
        int left = checkAvl(node.getLeft());
        int right = checkAvl(node.getRight());
        assertTrue(Math.abs(left - right) <= 1, "unbalanced at " + node.getKey());
        assertEquals(1 + Math.max(left, right), node.getHeight(), "wrong height at " + node.getKey());
        return 1 + Math.max(left, right);
    }

    /** Returns the black height and checks the red-black rules below this node. */
    private static int checkRb(RBNode<Integer> node) {
        if (node == null) return 1;
        if (node.isRed()) {
            assertFalse(node.hasRedChild(), "red node " + node.getKey() + " has a red child");
        }
        int left = checkRb(node.getLeft());
        int right = checkRb(node.getRight());
        assertEquals(left, right, "black height differs at " + node.getKey());
        return left + (node.isBlack() ? 1 : 0);
    }

    private static void collect(TreeNodeDto node, Set<Integer> ids, List<Object> values) {
        if (node == null) return;
        ids.add(node.id());
        values.add(node.value());
        collect(node.left(), ids, values);
        collect(node.right(), ids, values);
    }
}
