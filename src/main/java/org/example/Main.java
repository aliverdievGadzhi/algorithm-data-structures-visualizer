package org.example;

import trees.AVLTree;
import trees.TreeOperationResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regenerates the example JSON files in the project root. */
public class Main {

    public static void main(String[] args) throws IOException {
        Files.writeString(Path.of("example-insert.json"), insertExample().toJson());
        Files.writeString(Path.of("example-delete.json"), deleteExample().toJson());
        System.out.println("Wrote example-insert.json and example-delete.json");
    }

    /** Inserting 65 makes the root lean right-left, so it needs a double rotation. */
    public static TreeOperationResponse insertExample() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int value : new int[]{50, 30, 70, 60, 80}) {
            tree.insert(value);
        }
        return tree.insert(65);
    }

    /** Deleting the root (two children) removes its successor 60, which leaves 70 unbalanced. */
    public static TreeOperationResponse deleteExample() {
        AVLTree<Integer> tree = new AVLTree<>();
        for (int value : new int[]{50, 30, 70, 20, 60, 80, 90}) {
            tree.insert(value);
        }
        return tree.remove(50);
    }
}
