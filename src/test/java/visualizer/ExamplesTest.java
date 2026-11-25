package visualizer;

import org.junit.jupiter.api.Test;
import visualizer.steps.OperationResponse;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The example files must match what the code produces now. Run Main to regenerate them. */
class ExamplesTest {

    @Test
    void insertExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-insert.json", Main.insertExample());
    }

    @Test
    void deleteExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-delete.json", Main.deleteExample());
    }

    @Test
    void bstInsertExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-bst-insert.json", Main.bstInsertExample());
    }

    @Test
    void bstDeleteExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-bst-delete.json", Main.bstDeleteExample());
    }

    @Test
    void rbInsertExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-rb-insert.json", Main.rbInsertExample());
    }

    @Test
    void rbDeleteExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-rb-delete.json", Main.rbDeleteExample());
    }

    @Test
    void heapInsertExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-heap-insert.json", Main.heapInsertExample());
    }

    @Test
    void heapExtractExampleIsUpToDate() throws Exception {
        assertMatches("examples/example-heap-extract.json", Main.heapExtractExample());
    }

    private static void assertMatches(String file, OperationResponse<?> expected) throws Exception {
        String actual = Files.readString(Path.of(file));
        assertEquals(normalize(expected.toJson()), normalize(actual), file + " is outdated, run Main");
    }

    private static String normalize(String json) {
        return json.replace("\r\n", "\n").strip();
    }
}
