package visualizer;

import visualizer.heaps.MinHeap;
import visualizer.steps.OperationResponse;
import visualizer.trees.AVLTree;
import visualizer.trees.RBTree;
import visualizer.trees.SimpleBinarySearchTree;
import visualizer.trees.TreeNodeDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Regenerates the example JSON files in the examples directory. */
public class Main {

    public static void main(String[] args) throws IOException {
        Files.writeString(Path.of("examples/example-insert.json"), insertExample().toJson());
        Files.writeString(Path.of("examples/example-delete.json"), deleteExample().toJson());
        Files.writeString(Path.of("examples/example-bst-insert.json"), bstInsertExample().toJson());
        Files.writeString(Path.of("examples/example-bst-delete.json"), bstDeleteExample().toJson());
        Files.writeString(Path.of("examples/example-rb-insert.json"), rbInsertExample().toJson());
        Files.writeString(Path.of("examples/example-rb-delete.json"), rbDeleteExample().toJson());
        Files.writeString(Path.of("examples/example-heap-insert.json"), heapInsertExample().toJson());
        Files.writeString(Path.of("examples/example-heap-extract.json"), heapExtractExample().toJson());
        System.out.println("Wrote the example JSON files");
    }

    /** Inserting 65 makes the root lean right-left, so it needs a double rotation. */
    public static OperationResponse<TreeNodeDto> insertExample() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int value : new int[]{50, 30, 70, 60, 80}) {
            tree.insert(value);
        }
        return tree.insert(65);
    }

    /** Deleting the root (two children) removes its successor 60, which leaves 70 unbalanced. */
    public static OperationResponse<TreeNodeDto> deleteExample() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int value : new int[]{50, 30, 70, 20, 60, 80, 90}) {
            tree.insert(value);
        }
        return tree.remove(50);
    }

    /** Inserting 45 walks down three levels and adds a leaf. */
    public static OperationResponse<TreeNodeDto> bstInsertExample() {
        return bst(50, 30, 70, 20, 40, 60).insert(45);
    }

    /** Deleting the root (two children): its successor 60 is replaced by its only child 65. */
    public static OperationResponse<TreeNodeDto> bstDeleteExample() {
        return bst(50, 30, 70, 20, 60, 80, 65).remove(50);
    }

    /** Inserting 60 needs both fixes: recoloring for a red uncle, then a double rotation. */
    public static OperationResponse<TreeNodeDto> rbInsertExample() {
        return rb(70, 20, 10, 50, 80, 40, 30).insert(60);
    }

    /** Deleting 50 (two children) makes a double black: first a red sibling, then a sibling with a red child. */
    public static OperationResponse<TreeNodeDto> rbDeleteExample() {
        return rb(60, 30, 50, 20, 10, 40).remove(50);
    }

    /** Inserting 3, the new minimum, sifts it up from the last leaf to the root. */
    public static OperationResponse<List<Integer>> heapInsertExample() {
        return sampleHeap().insert(3);
    }

    /** Extracting the minimum moves the last element to the root and sifts it down to a leaf. */
    public static OperationResponse<List<Integer>> heapExtractExample() {
        return sampleHeap().extractMin();
    }

    private static MinHeap<Integer> sampleHeap() {
        MinHeap<Integer> heap = new MinHeap<>();
        for (int value : new int[]{5, 10, 8, 20, 15, 9}) {
            heap.insert(value);
        }
        return heap;
    }

    private static SimpleBinarySearchTree<Integer> bst(int... values) {
        SimpleBinarySearchTree<Integer> tree = new SimpleBinarySearchTree<>();
        for (int value : values) {
            tree.insert(value);
        }
        return tree;
    }

    private static RBTree<Integer> rb(int... values) {
        RBTree<Integer> tree = new RBTree<>();
        for (int value : values) {
            tree.insert(value);
        }
        return tree;
    }
}
