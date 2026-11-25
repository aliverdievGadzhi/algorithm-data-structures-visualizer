package visualizer.trees;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import visualizer.steps.OperationResponse;
import visualizer.steps.Step;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TreeInvariantTest {

    // ---------- random operations against java.util.TreeSet ----------

    @Test
    void bstStaysValidUnderRandomOperations() {
        randomOperations(SimpleBinarySearchTree<Integer>::new, (BSTNode<Integer> root) -> { });
    }

    @Test
    void avlStaysBalancedUnderRandomOperations() {
        randomOperations(AVLTree<Integer>::new, (AVLNode<Integer> root) -> checkAvl(root));
    }

    @Test
    void rbKeepsColorRulesUnderRandomOperations() {
        randomOperations(RBTree<Integer>::new, (RBNode<Integer> root) -> {
            if (root != null) assertTrue(root.isBlack(), "root must be black");
            checkRb(root);
        });
    }

    private static <N extends AbstractBinaryNode<Integer, N>> void randomOperations(
            Supplier<? extends AbstractBinarySearchTree<Integer, N>> factory, Consumer<N> invariant) {
        Random random = new Random(42);
        for (int run = 0; run < 300; run++) {
            AbstractBinarySearchTree<Integer, N> tree = factory.get();
            TreeSet<Integer> expected = new TreeSet<>();
            for (int op = 0; op < 60; op++) {
                int value = random.nextInt(40);
                OperationResponse<TreeNodeDto> response;
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
            List<Step<TreeNodeDto>> steps = tree.remove(5).steps();
            assertNull(tree.getRoot());
            assertNull(steps.getLast().state());
        }
    }

    @Test
    void removeRootWithTwoChildren() {
        for (AbstractBinarySearchTree<Integer, ?> tree : allTrees()) {
            tree.insert(2);
            tree.insert(1);
            tree.insert(3);
            assertTrue(actions(tree.remove(2)).contains("replace"));
            assertNotNull(tree.getRoot());
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
        TreeNodeDto state = stateOf(tree.insert(3), "unbalanced");
        assertEquals(-2, state.balance());
        assertEquals(2, state.height());
    }

    @Test
    void nodeIdsStayUniqueWhenKeyIsCopied() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int v : new int[]{2, 1, 3}) tree.insert(v);
        Set<Integer> ids = new HashSet<>();
        List<Integer> values = new ArrayList<>();
        collect(stateOf(tree.remove(2), "replace"), ids, values);
        assertEquals(3, ids.size());
        assertEquals(List.of(1, 3, 3), values.stream().sorted().toList());
    }

    @Test
    void jsonHasOnlyTheFieldsOfEachTree() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode avlState = mapper.readTree(new AVLTree<Integer>().insert(1).toJson()).path("steps").path(0).path("state");
        assertTrue(avlState.has("height") && avlState.has("balance") && !avlState.has("color"));

        JsonNode rbJson = mapper.readTree(new RBTree<Integer>().insert(1).toJson());
        JsonNode rbState = rbJson.path("steps").path(0).path("state");
        assertTrue(rbState.has("color") && !rbState.has("height"));
        assertEquals("rb", rbJson.path("structure").asText());
    }

    // ---------- helpers ----------

    private static List<AbstractBinarySearchTree<Integer, ?>> allTrees() {
        return List.of(new SimpleBinarySearchTree<Integer>(), new AVLTree<Integer>(), new RBTree<Integer>());
    }

    private static List<String> actions(OperationResponse<TreeNodeDto> response) {
        return response.steps().stream().map(Step::action).toList();
    }

    /** Returns the snapshot of the first step with the given action; fails if there is none. */
    private static TreeNodeDto stateOf(OperationResponse<TreeNodeDto> response, String action) {
        Step<TreeNodeDto> step = response.steps().stream()
                .filter(s -> s.action().equals(action)).findFirst().orElseThrow();
        assertNotNull(step.state());
        return step.state();
    }

    /** Inserts all values and returns the rotations done by the last insert. */
    private static List<String> rotations(int first, int... rest) {
        AVLTree<Integer> tree = new AVLTree<>();
        OperationResponse<TreeNodeDto> last = tree.insert(first);
        for (int v : rest) last = tree.insert(v);
        return actions(last).stream().filter(a -> a.startsWith("rotate")).toList();
    }

    private static void checkSteps(OperationResponse<TreeNodeDto> response) {
        List<Step<TreeNodeDto>> steps = response.steps();
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

    private static void collect(TreeNodeDto node, Set<Integer> ids, List<Integer> values) {
        if (node == null) return;
        ids.add(node.id());
        values.add((Integer) node.value());
        collect(node.left(), ids, values);
        collect(node.right(), ids, values);
    }
}
