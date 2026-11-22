package heaps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import steps.OperationResponse;
import steps.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {

    @Test
    void matchesPriorityQueueUnderRandomOperations() {
        Random random = new Random(7);
        for (int run = 0; run < 300; run++) {
            MinHeap<Integer> heap = new MinHeap<>();
            PriorityQueue<Integer> expected = new PriorityQueue<>();
            for (int op = 0; op < 60; op++) {
                OperationResponse<List<Integer>> response;
                if (random.nextInt(3) > 0) {
                    int value = random.nextInt(50);
                    response = heap.insert(value);
                    expected.add(value);
                } else {
                    response = heap.extractMin();
                    assertEquals(expected.poll(), response.value());
                }
                checkSteps(response);
                List<Integer> state = response.steps().getLast().state();
                checkHeapProperty(state);
                assertEquals(expected.stream().sorted().toList(), state.stream().sorted().toList());
            }
        }
    }

    @Test
    void extractFromEmptyHeap() {
        OperationResponse<List<Integer>> response = new MinHeap<Integer>().extractMin();
        assertEquals(List.of("empty", "done"), actions(response));
        assertNull(response.value());
        assertEquals(List.of(), response.steps().getLast().state());
    }

    @Test
    void newMinimumSiftsUpToTheRoot() {
        MinHeap<Integer> heap = new MinHeap<>();
        for (int v : new int[]{5, 10, 8, 20}) heap.insert(v);
        OperationResponse<List<Integer>> response = heap.insert(1);
        assertEquals(2, actions(response).stream().filter(a -> a.equals("swap")).count());
        assertEquals(1, response.steps().getLast().state().getFirst());
    }

    @Test
    void atIsAnIndexOrAPairOfIndexes() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.insert(2);
        for (Step<List<Integer>> step : heap.insert(1).steps()) {
            switch (step.action()) {
                case "insert", "sift_up" -> assertEquals(1, step.at());
                case "compare", "swap" -> assertEquals(List.of(1, 0), step.at());
                case "done" -> assertNull(step.at());
                default -> fail("unexpected action " + step.action());
            }
        }
    }

    @Test
    void jsonRoundTrip() throws Exception {
        MinHeap<Integer> heap = new MinHeap<>();
        for (int v : new int[]{4, 7, 1, 9}) heap.insert(v);
        OperationResponse<List<Integer>> response = heap.extractMin();

        OperationResponse<List<Integer>> parsed = new ObjectMapper()
                .readValue(response.toJson(), new TypeReference<OperationResponse<List<Integer>>>() { });
        assertEquals(response, parsed);
        assertEquals("heap", parsed.structure());
        assertEquals("extract_min", parsed.operation());
    }

    private static List<String> actions(OperationResponse<List<Integer>> response) {
        return response.steps().stream().map(Step::action).toList();
    }

    private static void checkSteps(OperationResponse<List<Integer>> response) {
        List<Step<List<Integer>>> steps = response.steps();
        assertFalse(steps.isEmpty());
        assertEquals("done", steps.getLast().action());
        for (int i = 0; i < steps.size(); i++) {
            assertEquals(i, steps.get(i).index());
            assertTrue(steps.get(i).comment().matches("\\p{ASCII}+"), steps.get(i).comment());
        }
    }

    private static void checkHeapProperty(List<Integer> items) {
        for (int i = 1; i < items.size(); i++) {
            assertTrue(items.get((i - 1) / 2) <= items.get(i), "parent is bigger than child at index " + i + ": " + items);
        }
    }
}
