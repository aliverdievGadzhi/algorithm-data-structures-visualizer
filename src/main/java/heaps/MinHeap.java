package heaps;

import steps.OperationResponse;
import steps.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A binary min-heap stored in a list: the children of index {@code i} are {@code 2i + 1} and {@code 2i + 2},
 * and no child is smaller than its parent, so the minimum is always at index 0.
 * <p>
 * Every operation is recorded as steps; {@code state} is the whole array, {@code at} is an index or a pair of indexes.
 */
public class MinHeap<T extends Comparable<T>> {

    private final List<T> items = new ArrayList<>();
    private List<Step<List<T>>> steps = new ArrayList<>();

    public OperationResponse<List<T>> insert(T value) {
        Objects.requireNonNull(value, "value");
        steps = new ArrayList<>();

        items.add(value);
        int i = items.size() - 1;
        record("insert", i, "Append " + value + " at index " + i);
        if (i > 0) {
            record("sift_up", i, "Sift " + value + " up while it is smaller than its parent");
        }
        while (i > 0) {
            int parent = (i - 1) / 2;
            boolean smaller = less(i, parent);
            record("compare", List.of(i, parent), items.get(i) + (smaller ? " < " : " >= ")
                    + items.get(parent) + " (its parent)" + (smaller ? ", swap them" : ", stop"));
            if (!smaller) break;
            swap(i, parent);
            i = parent;
        }
        return finish("insert", value, "Insert " + value + " finished");
    }

    public OperationResponse<List<T>> extractMin() {
        steps = new ArrayList<>();
        if (items.isEmpty()) {
            record("empty", null, "The heap is empty, nothing to extract");
            return finish("extract_min", null, "Nothing extracted, the heap is unchanged");
        }

        T min = items.getFirst();
        T last = items.removeLast();
        if (items.isEmpty()) {
            record("extract", 0, "Remove the minimum " + min + ", the heap is now empty");
        } else {
            items.set(0, last);
            record("extract", 0, "Remove the minimum " + min + " and move the last element " + last + " to the root");
            siftDown();
        }
        return finish("extract_min", min, "Extracted the minimum " + min);
    }

    private void siftDown() {
        int i = 0;
        record("sift_down", i, "Sift " + items.get(i) + " down while a child is smaller");
        while (2 * i + 1 < items.size()) {
            int child = 2 * i + 1;
            if (child + 1 < items.size() && less(child + 1, child)) child++;
            boolean smaller = less(child, i);
            record("compare", List.of(i, child), "Smaller child " + items.get(child) + (smaller ? " < " : " >= ")
                    + items.get(i) + (smaller ? ", swap them" : ", stop"));
            if (!smaller) break;
            swap(i, child);
            i = child;
        }
    }

    private void swap(int i, int j) {
        Collections.swap(items, i, j);
        record("swap", List.of(i, j), "Swap " + items.get(j) + " and " + items.get(i));
    }

    private boolean less(int i, int j) {
        return items.get(i).compareTo(items.get(j)) < 0;
    }

    private void record(String action, Object at, String comment) {
        steps.add(new Step<>(steps.size(), action, at, comment, List.copyOf(items)));
    }

    private OperationResponse<List<T>> finish(String operation, T value, String comment) {
        record("done", null, comment);
        return new OperationResponse<>("heap", operation, value, List.copyOf(steps));
    }
}
